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


public interface CacheAble {
	/**
	 * 请确保返回的路径 唯一
	 * @return 相对于缓存根目录的路径 或 URL
	 */
	public String getCacheRelativePathOrURL();

	/**
	 * 缓存生命周期。单位 秒
	 * 
	 * @return 缓存的秒数<br/>
	 *       Cache.NEVER_EXPIRES   缓存不过期
	 * 		 Cache.EXPIRED 缓存过期，获取数据时不采用缓存	
	 */
	public long getCacheTime();
}
