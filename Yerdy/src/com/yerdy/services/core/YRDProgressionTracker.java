package com.yerdy.services.core;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.yerdy.services.core.YRDPersistence.AnalyticKey;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.purchases.YRDHistoryTracker;

import android.content.Context;

public class YRDProgressionTracker {

	// contents are reset each time data is pushed up the server
	// format:  { "group1" : { "event1": { "counter" : .., "playtime": ..., "launches": ... }, "event2" : {...}, ... }, ... }
	private JSONObject _tracked = new JSONObject();
	
	// all events ever logged (only names stored, to prevent duplicates)
	// format:  { "group1" : [ "all", "logged", "events", "for", "group1" ], "group2" : [ "events" ] }
	private JSONObject _allLoggedEvents;
	
	private final YRDPersistence _persistance;
	private final YRDHistoryTracker _historyTracker;
	
	private final String KEY_COUNTER = "counter";
	private final String KEY_PLAYTIME = "playtime";
	private final String KEY_LAUNCH = "launches";
	
	public YRDProgressionTracker(Context cxt, YRDHistoryTracker historyTracker) {
		_persistance = new YRDPersistence(cxt, cxt.getApplicationInfo().packageName, false);
		_historyTracker = historyTracker;
		_tracked = _persistance.getJSON(AnalyticKey.PROGRESSION_EVENTS);
		_allLoggedEvents = _persistance.getJSON(AnalyticKey.PROGRESSION_ALL_LOGGED_EVENTS);
	}
	
	public void startProgression(String group, String event, int launches, long playtime) {
		if (wasGroupStarted(group)) {
			YRDLog.e(getClass(), String.format(
					"Failed to start player progression category '%s' with milestone '%s', already started", group, event));
			YRDLog.e(getClass(), "The 'logPlayerProgression(...)' method can be used to log additional milestones for a category");
			return;
		}
		
		storeProgression(group, event, launches, playtime);
	}

	public void trackProgression(String group, String event, int launches, long playtime) {
		if (!wasGroupStarted(group)) {
			YRDLog.e(getClass(), String.format(
					"Failed to log player progression category '%s' with milestone '%s', category was never started.", group, event));
			YRDLog.e(getClass(), "The 'startPlayerProgression(...)' method can be used to start a category");
			return;
		}
		
		if (wasEventLogged(group, event)) {
			YRDLog.e(getClass(), String.format(
					"Failed to log player progression category '%s' with milestone '%s', milestone was already logged.", group, event));
			return;
		}
		
		storeProgression(group, event, launches, playtime);
	}
	
	// startProgression/trackProgression do some sanity checks/validation, then call this method to write the event
	private void storeProgression(String group, String event, int launches, long playtime) {
		String servicesEventName = "_" + event; // to coerce it to a string for the services
		
		// update counters
		JSONObject groupJson = _tracked.optJSONObject(group);
		
		if(groupJson != null) {
			try
			{
				JSONObject data = groupJson.optJSONObject(servicesEventName);
				groupJson.put(servicesEventName, (data != null)?(updateData(data, launches, playtime)):(createData(launches, playtime)));
			} catch (Exception e) { }
		} else {
			try {
				_tracked.put(group, createGroup(servicesEventName, launches, playtime));
			} catch (JSONException e) {
				YRDLog.w(getClass(), "Failed to store progression event");
			}
		}
		
		// update all logged events
		try {
			JSONArray groupLoggedEvents = _allLoggedEvents.optJSONArray(group);
			if (groupLoggedEvents == null) {
				groupLoggedEvents = new JSONArray();
				_allLoggedEvents.put(group, groupLoggedEvents);
			}
			groupLoggedEvents.put(event);
		} catch (JSONException e) {
			YRDLog.w(getClass(), "Failed to store list of logged progression events");
		}
		
		_historyTracker.addPlayerProgression(group, servicesEventName);
		
		_persistance.setJSON(AnalyticKey.PROGRESSION_EVENTS, _tracked);
		_persistance.setJSON(AnalyticKey.PROGRESSION_ALL_LOGGED_EVENTS, _allLoggedEvents);
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
		_persistance.setJSON(AnalyticKey.PROGRESSION_EVENTS, _tracked);
		_persistance.save();
		return response;
	}
	
	
	private boolean wasGroupStarted(String group) {
		return _allLoggedEvents.optJSONArray(group) != null;
	}
	
	private boolean wasEventLogged(String group, String event) {
		JSONArray arr = _allLoggedEvents.optJSONArray(group);
		if (arr == null)
			return false;
		
		for (int i = 0; i < arr.length(); i++) {
			String item = arr.optString(i, "");
			if (item.equals(event))
				return true;
		}
		
		return false;
	}
}
