package com.yerdy.services.logging;

/**
 * Used to mute certain log levels from YRDLog
 * {@link YRDLog}
 * @author Chris
 *
 */
public enum YRDLogLevel {
	YRDLogSilent(0),
	YRDLogError(1),
	YRDLogWarn(2),
	YRDLogInfo(3),
	YRDLogDebug(4),
	YRDLogVerbose(5);
	
	private int _level = 0;
	private YRDLogLevel(int value) {
		_level = value;
	}
	
	public boolean isAllowed(YRDLogLevel currentLevel) {
		return (_level <= currentLevel._level);
	}
}
