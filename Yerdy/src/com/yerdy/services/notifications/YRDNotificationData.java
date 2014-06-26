package com.yerdy.services.notifications;

import android.content.Context;

/**
 * Notification Data Object
 * @author Chris
 *
 */
public class YRDNotificationData {
	
	public int notificationId;
	public CharSequence title;
	public CharSequence text;
	public CharSequence tickerText;
	public long atTime;
	public Context context;
	
	public YRDNotificationData(int notificationId, CharSequence title, CharSequence text, CharSequence tickerText,
			long atTime, Context applicationContext) {
		this.notificationId = notificationId;
		this.title = title;
		this.text = text;
		this.tickerText = tickerText;
		this.atTime = atTime;
		this.context = applicationContext;
	}

}
