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
package crow.weibo;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import crow.util.Util;
import crow.weibo.kaixin.KaixinWeibo;
import crow.weibo.renren.RenrenWeibo;
import crow.weibo.sina.SinaWeibo;
import crow.weibo.tencent.TencentWeibo;
import crow.weibo.util.WeiboUtil;

/**
 * 注意 人人微博需要在服务器上设定回调地址。请确保服务器上的回调地址与获取 RequestToken 的地址相同
 * @author crow
 *
 */
public abstract class Weibo {
	// 保存持久化数据的Preference 文件名
	protected static final String PREF_WEIBO = "sdk_weibo";
	
	protected static final String GET_METHOD = "GET";
	protected static final String POST_METHOD = "POST";
	/**
	 * 新浪微博
	 */
	public static final int SINA_WEIBO = 0;

	/**
	 * 腾讯微博
	 */
	public static final int TENCENT_WEIBO = 1;

	/**
	 * 人人网微博
	 */
	public static final int RENREN_WEIBO = 2;

	/**
	 * 开心网微博
	 */
	public static final int KAIXIN_WEIBO = 3;

	// 存放所有的ApiKey 和 ApiSecret
	private static String[][] KEY_SECRET = new String[4][2];

	// 当前微博的类型id
	private int type;

	/**
	 * 当前微博的类型id
	 * 
	 * @param type
	 */
	protected Weibo(int type) {
		this.type = type;
	}

	/**
	 * 获取当前微博的apiKey
	 * 
	 * @return
	 */
	public String getApiKey() {
		return KEY_SECRET[type][0];
	}

	/**
	 * 获取当前微博的apiSecret
	 * 
	 * @return
	 */
	public String getApiSecret() {
		return KEY_SECRET[type][1];
	}

	/**
	 * 
	 * @param type
	 *            用于指定微博的类型、 如新浪 {@link #SINA_WEIBO}
	 * @param apiKey
	 *            微博平台分配的API Key
	 * @param apiSecret
	 *            微博平台分配的 API Secret
	 */
	public static void setApiKeyAndSecret(int type, String apiKey,
			String apiSecret) {
		if (type < 0 || type >= KEY_SECRET.length) {
			throw new RuntimeException("不支持该类型的微博平台。");
		}
		KEY_SECRET[type][0] = apiKey;
		KEY_SECRET[type][1] = apiSecret;
	}

	/**
	 * 
	 * @param type
	 *            用于指定微博的类型、 如新浪 {@link #SINA_WEIBO}
	 * @see SINA_WEIBO
	 */
	public static Weibo getInstance(Context context,int type) {
		if (type < 0 || type >= KEY_SECRET.length) {
			throw new RuntimeException("不支持该类型的微博平台。");
		} else if (Util.isEmpty(KEY_SECRET[type][0])
				|| Util.isEmpty(KEY_SECRET[type][0])) {
			throw new RuntimeException("未设置类型id 为:" + type
					+ " 微博的ApiKey 和 ApiSecret。");
		}
		context = context.getApplicationContext();
		switch (type) {
		case SINA_WEIBO:
			return new SinaWeibo(context);
		case TENCENT_WEIBO:
			return new TencentWeibo(context);
		case RENREN_WEIBO:
			return new RenrenWeibo(context);
		case KAIXIN_WEIBO:
			return new KaixinWeibo(context);
		}
		return null;
	}
	
	public abstract AccessToken getAccessToken();

	public String makeSign(List<PostParameter> params, String requestURL,
			String httpMethod, String apiSecret, String tokenSecret) {
		Collections.sort(params);
		String baseString = WeiboUtil.generateSignatureBase(requestURL,
				httpMethod, params);
		// 签名
		String signature = WeiboUtil.hmacSHA1Signature(baseString, apiSecret,
				tokenSecret);
		return signature;
	}

	/**
	 * 获取request token
	 * 
	 * @param callback_url
	 *            用户授权候的url 回调
	 * @return
	 * @throws WeiboException
	 */
	public abstract RequestToken getOAuthRequestToken(String callback_url)
			throws WeiboException;
}
