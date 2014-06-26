package com.yerdy.services.launch;

import java.util.Locale;

import org.json.JSONObject;

import com.yerdy.services.util.YRDJsonProcessor;

/**
 * Processes Server response for Launch reports
 * @author Chris
 *
 */
public class YRDLaunchProcessor extends YRDJsonProcessor {

	private YRDUserType _userType = YRDUserType.NONE;
	private String _tag = "";
	private boolean _success = false;
	
	public String getTag() {
		return _tag;
	}

	public YRDUserType getUserType() {
		return _userType;
	}
	
	public boolean getSuccess() {
		return _success;
	}

	public void parseJSON(JSONObject json) {
		if(json != null) {
			JSONObject jsonAttrs = json.optJSONObject("@attributes");
			if(jsonAttrs != null) {
				_success = jsonAttrs.optBoolean("success", false);
				if(_success) {
					_tag = getString(jsonAttrs, "tag", "");
					String type = getString(jsonAttrs, "type", "none");
					_userType = YRDUserType.valueOf(type.toUpperCase(Locale.getDefault()));
				}
			}
		}
	}
}
