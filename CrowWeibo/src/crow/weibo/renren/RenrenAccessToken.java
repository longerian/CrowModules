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
package crow.weibo.renren;

import java.io.File;

import crow.weibo.AccessToken;
import crow.weibo.WeiboException;


public class RenrenAccessToken extends AccessToken{
	private RenrenWeibo weibo;
	public RenrenAccessToken(RenrenWeibo weibo,String token, String tokenSecret) {
		super(token, tokenSecret);
		this.weibo = weibo;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean updateStatus(String status) throws WeiboException {
		return weibo.updateStatus(this, status);		
	}

	@Override
	public boolean uploadStatus(String status, File file) throws WeiboException {
		// TODO Auto-generated method stub
		return false;
	}
}
