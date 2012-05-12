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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Json 相关工具集
 * @author crow
 *
 */
public class JsonUtil {
	private static String TAG = "JsonUtil";

	/**
	 * map 对象转化为 json 字符串
	 * map 中可包含 Set
	 * @param map map 数据
	 * @return json 字符串 ， null 转换失败
	 */
	public static String toJsonString(Map<String, Object> map) {
		String jsonString = null;
		try {
			jsonString = toJSONObject(map).toString();
		} catch (JSONException e) {
			Log.e(TAG, "JsonException in serialization: ", e);
		}
		return jsonString;
	}

	private static JSONObject toJSONObject(Map<String, Object> map)
			throws JSONException {
		JSONObject jsonOb = new JSONObject();
		if ((map == null) || (map.isEmpty()))
			return jsonOb;
		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Object value = map.get(key);
			if ((value instanceof String) || (value instanceof Integer)
					|| (value instanceof Double) || (value instanceof Long)
					|| (value instanceof Float)) {
				jsonOb.put(key, value);
				continue;
			}
			if ((value instanceof Map<?, ?>)) {
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> valueMap = (Map<String, Object>) value;
					jsonOb.put(key, toJSONObject(valueMap));
				} catch (ClassCastException e) {
					Log.w(TAG, "Unknown map type in json serialization: ", e);
				}
				continue;
			}
			if ((value instanceof Set<?>)) {
				try {
					@SuppressWarnings("unchecked")
					Set<Object> valueSet = (Set<Object>) value;
					jsonOb.put(key, toJSONObject(valueSet));
				} catch (ClassCastException e) {
					Log.w(TAG, "Unknown map type in json serialization: ", e);
				}
				continue;
			}
			Log.e(TAG, "Unknown value in json serialization: "
					+ value.toString() + " : "
					+ value.getClass().getCanonicalName().toString());
		}
		return jsonOb;
	}

	private static JSONArray toJSONObject(Set<Object> set) throws JSONException {
		JSONArray jsonOb = new JSONArray();
		if ((set == null) || (set.isEmpty()))
			return jsonOb;
		Iterator<Object> iter = set.iterator();
		while (iter.hasNext()) {
			Object value = iter.next();
			if ((value instanceof String) || (value instanceof Integer)
					|| (value instanceof Double) || (value instanceof Long)
					|| (value instanceof Float)) {
				jsonOb.put(value);
				continue;
			}
			if ((value instanceof Map<?, ?>)) {
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> valueMap = (Map<String, Object>) value;
					jsonOb.put(toJSONObject(valueMap));
				} catch (ClassCastException e) {
					Log.w(TAG, "Unknown map type in json serialization: ", e);
				}
				continue;
			}
			if ((value instanceof Set<?>)) {
				try {
					@SuppressWarnings("unchecked")
					Set<Object> valueSet = (Set<Object>) value;
					jsonOb.put(toJSONObject(valueSet));
				} catch (ClassCastException e) {
					Log.w(TAG, "Unknown map type in json serialization: ", e);
				}
				continue;
			}
			Log.e(TAG, "Unknown value in json serialization: "
					+ value.toString() + " : "
					+ value.getClass().getCanonicalName().toString());
		}
		return jsonOb;
	}
}
