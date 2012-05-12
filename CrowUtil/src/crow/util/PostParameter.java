/*
Copyright (c) 2007-2009, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package crow.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.util.ByteArrayBuffer;

/**
 * GET 或 POST 的参数 A data class representing HTTP Post parameter
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class PostParameter implements java.io.Serializable,
		Comparable<PostParameter> {
	String name;
	String value;
	private File file = null;

	private static final long serialVersionUID = -8708108746980739212L;

	public PostParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public PostParameter(String name, double value) {
		this.name = name;
		this.value = String.valueOf(value);
	}

	public PostParameter(String name, int value) {
		this.name = name;
		this.value = String.valueOf(value);
	}

	public PostParameter(String name, File file) {
		this.name = name;
		this.file = file;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public File getFile() {
		return file;
	}

	public boolean isFile() {
		return null != file;
	}

	private static final String JPEG = "image/jpeg";
	private static final String GIF = "image/gif";
	private static final String PNG = "image/png";
	private static final String OCTET = "application/octet-stream";

	/**
	 * 
	 * @return content-type
	 */
	public String getContentType() {
		if (!isFile()) {
			throw new IllegalStateException("not a file");
		}
		String contentType;
		String extensions = file.getName();
		int index = extensions.lastIndexOf(".");
		if (-1 == index) {
			// no extension
			contentType = OCTET;
		} else {
			extensions = extensions.substring(extensions.lastIndexOf(".") + 1)
					.toLowerCase();
			if (extensions.length() == 3) {
				if ("gif".equals(extensions)) {
					contentType = GIF;
				} else if ("png".equals(extensions)) {
					contentType = PNG;
				} else if ("jpg".equals(extensions)) {
					contentType = JPEG;
				} else {
					contentType = OCTET;
				}
			} else if (extensions.length() == 4) {
				if ("jpeg".equals(extensions)) {
					contentType = JPEG;
				} else {
					contentType = OCTET;
				}
			} else {
				contentType = OCTET;
			}
		}
		return contentType;
	}

	public byte[] toMultipartByte(String boundary, String encoding) throws UnsupportedEncodingException {
		String seprator = "\r\n";
		StringBuilder buffer = new StringBuilder();
		if (isFile()) {
			buffer.append("--").append(boundary);
			buffer.append(seprator);
			
			buffer.append("Content-Disposition: form-data; name=\"").append(
					name).append("\"; filename=\"").append(file.getName())
					.append("\"");
			buffer.append(seprator);
			
			
			buffer.append("Content-Type: ").append(getContentType()).append(
					"; charset=").append(encoding);
			buffer.append(seprator);
			
			buffer.append("Content-Transfer-Encoding: binary");
			buffer.append(seprator);
			buffer.append(seprator);
			
			ByteArrayBuffer bb = new ByteArrayBuffer(300);
			byte [] temp = buffer.toString().getBytes(encoding);
			bb.append(temp,0,temp.length);
			
			InputStream is = null;
			try {				
				is = new FileInputStream(file);
				int len = 0;
				byte arr [] = new byte[1024];
				while((len = is.read(arr))!= -1){
					bb.append(arr,0,len);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				if (is!=null){
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}	
			
			bb.append(seprator.getBytes(),0,seprator.getBytes().length);
			return bb.toByteArray();
		} else {
			buffer.append("--").append(boundary);
			buffer.append(seprator);
			
			buffer.append("Content-Disposition: form-data; name=\"").append(
					name).append("\"");
			buffer.append(seprator);
			
			buffer.append("Content-Type: text/plain; charset=").append(encoding);
			buffer.append(seprator);
			
			buffer.append("Content-Transfer-Encoding: 8bit");
			buffer.append(seprator);
			buffer.append(seprator);
			buffer.append(value);
			buffer.append(seprator);
		}
		return buffer.toString().getBytes(encoding);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + value.hashCode();
		result = 31 * result + (file != null ? file.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof PostParameter) {
			PostParameter that = (PostParameter) obj;

			if (file != null ? !file.equals(that.file) : that.file != null)
				return false;

			return this.name.equals(that.name) && this.value.equals(that.value);
		}
		return false;
	}

	@Override
	public String toString() {
		return "PostParameter{" + "name='" + name + '\'' + ", value='" + value
				+ '\'' + ", file=" + file + '}';
	}

	public int compareTo(PostParameter that) {
		int compared;
		compared = name.compareTo(that.name);
		if (0 == compared) {
			compared = value.compareTo(that.value);
		}
		return compared;
	}
}
