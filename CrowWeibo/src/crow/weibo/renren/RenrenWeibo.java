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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import crow.util.Util;
import crow.weibo.AccessToken;
import crow.weibo.PostParameter;
import crow.weibo.RequestToken;
import crow.weibo.Weibo;
import crow.weibo.WeiboException;
import crow.weibo.util.WeiboUtil;

public class RenrenWeibo extends Weibo {
	private Context context;
	private static final String AUTHORIZE_URL = "https://graph.renren.com/oauth/authorize";	
		
	private static final String[] DEFAULT_PERMISSIONS = { "publish_feed",
		"create_album", "photo_upload", "read_user_album", "status_update" };
	
	public RenrenWeibo(Context context) {
		super(Weibo.RENREN_WEIBO);
		this.context = context;
	}

	@Override
	public RequestToken getOAuthRequestToken(String callbackUrl)
			throws WeiboException {
		// 请求的参数
		List<PostParameter> parameters = new ArrayList<PostParameter>();
		parameters.add(new PostParameter("client_id", getApiKey()));
		parameters.add(new PostParameter("redirect_uri", callbackUrl));
		parameters.add(new PostParameter("response_type", "token"));
		parameters.add(new PostParameter("display", "touch"));
		
		String scope = TextUtils.join(" ", DEFAULT_PERMISSIONS);
		parameters.add(new PostParameter("scope", scope));
		
		String url = AUTHORIZE_URL + "?" + WeiboUtil.encodeParameters(parameters,"&",false);		
		
		return new RenrenRequestToken(callbackUrl,"","",url,this);
	}
	
	public RenrenAccessToken getAccessToken(String url){
		Bundle bundle = WeiboUtil.parseToken(url);
		String token = bundle.getString("access_token");
		String expires_in  = bundle.getString("expires_in");		
		return new RenrenAccessToken(this,token, "");
		
		
	}
		
	public boolean updateStatus(RenrenAccessToken accessToken, String status) throws WeiboException {
		List<PostParameter> params = new ArrayList<PostParameter>();
		params.add(new PostParameter("method", "feed.publishFeed"));
		params.add(new PostParameter("v", "1.0"));
		params.add(new PostParameter("access_token", accessToken.getToken()));
		
		params.add(new PostParameter("name", "testName"));
		params.add(new PostParameter("description", "testDescription"));
		params.add(new PostParameter("url", "http://www.baidu.com"));
		params.add(new PostParameter("image", "http://www.baidu.com/img/slogo_guoqing_20111001_9fd1b759468fee84cbc839118625374d.gif"));
		params.add(new PostParameter("caption", "新鲜事标题"));
		
		Collections.sort(params);
		StringBuffer buf = new StringBuffer();
		for(PostParameter p : params){
			buf.append(p.getName()).append("=").append(p.getValue());			
		}
		buf.append(getApiSecret());
		
		params.add(new PostParameter("sig", Util.md5(buf.toString())));		
			
		String url = "http://api.renren.com/restserver.do";
		
		try {
			String result = Util.urlPost(url, WeiboUtil.encodeParameters(params, "&", false));
			System.out.println(result);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public AccessToken getAccessToken() {
		// TODO Auto-generated method stub
		return null;
	}
}
