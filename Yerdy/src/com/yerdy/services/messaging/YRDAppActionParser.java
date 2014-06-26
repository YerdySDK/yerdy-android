package com.yerdy.services.messaging;


/**
 * Used to translate action strings from server into a useable format
 * @author Chris
 */
public class YRDAppActionParser {
	private YRDAppActionType _actionType = YRDAppActionType.EMPTY;
	private String _actionInfo = null;
	
	public YRDAppActionParser(YRDAppActionType type, String action) {
		_actionType = type;
		_actionInfo = action;
	}
	
	public static YRDAppActionParser parseAction(String action) {
		if(action == null || action.length() == 0) {
			return new YRDAppActionParser(YRDAppActionType.EMPTY, null);
		} else if(Actions.IAP.is(action)) {
			return new YRDAppActionParser(YRDAppActionType.IN_APP_PURCHASE, Actions.IAP.cut(action));
		} else if(Actions.ITEM.is(action)) {
			return new YRDAppActionParser(YRDAppActionType.ITEM_PURCHASE, Actions.ITEM.cut(action));
		} else if(Actions.REWARD.is(action)) {
			return new YRDAppActionParser(YRDAppActionType.REWARD, Actions.REWARD.cut(action));
		} else if(Actions.NAV.is(action)) {
			return new YRDAppActionParser(YRDAppActionType.NAVIGATION, Actions.NAV.cut(action));
		} else {
			return null;
		}
	}
	
	public YRDAppActionType getActionType() {
		return _actionType;
	}
	
	public String getActionInfo() {
		return _actionInfo;
	}
	
	enum Actions {
		IAP("iap:"),
		ITEM("item:"),
		REWARD("reward:"),
		NAV("nav:");
		
		private String _value = null;
		
		private Actions(String value) {
			_value = value;
		}
		
		public boolean is(String value) {
			return (value.startsWith(_value));
		}
		
		public String cut(String value) {
			return (value.replaceFirst(_value, ""));
		}
	}
}
