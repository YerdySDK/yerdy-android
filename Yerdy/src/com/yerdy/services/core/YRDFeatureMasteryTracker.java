package com.yerdy.services.core;

import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.yerdy.services.core.YRDPersistence.AnalyticKey;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.purchases.YRDHistoryTracker;

public class YRDFeatureMasteryTracker {
	
	// JSON keys
	private static final String COUNT = "count";
	private static final String SUBMITTED = "submitted";
	
	private static final int THRESHOLD_COUNT = 3;
	private int[] _defaultThresholds = { 1, 4, 8 };
	private Map<String, int[]> _thresholds;
	
	private YRDPersistence _persistance;
	private YRDHistoryTracker _historyTracker;
	
	// format:
	// { "<feature name>" : { 
	//		"count" : 2,  // number of occurrences
	//		"submitted" : 1  // highest level submitted to server (1, 2, or 3)
	//	 },
	//	 "<feature name 2>" : { 
	//		"count": 5, 
	//		"submitted": 2 
	//	 }, 
	//	... }
	private JSONObject _featureTracking;
	
	// contents are reset each time data is pushed up the server
	// format:  { "feature1" : { "_<level>": { "counter" : 1, "playtime": ..., "launches": ... }, "event2" : {...}, ... }, ... }
	private JSONObject _tracked;
	
	
	public YRDFeatureMasteryTracker(Context cxt, YRDHistoryTracker historyTracker) {
		_persistance = new YRDPersistence(cxt, cxt.getApplicationInfo().packageName, false);
		_featureTracking = _persistance.getJSON(AnalyticKey.FEATURE_MASTERIES);
		_tracked = _persistance.getJSON(AnalyticKey.FEATURE_MASTERIES_COUNTERS);
		_historyTracker = historyTracker;
		_thresholds = new HashMap<String, int[]>();
	}
	
	public void setFeatureUseLevels(int usesForNovice, int usesForAmateur, int usesForMaster) {
		if (!(usesForNovice < usesForAmateur && usesForAmateur < usesForMaster) || usesForNovice < 0 || usesForAmateur < 0 || usesForMaster < 0) {
			YRDLog.e(getClass(), "Invalid values for default feature uses.  (novice=" + usesForNovice +", amateur=" + usesForAmateur + ", master=" + usesForMaster + ")");
			YRDLog.e(getClass(), "All values must be positive and in ascending order (novice < amateur < master)");
			return;
		}
		
		_defaultThresholds[0] = usesForNovice;
		_defaultThresholds[1] = usesForAmateur;
		_defaultThresholds[2] = usesForMaster;
	}
	
	public void setFeatureUseLevels(String feature, int usesForNovice, int usesForAmateur, int usesForMaster) {
		if (feature == null) {
			YRDLog.e(getClass(), "setFeatureUseLevels - feature must not be null");
			return;
		}
		
		if (!(usesForNovice < usesForAmateur && usesForAmateur < usesForMaster) || usesForNovice < 0 || usesForAmateur < 0 || usesForMaster < 0) {
			YRDLog.e(getClass(), "Invalid values for " + feature + "feature uses.  (novice=" + usesForNovice +", amateur=" + usesForAmateur + ", master=" + usesForMaster + ")");
			YRDLog.e(getClass(), "All values must be positive and in ascending order (novice < amateur < master)");
			return;
		}
		
		int[] thresholds = new int[] { usesForNovice, usesForAmateur, usesForMaster };
		_thresholds.put(feature, thresholds);
	}
	
	public void logFeatureUse(String feature, int launches, long playtime) {
		try {
			JSONObject featureObj = _featureTracking.optJSONObject(feature);
			if (featureObj == null) {
				featureObj = new JSONObject();
				_featureTracking.put(feature, featureObj);
			}
			
			int count = featureObj.optInt(COUNT, 0);
			count += 1;
			featureObj.put(COUNT, count);

			sendFeatureEventIfNeeded(feature, launches, playtime);
			
			_persistance.setJSON(AnalyticKey.FEATURE_MASTERIES, _featureTracking);
			_persistance.save();

			YRDLog.d(getClass(), "logFeatureUse, _featureTracking: " + _featureTracking.toString());
		} catch (JSONException ex) { 
			YRDLog.e(getClass(), "Failed to logFeatureUse - " + feature);
			ex.printStackTrace();
		}
	}
	
	private void sendFeatureEventIfNeeded(String feature, int launches, long playtime) throws JSONException {
		JSONObject featureObj = _featureTracking.optJSONObject(feature);
		if (featureObj == null)
			return;
		
		int count = featureObj.optInt(COUNT, 0);
		int submitted = featureObj.optInt(SUBMITTED, 0);
		
		int[] thresholds = _thresholds.containsKey(feature) ? _thresholds.get(feature) : _defaultThresholds; 
		
		for (int i = 0; i < THRESHOLD_COUNT; i++) {
			int level = i + 1;
			
			if (count >= thresholds[i] && level > submitted) {
				submitted = level;
				featureObj.put(SUBMITTED, level); // gets saved back to persistence via calling method
				
				sendFeatureEvent(feature, level, launches, playtime);
			}
		}
	}
	
	private void sendFeatureEvent(String feature, int level, int launches, long playtime) throws JSONException {
		JSONObject eventObj = new JSONObject();
		eventObj.put("counter", 1);
		eventObj.put("launches", launches);
		eventObj.put("playtime", playtime);
		
		JSONObject groupObj = _tracked.optJSONObject(feature);
		if (groupObj == null) {
			groupObj = new JSONObject();
			_tracked.put(feature, groupObj);
		}
		
		groupObj.put("_" + Integer.toString(level), eventObj);
		_persistance.setJSON(AnalyticKey.FEATURE_MASTERIES_COUNTERS, _tracked);
		_persistance.save();
		
		YRDLog.d(getClass(), "sendFeatureEvent, _tracked: " + _tracked.toString());
	}
	
	public boolean isReadyToReport() {
		return (_tracked.length() > 0);
	}
	
	public JSONObject getAndResetCounters() {
		JSONObject response = _tracked;
		_tracked = new JSONObject();
		_persistance.setJSON(AnalyticKey.FEATURE_MASTERIES_COUNTERS, _tracked);
		_persistance.save();
		return response;
	}
	
}
