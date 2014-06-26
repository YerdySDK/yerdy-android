package com.yerdy.services.core;

import org.json.JSONException;
import org.json.JSONObject;

import com.yerdy.services.core.YRDPersistence.AnalyticKey;
import com.yerdy.services.logging.YRDLog;

import android.content.Context;

public class YRDProgressionTracker {

	private JSONObject _tracked = new JSONObject();
	
	private YRDPersistence _persistance;
	
	private final String KEY_COUNTER = "counter";
	private final String KEY_PLAYTIME = "playtime";
	private final String KEY_LAUNCH = "launches";
	
	public YRDProgressionTracker(Context cxt) {
		_persistance = new YRDPersistence(cxt, cxt.getApplicationInfo().packageName, false);
		_tracked = _persistance.getCounter(AnalyticKey.PROGRESSION_EVENTS);
	}

	public void trackProgression(String group, String event, int lauches, long playtime) {
		JSONObject groupJson = _tracked.optJSONObject(group);
		
		if(groupJson != null) {
			try
			{
				JSONObject data = groupJson.optJSONObject(event);
				groupJson.put(event, (data != null)?(updateData(data, lauches, playtime)):(createData(lauches, playtime)));
			} catch (Exception e) { }
		} else {
			try {
				_tracked.put(group, createGroup(event, lauches, playtime));
			} catch (JSONException e) {
				YRDLog.w(getClass(), "Failed to store progression event");
			}
		}
		
		_persistance.setCounter(AnalyticKey.PROGRESSION_EVENTS, _tracked);
		_persistance.save();
		
		YRDLog.i(getClass(), "POST UPDATE");
		try {
			YRDLog.i(getClass(), _tracked.toString(1));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private JSONObject createGroup(String event, int launchDelta, long playtimeDelta) throws JSONException {
		JSONObject json = new JSONObject();
		JSONObject data = createData(launchDelta, playtimeDelta);
		json.put(event, data);
		return json;
	}
	
	private JSONObject createData(int launchDelta, long playtimeDelta) throws JSONException {
		JSONObject json = new JSONObject();
		json.put(KEY_COUNTER, 1);
		json.put(KEY_LAUNCH, launchDelta);
		json.put(KEY_PLAYTIME, playtimeDelta);
		return json;
	}
	
	private JSONObject updateData(JSONObject json, int launchDelta, long playtimeDelta) throws JSONException {
		int counter = json.optInt(KEY_COUNTER, 0);
		json.put(KEY_COUNTER, (counter+1));
		int launches = json.optInt(KEY_LAUNCH, 0);
		json.put(KEY_LAUNCH, (launches + launchDelta));
		long playtime = json.optLong(KEY_PLAYTIME, 0L);
		json.put(KEY_PLAYTIME, (playtime + playtimeDelta));
		return json;
	}
	
	public boolean isReadyToReport() {
		return (_tracked.length() > 0);
	}
	
	public JSONObject getAndResetProgressionEvents() {
		JSONObject response = _tracked;
		_tracked = new JSONObject();
		_persistance.setCounter(AnalyticKey.PROGRESSION_EVENTS, _tracked);
		_persistance.save();
		return response;
	}
}
