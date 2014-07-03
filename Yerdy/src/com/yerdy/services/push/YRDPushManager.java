package com.yerdy.services.push;

import android.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

import com.amazon.device.messaging.ADM;
import com.amazon.device.messaging.development.ADMManifest;
import com.google.android.gcm.GCMRegistrar;
import com.yerdy.services.core.YRDAnalytics;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.push.gcm.GCMIntentService.MessageID;

public class YRDPushManager {
	/**
	 * YRDPushMananger Call via AndroidApplication onCreate or
	 * UnityPlayerActivity onCreate
	 */

	/**
	 * Test this permission to see if we're running with a google-type manifest
	 */
	public static final String MESSAGE_PERMISSION = "com.google.android.c2dm.permission.RECEIVE";

	public static Class<?> INTENT_CLASS = null;
	public static int ICON = 0;

	public static final String PREF_NAME = "gcmSharedPref";
	public static final String PREF_CLASS_KEY = "gcmClassKey";
	static final String PREF_ENABLE_LOCAL = "gcmEnableLocal";
	static final String PREF_ENABLE_LOCAL_SOUND = "gcmEnableLocalSound";

	public static final String TYPE_GCM = "gcm";
	public static final String TYPE_ADM = "adm";
	private static String _senderId = null;

	public static void Register(Context cxt, int icon) {
		boolean allowGCMReg = true;
		boolean allowADMReg = true;
		boolean admAvailiable = false;

		if (PackageManager.PERMISSION_GRANTED != cxt
				.checkCallingOrSelfPermission(MESSAGE_PERMISSION)) {
			YRDLog.w(YRDPushManager.class, "Missing permission: "
					+ MESSAGE_PERMISSION);
			allowGCMReg = false;
		}

		YRDPushManager.INTENT_CLASS = cxt.getClass();
		YRDPushManager.ICON = icon;

		try {
			Class.forName("com.amazon.device.messaging.ADM");
			admAvailiable = true;
		} catch (Exception e) {
			YRDLog.w(YRDPushManager.class, "ADM Not availiable");
		}

		if (allowGCMReg) {
			try {
				GCMRegistrar.checkDevice(cxt.getApplicationContext());
				GCMRegistrar.checkManifest(cxt.getApplicationContext());
			} catch (Exception e) {
				allowGCMReg = false;
				YRDLog.e(YRDPushManager.class, e.getMessage());
			}
		}

		if (admAvailiable) {
			YRDLog.e(YRDPushManager.class, "ADM Availiable");
			try {
				ADMManifest.checkManifestAuthoredProperly(cxt);
				YRDLog.e(YRDPushManager.class, "ADM checked");
			} catch (Exception e) {
				allowADMReg = false;
				YRDLog.e(YRDPushManager.class, e.getMessage());
			}
		} else {
			allowADMReg = false;
		}

		if (allowGCMReg)
			runGCMReg(cxt);
		if (allowADMReg)
			runADMReg(cxt);

		SharedPreferences prefs = cxt.getApplicationContext()
				.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putString(PREF_CLASS_KEY, cxt.getClass().getName());
		edit.commit();
	}

	public static String getSenderID(Context context) {
		if (_senderId == null) {
			// Yerdy_GCMSenderId
			try {
				ApplicationInfo ai = context.getPackageManager()
						.getApplicationInfo(context.getPackageName(),
								PackageManager.GET_META_DATA);
				String val = ai.metaData.getString("Yerdy_GCMSenderId");
				if(val != null) {
					_senderId = val.replace("gcm:", "");
				} else {
					YRDLog.e(YRDPushManager.class, "Unable to read GCM Sender ID from Manifest, please check your manifest configuration");
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return _senderId;
	}

	// public static String SENDER_ID = "958366145980";

	private static void runGCMReg(final Context cxt) {
		final String registrationId;
		// Nexus 4 shows checkbox to receive push messages in app preferences
		// NOTE: check older devices for this setting
		registrationId = GCMRegistrar.getRegistrationId(cxt
				.getApplicationContext());
		if (registrationId.equals("")) {
			String senderId = YRDPushManager.getSenderID(cxt);
			if(senderId != null) {
				GCMRegistrar.register(cxt.getApplicationContext(), senderId);
			} else {
				YRDLog.e(YRDPushManager.class, "Unable to register with GCM Sender ID was null");
			}
		} else {
			YRDLog.i(YRDPushManager.class, "Already GCM registered: "
					+ registrationId);
			storeId(cxt, registrationId, TYPE_GCM);
		}
	}

	private static void runADMReg(final Context cxt) {
		final ADM adm = new ADM(cxt);
		final String registrationId;
		// Nexus 4 shows checkbox to receive push messages in app preferences
		// NOTE: check older devices for this setting
		registrationId = adm.getRegistrationId();

		if (registrationId == null) {
			// startRegister() is asynchronous; your app is notified via the
			// onRegistered() callback when the registration ID is available.
			adm.startRegister();
		} else {
			YRDLog.i(YRDPushManager.class, "Already ADM registered: "
					+ registrationId);
			storeId(cxt, registrationId, TYPE_ADM);
		}
	}

	public static void storeId(final Context cxt, String id, String type) {
		try {
			if (cxt != null) {
				if (id != null && id.length() > 0) {
					YRDLog.i(YRDPushManager.class, "Store Token: " + id);
					YRDAnalytics.getInstance().setPushToken(id);
					YRDAnalytics.getInstance().setPushTokenType(type);
				} else {
					YRDLog.i(YRDPushManager.class, "Store Token: NULL or Empty");
					YRDAnalytics.getInstance().setPushToken(null);
					YRDAnalytics.getInstance().setPushTokenType(null);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public static void configureLocalMessages(Activity act, boolean enabeld,
			boolean sound) {
		SharedPreferences prefs = act.getApplicationContext()
				.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putBoolean(PREF_ENABLE_LOCAL, enabeld);
		edit.putBoolean(PREF_ENABLE_LOCAL_SOUND, sound);
		edit.putString(PREF_CLASS_KEY, act.getClass().getName());
		edit.commit();
	}

	public static boolean getPushMessagesEnabled(Context cxt) {
		SharedPreferences prefs = cxt.getSharedPreferences(PREF_NAME,
				Context.MODE_PRIVATE);
		boolean flag = prefs.getBoolean(PREF_ENABLE_LOCAL, true);
		return flag;
	}

	public static boolean getPushMessagesSoundEnabled(Context cxt) {
		SharedPreferences prefs = cxt.getSharedPreferences(PREF_NAME,
				Context.MODE_PRIVATE);
		boolean flag = prefs.getBoolean(PREF_ENABLE_LOCAL_SOUND, true);
		return flag;
	}

	public static Notification buildNotification(Context context,
			PendingIntent intent, int icon, String title, String message,
			long when, MessageID messageId) {
		Notification notification;
		int defaults = Notification.DEFAULT_LIGHTS
				| Notification.DEFAULT_VIBRATE;
		if (YRDPushManager.getPushMessagesSoundEnabled(context))
			;
		defaults |= Notification.DEFAULT_SOUND;

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			notification = buildNotificationGingerbread(context, intent, icon,
					title, message, when, defaults);
		} else {
			notification = buildNotificationHoneycomb(context, intent, icon,
					title, message, when, defaults);
		}
		return notification;
	}

	@TargetApi(10)
	@SuppressWarnings("deprecation")
	private static Notification buildNotificationGingerbread(Context context,
			PendingIntent intent, int icon, String title, String message,
			long when, int defaults) {
		if(icon == 0)
			icon = android.R.drawable.ic_menu_info_details;
		Notification notification = new Notification(icon, message, when);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults = defaults;
		return notification;
	}

	@SuppressWarnings("deprecation")
	@TargetApi(11)
	private static Notification buildNotificationHoneycomb(Context context,
			PendingIntent intent, int icon, String title, String message,
			long when, int defaults) {
		if(icon == 0)
			icon = android.R.drawable.ic_menu_info_details;
		Notification notification = new Notification.Builder(context)
				.setContentTitle(title).setContentText(message)
				.setSmallIcon(icon).setContentIntent(intent)
				.setAutoCancel(true).setDefaults(defaults).getNotification();
		return notification;
	}

	public static void onDestroy(Context cxt) {
		GCMRegistrar.onDestroy(cxt);
	}
}
