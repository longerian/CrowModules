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
package yek.weibo.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.util.ByteArrayBuffer;

import yek.util.BASE64Encoder;
import yek.util.Util;
import yek.weibo.PostParameter;
import android.os.Bundle;

public class WeiboUtil {

	/**
	 * 参数编码
	 * 
	 * @param value
	 * @return
	 */
	public static String encode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return URLEncoder.encode(s, "UTF-8").replace("+", "%20").replace(
					"*", "%2A").replace("%7E", "~").replace("#", "%23");
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
	 * 处理请求URL,返回URL 的Path 路径。去掉URL 后面的参数
	 * 
	 * @param url
	 * @return
	 */
	public static String getNormalizedUrl(String url) {
		try {
			URL ul = new URL(url);
			StringBuilder buf = new StringBuilder();
			buf.append(ul.getProtocol());
			buf.append("://");
			buf.append(ul.getHost());
			if ((ul.getProtocol().equals("http") || ul.getProtocol().equals(
					"https"))
					&& ul.getPort() != -1) {
				buf.append(":");
				buf.append(ul.getPort());
			}
			buf.append(ul.getPath());
			return buf.toString();
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 处理签名请求和参数,生成 OAUTH 签名的Base String
	 * 
	 * @param url
	 * @param httpMethod
	 * @param parameters
	 * @return
	 */
	public static String generateSignatureBase(String url, String httpMethod,
			List<PostParameter> parameters) {

		StringBuilder base = new StringBuilder();
		base.append(httpMethod.toUpperCase());
		base.append("&");
		base.append(WeiboUtil.encode(WeiboUtil.getNormalizedUrl(url)));
		base.append("&");
		base.append(WeiboUtil.encode(WeiboUtil.encodeParameters(parameters,
				"&", false)));

		return base.toString();
	}

	/**
	 * Sina tencent 通用 用HmacSHA1 生成签名值
	 * 
	 * @param base
	 *            要签名的字符串
	 * @param consumerSecret
	 *            访问微博平台的API Secret
	 * @param accessTokenSecret
	 *            访问微博平台的 Access Secret
	 * @return
	 */
	public static String hmacSHA1Signature(String base, String consumerSecret,
			String accessTokenSecret) {
		String HMAC_SHA1 = "HmacSHA1";
		try {
			Mac mac = Mac.getInstance(HMAC_SHA1);
			String oauthSignature = encode(consumerSecret)
					+ "&"
					+ ((accessTokenSecret == null) ? ""
							: encode(accessTokenSecret));
			SecretKeySpec spec = new SecretKeySpec(oauthSignature.getBytes(),
					HMAC_SHA1);
			mac.init(spec);
			byte[] bytes = mac.doFinal(base.getBytes());
			return new String(BASE64Encoder.encode(bytes));
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Generate the timestamp for the signature.
	 * 
	 * @return
	 */
	public static String generateTimeStamp() {
		return String.valueOf(System.currentTimeMillis() / 1000);
	}

	/**
	 * Just a simple implementation of a random number between 123400 and
	 * 9999999
	 * 
	 * @return
	 */
	public static String generateNonce() {
		Random random = new Random();
		return String.valueOf(random.nextInt(9876599) + 123400);
	}

	/**
	 * 分解如下类型的返回字符串，a=key&b=secret<br/>
	 * 返回如下结果：<br/>
	 * String [] {key,secret}
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public static Bundle parseToken(String response) {
		Bundle bundle = new Bundle();
		if (response == null || response.equals("")) {
			return bundle;
		}
		response = response.replace("#", "?");
		if (response.contains("?")) {
			response = response.substring(response.indexOf("?") + 1);
		}
		String[] tokenArray = response.split("&");

		for (String tokenpair : tokenArray) {
			String[] token = tokenpair.split("=");
			if (token.length != 0) {
				bundle.putString(token[0], URLDecoder.decode(token[1]));
			}
		}
		return bundle;
	}

	public static String multipartPost(HttpURLConnection conn,
			List<PostParameter> params) throws IOException {
		OutputStream os;
		List<PostParameter> dataparams = new ArrayList<PostParameter>();
		for (PostParameter key : params) {
			if (key.isFile()) {
				dataparams.add(key);
			}
		}

		String BOUNDARY = Util.md5(String.valueOf(System.currentTimeMillis()));
		
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Charsert", "UTF-8");

		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="
				+ BOUNDARY);

		ByteArrayBuffer buff = new ByteArrayBuffer(1000);

		for (PostParameter p : params) {			
			byte[] arr = p.toMultipartByte(BOUNDARY, "UTF-8");
			buff.append(arr, 0, arr.length);
		}
		String end = "--" + BOUNDARY + "--" + "\r\n";
		byte[] endArr = end.getBytes();
		buff.append(endArr, 0, endArr.length);

		conn.setRequestProperty("Content-Length", buff.length() + "");
		conn.connect();
		os = new BufferedOutputStream(conn.getOutputStream());
		os.write(buff.toByteArray());
		buff.clear();
		os.flush();
		String response = "";
		response = Util.inputStreamToString(conn.getInputStream());
		return response;
	}

	public static String multipartPost(String requestURL,
			List<PostParameter> params) throws IOException {
		URL url = new URL(requestURL);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		return multipartPost(conn, params);
	}

	public static String encodePostBody(List<PostParameter> parameters,
			String boundary) {
		if (parameters == null)
			return "";
		StringBuilder sb = new StringBuilder();

		for (PostParameter key : parameters) {
			if (key.isFile()) {
				continue;
			}

			sb
					.append("Content-Disposition: form-data; name=\""
							+ key.getName()
							+ "\"\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: 8bit\r\n\r\n"
							+ key.getValue());
			sb.append("\r\n" + "--" + boundary + "\r\n");
		}

		return sb.toString();
	}
}
