package com.yerdy.services.purchases;

import java.util.*;
import org.json.*;

import com.yerdy.services.core.YRDPersistence;
import com.yerdy.services.core.YRDPersistence.AnalyticKey;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.util.JSONUtil;

import android.content.Context;

public class YRDHistoryTracker {
	
	private static final String LAST_FEATURE_USES = "FeatureUses";
	private static final String LAST_ITEM_PURCHASES = "ItemPurchases";
	private static final String LAST_MESSAGES = "Messages";
	private static final String LAST_PROGRESSION_CATEGORIES = "ProgressionCategories";
	private static final String LAST_PROGRESSION_MILESTONES = "ProgressionMilestones";
	private static final String LAST_FEATURE_NAMES = "FeatureNames";
	private static final String LAST_FEATURE_LEVELS = "FeatureLevels";
	
	private static int MAX_ITEMS_TO_TRACK = 3;
	
	
	private YRDPersistence _persistence;
	
	public YRDHistoryTracker(Context ctx) {
		_persistence = new YRDPersistence(ctx, ctx.getApplicationInfo().packageName, false);
	}
	
	public void addFeatureUse(String feature) {
		if (feature == null) {
			YRDLog.e(getClass(), "addFeatureUse - feature was null");
			return;
		}
		addHistoryItem(LAST_FEATURE_USES, feature);
	}
	
	public List<String> getLastFeatureUses() {
		return getHistoryItems(LAST_FEATURE_USES);
	}
	
	public void addItemPurchase(String item) {
		if (item == null) {
			YRDLog.e(getClass(), "addItemPurchase - item was null");
			return;
		}
		addHistoryItem(LAST_ITEM_PURCHASES, item);
	}
	
	public List<String> getLastItemPurchases() {
		return getHistoryItems(LAST_ITEM_PURCHASES);
	}
	
	public void addMessage(String msgId) {
		if (msgId == null) {
			YRDLog.e(getClass(), "addMessage - msgId was null");
			return;
		}
		addHistoryItem(LAST_MESSAGES, msgId);
	}
	
	public List<String> getLastMessages() {
		return getHistoryItems(LAST_MESSAGES);
	}
	
	public void addPlayerProgression(String category, String milestone) {
		if (category == null || milestone == null) {
			YRDLog.e(getClass(), "addPlayerProgression - category or milestone was null");
			return;
		}
		addHistoryItem(LAST_PROGRESSION_CATEGORIES, category);
		addHistoryItem(LAST_PROGRESSION_MILESTONES, milestone);
	}
	
	public List<String> getLastPlayerProgressionCategories() {
		return getHistoryItems(LAST_PROGRESSION_CATEGORIES);
	}
	
	public List<String> getLastPlayerProgressionMilestones() {
		return getHistoryItems(LAST_PROGRESSION_MILESTONES);
	}
	
	public void addFeatureLevel(String feature, int level) {
		addHistoryItem(LAST_FEATURE_NAMES, feature);
		addHistoryItem(LAST_FEATURE_LEVELS, "_" + Integer.toString(level));
	}
	
	public List<String> getLastFeatureNames() {
		return getHistoryItems(LAST_FEATURE_NAMES);
	}
	
	public List<String>getLastFeatureLevels() {
		return getHistoryItems(LAST_FEATURE_LEVELS);
	}
	
	private void addHistoryItem(String type, String item) {
		YRDLog.d(getClass(), "addHistoryItem(" + type + "," + item +")");
		JSONObject json = _persistence.getJSON(AnalyticKey.HISTORY_ITEMS);
		
		try
		{
			JSONArray origArray = json.optJSONArray(type);
			if (origArray == null) {
				JSONArray array = new JSONArray();
				array.put(item);
				json.put(type, array);
			} else {
				JSONArray array = JSONUtil.insertFront(item, origArray);
				json.put(type, JSONUtil.trimToLength(array, MAX_ITEMS_TO_TRACK));
			}
			
			_persistence.setJSON(AnalyticKey.HISTORY_ITEMS, json);
			_persistence.save();
		}
		catch (JSONException ex)
		{
			YRDLog.e(getClass(), "exception in addHistoryItem");
			ex.printStackTrace();
		}
		
	}

	private List<String> getHistoryItems(String type) {
		JSONObject json = _persistence.getJSON(AnalyticKey.HISTORY_ITEMS);
		JSONArray array = json.optJSONArray(type);
		if (array == null)
			return new ArrayList<String>();
		
		List<Object> objList = JSONUtil.arrayToList(array);
		List<String> retVal = new ArrayList<String>();
		for (Object obj : objList) {
			if (obj instanceof String)
				retVal.add((String)obj);
		}
		return retVal;
	}
}
