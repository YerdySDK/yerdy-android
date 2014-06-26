package com.yerdy.services.purchases;

import org.json.JSONObject;

import com.yerdy.services.util.YRDJsonProcessor;

public class YRDResultProcessor extends YRDJsonProcessor {

	public static final int INVALID_MESSAGE = 0;

	private int _resultCode = INVALID_MESSAGE;
	private boolean _success = false;
	
	public boolean getSuccess() {
		return _success;
	}
	
	public int getResultCode() {
		return _resultCode;
	}

	public void parseJSON(JSONObject json) {
		if(json != null) {
			_resultCode = json.optInt("result", INVALID_MESSAGE);
			_success = !(_resultCode == INVALID_MESSAGE);
		}
	}

}
