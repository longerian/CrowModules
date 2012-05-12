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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	public static void zip(String sourceDir, String zipFile) throws Exception {
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new BufferedOutputStream(
					new FileOutputStream(zipFile)));
			File file = new File(sourceDir);
			String basePath = null;
			if (file.isDirectory()) {
				basePath = file.getPath();
			} else {
				basePath = file.getParent();
			}
			zipFile(file, basePath, zos);
		} finally {
			if (zos != null) {
				try {
					zos.closeEntry();
				} catch (IOException e) {
					// donothing
				}
				try {
					zos.flush();
					zos.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}
	}

	/**
	 * 
	 * create date:2009- 6- 9 author:Administrator
	 * 
	 * @param source
	 * @param basePath
	 * @param zos
	 * @throws IOException
	 */
	private static void zipFile(File source, String basePath,
			ZipOutputStream zos) throws Exception {
		File[] files = null;

		if (source.isDirectory()) {
			files = source.listFiles();
		} else {
			files = new File[1];
			files[0] = source;
		}

		String pathName;
		byte[] buf = new byte[1024];
		int length = 0;

		for (File file : files) {
			if (file.isDirectory()) {
				pathName = file.getPath().substring(basePath.length() + 1)
						+ "/";
				zos.putNextEntry(new ZipEntry(pathName));
				zipFile(file, basePath, zos);
			} else {
				pathName = file.getPath().substring(basePath.length() + 1);
				InputStream is = null;
				try {
					is = new BufferedInputStream(new FileInputStream(file));
					zos.putNextEntry(new ZipEntry(pathName));
					while ((length = is.read(buf)) != -1) {
						zos.write(buf, 0, length);
					}
				} finally {
					is.close();
				}
			}
		}

	}

	/**
	 * @param zipfile
	 *            zip 文件
	 * @param destDir
	 * @throws ZipException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void unZip(String zipfile, String destDir)
			throws ZipException, IOException {
		byte b[] = new byte[1024];
		int length;

		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(new File(zipfile));
			Enumeration<ZipEntry> enumeration = (Enumeration<ZipEntry>) zipFile
					.entries();
			ZipEntry zipEntry = null;
			while (enumeration.hasMoreElements()) {
				zipEntry = enumeration.nextElement();
				File loadFile = new File(destDir, zipEntry.getName());
				if (zipEntry.isDirectory()) {
					if (!loadFile.exists())
						loadFile.mkdirs();
				} else {
					if (!loadFile.getParentFile().exists()) {
						loadFile.getParentFile().mkdirs();
					}
					OutputStream os = null;
					InputStream is = null;
					try {
						os = new BufferedOutputStream(new FileOutputStream(
								loadFile));
						is = new BufferedInputStream(zipFile
								.getInputStream(zipEntry));
						while ((length = is.read(b)) != -1) {
							os.write(b, 0, length);
						}
						os.flush();
					} finally {
						if (os != null)
							os.close();
						if (is != null)
							is.close();
					}
				}
			}
		} finally {
			if (zipFile != null)
				zipFile.close();
		}
	}

	/**
	 * 
	 * @param in
	 *            解压完毕后关闭相应数据流
	 * @param destDir
	 *            目的文件夹
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void unZip(InputStream in, String destDir)
			throws FileNotFoundException, IOException {
		final int BUFFER = 1024;
		byte data[] = new byte[BUFFER];
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(in);
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					File childDir = new File(destDir, entry.getName());
					if (!childDir.exists()) {
						childDir.mkdirs();
					}
				} else {
					File destFile = new File(destDir, entry.getName());
					if (!destFile.getParentFile().exists()) {
						destFile.getParentFile().mkdirs();
					}
					OutputStream dest = null;
					try {
						dest = new BufferedOutputStream(new FileOutputStream(
								destFile));
						int count;
						while ((count = zis.read(data, 0, BUFFER)) != -1) {
							dest.write(data, 0, count);
						}
						dest.flush();
					} finally {
						if (dest != null)
							dest.close();
					}
				}
			}
		} finally {
			if (zis != null)
				zis.close();
		}
	}
}
