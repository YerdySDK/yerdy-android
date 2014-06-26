package com.yerdy.services.lvl;

/**
 * Enum for representing the status of the Google LVL checks
 * @author Chris
 */
public enum LVLStatus {
	LICENSED("store"),
	UNLICENSED("unknown"),
	NONE("none"),
	NOT_APPLICABLE("NA");
	
	private final String _key;
	
	LVLStatus (String key) {
		_key = key;
	}
	
	public String getKey() {
		return _key;
	}
}
