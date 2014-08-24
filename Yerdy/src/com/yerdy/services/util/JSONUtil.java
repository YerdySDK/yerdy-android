package com.yerdy.services.util;

import java.util.*;
import org.json.*;
import com.yerdy.services.logging.YRDLog;


public final class JSONUtil {
	
	public static List<Object> arrayToList(JSONArray array) {
		if (array == null) {
			return new ArrayList<Object>();
		}
		
		List<Object> list = new ArrayList<Object>();
		try
		{
			for (int i = 0; i < array.length(); i++)
				list.add(array.get(i));
		}
		catch (JSONException ex)
		{
			YRDLog.e(JSONUtil.class, "Exception in arrayToList");
			ex.printStackTrace();
		}
		return list;
	}
	
	public static List<String> arrayToStringList(JSONArray array) {
		List<Object> list = arrayToList(array);
		List<String> retVal = new ArrayList<String>();
		for (Object obj : list) {
			if (obj instanceof String)
				retVal.add((String)obj);
		}
		return retVal;
	}
	
	/*
	 * Creates a new JSONArry that is equivalent to inserting 'item' at the front of 'array'
	 */
	public static JSONArray insertFront(Object item, JSONArray array) {
		JSONArray itemAsArray = new JSONArray();
		itemAsArray.put(item);
		return concat(itemAsArray, array);
	}
	
	/*
	 * Creates a new JSONArray with the object at the given index removed
	 */
	public static JSONArray removeAtIndex(JSONArray array, int index) {
		JSONArray retVal = new JSONArray();
		try
		{
			for (int i = 0; i < array.length(); i++) {
				if (i != index)
					retVal.put(array.get(i));
			}
		}
		catch (JSONException ex)
		{
			YRDLog.e(JSONUtil.class, "Exception in removeAtIndex");
		}
		return retVal;
	}
	
	/*
	 * Creates a new JSONArray by concatenating the 2 provided JSONArrays
	 */
	public static JSONArray concat(JSONArray first, JSONArray last) {
		JSONArray retVal = new JSONArray();
		
		try
		{
			for (int i = 0; i < first.length(); i++) {
				retVal.put(first.get(i));
			}
			for (int i = 0; i < last.length(); i++) {
				retVal.put(last.get(i));
			}
		}
		catch (JSONException ex)
		{
			YRDLog.e(JSONUtil.class, "Exception in concat");
			ex.printStackTrace();
		}
		
		return retVal;
	}
	
	/*
	 * Creates a new JSONArray by copying over a max of 'length' objects to the new JSONArray
	 */
	public static JSONArray trimToLength(JSONArray array, int length) {
		JSONArray retVal = new JSONArray();

		try
		{
			int maxItemsToCopy = Math.min(array.length(), length);
			for (int i = 0; i < maxItemsToCopy; i++)
				retVal.put(array.get(i));
		}
		catch (JSONException ex)
		{
			YRDLog.e(JSONUtil.class, "Exception in trimToLength");
			ex.printStackTrace();
		}
		
		return retVal;
	}
	
}
