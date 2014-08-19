package com.yerdy.services.core;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.yerdy.services.core.YRDPersistence.AnalyticKey;
import com.yerdy.services.logging.YRDLog;

import android.content.Context;

public class YRDEventTracker {

	private JSONObject _tracked = new JSONObject();
	
	private YRDPersistence _persistance;
	
	public YRDEventTracker(Context cxt) {
		_persistance = new YRDPersistence(cxt, cxt.getApplicationInfo().packageName, false);
		_tracked = _persistance.getJSON(AnalyticKey.CUSTOM_EVENTS);
	}
	
	public void trackEvent(String name, Map<String, String> map, int count) throws JSONException {
		//map == String parameterName, String bucketName
		
		// TODO Auto-generated method stub
		JSONObject named = _tracked.optJSONObject(name);
		if(named == null) {
			named = new JSONObject();
			named.put("params", new JSONObject());
		}
		JSONObject params = named.getJSONObject("params");
		int modCount = named.optInt("mod", 0);
		named.put("mod", modCount + 1);
		
		if(map != null && map.size() > 0) {
			for(String parameterName : map.keySet()) {
				String bucketName = "_"+map.get(parameterName);
				
				if(params.has(parameterName)) {
					JSONObject paramJson = params.optJSONObject(parameterName);
					count += paramJson.optInt(bucketName, 0);
					paramJson.put(bucketName, count);
				} else {
					JSONObject bucketCount = new JSONObject();
					bucketCount.put(bucketName, count);
					params.put(parameterName, bucketCount);
				}
	
			}
			named.put("params", params);
		}
		_tracked.put(name, named);
		YRDLog.i("test", _tracked.toString(1));
			
		_persistance.setJSON(AnalyticKey.CUSTOM_EVENTS, _tracked);
		_persistance.save();
	}

	public boolean isReadyToReport() {
		return (_tracked.length() > 0);
	}
	
	public JSONObject getAndResetCustomEvents() {
		JSONObject response = _tracked;
		_tracked = new JSONObject();
		_persistance.setJSON(AnalyticKey.CUSTOM_EVENTS, _tracked);
		_persistance.save();
		return response;
	}
}
