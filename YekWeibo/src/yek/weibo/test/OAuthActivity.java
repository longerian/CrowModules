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
package yek.weibo.test;

import yek.weibo.AccessToken;
import yek.weibo.R;
import yek.weibo.WeiboException;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuthActivity extends Activity {
	WebView web;
	yek.weibo.RequestToken requestToken;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oauth);
		web = (WebView) findViewById(R.id.webView);
		
		web.setWebViewClient(new WebViewClient(){
			
			

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (requestToken.isOAuthCallBack(url)){
					try {
						AccessToken token = requestToken.getAccessToken(url);
						setResult(Activity.RESULT_OK);						
					} catch (WeiboException e) {
						setResult(Activity.RESULT_CANCELED);						
					}
					web.stopLoading();
					finish();
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (requestToken.isOAuthCallBack(url)){
					try {
						AccessToken token = requestToken.getAccessToken(url);
						setResult(Activity.RESULT_OK);						
					} catch (WeiboException e) {
						setResult(Activity.RESULT_CANCELED);						
					}
					web.stopLoading();
					finish();
					return true;
				}
				return super.shouldOverrideUrlLoading(view, url);
			}			
		});

		new Thread(new Runnable() {
			@Override
			public void run() {
				int type = OAuthActivity.this.getIntent()
						.getIntExtra("type", 0);
				yek.weibo.Weibo weibo = yek.weibo.Weibo.getInstance(
						OAuthActivity.this, type);

				try {
					requestToken = weibo.getOAuthRequestToken("http://graph.renren.com/oauth/login_success.html");
					web.loadUrl(requestToken.getAuthenticationURL());
				} catch (WeiboException e) {
					setResult(Activity.RESULT_CANCELED);
					finish();
				}				
			}
		}).start();
	}
}
