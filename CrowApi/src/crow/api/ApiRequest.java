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
package crow.api;

import java.util.Map;

import crow.cache.CacheAble;

/**
 * Api 请求对象， 通过它对请求的选定数据进行设定。
 * @author crow
 *
 * @param <T>  数据请求的返回对象的具体类型
 * @param <C>  Api 上下文 环境的具体类型
 */
public interface ApiRequest<T extends ApiResponse,C extends ApiContext> extends CacheAble{
	
	/**
	 * 由 ApiClient 调用，获取Api 请求相关参数。
	 * 注： 
	 *    Api 请求的公共或相对固定参数由ApiClient 提供，若此处 提供会进行覆盖
	 * @param context  
	 * @return  
	 */
	public Map<String, String> getTextParams(C context);
	
	/**
	 * Api 请求的服务器地址
	 * @param context
	 * @return
	 */
	public String getRequestURL(C context);
	
	/**
	 * ApiRequest 对应 ApiResponse 对象的class 对象
	 * 可利用它进行 Api解析handler 的查找 或 利用反射进行解析（当Api 返回数据与 ApiResponse 对象有简单对应规则时）
	 * @return
	 */
	public Class<T> getResponseClass();
	
	/**
	 * ApiRequest对应的Http请求对象的class对象
	 * 可利用它生成HttpPost对象或者HttpGet对象
	 * @return
	 */
	public Class<?> getHttpRequestClass();
	
}
