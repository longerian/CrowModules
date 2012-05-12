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
package crow.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;

/**
 * Bitmap 操作相关类
 * @author crow
 *
 */
public class BitmapUtil {
	/**
	 * 对图片大小进行粗略调整，使其大体适应图片宽高 主要用于大图片加载，防止因图片太大出现内存溢出
	 * 
	 * @param file
	 *            要读取的图片文件地址
	 * @param width
	 *            图片 宽度
	 * @param height
	 *            图片 高度
	 * @return 调整后的粗略图片
	 */
	public static Bitmap loadCompress(File file, int width, int height) {
		String pathName = file.getAbsolutePath();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;// 不加载bitmap到内存中
		BitmapFactory.decodeFile(pathName, options);
		int outWidth = options.outWidth;
		int outHeight = options.outHeight;
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		options.inSampleSize = 1;
		if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
			int sampleSize = (outWidth / width + outHeight / height) / 2;
			options.inSampleSize = sampleSize;
		}
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
	
	/**
	 * 从文件 加载图片
	 * @param file 图片对应的文件
	 * @return bitmap
	 */
	public static Bitmap loadBitmap(File file){		
		BitmapFactory.Options options = new BitmapFactory.Options();
		return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
	}

	/**
	 * 从缓存加载图片 。如缓存不存在，从url 加载并缓存
	 * @param cacheFile 缓存文件
	 * @param url  图片 资源对应的缓存
	 * @return bitmap
	 */
	public static Bitmap loadBitmap(File cacheFile,String url){
		if (!cacheFile.exists()){
			if (!DownloadUtil.downloadFile(url, cacheFile, 6000)){
				return null;
			}
		}		
		return loadBitmap(cacheFile);
	}
	/**
	 * 对图片大小进行精确调整，使其宽度为 w, 高度为 h。 不会对图片进行回收
	 * 
	 * @param bitmap
	 *            调整前图片
	 * @param w
	 *            调整后宽度
	 * @param h
	 *            调整后高度
	 * @return 调整后图片
	 */
	public static Bitmap resizeBitmap(Bitmap bitmap, int w, int h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int newWidth = w;
		int newHeight = h;
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		return resizedBitmap;
	}
	
	/**
	 * 保存bitmap 到指定文件
	 * @param bitmap 
	 * @param file 保存的图片文件
	 * @param format 编码格式 
	 * @return true 保存成功， false 保存不成功
	 */
	public static boolean saveBitmap(Bitmap bitmap, String file,
			CompressFormat format) {
		OutputStream os = null;
		try {
			File f = new File(file);
			if (!f.getParentFile().exists()){
				f.getParentFile().mkdirs();
			}
			os = new BufferedOutputStream(new FileOutputStream(file));
			if (bitmap.compress(format, 100, os)) {
				os.flush();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
		return false;
	}
}
