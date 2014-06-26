package com.yerdy.services.messaging;

import java.util.ArrayList;
import java.util.List;

import android.os.SystemClock;

public class YRDConversionTracker {
	private List<YRDBasePurchaseAction> _trackedPurchases = new ArrayList<YRDBasePurchaseAction>();
	private final long TIME_LIMIT = 900000;

	/**
	 * Sets the action time to the current system clock and adds it to the tracked list
	 * @param action - purchase action base object
	 */
	public void track(YRDBasePurchaseAction action) {
		action.trackCurrentTime();
		_trackedPurchases.add(action);
	}

	/**
	 * Looks through the tracked purchases for any matches, and removes expired items
	 * @param sku - sku or product id of the purchase to check for
	 * @return - returns the message id if it exists and is not expired otherwise returns -1
	 */
	public int check(String sku) {
		YRDBasePurchaseAction existing = null;
		List<YRDBasePurchaseAction> expired = new ArrayList<YRDBasePurchaseAction>();
		long timeLimit = SystemClock.elapsedRealtime() - TIME_LIMIT;

		synchronized(_trackedPurchases) {
			for(YRDBasePurchaseAction pa : _trackedPurchases) {
				if(pa.getTimestamp() < timeLimit) {
					expired.add(pa);
				} else if(pa.getAction().equals(sku)) {
					existing = pa;
					break;
				}
			}
			for(YRDBasePurchaseAction pa : expired) {
				_trackedPurchases.remove(pa);
			}
			if(existing != null) {
				_trackedPurchases.remove(existing);
			}
		}
		if(existing != null)
			return existing.getMessageId();
		else
			return -1;
	}
}
