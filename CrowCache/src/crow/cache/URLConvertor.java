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

public interface URLConvertor {

	/**
	 * 通过url 获取缓存的相对路径
	 * @param url  以 http 开头的地址
	 * @return  相对 于缓存根目录 的地址<br/>
	 *          null 使用Cache 类的默认转换方式(md5)
	 */
	public String getCacheRelativePath(String url);

}
