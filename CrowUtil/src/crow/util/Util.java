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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;

import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

/**
 * Yek 通用工具类
 * 
 * @author crow
 * 
 */
public class Util {

	/**
	 * 对字符串进行 {@code md5}计算
	 * 
	 * @param str
	 *            求 {@code md5}的字符串
	 * @return 字符的md5值(大写) null str 为空，或无法进行md5运算
	 */
	public static String md5(String str) {
		MessageDigest messageDigest = null;
		String digest = null;
		if ((str != null) && (str.length() > 0)) {
			try {
				(messageDigest = MessageDigest.getInstance("MD5")).update(str
						.getBytes());
				digest = String.format("%032X", new Object[] { new BigInteger(
						1, (messageDigest).digest()) });
			} catch (Exception e) {
				digest = str.substring(0, 32);
			}
		}
		return digest;
	}

	/**
	 * 判断当前运行环境是否为模拟器
	 * 
	 * @return true 当前设备为模拟器 false 当前设备为真机
	 */
	public static boolean isEmulator() {
		return ("unknown".equals(Build.BOARD))
				&& ("generic".equals(Build.DEVICE))
				&& ("generic".equals(Build.BRAND));
	}

	/**
	 * 获取设备编号， 若获取不到，用 android_id 代替。
	 * 
	 * @param context
	 * @return null 若为模拟器或无法获取 模拟器也可能反回全0字符串
	 */
	public static String getDeviceId(Context context) {
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telManager.getDeviceId();
		if (imei != null) {
			return imei;
		}
		String dv = null;
		if ((dv = Settings.Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID)) != null) {
			dv = md5(dv).toUpperCase();
		}
		return dv;
	}

	// public static String getUUID(Context context) {
	// final TelephonyManager tm = (TelephonyManager) context
	// .getSystemService(Context.TELEPHONY_SERVICE);
	// final String tmDevice, tmSerial, androidId;
	// tmDevice = "" + tm.getDeviceId();
	// tmSerial = "" + tm.getSimSerialNumber();
	// androidId = ""
	// + android.provider.Settings.Secure.getString(context
	// .getContentResolver(),
	//
	// android.provider.Settings.Secure.ANDROID_ID);
	//
	// UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice
	// .hashCode() << 32)
	// | tmSerial.hashCode());
	// String uniqueId = deviceUuid.toString();
	// return uniqueId;
	// }

	/**
	 * 获取 meta 中设定的值 例： <code>
	 * <meta-data android:name="keyName"
	 * android:value="0123456789abc" />
	 * </code>
	 * 
	 * @param context
	 * @param keyName
	 *            name of meta-data
	 * @return value of meta-data null 没有获取到
	 */
	public static String readMetaByKey(Context context, String keyName) {
		try {
			ApplicationInfo appi = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			Bundle bundle = appi.metaData;
			return (String) bundle.get(keyName);
		} catch (Exception e) {
			return null;
		}
	}

	// private static String userAgent;
	//
	// /**
	// * 获取数据URL连接的 UserAgent
	// */
	// public static String getUserAgent(Context context) {
	// if (userAgent == null) {
	// String ua = new WebView(context).getSettings().getUserAgentString();
	// if ((ua == null) || (ua.length() == 0) || (ua.equals("Java0"))) {
	// String osName = System.getProperty("os.name", "Linux");
	// String osVersion = "Android " + Build.VERSION.RELEASE;
	// Locale locale = Locale.getDefault();
	// String lang = locale.getLanguage().toLowerCase();
	// if (lang.length() == 0)
	// lang = "en";
	// String country = locale.getCountry().toLowerCase();
	// if (country.length() > 0)
	// lang = lang + "-" + country;
	// String build = Build.MODEL + " Build/" + Build.ID;
	// ua = "Mozilla/5.0 (" + osName + "; U; " + osVersion + "; "
	// + lang + "; " + build
	// + ") AppleWebKit/0.0 (KHTML, like "
	// + "Gecko) Version/0.0 Mobile Safari/0.0";
	// }
	// userAgent = ua;
	// }
	// return userAgent;
	// }
	//
	// public static void setUserAgent(WebView webView) {
	// String ua = getUserAgent(webView.getContext().getApplicationContext());
	// webView.getSettings().setUserAgentString(ua);
	// }
	//
	// public static void setUserAgent(HttpURLConnection uRLConnection,
	// Context context) {
	// uRLConnection.setRequestProperty("User-Agent", getUserAgent(context));
	// }

	/**
	 * 检查指定的字符串是否为空。
	 * <ul>
	 * <li>Util.isEmpty(null) = true</li>
	 * <li>Util.isEmpty("") = true</li>
	 * <li>Util.isEmpty("   ") = true</li>
	 * <li>Util.isEmpty("abc") = false</li>
	 * </ul>
	 * 
	 * @param value
	 *            待检查的字符串
	 * @return true/false
	 */
	public static boolean isEmpty(String value) {
		int strLen;
		if (value == null || (strLen = value.length()) == 0) {
			return true;
		}
		for (int i = 0; i < strLen; i++) {
			if ((Character.isWhitespace(value.charAt(i)) == false)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取运营商信息 需要在manifest 中加入如下权限 <uses-permission
	 * android:name="android.permission.READ_PHONE_STATE">
	 * 
	 * @param context
	 * @return "中国移动" / "中国联通" / "中国电信" / ""
	 */
	public static String getCarrier(Context context) {
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = telManager.getSubscriberId();
		if (imsi != null && !"".equals(imsi)) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
				return "中国移动";
			} else if (imsi.startsWith("46001")) {
				return "中国联通";
			} else if (imsi.startsWith("46003")) {
				return "中国电信";
			}
		}
		return "";
	}

	/**
	 * 判断网络是否可访问
	 * 
	 * @param context
	 * @return {@code true} 网络可访问 {@code false} 网络不可访问
	 */
	public static boolean canAccessNetwork(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager.getActiveNetworkInfo() != null
				&& connManager.getActiveNetworkInfo().isAvailable()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断当前网络是否为wifi网络
	 * 
	 * @param context
	 * @return true 当前活动网络为wifi网络 false 其它
	 */
	public static boolean isWifi(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connManager.getActiveNetworkInfo();
		if (netInfo != null) {
			int type = netInfo.getType();
			if (type == ConnectivityManager.TYPE_WIFI) {
				return true;
			}
		}
		return false;
	}

	public static String encode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return URLEncoder.encode(s, "UTF-8").replace("+", "%20")
					.replace("*", "%2A").replace("%7E", "~")
					.replace("#", "%23");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * 对参数编码
	 * 
	 * @param postParams
	 * @param splitter
	 * @param quot
	 * @return
	 */
	public static String encodeParameters(List<PostParameter> postParams,
			String splitter, boolean quot) {
		StringBuffer buf = new StringBuffer();
		for (PostParameter param : postParams) {
			if (buf.length() != 0) {
				if (quot) {
					buf.append("\"");
				}
				buf.append(splitter);
			}
			buf.append(encode(param.getName())).append("=");
			if (quot) {
				buf.append("\"");
			}
			buf.append(encode(param.getValue()));
		}
		if (buf.length() != 0) {
			if (quot) {
				buf.append("\"");
			}
		}
		return buf.toString();
	}

	/**
	 * 使用md5方式对参数进行签名 N1=Value1,N2=Value2 secret splitter <br/>
	 * * sign = md5(N1splitterValue1N2splitterValue2secret)
	 * 
	 * @param params
	 *            要签名的参数
	 * @param secret
	 *            签名的密钥
	 * @param splitter
	 *            参数名与Value的分隔符
	 * @return
	 */
	public static String md5Sign(List<PostParameter> params, String secret,
			String splitter) {
		Collections.sort(params);
		splitter = splitter == null ? "" : splitter;
		StringBuilder buffer = new StringBuilder();
		for (PostParameter param : params) {
			buffer.append(param.getName()).append(splitter)
					.append(param.getValue());
		}
		buffer.append(secret);
		return md5(buffer.toString());
	}

	public static String urlGet(String url) throws IOException {
		URLConnection conn = new URL(url).openConnection();
		conn.setConnectTimeout(20000);
		InputStream input = conn.getInputStream();
		String result = inputStreamToString(input);
		return result;
	}

	public static String urlPost(String url, String encodePostParam)
			throws IOException {
		HttpURLConnection conn = null;
		conn = (HttpURLConnection) new URL(url).openConnection();
		return urlPost(conn, encodePostParam);
	}

	public static String urlPost(HttpURLConnection conn, String encodePostParam)
			throws IOException {
		int responseCode = -1;
		OutputStream osw = null;
		try {
			conn.setConnectTimeout(20000);
			conn.setReadTimeout(12000);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setDoOutput(true);
			byte[] bytes = encodePostParam.getBytes("UTF-8");
			conn.setRequestProperty("Content-Length",
					Integer.toString(bytes.length));
			osw = conn.getOutputStream();
			osw.write(bytes);
			osw.flush();
			responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new IOException("请求错误");
			} else {
				String s = inputStreamToString(conn.getInputStream());
				return s;
			}
		} finally {
			try {
				if (osw != null)
					osw.close();
			} catch (Exception ignore) {
			}
		}
	}

	/**
	 * 将inputStream 以UTF-8编码转换为字符串
	 * 
	 */
	public static String inputStreamToString(InputStream is) throws IOException {				
		ByteArrayOutputStream out = new ByteArrayOutputStream();			
		byte arr[] = new byte[32];
		int len = 0;
		try {
			while ((len = is.read(arr)) != -1) {
				out.write(arr, 0, len);
			}
		} finally {
			try {
				is.close();
				out.close();
			} catch (IOException e) {
				// do nothing
			}
		}		
		return out.toString(HTTP.UTF_8);
	}

	/**
	 * 将Assists 中文件读取为字符串
	 */
	public static String getAssistFileAsString(Context context,
			String assistFile) throws IOException {
		String result = null;
		AssetManager assetManager = context.getAssets();
		result = inputStreamToString(assetManager.open(assistFile));
		return result;
	}
}
