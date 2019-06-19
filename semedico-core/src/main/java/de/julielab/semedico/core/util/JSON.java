package de.julielab.semedico.core.util;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

import java.util.*;

public class JSON {
	public static List<String> jsonArray2List(JSONArray jsonArray) {
		if (null == jsonArray)
			return Collections.emptyList();
		List<String> list = new ArrayList<>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			list.add(jsonArray.getString(i));
		}
		return list;
	}

	public static Set<String> jsonArray2Set(JSONArray jsonArray) {
		if (null == jsonArray)
			return Collections.emptySet();
		Set<String> set = new HashSet<>(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			set.add(jsonArray.getString(i));
		}
		return set;
	}

	/**
	 * Returns the JSONArray at <tt>key</tt> if such an array exists. Returns <tt>null</tt>, if <tt>key</tt> is not
	 * present in <tt>jsonObject</tt>.
	 * 
	 * @param jsonObject
	 * @param key
	 * @return
	 */
	public static List<String> jsonArrayProperty2List(JSONObject jsonObject, String key) {
		if (!jsonObject.has(key))
			return Collections.emptyList();
		return jsonArray2List(jsonObject.getJSONArray(key));
	}

	public static List<String> jsonArrayProperty2List(JSONObject jsonObject, String key, List<String> defaultValue) {
		List<String> list = jsonArrayProperty2List(jsonObject, key);
		if (null == list)
			return defaultValue;
		return list;
	}

	public static JSONArray getJSONArray(JSONObject o, String key) {
		if (o.isNull(key))
			return null;
		return o.getJSONArray(key);
	}

	public static JSONArray getJSONArray(JSONArray a, int index) {
		if (a.isNull(index))
			return null;
		return a.getJSONArray(index);
	}

	public static String getString(JSONObject o, String key) {
		if (o.isNull(key))
			return null;
		return o.getString(key);
	}
	
	public static Boolean getBoolean(JSONObject o, String key) {
		if (o.isNull(key))
			return null;
		return o.getBoolean(key);
	}

	public static Integer getInt(JSONObject o, String key) {
		if (o.isNull(key))
			return null;
		return o.getInt(key);
	}

	public static String getString(JSONArray a, int index) {
		if (a.isNull(index))
			return null;
		return a.getString(index);
	}
}
