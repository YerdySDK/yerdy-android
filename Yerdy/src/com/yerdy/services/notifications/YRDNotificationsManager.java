package com.yerdy.services.notifications;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.push.gcm.GCMIntentService;

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
        timer.cancel();
        timer = new Timer("YerdyNotifications", true);
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
        GCMIntentService.notify(noteData.context, GCMIntentService.MessageID.LOCAL, noteData.title.toString(), noteData.text.toString());
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
