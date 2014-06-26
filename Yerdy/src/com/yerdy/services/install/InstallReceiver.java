package com.yerdy.services.install;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yerdy.services.core.YRDPersistence;
import com.yerdy.services.logging.YRDLog;

/**
 * Provide an intent handler for intents of type
 * com.android.vending.INSTALL_REFERRER. Receiver most likely doesn't have
 * access to the full Application context (it won't be running) and will simply
 * need to capture the data for when the Application IS running
 * 
 */
public class InstallReceiver extends BroadcastReceiver {

	public static final CharSequence INSTALL_SUBMITTED = "INSTALL_SUBMITTED";
	public static final CharSequence INSTALL_REFERER = "INSTALL_REFERER";

	public static YRDPersistence getPersistence(Context context, String packageName) {
		return new YRDPersistence(context, packageName + ".InstallReceiver", false);
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		String referrer = "";
		try {
			referrer = intent.getStringExtra("referrer");
			if (referrer == null) {
				referrer = "null_referrer_found";
			}
		} catch (Exception e) {
			referrer = "exception_found_retrieving_referrer";
		}

		// MdotM example code
		/*
		 * String deviceId = "0"; try { final TelephonyManager telephonyManager
		 * = (TelephonyManager) context
		 * .getSystemService(Context.TELEPHONY_SERVICE); deviceId =
		 * telephonyManager.getDeviceId(); if (deviceId == null) deviceId = "0";
		 * } catch (Exception e) { deviceId = "0"; }
		 * 
		 * String androidId = "0";
		 * 
		 * try { androidId =
		 * Settings.Secure.getString(context.getContentResolver(),
		 * Settings.Secure.ANDROID_ID); if (androidId == null) androidId = "0";
		 * } catch (Exception e) { androidId = "0"; }
		 */
		Context applicationContext = context.getApplicationContext();
		if (applicationContext != null) {
			String packageName = applicationContext.getPackageName();
			YRDPersistence persistence = getPersistence(context, packageName);

			boolean submitted = persistence.getValue(INSTALL_SUBMITTED, false);
			if (!submitted) {
				persistence.setValue(INSTALL_REFERER, referrer);
				persistence.save();
			}
			YRDLog.v(getClass(), " referer:" + referrer + "previously reported" + submitted);
		}

		// MdotM example code
		/*
		 * String postBackUrl =
		 * "http://ads.mdotm.com/ads/receiver.php?referrer=" +
		 * URLEncoder.encode(referrer) + "&package=" +
		 * URLEncoder.encode(packageName) + "&deviceid=" +
		 * URLEncoder.encode(deviceId) + "&androidid=" +
		 * URLEncoder.encode(androidId); new Thread() { public void run() { try
		 * { HttpClient httpClient = new DefaultHttpClient(); HttpGet httpGet =
		 * new HttpGet(postBackUrl); httpClient.execute(httpGet); } catch
		 * (Exception e) { return; } } }.start();
		 */
	}
}
