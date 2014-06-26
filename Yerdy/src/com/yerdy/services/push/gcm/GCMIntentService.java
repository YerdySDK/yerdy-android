package com.yerdy.services.push.gcm;

//import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.push.YRDPushManager;

public class GCMIntentService extends GCMBaseIntentService {

//	private static Runnable delayed = null;
//	private static Handler mainThread = null;
	
	public enum MessageID
	{
		SERVER(0),
		LOCAL(1);
		
		public int _value = 0;
		private MessageID(int value) {
			_value = value;
		}
	}
	
	public GCMIntentService() {
        super();
    }
	
	@Override
	protected String[] getSenderIds(Context context) {
		return new String[]{YRDPushManager.getSenderID(context)};
	}

    @Override
    protected void onRegistered(Context context, String registrationId) {
        YRDLog.i(this.getClass(), "onRegistered: " + registrationId);

        // looks looks like this flag is for programmers use only
        GCMRegistrar.setRegisteredOnServer(context, true);
        YRDPushManager.storeId(context, registrationId, YRDPushManager.TYPE_GCM);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
    	YRDLog.i(this.getClass(), "onUnregistered: " + registrationId);

        // looks looks like this flag is for programmers use only
        GCMRegistrar.setRegisteredOnServer(context, false);
    }


    // NOTE: will not do anything to main activity on itself when message is
    //       received and app is not killed

    @Override
    protected void onMessage(Context context, Intent intent) {
    	YRDLog.i(this.getClass(), "onMessage: " + intent.getExtras());

        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("msg");

        if(context == null)
        	return;
        
        if(title == null)
        	title = "";
        if(message == null)
        	message = "";
        
        notify(context, MessageID.SERVER, title, message);
    }

    @Override
    public void onError(Context context, String errorId) {
    	YRDLog.i(this.getClass(), "onError: " + errorId);
    }
    
    public static void notify(Context context, MessageID msgId, String title, String message) {
        int icon = YRDPushManager.ICON;
        if(icon == 0) {
    		// Prep native-type Notification object. Use Resources instead of R,
    		// as Unity will FUCK-UP your R values with its own gen folder.
        	// R works in Office games, but its preferable to let late bindings rule on any Unity Resource.
    		Resources res = context.getResources();
    		icon = res.getIdentifier("notification_icon", "drawable", context.getPackageName());
        }
        NotificationManager manager;
        Notification notification;
        Intent notifyIntent;
        PendingIntent intent;
		Class<?> cls = null;

        if(YRDPushManager.INTENT_CLASS == null)
        {
    		SharedPreferences prefs = context.getSharedPreferences(YRDPushManager.PREF_NAME, Context.MODE_PRIVATE);
    		String name = prefs.getString(YRDPushManager.PREF_CLASS_KEY, null);
        	
        	if(name != null)
        	{
				try {
					cls = Class.forName(name);
				} catch (ClassNotFoundException e) {
					cls = null;
					e.printStackTrace();
				}
        	}
        }
        else
        {
        	cls = YRDPushManager.INTENT_CLASS;
        }
        
        if(cls == null)
        {
    		YRDLog.e(GCMIntentService.class, "cls == null");
        	return;
        }
        
        notifyIntent = new Intent(context, cls);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent = PendingIntent.getActivity(context, 0, notifyIntent, 0);

        notification = YRDPushManager.buildNotification(context, intent, icon, title, message, System.currentTimeMillis(), msgId);
        
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(msgId._value, notification);
    }
}
