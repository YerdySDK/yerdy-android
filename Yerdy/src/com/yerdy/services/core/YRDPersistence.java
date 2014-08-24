package com.yerdy.services.core;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.yerdy.services.purchases.PurchaseData;

/**
 * iOS provides both itself and the app its supporting an interface to the
 * keychain for persistance beyond the installation life of the app. This class
 * is meant to provide the Android equivalent. Out the gate it will provide
 * persistence, but not outside the life of the app.
 * 
 * @author m2
 *
 */
public class YRDPersistence implements Serializable {

	private static final long serialVersionUID = -3330666658319919227L;

	SharedPreferences preferences;
	SharedPreferences.Editor editor;

	public enum AnalyticKey implements CharSequence {
		BACKGROUND_TIMER,
		VERSION,
		LAUNCHES_LIFETIME,
		LAUNCHES_VERSIONED,
		ENTERS_VERSIONED,
		EXITS_VERSIONED,
		SET_INITIAL_CURRENCY,
		SPENT_CURRENCY_LIFETIME,
		EARNED_CURRENCY_LIFETIME,
		PURCHASED_CURRENCY_LIFETIME,
		PURCHASED_ITEMS_LIFETIME,
		PURCHASED_ITEMS_VERSIONED,
		PLAY_TIME_LIFETIME,
		PLAY_TIME_VERSIONED,
		REPORT_FIRST_PURCHASE,
		UNPUSHED_SAVED_PURCHASES_LIST,
		GCM_TOKEN,
		GCM_TOKEN_TYPE,
		CACHED_COUNTRY,
		POST_IAP_INDEX,
		SCREEN_VISITS,
		LAUNCHES_PROGRESSION,
		CURRENCY_TIMED,
		VIRTUAL_PURCHASE_COUNT_TIMED,
		PROGRESSION_EVENTS,
		PROGRESSION_ALL_LOGGED_EVENTS,
		CUSTOM_EVENTS,
		IS_PRE_YERDY_USER,
		TRACK_PRE_YERDY_USER,
		ADS_VERSIONED,
		HISTORY_ITEMS,
		UNPUSHED_VIRTUAL_PURCHASES, // virtual purchases cached locally for submission next time we're online
		PENDING_VIRTUAL_PURCHASES, // virtual purchases on hold due to validating first IAP
		PURCHASE_VALIDATION_STATE; // see YRDAnalytics.ValidationState

		@Override
		public int length() {
			return this.toString().length();
		}

		@Override
		public char charAt(int index) {
			return this.toString().charAt(index);
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return this.toString().subSequence(start, end);
		}
	}

	@SuppressLint("WorldWriteableFiles")
	@SuppressWarnings("deprecation")
	public YRDPersistence(Context context, String name, boolean shared) {
		name += serialVersionUID;
		if( shared ) {
			preferences = context.getSharedPreferences(name, Context.MODE_WORLD_WRITEABLE);
		} else {
			preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		}
		editor = preferences.edit();
	}

	public void setValue(CharSequence key, boolean param) {
		editor.putBoolean(key.toString(), param);
	}

	public void setValue(CharSequence key, int param) {
		editor.putInt(key.toString(), param);
	}

	public void setValue(CharSequence key, float param) {
		editor.putFloat(key.toString(), param);
	}

	public void setValue(CharSequence key, long param) {
		editor.putLong(key.toString(), param);
	}

	public void setValue(CharSequence key, String param) {
		editor.putString(key.toString(), param);
	}

	public boolean getValue(CharSequence key, boolean defValue) {
		return preferences.getBoolean(key.toString(), defValue);
	}

	public int getValue(CharSequence key, int defValue) {
		return preferences.getInt(key.toString(), defValue);
	}

	public float getValue(CharSequence key, float defValue) {
		return preferences.getFloat(key.toString(), defValue);
	}

	public long getValue(CharSequence key, long defValue) {
		return preferences.getLong(key.toString(), defValue);
	}

	public String getValue(CharSequence key, String defValue) {
		return preferences.getString(key.toString(), defValue);
	}

	public void save() {
		editor.commit();

	}

	public void setValue(CharSequence key, List<PurchaseData> values) {
		JSONArray array = new JSONArray();
		for (PurchaseData value : values) {
			try {
				array.put(value.toJSONObject());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		setValue(key, array.toString());
	}

	public List<PurchaseData> getValueList(CharSequence key) {
		LinkedList<PurchaseData> list = new LinkedList<PurchaseData>();
		if (preferences.contains(key.toString())) {
			String json = preferences.getString(key.toString(), null);
			try {
				JSONArray array = new JSONArray(json);
				for (int i = 0; i < array.length(); i++) {
					JSONObject purchaseJSON = array.getJSONObject(i);
					list.add(PurchaseData.parseJSON(purchaseJSON));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public void setJSON(CharSequence key, JSONObject value) {
		editor.putString(key.toString(), value.toString());
	}
	
	public JSONObject getJSON(CharSequence key) {
		String jsonStr = preferences.getString(key.toString(), null);
		if(jsonStr != null) {
			try {
				return new JSONObject(jsonStr);
			} catch (Exception e) {
				//Do nothing on error
			}
		}
		return new JSONObject();
	}
	
	public void setJSONArray(CharSequence key, JSONArray value) {
		editor.putString(key.toString(), value.toString());
	}
	
	public JSONArray getJSONArray(CharSequence key) {
		String jsonStr = preferences.getString(key.toString(), null);
		if(jsonStr != null) {
			try {
				return new JSONArray(jsonStr);
			} catch (Exception e) {
				//Do nothing on error
			}
		}
		return new JSONArray();
	}

	public void clear() {
		editor.clear();
	}

	public boolean hasKey(CharSequence key) {
		return preferences.contains(key.toString());
	}

	public void deleteKey(CharSequence key) {
		editor.remove(key.toString());
	}

}
