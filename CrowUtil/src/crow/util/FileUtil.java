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
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;
import android.os.StatFs;

public class FileUtil {
	/**
	 * 获取 SD 卡剩余空间
	 * 
	 * @return SD 卡剩余空间 byte 数
	 */
	public static long getSdCardAvailableSize() {
		String sdpath = Environment.getExternalStorageDirectory().getPath();
		StatFs sdStatFs = new StatFs(sdpath);
		return (long) sdStatFs.getAvailableBlocks() * sdStatFs.getBlockSize();
	}

	/**
	 * 连接 path 片断为完整的Path 路径 例: <br/>
	 * joinPath("file:///","/sdcard","sample.txt") file:///sdcard/sample.txt
	 * joinPath("sdcard","yek") sdcard/yek
	 * 
	 * @param paths path 片断
	 * @return 组合后的片断
	 */
	public static String joinPath(String... paths) {
		StringBuffer pathBuffer = new StringBuffer();
		boolean endWithSlash = false;
		for (String p : paths) {
			if ("".equals(p))
				continue;
			boolean startWithSlash = p.startsWith("/");
			if (endWithSlash && startWithSlash) {
				p = p.substring(1);
			} else if (!endWithSlash && !startWithSlash
					&& pathBuffer.length() > 0) {
				pathBuffer.append("/");
			}
			pathBuffer.append(p);
			endWithSlash = p.endsWith("/");
		}
		return pathBuffer.toString();
	}

	/**
	 * 删除文件或整个目录
	 * 
	 * @param file
	 */
	public static void deleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (File f : files) {
					deleteFile(f);
				}
				file.delete();
			}
		}
	}

	/**
	 * 清除文件目录
	 * 
	 * @param file
	 */
	public static void clearDir(File file) {
		if (file.exists() && file.isDirectory()) {
			File files[] = file.listFiles();
			for (File f : files) {
				deleteFile(f);
			}
		}
	}
    
	/**
	 * 将数据流保存为文件
	 * @param input  要保存的数据流
	 * @param file 保存后的文件
	 * @return true 保存成功，false 保存失败
	 */
	public static boolean saveStreamAsFile(InputStream input, File file) {
		OutputStream os = null;
		byte buffer[] = new byte[1024];
		try {
			File work = file.getParentFile();
			if (!work.exists()) {
				work.mkdirs();
			}
			os = new BufferedOutputStream(new FileOutputStream(file));
			int length = 0;
			while ((length = input.read(buffer)) != -1) {
				os.write(buffer, 0, length);
			}
			os.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取文件名称
	 * @param urlOrPath  url 或  文件路径
	 * @return 文件的名称
	 */
	public static String getFileName(String urlOrPath){
		return urlOrPath.substring(urlOrPath.lastIndexOf("/")+1);
	}
}
