package com.yerdy.services.util;

import org.json.JSONObject;

public abstract class YRDJsonProcessor {
	
	protected abstract void parseJSON(JSONObject json);
	
	protected String getString(JSONObject json, String key, String fallback) {
		return (json.isNull(key))?(fallback):(json.optString(key, fallback));
	}
	
	protected int getFloatSecondsAsMilliseconds(JSONObject json, String key, double fallback) {
		double value = json.optDouble(key, fallback);
		return (int) (value * 1000);
	}
	
	protected int getColor(JSONObject json, String key, int fallback) {
		if(json.has(key)) {
			String value = json.optString(key, null);
			try {
				long color = Long.parseLong(value.substring(1), 16);
				if(value.length() == 7)
					return (int) (color | 0xff000000);
				else
					return (int)(color);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return fallback;
	}
}
