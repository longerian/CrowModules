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
package crow.loader;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import crow.cache.Cache;

/**
 * 	异步加载的调度类。实现数据加载。与加载结果的主线程回调工作。
 *	一个调度类，可同时调度多个加载类。内部采用线程池实现异步数据的同时加载。
 * @author crow
 *
 */
public class AsyncLoaderEngine {
	private final static int SendMessage = 1;

	private Handler handler;
	private ThreadPoolExecutor threadPool;
	private Cache cache;

	public AsyncLoaderEngine(Context context, ThreadPoolExecutor threadPool,
			Cache cache) {
		this.cache = cache;
		this.threadPool = threadPool;
		handler = new Handler(context.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SendMessage:
					LoaderRunnable<?, ?, ?> r = (LoaderRunnable<?, ?, ?>) msg.obj;
					r.sendMessageToLoader();
					break;
				}
			}
		};
	}
    	
	@SuppressWarnings("hiding")
	public <I, R, T> void asyncLoad(AsyncLoader<I, R, T> loader, I id, T target) {
		Runnable r = new LoaderRunnable<I, R, T>(loader, id, target);
		loader.addRunnable(id, r);
		threadPool.execute(r);
	}

	/**
	 * 取消下载
	 * 
	 * @param id
	 * @return
	 */
	public void cancel(Runnable r) {
		threadPool.remove(r);
	}

	/**
	 * 取消集合中的所有数据下载
	 * @param runnables
	 */
	public void cancel(Collection<Runnable> runnables) {
		if (runnables != null) {
			for (Runnable r : runnables)
				threadPool.remove(r);
		}
	}

	@SuppressWarnings("hiding")
	private class LoaderRunnable<I, R, T> implements Runnable {
		private AsyncLoader<I, R, T> loader;
		private I id;

		private T target;
		private R resource;

		public LoaderRunnable(AsyncLoader<I, R, T> loader, I id, T target) {
			this.loader = loader;
			this.id = id;
			this.target = target;
		}

		public void sendMessageToLoader() {
			loader.removeRunnable(id); // 从加载队列移除
			if (resource == null) {
				loader.onLoadFail(id, target);
			} else {
				loader.onLoaded(id, resource, target);
			}
			resource = null;
			target = null;

		}

		@Override
		public void run() {
			resource = loader.loadFromCache(cache, id);
			if (resource == null && loader.downloadToCache(cache, id)) {
				resource = loader.loadFromCache(cache, id);
			}
			if (resource != null) {
				loader.addToMemBuffer(id, resource);
			}

			Message message = new Message();
			message.what = SendMessage;
			message.obj = this;
			handler.sendMessage(message);
		}

	}
}
