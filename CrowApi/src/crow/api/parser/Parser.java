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
package crow.api.parser;

import java.io.File;
import java.io.InputStream;

/**
 * 对返回数据进行解析。
 * 如：
 *    xml 格式数据解析，json 格式数据解析
 * @author crow
 *
 */
public interface Parser {
	/**
	 * 把字符串转换为响应对象。
	 * 
	 * @param <T>
	 *            领域泛型
	 * @param str
	 *            要解析的字符串
	 * @param clazz
	 *            领域类型
	 * @return 响应对象
	 * @throws ParseException
	 */
	public <T> T parse(String str, Class<T> clazz) throws ParseException;
	
	/**
	 * 把字符串转换为响应对象。
	 * 
	 * @param <T>
	 *            领域泛型
	 * @param file
	 *            要解析的文件
	 * @param clazz
	 *            领域类型
	 * @return 响应对象
	 * @throws ParseException
	 */
	public <T> T parse(File file, Class<T> clazz) throws ParseException;
	
	/**
	 * 把字符串转换为响应对象。
	 * 
	 * @param <T>
	 *            领域泛型
	 * @param input
	 *            要解析的数据流
	 * @param clazz
	 *            领域类型
	 * @return 响应对象
	 * @throws ParseException
	 */
	public <T> T parse(InputStream input, Class<T> clazz) throws ParseException;
}
