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
package crow.weibo.test;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import crow.util.DownloadUtil;
import crow.util.Util;
import crow.weibo.AccessToken;
import crow.weibo.R;
import crow.weibo.Weibo;
import crow.weibo.WeiboException;

public class MainActivity extends Activity implements OnClickListener {
	private View kaixin, renren, sina, tencent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(crow.weibo.R.layout.main);
		// 设置 ApiKey、 ApiSecret
		Weibo.setApiKeyAndSecret(Weibo.KAIXIN_WEIBO,
				"78898190360968205b25c8f038b162bf",
				"bb181f7abc33da0a7632c067aeee8e69");
		Weibo.setApiKeyAndSecret(Weibo.RENREN_WEIBO,
				"db5d18d34c6345848e2b551dacddb756",
				"36b3b54a222a468d845ccba8721e61fc");
		Weibo.setApiKeyAndSecret(Weibo.SINA_WEIBO, "538090564",
				"14327682977fc2dfde3ff1c274dfdbea");
		Weibo.setApiKeyAndSecret(Weibo.TENCENT_WEIBO,
				"671dc036a4924fa39027abe4b7a7091a",
				"22b430ced88be161924acdfe40dbb68c");

		kaixin = findViewById(R.id.kaixin);
		renren = findViewById(R.id.renren);
		sina = findViewById(R.id.sina);
		tencent = findViewById(R.id.tencent);

		kaixin.setOnClickListener(this);
		renren.setOnClickListener(this);
		sina.setOnClickListener(this);
		tencent.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Weibo weibo;
		AccessToken token;
		Intent intent;
		switch (v.getId()) {
		case R.id.kaixin:
			weibo = Weibo.getInstance(this, Weibo.KAIXIN_WEIBO);
			token = weibo.getAccessToken();
			if (token == null) {
				intent = new Intent();
				intent.setClass(this, OAuthActivity.class);
				intent.putExtra("type", Weibo.KAIXIN_WEIBO);
				startActivityForResult(intent, Weibo.KAIXIN_WEIBO);
			} else {
				sendWeibo(token);
			}
			break;
		case R.id.renren:
			weibo = Weibo.getInstance(this, Weibo.RENREN_WEIBO);
			token = weibo.getAccessToken();
			if (token == null) {
				intent = new Intent();
				intent.setClass(this, OAuthActivity.class);
				intent.putExtra("type", Weibo.RENREN_WEIBO);
				startActivityForResult(intent, Weibo.RENREN_WEIBO);
			} else {
				sendWeibo(token);
			}
			break;
		case R.id.sina:
			weibo = Weibo.getInstance(this, Weibo.SINA_WEIBO);
			token = weibo.getAccessToken();
			if (token == null) {
				intent = new Intent();
				intent.setClass(this, OAuthActivity.class);
				intent.putExtra("type", Weibo.SINA_WEIBO);
				startActivityForResult(intent, Weibo.SINA_WEIBO);
			} else {
				sendWeibo(token);
			}
			break;
		case R.id.tencent:
			weibo = Weibo.getInstance(this, Weibo.TENCENT_WEIBO);
			token = weibo.getAccessToken();
			if (token == null) {
				intent = new Intent();
				intent.setClass(this, OAuthActivity.class);
				intent.putExtra("type", Weibo.TENCENT_WEIBO);
				startActivityForResult(intent, Weibo.TENCENT_WEIBO);
			} else {
				sendWeibo(token);
			}
			break;
		}
	}

	private void sendWeibo(final AccessToken token) {
		new Thread(new Runnable() {

			@Override
			public void run() {
//				try {
//					token.updateStatus("微博数据"
//							+ Util.md5(System.currentTimeMillis() + ""));
//				} catch (WeiboException e) {
//					// TODO 发送失败，进行通知
//					e.printStackTrace();
//				}
				File f = new File(MainActivity.this.getCacheDir(), "mypic");
				if (!f.exists()) {
					DownloadUtil
							.downloadFile(
									"http://www.baidu.com/img/slogo_guoqing_20111001_9fd1b759468fee84cbc839118625374d.gif",
									f, 30000);
				}

				try {
					token.uploadStatus("看看这张图" + Util.md5(System.currentTimeMillis() + ""), f);
				} catch (WeiboException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			sendWeibo(Weibo.getInstance(this, requestCode).getAccessToken());
			switch (requestCode) {
			case Weibo.TENCENT_WEIBO:
			case Weibo.SINA_WEIBO:
			case Weibo.RENREN_WEIBO:
			case Weibo.KAIXIN_WEIBO:
				sendWeibo(Weibo.getInstance(this, requestCode).getAccessToken());
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
