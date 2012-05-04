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

import yek.weibo.RequestToken;
import yek.weibo.WeiboException;
import yek.weibo.util.WeiboUtil;
import android.os.Bundle;

public class KaixinRequestToken extends RequestToken {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1782799643092330764L;	
	
	private KaixinWeibo weibo;

	public KaixinRequestToken(KaixinWeibo weibo,String oauthCallback,String token, String tokenSecret) {
		super(oauthCallback,token, tokenSecret);
		this.weibo = weibo;
	}

	@Override
	public String getAuthenticationURL() {
		return "http://api.kaixin001.com/oauth/authorize?oauth_token="
				+ getToken() + "&oauth_client=1";
	}

	@Override
	public KaixinAccessToken getAccessToken(String oauthCallBack)
			throws WeiboException {
		Bundle bundle = WeiboUtil.parseToken(oauthCallBack);
		String verifier = bundle.getString("oauth_verifier");
		return weibo.getAccessToken(getToken(), getTokenSecret(), verifier);
	}
}
