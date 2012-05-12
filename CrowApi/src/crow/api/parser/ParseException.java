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
 * 数据解析异常
 * 
 * @author crow
 * 
 */
@SuppressWarnings("serial")
public class ParseException extends Exception {
	public ParseException() {
		super();
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(Exception e) {
		super(e);
	}

	public ParseException(String message) {
		super(message);
	}
}
