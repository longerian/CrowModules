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
package yek.weibo.renren;

import yek.weibo.RequestToken;
import yek.weibo.WeiboException;
import yek.weibo.util.WeiboUtil;
import android.os.Bundle;

public class RenrenRequestToken extends RequestToken{
	private String url;
	private RenrenWeibo weibo;
	public RenrenRequestToken(String oauthCallback, String token,
			String tokenSecret,String authenticationURL,RenrenWeibo weibo) {
		super(oauthCallback, token, tokenSecret);
		this.url = authenticationURL;
		this.weibo = weibo;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public RenrenAccessToken getAccessToken(String oauthCallBack)
			throws WeiboException {				
		return weibo.getAccessToken(oauthCallBack);
	}

	@Override
	public String getAuthenticationURL() {
		return url;
	}
}
