package com.yerdy.services.push.gcm;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class GCMBroadcastRecieverEx extends GCMBroadcastReceiver {

	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		return "com.yerdy.services.push.gcm.GCMIntentService";
	}
}
