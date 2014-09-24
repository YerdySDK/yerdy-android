package com.yerdy.services.notifications;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.yerdy.services.Yerdy;
import com.yerdy.services.logging.YRDLog;

/**
 * Manages local system notifications
 * 
 * @author Chris
 * 
 */
public class YRDNotificationsManager {

	private static YRDNotificationsManager instance = new YRDNotificationsManager();

	private Timer timer;

	private int currNotificationId = 0;

	public static YRDNotificationsManager getInstance() {
		return instance;
	}

	private YRDNotificationsManager() {
		timer = new Timer("YerdyNotifications", true);
	}

	public void purgeNotifications() {

		timer.purge();

	}

	public int newNotification(YRDNotificationData notification) {
		notification.notificationId = ++currNotificationId;
		setNotification(notification);
		return currNotificationId;
	}
	
	public void setNotifications(String jsonNotifications, Context context) {
		YRDLog.i( getClass(), "Setting notifications with json: "+jsonNotifications);
		try {
			JSONArray notificationsArray = new JSONArray(jsonNotifications);
			
			for (int i = 0; i < notificationsArray.length(); i++) {
				JSONObject jsonObj = notificationsArray.optJSONObject(i);
				if (jsonObj == null)
					continue;
				
				//int notificationId, CharSequence title, CharSequence text, CharSequence tickerText,
				// long notifyTime, Context applicationContext
				// retVal.transactionAmount = json.optString("transactionAmount");
				// retVal.firstPurchase = json.optBoolean("firstPurchase");
				// retVal.postIapIndex = json.optInt("postIapIndex");
			
				YRDNotificationData notification = new YRDNotificationData(
						jsonObj.optInt("notificationId"),
						jsonObj.optString("title"),
						jsonObj.optString("alertBody"),
						jsonObj.optString("tickerText"),
						jsonObj.optLong("notifyTime"),
						context);
				
				YRDLog.i(
						getClass(),
						"Adding notifications with title: "+notification.title+", body: "+notification.text);
				setNotification( notification);
				
			}
		} catch (Exception e) {
			//Do nothing on error
			YRDLog.w(
					getClass(),
					"Error trying to add notifications using json:\n"+jsonNotifications);
		}
	}

	public void setNotification(YRDNotificationData notification) {

		long delay = notification.atTime - System.currentTimeMillis();

		if (delay < 0L) {
			showNotifcation(notification);
		} else {
			timer.schedule(new NotifcationTask(notification), delay);
		}

	}

	@SuppressWarnings("deprecation")
	private void showNotifcation(YRDNotificationData noteData) {

		Context context = noteData.context;

		// Prep Notification object. Use Resources instead of R,
		// as Unity will regenerate the R values, not an issue for native games
		Resources res = context.getResources();
		int icon = res.getIdentifier("notification_icon", "drawable", Yerdy
				.getInstance().getAppPackage());

		// Unity makes this a loose binding now, make sure someone knows. Normal
		// Java shouldn't experience this
		if (0 == icon) {
			YRDLog.w(
					getClass(),
					"Missing resource 'drawable.notification_icon' check you have one in Assets/Plugins/Android/res");
		}

		Notification nativeNotification = new Notification(icon,
				noteData.tickerText, noteData.atTime);

		// Prep PendingIntent assigned to notification (start game)
		Intent notificationIntent = new Intent("android.intent.action.MAIN");
		// notificationIntent.setComponent(null);
		notificationIntent.addCategory("android.intent.category.LAUNCHER");

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		nativeNotification.setLatestEventInfo(context, noteData.title,
				noteData.text, contentIntent);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager
				.notify(noteData.notificationId, nativeNotification);
	}

	class NotifcationTask extends TimerTask {
		YRDNotificationData notification;

		NotifcationTask(YRDNotificationData notification) {
			this.notification = notification;
		}

		@Override
		public void run() {
			showNotifcation(notification);
		}

	}
}
