package com.yerdy.services.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;

public class YerdyUtil {


	/**
	 * @return unique device identifier
	 */
	public static String getUDID(Context cxt) {
		if(cxt == null)
			return "";
		return UDIDUtil.getUDIDNew(cxt);
	}
	
	public static String getUDID() {
		return getUDID(UnityPlayer.currentActivity);
	}

	/**
	 * 
	 * @return unique network identifier (Mac Address)
	 */
	public static String getNID(Context cxt) {
		if(cxt == null)
			return "";
		return Base64.encodeToString(UDIDUtil.getMacAddress(cxt).getBytes(), Base64.DEFAULT).trim();
	}

	public static String getOS() {
		return "Android OS " + Build.VERSION.RELEASE;
		/* return System.getProperty("os.name"); // Should return linux */
	}

	public static String getHardware() {
		return Build.MANUFACTURER.toLowerCase(Locale.getDefault()) + ' ' + Build.MODEL.toLowerCase(Locale.getDefault());
	}

	/**
	 * 
	 * @return String representation of device timezone as per RFC 822
	 */
	public static String getTimezone() {
		return new SimpleDateFormat("Z").format(new Date());
	}

	/**
	 * @return String representation of device Language code as per ISO 639
	 */
	public static String getLanguage() {
		return Locale.getDefault().getLanguage();
	}

	/**
	 * Convenience method to send the root activity into the background
	 * 
	 * @return true if the request was successful
	 */
	public static boolean backgroundRoot(Activity act) {
		if(act == null)
			return false;
		return act.moveTaskToBack(true);
	}
	
	public static boolean backgroundRoot() {
		return backgroundRoot(UnityPlayer.currentActivity);
	}

	public static boolean networkUnreachable(Context cxt) {
		ConnectivityManager connectivityManager = (ConnectivityManager) cxt.getSystemService(
				Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (null == activeNetworkInfo) {
			return true;
		}
		return !activeNetworkInfo.isConnected();
	}
}
