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

import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import android.widget.ImageView;
import crow.cache.Cache;
import crow.util.BitmapUtil;

/**
 * 异步图片 加载
 * 
 * @author crow
 *
 */
public class BitmapAsyncLoader extends AsyncLoader<String, Bitmap, ImageView> {

	public BitmapAsyncLoader(AsyncLoaderEngine engine) {
		super(engine);
	}
	
	/**
	 * 将url 对应的图片加载到本地缓存。
	 */
	@Override
	protected boolean downloadToCache(Cache cache, String url) {
		try {
			cache.cacheURL(url);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	/**
	 * 如果对图片需按采样率生成，请写此方法
	 */
	@Override
	protected Bitmap loadFromCache(Cache cache, String url) {
		Bitmap bm = null;
		File file = cache.getCacheFile(url);
		if (file.exists()) {
			bm = BitmapUtil.loadBitmap(file);
		}
		return bm;
	}
	
	/**
	 * 空实现，若加载失败需要设为特殊图片。可在此方法中完成。
	 */
	@Override
	protected void onLoadFail(String url, ImageView target) {
		
	}

	@Override
	protected void onLoaded(String url, Bitmap bm, ImageView target) {
		if (target != null){
			target.setImageBitmap(bm);
		}
	}
	
	
}
