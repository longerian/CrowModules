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

import android.net.Uri;
import yek.util.Util;
import yek.weibo.AccessToken;
import yek.weibo.RequestToken;
import yek.weibo.WeiboException;

public class TencentRequestToken extends RequestToken {
	private TencentWeibo weibo;
	public TencentRequestToken(TencentWeibo weibo,String oauthCallback, String token,
			String tokenSecret) {
		super(oauthCallback, token, tokenSecret);
		this.weibo = weibo;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public TencentAccessToken getAccessToken(String oauthCallBack)
			throws WeiboException {
		Uri uri = Uri.parse(oauthCallBack);
		String oauth_verifier = uri.getQueryParameter("oauth_verifier");	
		String oauth_token = uri.getQueryParameter("oauth_token");	
		oauth_token = Util.isEmpty(oauth_token) ? getToken() : oauth_token;
		return weibo.getAccessToken(oauth_token, getTokenSecret(), oauth_verifier);
	}

	@Override
	public String getAuthenticationURL() {
		String url = "http://open.t.qq.com/cgi-bin/authorize?oauth_token="
				+ getToken();
		return url;
	}

}
