package com.yerdy.services.messaging;

import android.app.Activity;

public interface YRDMessagePresenterDelegate {

	/**
	 * Called just as the message is about to be displayed
	 * @param placement
	 */
	public void willPresentMessage(Activity activity, YRDMessagePreseneter presenter, YRDMessage message);
	
	/**
	 * Called right after a message is displayed
	 * @param placement
	 */
	public void didPresentMessage(Activity activity, YRDMessagePreseneter presenter, YRDMessage message);
	
	/**
	 * Called just before the message is going to be dismissed
	 * @param placement
	 */
	public void willDismissMessage(Activity activity, YRDMessagePreseneter presenter, YRDMessage message, YRDMessageActionType action, String actionParamter);

	/**
	 * Called after the message has been dismissed
	 * @param placement
	 */
	public void didDismissMessage(Activity activity, YRDMessagePreseneter presenter, YRDMessage message, YRDMessageActionType action, String actionParameter);
}
