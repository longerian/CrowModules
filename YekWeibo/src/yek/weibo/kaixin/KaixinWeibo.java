/*Copyright (C) 2012 Crow Hou (crow_hou@126.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package yek.weibo.kaixin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import yek.util.Util;
import yek.weibo.PostParameter;
import yek.weibo.RequestToken;
import yek.weibo.Weibo;
import yek.weibo.WeiboException;
import yek.weibo.util.WeiboUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class KaixinWeibo extends Weibo {
	private static final String PREF_ACCESSTOKEN = "kaixin_oauth_token";
	private static final String PREF_ACCESSTOKEN_SECRET = "kaixin_oauth_token_secret";
	private Context context;

	/**
	 * Kaixin授权地址
	 */
	private static final String KX_REQUEST_TOKEN_URL = "http://api.kaixin001.com/oauth/request_token";
	public static final String KX_AUTHORIZE_URL = "http://api.kaixin001.com/oauth/authorize";
	private static String KX_ACCESS_TOKEN_URL = "http://api.kaixin001.com/oauth/access_token";

	/**
	 * 常量字符串
	 */
	public static final String OAUTH_VERSION = "1.0";
	private static final String OUATH_TOKEN = "oauth_token";
	private static final String OUATH_TOKEN_SECRET = "oauth_token_secret";
	private static final String OUATH_TOKEN_VERIFIER = "oauth_verifier";

	public KaixinWeibo(Context context) {
		super(Weibo.KAIXIN_WEIBO);
		this.context = context;
	}

	@Override
	public RequestToken getOAuthRequestToken(String callbackUrl)
			throws WeiboException {
		List<PostParameter> params = new ArrayList<PostParameter>();

		// 设置回调
		params.add(new PostParameter("oauth_callback", callbackUrl));
		// 设置权限范围
		params.add(new PostParameter("scope", "basic create_records"));

		// 公用参数
		String timestamp = WeiboUtil.generateTimeStamp();
		String onceMd5 = Util.md5(WeiboUtil.generateNonce());
		params.add(new PostParameter("oauth_consumer_key", getApiKey()));
		params.add(new PostParameter("oauth_signature_method", "HMAC-SHA1"));
		params.add(new PostParameter("oauth_timestamp", timestamp));
		params.add(new PostParameter("oauth_nonce", onceMd5));
		// 签名
		String signature = makeSign(params, KX_REQUEST_TOKEN_URL, GET_METHOD,
				getApiSecret(), null);
		params.add(new PostParameter("oauth_signature", signature));

		// 请求url
		String url = KX_REQUEST_TOKEN_URL + "?"
				+ WeiboUtil.encodeParameters(params, "&", false);
		try {
			String result = Util.urlGet(url);
			Bundle token = WeiboUtil.parseToken(result);
			String tokenKey = token.getString(OUATH_TOKEN);
			String tokenSecret = token.getString(OUATH_TOKEN_SECRET);
			if (tokenKey != null && tokenSecret != null) {
				return new KaixinRequestToken(this, callbackUrl, tokenKey,
						tokenSecret);
			} else {
				throw new WeiboException("未获取到授权。");
			}
		} catch (Exception e) {
			throw new WeiboException(e);
		}
	}

	public KaixinAccessToken getAccessToken(String requestToken,
			String requestTokenSecret, String verifier) throws WeiboException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter(OUATH_TOKEN, requestToken));
		params.add(new PostParameter(OUATH_TOKEN_VERIFIER, verifier));

		// 公用参数
		String timestamp = WeiboUtil.generateTimeStamp();
		String onceMd5 = Util.md5(WeiboUtil.generateNonce());
		params.add(new PostParameter("oauth_consumer_key", getApiKey()));
		params.add(new PostParameter("oauth_signature_method", "HMAC-SHA1"));
		params.add(new PostParameter("oauth_timestamp", timestamp));
		params.add(new PostParameter("oauth_nonce", onceMd5));
		// 签名
		String signature = makeSign(params, KX_ACCESS_TOKEN_URL, GET_METHOD,
				getApiSecret(), requestTokenSecret);
		params.add(new PostParameter("oauth_signature", signature));

		String url = KX_ACCESS_TOKEN_URL + "?"
				+ WeiboUtil.encodeParameters(params, "&", false);

		try {
			String result = Util.urlGet(url);
			Bundle bundle = WeiboUtil.parseToken(result);
			String token = bundle.getString(OUATH_TOKEN);
			String tokenSecret = bundle.getString(OUATH_TOKEN_SECRET);
			if (token != null && tokenSecret != null) {
				SharedPreferences pref = context.getSharedPreferences(
						Weibo.PREF_WEIBO, Context.MODE_PRIVATE);
				pref.edit().putString(PREF_ACCESSTOKEN, token).putString(
						PREF_ACCESSTOKEN_SECRET, tokenSecret).commit();
				return new KaixinAccessToken(this, token, tokenSecret);
			} else {
				throw new WeiboException("未获取到授权。");
			}
		} catch (Exception e) {
			throw new WeiboException(e);
		}
	}

	public boolean updateStatus(KaixinAccessToken accessToken, String status)
			throws WeiboException {
		return uploadStatus(accessToken, status, null);
	}

	public boolean uploadStatus(KaixinAccessToken accessToken, String status,
			File file) throws WeiboException {
		String upload = "http://api.kaixin001.com/records/add.json";
		List<PostParameter> params = new ArrayList<PostParameter>();
		status = status.length() > 140 ? status.substring(0, 140) : status;
		params.add(new PostParameter("content", status));
		params.add(new PostParameter("oauth_token", accessToken.getToken()));
		params.add(new PostParameter("oauth_version", OAUTH_VERSION));

		// 公用参数
		String timestamp = WeiboUtil.generateTimeStamp();
		String onceMd5 = Util.md5(WeiboUtil.generateNonce());
		params.add(new PostParameter("oauth_consumer_key", getApiKey()));
		params.add(new PostParameter("oauth_signature_method", "HMAC-SHA1"));
		params.add(new PostParameter("oauth_timestamp", timestamp));
		params.add(new PostParameter("oauth_nonce", onceMd5));
		// 签名
		String signature = makeSign(params, upload, POST_METHOD,
				getApiSecret(), accessToken.getTokenSecret());
		params.add(new PostParameter("oauth_signature", signature));

		if (file != null) {
			params.add(new PostParameter("pic", file));
		}
		
		try {
			String s = WeiboUtil.multipartPost(upload, params);
			JSONObject json = new JSONObject(s);
			if (!Util.isEmpty(json.getString("rid"))){ // 分享成功
				return true;
			}
		} catch (Exception e) {
			throw new WeiboException("未获取到授权。", e);
		}
		return false;
	}

	@Override
	public KaixinAccessToken getAccessToken() {
		SharedPreferences pref = context.getSharedPreferences(Weibo.PREF_WEIBO,
				Context.MODE_PRIVATE);
		String token = pref.getString(PREF_ACCESSTOKEN, null);
		String tokenSecret = pref.getString(PREF_ACCESSTOKEN_SECRET, null);
		if ((!Util.isEmpty(token)) && (!Util.isEmpty(tokenSecret))) {
			return new KaixinAccessToken(this, token, tokenSecret);
		}
		return null;
	}
}
