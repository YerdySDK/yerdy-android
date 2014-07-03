package com.yerdy.services.push.adm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import com.amazon.device.messaging.ADMConstants;
import com.amazon.device.messaging.ADMMessageHandlerBase;
import com.amazon.device.messaging.ADMMessageReceiver;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.push.YRDPushManager;
import com.yerdy.services.push.gcm.GCMIntentService;
import com.yerdy.services.push.gcm.GCMIntentService.MessageID;

public class ADMIntentService extends ADMMessageHandlerBase {

	public static class ADMBroadcastReciever extends ADMMessageReceiver {
		public ADMBroadcastReciever() {
			super(ADMIntentService.class);
		}
	}
	
	public ADMIntentService() {
		super(ADMIntentService.class.getName());
	}
	
	public ADMIntentService(final String className) {
		super(className);
	}
	
	@Override
	protected void onMessage(final Intent intent) {
    	YRDLog.i(this.getClass(), "onMessage: " + intent.getExtras());

        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("msg");
        
        Context context = getApplicationContext();

        if(context == null)
        	return;
        
        if(title == null)
        	title = "";
        if(message == null)
        	message = "";
        
        notify(context, MessageID.SERVER, title, message);
		
		// TODO Auto-generated method stub

	}
    
    public static void notify(Context context, MessageID msgId, String title, String message) {
    	YRDLog.i(ADMIntentService.class, "notify (msgid)::(title)::(message): " + msgId + " :: " + title + " :: " + message );
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
    		YRDLog.e(ADMIntentService.class, "cls == null");
        	return;
        }
        
        notifyIntent = new Intent(context, cls);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent = PendingIntent.getActivity(context, 0, notifyIntent, 0);

    	notification = YRDPushManager.buildNotification(context, intent, icon, title, message, System.currentTimeMillis(), msgId);

        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(msgId._value, notification);
    }
	
    /**
     * This method verifies the MD5 checksum of the ADM message.
     * 
     * @param extras Extra that was included with the intent.
     */
    @SuppressWarnings("unused")
	private void verifyMD5Checksum(final Bundle extras) 
    {        
        final Set<String> extrasKeySet = extras.keySet();
        final Map<String, String> extrasHashMap = new HashMap<String, String>();
        for (String key : extrasKeySet)
        {
            if (!key.equals(ADMConstants.EXTRA_MD5) && !key.equals("collapse_key"))
            {
                extrasHashMap.put(key, extras.getString(key));
            }            
        }
        final ADMSampleMD5ChecksumCalculator checksumCalculator = new ADMSampleMD5ChecksumCalculator();
        final String md5 = checksumCalculator.calculateChecksum(extrasHashMap);
        YRDLog.i(getClass(), "SampleADMMessageHandler:onMessage App md5: " + md5);
        
        /* Extract md5 from the extras in the intent. */
        final String admMd5 = extras.getString(ADMConstants.EXTRA_MD5);
        YRDLog.i(getClass(), "SampleADMMessageHandler:onMessage ADM md5: " + admMd5);
        
        /* Data integrity check. */
        if(!admMd5.trim().equals(md5.trim()))
        {
        	YRDLog.w(getClass(), "SampleADMMessageHandler:onMessage MD5 checksum verification failure. " +
            		"Message received with errors");
        }
    }

	@Override
	protected void onRegistered(String registrationId) {
        YRDLog.i(this.getClass(), "onRegistered: " + registrationId);
        YRDPushManager.storeId(getApplicationContext(), registrationId, YRDPushManager.TYPE_ADM);
	}

	@Override
	protected void onRegistrationError(String message) {
        YRDLog.i(this.getClass(), "onRegistrationError: " + message);
	}

	@Override
	protected void onUnregistered(String registrationId) {
    	YRDLog.i(this.getClass(), "onUnregistered: " + registrationId);
        YRDPushManager.storeId(getApplicationContext(), null, YRDPushManager.TYPE_ADM);
	}

}
