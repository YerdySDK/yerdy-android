package com.yerdy.services.launch;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class manages AB Testing tag and behavior classification applied to the app
 * 
 * @author m2
 * 
 */

public class YRDUserInfo {

	public static final String DEFAULTS_KEY = "ABTesting_currentABTag";
	public static final String PREF_NAME = "ABTesting";
	public static final String EMPTY = "";

	private static YRDUserInfo _instance = null;
	
	private YRDABTagChangeDelegate delegate;

	public String currentABTag = EMPTY;
	
	public YRDUserType userType = YRDUserType.NONE;
	private SharedPreferences _prefs = null;

	public static YRDUserInfo getInstance(Context cxt) {
		if(_instance == null)
			_instance = new YRDUserInfo(cxt);
		return _instance;
	}
	
	public YRDUserType getUserType() {
		return userType;
	}

	public void setUserType(YRDUserType userType) {
		this.userType = userType;
	}

	private YRDUserInfo(Context cxt) {
		_prefs = cxt.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

		if (_prefs.contains(DEFAULTS_KEY)) {
			currentABTag = _prefs.getString(DEFAULTS_KEY, EMPTY);
		}
	}

	public void setDelegate(YRDABTagChangeDelegate delegate) {
		this.delegate = delegate;
	}

	public void setCurrentABTag(String currentABTag) {
		if (null == currentABTag) {
			currentABTag = EMPTY;
		}

		this.currentABTag = currentABTag;

		if (_prefs.contains(DEFAULTS_KEY) && currentABTag.equals(_prefs.getString(DEFAULTS_KEY, EMPTY))) {
			return;
		}

		SharedPreferences.Editor editor = _prefs.edit();
		editor.putString(DEFAULTS_KEY, currentABTag);
		editor.commit();

		if(null != delegate ) {
			delegate.abTestingTagChanged(currentABTag);
		}

	}

	public boolean isInTest() {
		return (0 != currentABTag.length());
	}

	public String getCurrentABTag() {
		return currentABTag;
	}

	
	
}
