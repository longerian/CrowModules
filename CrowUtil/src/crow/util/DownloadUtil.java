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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载相关类。
 * @author crow
 *
 */
public class DownloadUtil {
	/**
	 * 将HttpURLconnection 对应的数据流下载到本地磁盘保存
	 * @param conn
	 *            连接超时可通过 HttpURLConnection 设置
	 * @param store
	 *            本地文件 目录结构不存在时，会进行创建
	 * @return true 下载成功， false 下载失败
	 */
	public static boolean downloadFile(HttpURLConnection conn, File store) {
		return downloadFile(conn, store, null);
	}

	/**
	 * 下载url 指定资源到本地磁盘
	 * @param url  资源对应的url  
	 * @param store 资源保存的本地文件
	 * @param timeout
	 * 			url 连接超时， 0 代表永不超时
	 * @return true 保存成功，false 保存失败
	 */
	public static boolean downloadFile(String url, File store, int timeout) {
		return downloadFile(url, store, timeout, null);
	}
	
	/**
	 * 下载进度回调通知的文件下载<br/>
	 * 
	 * @param url 资源对应的url
	 * @param store 保存的本地文件
	 * @param timeout 下载超时设定， 0 代表 不超时
	 * @param callback  下载进度的回调	 *
	 * @return true 下载成功， false 下载失败
	 * @see #ProgressCallback
	 */
	public static boolean downloadFile(String url, File store, int timeout,
			ProgressCallback callback) {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(timeout);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return downloadFile(conn, store, callback);
	}

	/**
	 * 下载进度回调通知的文件下载
	 * @param conn  http 连接，可在其中设定连接超时的参数
	 * @param store 下载资源保存的本地磁盘文件
	 * @param callback  下载进度的回调
	 * @return true 下载成功，false 下载失败
	 * @see #ProgressCallback
	 */
	public static boolean downloadFile(HttpURLConnection conn, File store,
			ProgressCallback callback) {
		InputStream is = null;
		OutputStream os = null;
		byte buffer[] = new byte[1024];
		long totalSize = 0;
		try {
			int responseCode;
			conn.connect();
			responseCode = conn.getResponseCode();
			totalSize = conn.getContentLength();
			if (responseCode == 200) {
				is = new BufferedInputStream(conn.getInputStream());
				File work = store.getParentFile();
				if (!work.exists()) {
					work.mkdirs();
				}
				os = new BufferedOutputStream(new FileOutputStream(store));
				int length = 0;
				while ((length = is.read(buffer)) != -1) {
					os.write(buffer, 0, length);
					if (callback != null)
						callback.onProgress(totalSize, store.length());
				}
				os.flush();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 下载进度的回调接口
	 * @author crow
	 *
	 */
	public interface ProgressCallback {
		/**
		 * 进度通知
		 * 
		 * @param totalSize
		 *            该文件的总大小 ，单位 byte
		 * @param currentSize
		 *            已经下载的大小 ，单位byte
		 */
		public void onProgress(long totalSize, long currentSize);
	}
}
