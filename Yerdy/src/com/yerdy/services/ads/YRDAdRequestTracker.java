package com.yerdy.services.ads;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.yerdy.services.core.YRDPersistence;
import com.yerdy.services.core.YRDPersistence.AnalyticKey;
import com.yerdy.services.logging.YRDLog;

public class YRDAdRequestTracker {
	
	private JSONObject _ads = new JSONObject();
	
	private final String KEY_REQUEST = "request";
	private final String KEY_FILL = "fill";
	
	private YRDPersistence _persistance;

	public YRDAdRequestTracker(Context cxt) {
		_persistance = new YRDPersistence(cxt, cxt.getApplicationInfo().packageName, false);
		_ads = _persistance.getJSON(AnalyticKey.ADS_VERSIONED);
	}
	
	public void reset() {
		_ads = new JSONObject();
		_persistance.deleteKey(AnalyticKey.ADS_VERSIONED);
		_persistance.save();
	}
	
	private JSONObject getJsonKey(String network) {
		JSONObject jsonNetwork = _ads.optJSONObject(network);
		if(jsonNetwork == null) {
			jsonNetwork = new JSONObject();
			try {
				jsonNetwork.putOpt(KEY_FILL, 0);
				jsonNetwork.putOpt(KEY_REQUEST, 0);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jsonNetwork;
	}
	
	public void logAdFill(String network) {
		updateNetwork(network, KEY_FILL);
	}
	
	public void logAdRequest(String network) {
		updateNetwork(network, KEY_REQUEST);
	}
	
	private void updateNetwork(String network, String key) {
		JSONObject jsonNetwork = getJsonKey(network);
		
		int value = jsonNetwork.optInt(key, 0);
		try {
			jsonNetwork.put(key, (value + 1));
			_ads.put(network, jsonNetwork);
		} catch (JSONException e) {
			YRDLog.w(getClass(), "Failed to log ad: " + key);
		}
		
		if(_persistance != null) {
			_persistance.setJSON(AnalyticKey.ADS_VERSIONED, _ads);
			_persistance.save();
		}
		
		try {
			YRDLog.i(YRDAdRequestTracker.class, _ads.toString(1));
		} catch (JSONException e) {
			YRDLog.i(YRDAdRequestTracker.class, "error showing ad: " + key);
		}
	}
	
	public Map<String, String> generateReport() {
		Map<String, String> map = new HashMap<String, String>();
        Iterator<?> keys = _ads.keys();

        while( keys.hasNext() ){
            String key = (String)keys.next();
            JSONObject values = _ads.optJSONObject(key);
            
            if(values != null) {
            	int requests = values.optInt(KEY_REQUEST, 0);
            	int fills = values.optInt(KEY_FILL, 0);
                map.put("ad["+key+"]", requests + ";" + fills);
            }
        }
        
        return map;
	}
}
