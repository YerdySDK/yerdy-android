package com.yerdy.services.logging;

import android.util.Log;

/**
 * Class is a wrapper for Android's {@link Log} class, but provides functionality
 * to mute certain log levels
 */
public class YRDLog {

	private static YRDLogLevel _currentLogLevel = YRDLogLevel.YRDLogError;
	private static final String NULL_MSG = "Log Message Is Null";

	/**
	 * @param level {@link com.yerdy.services.logging.YRDLogLevel} pass in leg level to filter yerdy logcat output, for release builds suggested to use {@link com.yerdy.services.logging.YRDLogLevel #YRDLogSilent} or {@link com.yerdy.services.logging.YRDLogLevel #YRDLogError}
	 */
	public static void SetLogLevel(YRDLogLevel level) {
		_currentLogLevel = level;
	}

	public static void v(Class<?> klass, String message) {
		klass = (klass != null) ? (klass) : (YRDLog.class);
		v(klass.getSimpleName(), message);
	}

	public static void v(String tag, String message) {
		tag = (tag != null) ? (tag) : (YRDLog.class.getSimpleName());
		message = (message != null) ? (message) : (NULL_MSG);
		if (YRDLogLevel.YRDLogVerbose.isAllowed(_currentLogLevel))
			Log.v(tag, message);
	}

	public static void d(Class<?> klass, String message) {
		klass = (klass != null) ? (klass) : (YRDLog.class);
		d(klass.getSimpleName(), message);
	}

	public static void d(String tag, String message) {
		tag = (tag != null) ? (tag) : (YRDLog.class.getSimpleName());
		message = (message != null) ? (message) : (NULL_MSG);
		if (YRDLogLevel.YRDLogDebug.isAllowed(_currentLogLevel))
			Log.d(tag, message);
	}

	public static void i(Class<?> klass, String message) {
		klass = (klass != null) ? (klass) : (YRDLog.class);
		i(klass.getSimpleName(), message);
	}

	public static void i(String tag, String message) {
		tag = (tag != null) ? (tag) : (YRDLog.class.getSimpleName());
		message = (message != null) ? (message) : (NULL_MSG);
		if (YRDLogLevel.YRDLogInfo.isAllowed(_currentLogLevel))
			Log.i(tag, message);
	}

	public static void w(Class<?> klass, String message) {
		klass = (klass != null) ? (klass) : (YRDLog.class);
		w(klass.getSimpleName(), message);
	}

	public static void w(String tag, String message) {
		tag = (tag != null) ? (tag) : (YRDLog.class.getSimpleName());
		message = (message != null) ? (message) : (NULL_MSG);
		if (YRDLogLevel.YRDLogWarn.isAllowed(_currentLogLevel))
			Log.w(tag, message);
	}

	public static void e(Class<?> klass, String message) {
		klass = (klass != null) ? (klass) : (YRDLog.class);
		e(klass.getSimpleName(), message);
	}

	public static void e(String tag, String message) {
		tag = (tag != null) ? (tag) : (YRDLog.class.getSimpleName());
		message = (message != null) ? (message) : (NULL_MSG);
		if (YRDLogLevel.YRDLogError.isAllowed(_currentLogLevel))
			Log.e(tag, message);
	}

	public static void wtf(Class<?> klass, String message) {
		klass = (klass != null) ? (klass) : (YRDLog.class);
		wtf(klass.getSimpleName(), message);
	}

	public static void wtf(String tag, String message) {
		tag = (tag != null) ? (tag) : (YRDLog.class.getSimpleName());
		message = (message != null) ? (message) : (NULL_MSG);
		Log.wtf(tag, message);
	}

}
