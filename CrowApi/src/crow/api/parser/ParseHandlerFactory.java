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

/**
 * 
 * @author crow
 *
 * @param <N> newHandler 返回 的 ParseHandler 对象 应为 <N> 类型对象
 */
public interface ParseHandlerFactory{
	/**
	 * 依据 Class 类型返回具体的数据解析类
	 * @param <T> 
	 * @param clazz
	 * @return
	 */
	public <T> ParseHandler<T> newHandler(Class<T> clazz);
	
}
