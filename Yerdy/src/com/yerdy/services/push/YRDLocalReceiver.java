package com.yerdy.services.push;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.push.gcm.GCMIntentService;

public class YRDLocalReceiver extends BroadcastReceiver {

	private final String EXTRA_TITLE = "extraTitle";
	private final String EXTRA_MESSAGE = "extraMessage";
	
	@Override
	public void onReceive(Context cxt, Intent intent) {
		PowerManager pm = (PowerManager) cxt.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "YRDLocalReceiver");
		
		// Acquire the lock
		wl.acquire();
		
		// You can do the processing here.
		Bundle extras = intent.getExtras();
		String title = extras.getString(EXTRA_TITLE);
		String msg = extras.getString(EXTRA_MESSAGE);

		YRDLog.i(this.getClass(), "getTitle: " + title + ", getMsg: " + msg);
		
		if(YRDPushManager.getPushMessagesEnabled(cxt))
		{
			GCMIntentService.notify(cxt, GCMIntentService.MessageID.LOCAL, title, msg);
		}
		else
		{
			YRDLog.i(this.getClass(), "GCMLocal Disabled");
		}

		// Release the lock
		wl.release();
	}
	
	public void SetAlarm(Activity app, String title, String msg, Calendar alarmTime, long repeatDelay) {
		CancelAlarm(app);
		Context cxt = app.getApplicationContext();

		AlarmManager am = (AlarmManager) cxt.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(cxt, YRDLocalReceiver.class);
		intent.putExtra(EXTRA_TITLE, title);
		intent.putExtra(EXTRA_MESSAGE, msg);		
		
		intent.setAction(this.getClass().getName());
		PendingIntent pi = PendingIntent.getBroadcast(cxt, 0, intent, 0);

		long when = alarmTime.getTimeInMillis();
		if (repeatDelay <= 0) {
			YRDLog.i(this.getClass(), "set alarm: ");
			am.set(AlarmManager.RTC_WAKEUP, when, pi);
		} else {
			YRDLog.i(this.getClass(), "set repeating alarm");
			am.setRepeating(AlarmManager.RTC_WAKEUP, when, repeatDelay, pi);
		}
	}

	public void SetAlarm(Activity app, String title, String msg, Calendar alarmTime) {
		SetAlarm(app, title, msg, alarmTime, -1);
	}

	public void CancelAlarm(Activity app) {
		YRDLog.i(this.getClass(), "cancelled alarm");
		Context cxt = app.getApplicationContext();
		Intent intent = new Intent(cxt, YRDLocalReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(cxt, 0, intent, 0);
		AlarmManager alarmManager = (AlarmManager) cxt
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}
}
