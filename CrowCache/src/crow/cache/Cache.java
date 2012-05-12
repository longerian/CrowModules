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
package crow.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.os.Environment;
import crow.util.FileUtil;
import crow.util.Util;

/**
 * 资源的存储路径为 : 缓存根目录 + 相对路径 <br/>
 * SD 卡存在会优先选则 SD 卡作为缓存地址。其次选则Ram.<br/>
 * 需要加上SD卡权限：<br/>
 * <code><uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/></code><br/>
 * 示例：
 *    
 * 
 * @author crow
 * 
 */
public final class Cache {
	/**
	 * 表示对应的数据永不过期
	 * 
	 * @see #isExpired(CacheAble)
	 * @see #isExpired(File, long)
	 * @see #isExpired(String, long)
	 */
	public static long NEVER_EXPIRES = 0;
	/**
	 * 表示对应的数据过期
	 * 
	 * @see #isExpired(CacheAble)
	 * @see #isExpired(File, long)
	 * @see #isExpired(String, long)
	 */
	public static long EXPIRED = -1;
	private URLConvertor urlConvertor;
	private File cacheDir;
	
	/**
	 * 使用缓存根目录与包名组合生成缓存
	 * @param context
	 */
	public Cache(Context context) {
		this(context, (String) null);
	}
    
	/**
	 * 使用默认缓存根目录。url 类型的资源会使用 URLConvertor 生成缓存相对路径名。
	 * @param context
	 * @param convertor  url 转换 缓存相对路径相关类。
	 */
	public Cache(Context context, URLConvertor convertor) {
		this(context, null, convertor);
	}
	
	/**
	 * 指定缓存根目录的部分路径。资源相对路径使用默认规则
	 * @param context
	 * @param cacheRelativePath  缓存根目录的部分路径
	 */
	public Cache(Context context, String cacheRelativePath) {
		this(context, cacheRelativePath, null);
	}
    
	/**
	 * 指定缓存根目录的部分路径。url 类型的资源会使用 URLConvertor 生成缓存相对路径名
	 * @param context
	 * @param cacheRelativePath  缓存根目录的部分路径
	 * @param convertor url 转换 缓存相对路径相关类。
	 */
	public Cache(Context context, String cacheRelativePath,
			URLConvertor convertor) {
		this.urlConvertor = convertor;
		if (cacheRelativePath == null) {
			cacheRelativePath = "cache_"
					+ context.getPackageName().replace(".", "_");
		}
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			cacheDir = new File(Environment.getExternalStorageDirectory(),
					cacheRelativePath);
		} else {
			cacheDir = context.getDir(cacheRelativePath, Context.MODE_PRIVATE);
		}
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}

	// /**
	// * 设置URL到相对路径的转换类。如不设置会将url 的md5值作为相对路径
	// *
	// * @param convertor
	// */
	// public void setURLConvertor(URLConvertor convertor) {
	// this.urlConvertor = convertor;
	// }

	/**
	 * 获取缓存在磁盘的绝对路径
	 * 
	 */
	public File getCacheDir() {
		return cacheDir;
	}

	/**
	 * 依据缓存文件的相对路径获取绝对路径。 如果 relativePath 为URL 地址,
	 * 
	 * @param relativePath
	 *            缓存文件相对于缓存根目录的路径 缓存文件的URL地址
	 * @return 在文件系统中的绝对地址
	 */
	public File getCacheFile(String relativePathOrURL) {
		String relativepath;
		if (relativePathOrURL.startsWith("http")) {
			relativepath = urlConvertor == null ? Util.md5(relativePathOrURL)
					: urlConvertor.getCacheRelativePath(relativePathOrURL);
			relativepath = relativepath == null ? Util.md5(relativePathOrURL): relativepath;
		} else {
			relativepath = relativePathOrURL;
		}
		File file = new File(cacheDir, relativepath);
		return file;
	}

	/**
	 * 获取可缓存对象在缓存中的绝对路径
	 * 
	 * @param cacheAble
	 * @return 在文件系统中的绝对地址
	 * @see CacheAble
	 */
	public File getCacheFile(CacheAble cacheAble) {
		return getCacheFile(cacheAble.getCacheRelativePathOrURL());
	}

	/**
	 * 依据相对路径或URL判断 对应缓存是否过期
	 * 
	 * @param relativePathOrURL
	 *            URL 或 缓存文件相对路径
	 * @param cacheTime
	 *            文件的缓存时长，单位秒
	 * @return false 文件还未过期 true 文件 过期或不存在
	 * @see  #NEVER_EXPIRES
	 * 
	 */
	public boolean isExpired(String relativePathOrURL, long cacheTime) {
		return isExpired(getCacheFile(relativePathOrURL), cacheTime);
	}

	/**
	 * 依据缓存文件绝对路径判断文件是否过期
	 * 
	 * @param file
	 *            缓存文件的绝对路径
	 * @param cacheTime
	 *            文件的缓存时长，单位秒
	 * @see #NEVER_EXPIRES	
	 * @see #EXPIRED
	 * @return false 文件还未过期 true 文件过期或不存在
	 */
	public boolean isExpired(File file, long cacheTime) {
		if (file.exists()) {
			if (cacheTime == NEVER_EXPIRES) {
				return false;
			} else if (cacheTime == EXPIRED) {
				return true;
			}
			return (file.lastModified() + cacheTime * 1000) < System
					.currentTimeMillis();
		}
		return true;
	}

	/**
	 * 判断缓存中的可缓存对象是否过期
	 * 
	 * @param cacheAble
	 *            可缓存对象
	 * @return false 缓存对象还未过期 true 缓存过期或不存在
	 */
	public boolean isExpired(CacheAble cacheAble) {
		return isExpired(getCacheFile(cacheAble), cacheAble.getCacheTime());
	}

	/**
	 * 依据相对路径或URL 清空缓存及对应信息
	 * 
	 * @param relativePathOrURL
	 *            URL 或 缓存文件相对路径
	 * 
	 */
	public void clearCache(String relativePathOrURL) {
		clearCache(getCacheFile(relativePathOrURL));
	}

	/**
	 * 依据缓存文件绝对路 清空缓存及对应信息
	 * 
	 * @param file
	 */
	public void clearCache(File file) {
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * 清理缓存中的可缓存对象
	 * 
	 * @param cacheAble
	 */
	public void clearCache(CacheAble cacheAble) {
		clearCache(getCacheFile(cacheAble));
	}

	/**
	 * 依据相对路径或URL 判断文件是否存在
	 * 
	 * @param relativePathOrURL
	 *            URL 或 缓存文件相对路径
	 * @return true 缓存存在 false 缓存不存在
	 */
	public boolean exists(String relativePathOrURL) {
		return getCacheFile(relativePathOrURL).exists();
	}

	/**
	 * 判断可缓存对象是否存在于缓存中
	 * 
	 * @param cacheAble
	 *            可缓存对象
	 * @return true 缓存对象存在于缓存中 false 缓存对象在缓存中不存在
	 */
	public boolean exists(CacheAble cacheAble) {
		return exists(cacheAble.getCacheRelativePathOrURL());
	}

	/**
	 * 保存可缓存对象到缓存中。<br/> 缓存成功才会覆盖旧缓存
	 * 
	 * @param input
	 *            缓存的数据流 可缓存对象对应的数据流
	 * @param true 缓存成功      false 缓存失败
	 * @return 资源对应的本地缓存文件
	 */
	public boolean cache(InputStream input, CacheAble cacheAble) {
		return cache(input, cacheAble.getCacheRelativePathOrURL());
	}

	/**
	 * 将URL 对应的资源做缓存。<br/> 缓存成功才会覆盖旧缓存
	 * 
	 * @param url
	 *            资源对应的URL 地址
	 * @return true 缓存成功      false 缓存失败
	 * @throws IOException
	 */
	public boolean cacheURL(String url) throws IOException {
		URL ul = new URL(url);
		URLConnection conn = ul.openConnection();
		conn.setConnectTimeout(30 * 1000);
		conn.setReadTimeout(2 * 60 * 1000);
		conn.connect();
		return cache(conn.getInputStream(), url);
	}

	/**
	 * 将InputStream 进行缓存，并关闭相应的流。<br/> 缓存成功才会覆盖旧缓存
	 * 
	 * @param input
	 *            缓存的数据流
	 * @param relativePathOrURL
	 *            缓存的相对目录或资源的URL
	 * @return true 缓存成功      false 缓存失败
	 */
	public boolean cache(InputStream input, String relativePathOrURL) {
		File file = getCacheFile(relativePathOrURL); // 缓存文件
		File temp = new File(file.getAbsoluteFile() + ".cacheTemp");  // 缓存临时文件 		
		if (FileUtil.saveStreamAsFile(input, temp)) { // 保存到临时文件
			// 临时文件变为缓存文件
			if (file.exists()) { // 删除原缓存文件
				file.delete();
			}
			temp.renameTo(file);
			return true;
		} else {
			if (temp.exists()) {
				temp.delete();
			}
			return false;
		}
	}
}
