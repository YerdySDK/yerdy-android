package com.yerdy.services.messaging;

import android.os.SystemClock;

public class YRDBasePurchaseAction {
	private String _action = null;
	private long _shownTimestamp = -1;
	private int _msgId = -1;
	
	protected YRDBasePurchaseAction(String action, int id) {
		_action = action;
		_msgId = id;
	}
	
	protected int getMessageId() {
		return _msgId;
	}
	
	protected String getAction() {
		return _action;
	}
	
	protected void trackCurrentTime() {
		_shownTimestamp = SystemClock.elapsedRealtime();
	}
	
	protected long getTimestamp() {
		return _shownTimestamp;
	}
}
