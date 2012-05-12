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

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import crow.cache.Cache;

/**
 * <pre>
 * 	 异步数据加载实现类需继承于该类，在该类中实现了数据的Memery Cache
 *	管理。数据加载逻辑，数据加载取消等功能。
 *	子类实现：
 *	子类实现只需完成
 *	protected abstract boolean downloadToCache(Cache cache, I id);
 *	实现把数据加载到磁盘缓存。
 *	protected abstract R loadFromCache(Cache cache, I id);
 *	实现把磁盘缓存数据解析为把加载的资源。 	
 *	加载的回调：在子类实现中可有默认实现。	
 *	    回调的方法有：
 *    回调方法都会在主线程中回调。
 *	protected abstract void onLoaded(I id, R resource, T target);
 *	    当数据加载成功时回调 。
 *	protected abstract void onLoadFail(I id, T target);
 * 	当数据加载失败时回调。
 * </pre>
 * @author crow
 * 
 * @param <I>
 *            资源对应的id ,如图片的url. 产品的id
 * @param <R> 加载的资源类型。
 * @param <T> 数据的作用目标对象。
 */
@SuppressWarnings("hiding")
public abstract class AsyncLoader<I, R, T> {
	// 异步加载引擎
	private AsyncLoaderEngine engine;
	// 加载后数据的弱引用的缓存
	private Map<I, SoftReference<R>> buffer = new HashMap<I, SoftReference<R>>();

	// 正在进行异步加载的集合
	private Map<I, Runnable> runnableSet = new HashMap<I, Runnable>();

	private Lock lock = new ReentrantLock();

	public AsyncLoader(AsyncLoaderEngine engine) {
		this.engine = engine;
	}

	final void addToMemBuffer(I id, R resource) {
		try {
			lock.lock();
			buffer.put(id, new SoftReference<R>(resource));
		} finally {
			lock.unlock();
		}
	}

	final void addRunnable(I id, Runnable runnable) {
		synchronized (runnableSet) {
			runnableSet.put(id, runnable);
		}
	}

	final void removeRunnable(I id) {
		synchronized (runnableSet) {
			runnableSet.remove(id);
		}
	}

	/**
	 * 进行资源的加载。 当返回值为null 时可设为默认数据，然后等待异步加载后进行更新。 如相应资源正在加载，不会进行重复加载。
	 * 
	 * @param id
	 *            资源对应的Id,如：url
	 * @param target
	 *            资源加载后作用的目标，如：加载图片数据时
	 * @return 内存中获取到的资源 null 未在内存中获取到，将通过异步加载引擎 获取。并通过 {@link #onLoaded()} 或
	 *         {@link #onLoadFail} 进行回调
	 */
	public final R load(I id, T target) {
		R resource = null;
		try {
			lock.lock();
			SoftReference<R> dRef = buffer.get(id);
			if (dRef != null && (resource = dRef.get()) == null) {
				buffer.remove(id);
			}
		} finally {
			lock.unlock();
		}
		if (resource == null) {
			synchronized (runnableSet) {
				if (runnableSet.containsKey(id)) {
					return null;
				}
			}
			engine.asyncLoad(this, id, target);
		}
		return resource;
	}

	/**
	 * 取消数据的异步加载
	 * 
	 * @param id
	 */
	public final void cancel(I id) {
		Runnable r = null;
		synchronized (runnableSet) {
			r = runnableSet.remove(id);
		}
		if (r != null) {
			engine.cancel(r);
		}
	}

	/**
	 * 取消所有数据的异步加载
	 */
	public final void cancel() {
		synchronized (runnableSet) {
			engine.cancel(runnableSet.values());
			runnableSet.clear();
		}
	}

	/**
	 * 从服务器或其它地方加载数据到本地缓存中
	 * 
	 * @param cache
	 *            本地缓存
	 * @param id
	 *            资源对应的id
	 * @return true 下载资源到本地缓存成功 false 下载资源到本地缓存不成功
	 */
	protected abstract boolean downloadToCache(Cache cache, I id);

	/**
	 * 从本地缓存中加载资源,由异步加载引擎调用
	 * 
	 * @param cache
	 *            本地缓存
	 * @param id
	 *            资源对应id
	 * @return 加载的数据 null 缓存中没有相应的数据
	 */
	protected abstract R loadFromCache(Cache cache, I id);

	/**
	 * 异步加载成功的回调
	 * 
	 * @param id
	 * @param resource
	 * @param target
	 */
	protected abstract void onLoaded(I id, R resource, T target);

	/**
	 * 异步加载失败时回调。
	 * 
	 * @param id
	 *            资源对应的Id
	 * @param target
	 *            资源加载后作用的目标
	 */
	protected abstract void onLoadFail(I id, T target);
}
