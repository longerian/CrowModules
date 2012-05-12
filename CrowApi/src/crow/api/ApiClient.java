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
package crow.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import crow.api.parser.ParseException;
import crow.api.parser.Parser;
import crow.cache.Cache;

/**
 * 
 * @author crow
 * 
 * @param <C>
 *            具体的APIContext,为APIContext 的子类
 * @param <T>
 *            具体的APIRequest,为APIRequest 的子类*
 * @param <R>
 *            具体的APIResponse 类型
 */
@SuppressWarnings("hiding")
public abstract class ApiClient<C extends ApiContext, R extends ApiResponse, T extends ApiRequest<? extends R, C>> {
	private static final int SEND_MESSAGE = 1;

	/** Api 建立数据连接超时 单位 milliseconds */
	public int API_CONNECT_TIMEOUT = 15 * 1000;
	/** Api 请求数据超时 单位 milliseconds */
	public int API_SOCKET_TIMEOUT = 30 * 1000;
	/** 发送数据的编码方式 默认 UTF-8 */
	public String PARAM_ENCODING = HTTP.UTF_8;

	private CookieStore cookieStore;
	private HttpContext localContext;
	private Parser parser; // 数据解析类
	private ThreadPoolExecutor threadPool;
	private Cache cache;
	private Handler handler;

	// 正在进行异步加载的集合
	private Map<T, Runnable> runnableSet = new HashMap<T, Runnable>();

	public ApiClient(Context context, Parser parser,
			ThreadPoolExecutor threadPool, Cache cache, CookieStore store) {
		this.parser = parser;
		this.threadPool = threadPool;
		this.cache = cache;
		this.handler = new Handler(context.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SEND_MESSAGE:
					RequestRunnable<?, ?> r = (RequestRunnable<?,?>) msg.obj;
					r.sendMessageToCallback();
					break;
				}
			}
		};
		// Create a local instance of cookie store
        cookieStore = store;
        // Create local HTTP context
        localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	/**
	 * 获取Api 上下文环境，在Api 请求参数生成。请求头生成在能会使用
	 * 
	 * @return 具体的上下文环境，要与用户状态，运行状态等关联
	 */
	public abstract C getApiContext();

	/**
	 * 将请求对象转换为 Post请求数据 完成Api请求的Post 数据的生成。 如默认参数，动态参数。如果签名有规则也可在此进行生成。
	 * 
	 * @param request
	 *            请求对象
	 * @return Post 请求数据
	 */
	protected abstract Map<String, String> makeRequestParam(T request);

	/**
	 * 生成Http 头信息
	 * 
	 * @param request
	 *            请求对象
	 * @return
	 */
	protected abstract Header[] makeRequestHeaders(T request);

	/**
	 * 进行Api 请求，如果有相同请求正在进行中。则忽略掉该请求。 请求过程为： 1.缓存未过期，返回缓存数据 2.缓存过期或解析失败，
	 * 
	 * @param <Q>
	 *            具体的Api 请求类型
	 * @param request
	 *            Api请求对象
	 * @param callback
	 *            请求的回调
	 */	
	public final <Q extends R> void request(T request, ApiCallback<Q> callback) {
		if (!runnableSet.containsKey(request)) { // 正在加载则不进行数据请求
			RequestRunnable<T, Q> r = new RequestRunnable<T,Q>(request, callback,
					false);
			runnableSet.put(request, r);
			threadPool.execute(r);
		}
	}
	
	public final <Q extends R> void requestWithDirty(T request,
			ApiCallback<Q> callback) {
		if (!runnableSet.containsKey(request)) {
			RequestRunnable<T, Q> r = new RequestRunnable<T,Q>(request, callback, true);
			runnableSet.put(request, r);
			threadPool.execute(r);
		}
	}

	/**
	 * 取消request 对应的数据加载
	 * 
	 * @param request
	 */
	public final void cancel(T request) {
		Runnable r;
		if ((r = runnableSet.remove(request)) != null) {
			threadPool.remove(r); // 取消数据加载
		}
	}

	/**
	 * 请求Api 返回结果到缓存
	 * 
	 * @param request
	 * @return
	 * @throws ApiException
	 */
	private final boolean postToCache(T request) throws ApiException {
		// 1.设定http请求参数
		BasicHttpParams httpParams = new BasicHttpParams();
		// 设定连接超时
		HttpConnectionParams.setConnectionTimeout(httpParams,
				API_CONNECT_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, API_SOCKET_TIMEOUT);
		
		HttpPost httpPost = new HttpPost(request.getRequestURL(getApiContext()));
		httpPost.setHeaders(makeRequestHeaders(request)); // 

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
        
		// 与 Request 相关参数
		Map<String, String> param = makeRequestParam(request);

		// 封装传送的数据
		ArrayList<BasicNameValuePair> postData = new ArrayList<BasicNameValuePair>();
		for (Map.Entry<String, String> m : param.entrySet()) {
			postData.add(new BasicNameValuePair(m.getKey(), m.getValue()));
		}

		try {
			// 对请求的数据进行UTF-8转码
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postData,
					PARAM_ENCODING);
			// 设置请求参数
			httpPost.setEntity(entity);
			// 获取响应对象
			HttpResponse httpResponse = httpClient.execute(httpPost, localContext);
			// 响应正常
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK
					|| httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
				List<Cookie> cookies = httpClient.getCookieStore().getCookies();
				for(Cookie c : cookies) {
					cookieStore.addCookie(c);
				}
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream input = httpEntity.getContent();
				return cache.cache(input, request);
			} else {
				throw new ApiException("Api HttpStatusCode:"
						+ httpResponse.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			throw new ApiException("Api 数据请求异常", e);
		}
	}

	/**
	 * 请求Api 返回结果到缓存
	 * 
	 * @param request
	 * @return
	 * @throws ApiException
	 */
	private final boolean getToCache(T request) throws ApiException {
		// 1.设定http请求参数
		BasicHttpParams httpParams = new BasicHttpParams();
		// 设定连接超时
		HttpConnectionParams.setConnectionTimeout(httpParams,
				API_CONNECT_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, API_SOCKET_TIMEOUT);

		// 与 Request 相关参数
		Map<String, String> param = makeRequestParam(request);
		StringBuilder basicUrl = new StringBuilder(request.getRequestURL(getApiContext())) ;
		basicUrl.append("?");
		// 封装传送的数据
		for (Map.Entry<String, String> m : param.entrySet()) {
			basicUrl.append(m.getKey())
				.append("=")
				.append(m.getValue())
				.append("&");
		}
		
		HttpGet httpGet = new HttpGet(basicUrl.toString());
		httpGet.setHeaders(makeRequestHeaders(request)); 
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		try {
			// 获取响应对象
			HttpResponse httpResponse = httpClient.execute(httpGet);
			// 响应正常
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK
					|| httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
				List<Cookie> cookies = httpClient.getCookieStore().getCookies();
				for(Cookie c : cookies) {
					cookieStore.addCookie(c);
				}
				HttpEntity httpEntity = httpResponse.getEntity();
				InputStream input = httpEntity.getContent();
				return cache.cache(input, request);
			} else {
				throw new ApiException("Api HttpStatusCode:"
						+ httpResponse.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			throw new ApiException("Api 数据请求异常", e);
		}
	}
	
	private class RequestRunnable<P extends T,Q extends R> implements Runnable {
		private P request;
		private ApiCallback<Q> callback;
		private boolean readDirty;
		private Q response;
		private ApiException e;

		public RequestRunnable(P request, ApiCallback<Q> callback,
				boolean readDirty) {
			this.request = request;
			this.callback = callback;
			this.readDirty = readDirty;
		}

		public void sendMessageToCallback() {
			runnableSet.remove(request); // 去掉runnable
			if (response != null) { // 请求结果存在
				if (response.isSuccess()) {
					callback.onSuccess(response);
				} else {
					callback.onFail(response);
				}
			} else { // 请求结果不存在
				callback.onException(e);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			if (!cache.isExpired(request)) { // 还未过期
				try {
					response = (Q) parser.parse(cache.getCacheFile(request),
							request.getResponseClass()); // 如果发生异常会进行网络请求
					handler.sendMessage(handler.obtainMessage(SEND_MESSAGE,
							this));
					return;
				} catch (ParseException e) {
					// do nothing
				}
			}

			// 数据过期或解析异常
			try {
				if(request.getHttpRequestClass().equals(HttpPost.class)) {
					if (!postToCache(request)) { // 缓存不成功
						throw new ApiException("缓存文件失败：" + request.toString());
					}
				} else if(request.getHttpRequestClass().equals(HttpGet.class)) {
					if (!getToCache(request)) { // 缓存不成功
						throw new ApiException("缓存文件失败：" + request.toString());
					}
				} else  {
					throw new ApiException("Http请求的方法错误，请检查具体ApiRequest对象的getHttpRequestClass()方法是否返回HttpPost.class或HttpGet.class：" + request.toString());
				}
			} catch (ApiException e) {
				this.e = e;
				if (!readDirty) { // 不进行脏读
					handler.sendMessage(handler.obtainMessage(SEND_MESSAGE,
							this));
					return;
				}
			}

			try {
				response = (Q) parser.parse(cache.getCacheFile(request), request
						.getResponseClass());
				if (!response.isSuccess()) { // 结果异常不进行缓存
					cache.clearCache(request);
				}
			} catch (ParseException e) {
				this.e = new ApiException("数据解析异常", e);
			}
			handler.sendMessage(handler.obtainMessage(SEND_MESSAGE, this));
		}
	}
}
