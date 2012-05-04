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
package yek.weibo.tencent;

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

public class TencentWeibo extends Weibo {
	private static final String PREF_ACCESSTOKEN = "tencent_oauth_token";
	private static final String PREF_ACCESSTOKEN_SECRET = "tencent_oauth_token_secret";
	private Context context;

	// 腾讯微博获取Request Token 的 URL 地址
	private static final String RequestTokenURL = "https://open.t.qq.com/cgi-bin/request_token";
	private static final String AccessTokenURL = "https://open.t.qq.com/cgi-bin/access_token";
	private static final String AddWeiboURL = "http://open.t.qq.com/api/t/add";
	private static final String AddWeiboWithPicURL = "http://open.t.qq.com/api/t/add_pic";

	private static final String oauth_version = "1.0";// (可选)
	private static final String oauth_signature_method = "HMAC-SHA1";// 签名方法，暂只支持HMAC-SHA1

	public TencentWeibo(Context context) {
		super(Weibo.TENCENT_WEIBO);
		this.context = context;
	}

	@Override
	public RequestToken getOAuthRequestToken(String callback_url)
			throws WeiboException {
		// 请求的参数
		List<PostParameter> parameters = new ArrayList<PostParameter>();
		String oauth_timestamp = WeiboUtil.generateTimeStamp();
		String oauth_nonce = WeiboUtil.generateNonce();

		parameters.add(new PostParameter("oauth_consumer_key", getApiKey()));
		parameters.add(new PostParameter("oauth_signature_method",
				oauth_signature_method));
		parameters.add(new PostParameter("oauth_timestamp", oauth_timestamp));
		parameters.add(new PostParameter("oauth_nonce", oauth_nonce));
		if (!Util.isEmpty(callback_url)) {
			parameters.add(new PostParameter("oauth_callback", callback_url));
		}
		parameters.add(new PostParameter("oauth_version", oauth_version));

		String signature = makeSign(parameters, RequestTokenURL, GET_METHOD,
				getApiSecret(), "");
		parameters.add(new PostParameter("oauth_signature", signature));

		String queryString = WeiboUtil.encodeParameters(parameters, "&", false);

		String url = Util.isEmpty(queryString) ? RequestTokenURL
				: RequestTokenURL + "?" + queryString;
		try {
			String result = Util.urlGet(url);
			Bundle token = WeiboUtil.parseToken(result);
			String apikey = token.getString("oauth_token");
			String apisecret = token.getString("oauth_token_secret");
			if (apikey != null && apisecret != null) {
				return new TencentRequestToken(this, callback_url, apikey,
						apisecret);
			} else {
				throw new WeiboException("未获取到授权。");
			}
		} catch (Exception e) {
			throw new WeiboException(e);
		}
	}

	public TencentAccessToken getAccessToken(String requestToken,
			String requestTokenSecret, String verifier) throws WeiboException {
		List<PostParameter> parameters = new ArrayList<PostParameter>();

		String oauth_timestamp = WeiboUtil.generateTimeStamp();
		String oauth_nonce = WeiboUtil.generateNonce();

		parameters.add(new PostParameter("oauth_consumer_key", getApiKey()));
		parameters.add(new PostParameter("oauth_nonce", oauth_nonce));
		parameters.add(new PostParameter("oauth_signature_method",
				oauth_signature_method));
		parameters.add(new PostParameter("oauth_timestamp", oauth_timestamp));
		parameters.add(new PostParameter("oauth_token", requestToken));
		parameters.add(new PostParameter("oauth_verifier", verifier));
		parameters.add(new PostParameter("oauth_version", oauth_version));

		String signature = makeSign(parameters, AccessTokenURL, GET_METHOD,
				getApiSecret(), requestTokenSecret);
		parameters.add(new PostParameter("oauth_signature", signature));

		String queryString = WeiboUtil.encodeParameters(parameters, "&", false);

		String url = Util.isEmpty(queryString) ? AccessTokenURL
				: AccessTokenURL + "?" + queryString;
		try {
			String result = Util.urlGet(url);
			Bundle bundle = WeiboUtil.parseToken(result);
			String token = bundle.getString("oauth_token");
			String tokenSecret = bundle.getString("oauth_token_secret");
			if (token != null && tokenSecret != null) {
				SharedPreferences pref = context.getSharedPreferences(
						Weibo.PREF_WEIBO, Context.MODE_PRIVATE);
				pref.edit().putString(PREF_ACCESSTOKEN, token).putString(
						PREF_ACCESSTOKEN_SECRET, tokenSecret).commit();
				return new TencentAccessToken(this, token, tokenSecret);
			} else {
				throw new WeiboException("未获取到授权。");
			}
		} catch (Exception e) {
			throw new WeiboException(e);
		}
	}

	public boolean updateStatus(TencentAccessToken accessToken, String status)
			throws WeiboException {
		List<PostParameter> parameters = new ArrayList<PostParameter>();

		String oauth_timestamp = WeiboUtil.generateTimeStamp();
		String oauth_nonce = WeiboUtil.generateNonce();

		parameters.add(new PostParameter("oauth_consumer_key", getApiKey()));
		parameters.add(new PostParameter("oauth_nonce", oauth_nonce));
		parameters.add(new PostParameter("oauth_signature_method",
				oauth_signature_method));
		parameters.add(new PostParameter("oauth_timestamp", oauth_timestamp));
		parameters
				.add(new PostParameter("oauth_token", accessToken.getToken()));
		parameters.add(new PostParameter("oauth_version", oauth_version));

		parameters.add(new PostParameter("format", "json"));
		parameters.add(new PostParameter("content", status));
		parameters.add(new PostParameter("clientip", "0.0.0.0"));

		String signature = makeSign(parameters, AddWeiboURL, POST_METHOD,
				getApiSecret(), accessToken.getTokenSecret());
		parameters.add(new PostParameter("oauth_signature", signature));

		String queryString = WeiboUtil.encodeParameters(parameters, "&", false);

		try {
			String result = Util.urlPost(AddWeiboURL, queryString);
			JSONObject json = new JSONObject(result);
			if (json.getInt("errcode") == 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean uploadStatus(TencentAccessToken accessToken, String status,
			File file) throws WeiboException {
		List<PostParameter> parameters = new ArrayList<PostParameter>();

		String oauth_timestamp = WeiboUtil.generateTimeStamp();
		String oauth_nonce = WeiboUtil.generateNonce();

		parameters.add(new PostParameter("oauth_consumer_key", getApiKey()));
		parameters.add(new PostParameter("oauth_nonce", oauth_nonce));
		parameters.add(new PostParameter("oauth_signature_method",
				oauth_signature_method));
		parameters.add(new PostParameter("oauth_timestamp", oauth_timestamp));
		parameters
				.add(new PostParameter("oauth_token", accessToken.getToken()));
		parameters.add(new PostParameter("oauth_version", oauth_version));

		parameters.add(new PostParameter("format", "json"));
		parameters.add(new PostParameter("content", status));
		parameters.add(new PostParameter("clientip", "0.0.0.0"));

		String signature = makeSign(parameters, AddWeiboWithPicURL,
				POST_METHOD, getApiSecret(), accessToken.getTokenSecret());
		parameters.add(new PostParameter("oauth_signature", signature));

		parameters.add(new PostParameter("pic", file));

		try {
			String result = WeiboUtil.multipartPost(AddWeiboWithPicURL,
					parameters);
			JSONObject json = new JSONObject(result);
			if (json.getInt("errcode") == 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public TencentAccessToken getAccessToken() {
		SharedPreferences pref = context.getSharedPreferences(Weibo.PREF_WEIBO,
				Context.MODE_PRIVATE);
		String token = pref.getString(PREF_ACCESSTOKEN, null);
		String tokenSecret = pref.getString(PREF_ACCESSTOKEN_SECRET, null);
		if ((!Util.isEmpty(token)) && (!Util.isEmpty(tokenSecret))) {
			return new TencentAccessToken(this, token, tokenSecret);
		}
		return null;
	}
}
