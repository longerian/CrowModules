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
package yek.weibo.sina;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yek.util.Util;
import yek.weibo.AccessToken;
import yek.weibo.PostParameter;
import yek.weibo.RequestToken;
import yek.weibo.Weibo;
import yek.weibo.WeiboException;
import yek.weibo.kaixin.KaixinAccessToken;
import yek.weibo.tencent.TencentAccessToken;
import yek.weibo.util.WeiboUtil;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * A java reporesentation of the <a href="http://open.t.sina.com.cn/wiki/">Weibo API</a>
 * @editor sinaWeibo
 */
/**
 * @author sinaWeibo
 * 
 */

public class SinaWeibo extends Weibo implements java.io.Serializable {
	private static final String PREF_ACCESSTOKEN = "sina_oauth_token";
	private static final String PREF_ACCESSTOKEN_SECRET = "sina_oauth_token_secret";
	private Context context;

	private final static String VERSION = "1.2.1";
	private String baseURL = "http://api.t.sina.com.cn/";

	public static String requestTokenURL = "http://api.t.sina.com.cn/oauth/request_token";
	public static String authorizationURL = "http://api.t.sina.com.cn/oauth/authorize";
	public static String authenticationURL = "http://api.t.sina.com.cn/oauth/authenticate";
	public static String accessTokenURL = "http://api.t.sina.com.cn/oauth/access_token";

	private Map<String, String> requestHeaders = new HashMap<String, String>();

	private static final long serialVersionUID = -1486360080128882436L;

	// --------------base method----------
	public SinaWeibo(Context context) {
		super(Weibo.SINA_WEIBO);
		this.context = context;

		// 请求头信息
		requestHeaders.put("X-Weibo-Client-Version", VERSION);
		requestHeaders.put("X-Weibo-Client-URL", "http://open.t.sina.com.cn/-"
				+ VERSION + ".xml");
	}

	private HttpURLConnection makeConnection(String url,
			List<PostParameter> params, String token, String tokenSecret)
			throws MalformedURLException, IOException {
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) new URL(url).openConnection();

		String authorization = null;
		
		String timestamp = WeiboUtil.generateTimeStamp();
		String nonce = WeiboUtil.generateNonce();

		List<PostParameter> oauthHeaderParams = new ArrayList<PostParameter>(5);
		oauthHeaderParams.add(new PostParameter("oauth_consumer_key",
				getApiKey()));
		oauthHeaderParams.add(new PostParameter("oauth_signature_method",
				"HMAC-SHA1"));
		oauthHeaderParams.add(new PostParameter("oauth_timestamp", timestamp));
		oauthHeaderParams.add(new PostParameter("oauth_nonce", nonce));

		oauthHeaderParams.add(new PostParameter("oauth_version", "1.0"));
		if (!Util.isEmpty(token)) {
			oauthHeaderParams.add(new PostParameter("oauth_token", token));
		}
		List<PostParameter> signatureBaseParams = new ArrayList<PostParameter>(
				oauthHeaderParams.size() + params.size());
		signatureBaseParams.addAll(oauthHeaderParams);
		signatureBaseParams.addAll(params);

		Collections.sort(signatureBaseParams);
		String oauthBaseString = WeiboUtil.generateSignatureBase(
				url, POST_METHOD, signatureBaseParams);

		// log("OAuth base string:", oauthBaseString);
		String signature = WeiboUtil.hmacSHA1Signature(oauthBaseString,
				getApiSecret(), tokenSecret);
		// log("OAuth signature:", signature);
		oauthHeaderParams.add(new PostParameter("oauth_signature", signature));
		authorization = "OAuth "
				+ WeiboUtil.encodeParameters(oauthHeaderParams, ",", true);

		conn.addRequestProperty("Authorization", authorization);
		for (String key : requestHeaders.keySet()) {
			conn.addRequestProperty(key, requestHeaders.get(key));
		}

		return conn;

	}

	/**
	 * 获取request token
	 * 
	 * @return generated request token.
	 * @throws WeiboException
	 *             when Weibo service or network is unavailable
	 * @since Weibo4J 1.2.1
	 * @see <a href="http://oauth.net/core/1.0/#auth_step1">OAuth Core 1.0 -
	 *      6.1. Obtaining an Unauthorized Request Token</a>
	 */
	@Override
	public SinaRequestToken getOAuthRequestToken(String callback_url)
			throws WeiboException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("oauth_callback", callback_url));
		params.add(new PostParameter("source", getApiKey()));

		// 设置Http Post 头信息

		try {
			HttpURLConnection conn = makeConnection(requestTokenURL, params,
					null, null);
			String result = Util.urlPost(conn, WeiboUtil.encodeParameters(
					params, "&", false));
			Bundle bundle = WeiboUtil.parseToken(result);
			String tokenSecret = bundle.getString("oauth_token_secret");
			String token = bundle.getString("oauth_token");
			if (!Util.isEmpty(token) && !Util.isEmpty(tokenSecret)){
				return new SinaRequestToken(this, callback_url, token, tokenSecret);
			}else{
				throw new WeiboException("获取失败");
			}			
		} catch (IOException e) {
			throw new WeiboException(e);
		}
	}

	public SinaAccessToken getAccessToken(SinaRequestToken requestToken,
			String verifier) throws WeiboException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("source", getApiKey()));
		params.add(new PostParameter("oauth_verifier", verifier));

		// 设置Http Post 头信息
		try {
			HttpURLConnection conn = makeConnection(accessTokenURL, params,
					requestToken.getToken(), requestToken.getTokenSecret());
			String result = Util.urlPost(conn, WeiboUtil.encodeParameters(
					params, "&", false));
			Bundle bundle = WeiboUtil.parseToken(result);
			
			String token = bundle.getString("oauth_token");
			String tokenSecret = bundle.getString("oauth_token_secret");

			if (token != null && tokenSecret != null) {
				SharedPreferences pref = context.getSharedPreferences(
						Weibo.PREF_WEIBO, Context.MODE_PRIVATE);
				pref.edit().putString(PREF_ACCESSTOKEN, token).putString(
						PREF_ACCESSTOKEN_SECRET, tokenSecret).commit();
				return new SinaAccessToken(this, token, tokenSecret);
			} else {
				throw new WeiboException("未获取到授权。");
			}
		} catch (IOException e) {
			throw new WeiboException(e);
		}
	}

	/**
	 * 发布一条微博信息
	 * 
	 * @param status
	 *            要发布的微博消息文本内容
	 * @return the latest status
	 * @throws WeiboException
	 *             when Weibo service or network is unavailable
	 * @since Weibo4J 1.2.1
	 * @see <a
	 *      href="http://open.t.sina.com.cn/wiki/index.php/Statuses/update">statuses/update
	 *      </a>
	 */

	public boolean updateStatus(AccessToken token, String status)
			throws WeiboException {
		String url = baseURL + "statuses/update.json";
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("status", status));
		params.add(new PostParameter("source", getApiKey()));

		try {
			HttpURLConnection conn = makeConnection(url, params, token
					.getToken(), token.getTokenSecret());
			String result = Util.urlPost(conn, WeiboUtil.encodeParameters(
					params, "&", false));
			Bundle bundle = WeiboUtil.parseToken(result);

			String tokenSecret = bundle.getString("oauth_token_secret");
			String oauth_token = bundle.getString("oauth_token");
			return true;
		} catch (IOException e) {
			throw new WeiboException(e);
		}
	}

	/**
	 * 发表图片微博消息。目前上传图片大小限制为<5M。
	 * 
	 * @param status
	 *            要发布的微博消息文本内容
	 * @param file
	 *            要上传的图片
	 * @return
	 * @throws WeiboException
	 *             when Weibo service or network is unavailable
	 * @since Weibo4J 1.2.1
	 * @see <a
	 *      href="http://open.t.sina.com.cn/wiki/index.php/Statuses/upload">statuses/upload
	 *      </a>
	 */

	public boolean uploadStatus(SinaAccessToken token, String status, File file)
			throws WeiboException {
		String url = baseURL + "statuses/upload.json";
		List<PostParameter> params = new ArrayList<PostParameter>();
		
		status = WeiboUtil.encode(status);		 
		params.add(new PostParameter("status", status));
		params.add(new PostParameter("source", getApiKey()));

		try {
			HttpURLConnection conn = makeConnection(url, params, token
					.getToken(), token.getTokenSecret());
			params.add(new PostParameter("pic", file));

			String result = WeiboUtil.multipartPost(conn, params);

			Bundle bundle = WeiboUtil.parseToken(result);

			String tokenSecret = bundle.getString("oauth_token_secret");
			String oauth_token = bundle.getString("oauth_token");
			return true;
		} catch (IOException e) {
			throw new WeiboException(e);
		}
	}

	@Override
	public SinaAccessToken getAccessToken() {
		SharedPreferences pref = context.getSharedPreferences(Weibo.PREF_WEIBO,
				Context.MODE_PRIVATE);
		String token = pref.getString(PREF_ACCESSTOKEN, null);
		String tokenSecret = pref.getString(PREF_ACCESSTOKEN_SECRET, null);
		if ((!Util.isEmpty(token)) && (!Util.isEmpty(tokenSecret))) {
			return new SinaAccessToken(this, token, tokenSecret);
		}
		return null;
	}
}