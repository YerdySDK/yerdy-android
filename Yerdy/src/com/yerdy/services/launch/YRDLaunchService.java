package com.yerdy.services.launch;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;

import com.yerdy.services.Yerdy;
import com.yerdy.services.core.YRDClient;
import com.yerdy.services.core.YRDPersistence;
import com.yerdy.services.core.YRDService;
import com.yerdy.services.install.InstallReceiver;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.lvl.LVLManager;
import com.yerdy.services.util.DigestUtil;
import com.yerdy.services.util.HTTPRequestData;
import com.yerdy.services.util.UDIDUtil;
import com.yerdy.services.util.YerdyUtil;

/**
 * Service implementation to send launch data to server
 * @author m2, Chris
 */
public class YRDLaunchService extends YRDService {
	/**
	 * Sends launch report to server
	 * 
	 * @param cxt - Activity context
	 * @param token - Push Message token
	 * @param tokenType - Push Message token type (Google / Amazon)
	 * @param info - extra query key/value parameters not explicitly
	 * @param launchClient - 
	 */
	public void reportLaunch(Context cxt, String token, String tokenType, Map<String, ? extends Object> info, YRDLaunchClient launchClient) {
		String udid = YerdyUtil.getUDID(cxt.getApplicationContext());
		Uri.Builder builder = getStandardURIBuilder(cxt);
		builder.appendQueryParameter("publisherid", Yerdy.getInstance().getPublisherKey());
		builder.appendQueryParameter("bundleid", Yerdy.getInstance().getAppPackage() + "." + Yerdy.getInstance().getPlatform().getName());
		builder.appendQueryParameter("deviceid", udid);
		builder.appendQueryParameter("v", Yerdy.getInstance().getAppVersion());
		if (null != token) {
			builder.appendQueryParameter("token", token);
			if(tokenType != null)
				builder.appendQueryParameter("token_type", tokenType);
		}
		builder.appendQueryParameter("nid", YerdyUtil.getNID(cxt));
		builder.appendQueryParameter("timezone", YerdyUtil.getTimezone());
		builder.appendQueryParameter("type", YerdyUtil.getHardware());
		builder.appendQueryParameter("os", YerdyUtil.getOS());
		//builder.appendQueryParameter("country", YerdyUtil.getCountry(cxt));
		builder.appendQueryParameter("language", YerdyUtil.getLanguage());
		for (Entry<String, ? extends Object> entry : info.entrySet()) {
			builder.appendQueryParameter(entry.getKey(), entry.getValue().toString());
		}
		builder.appendQueryParameter("src", LVLManager.getInstance().getStatus().getKey());
		builder.appendQueryParameter("fmt", "json");
		Uri uri = builder.build();

		executeWithRequest(cxt, uri, launchClient);
	}

	/* (non-Javadoc)
	 * @see com.yerdy.android.services.service.YRDService#prepConnection(com.yerdy.android.services.service.YRDClient, java.net.URI)
	 */
	@Override
	protected HttpURLConnection prepConnection(YRDClient client, URI javaURI) throws IOException {

		// See if InstallReceiver has any nuggets for us
		YRDPersistence persistence = InstallReceiver.getPersistence(getApplicationContext(),
				Yerdy.getInstance().getAppPackage());

		((YRDLaunchClient) client).setAttemptingInstallationReport(true);
		YRDLog.i(getClass(),"client notified it will attempt installation report was false");

		HashMap<String, Object> postParams = new HashMap<String, Object>();
		// Proceed with InstallReceiver data
		if(client instanceof YRDLaunchClient) {
			JSONObject screenVisits = ((YRDLaunchClient) client).getScreenVisits();
			if(screenVisits != null) {
				Iterator<?> keys = screenVisits.keys();
				while(keys.hasNext()) {
					String key = (String) keys.next();
					int value = screenVisits.optInt(key, 0);
					if(value > 0) {
						postParams.put("nav["+key+"]", value);
					}
				}
			}
			
			Map<String, String> adReports = ((YRDLaunchClient) client).getAdPerformance();
			if(adReports != null) {
				postParams.putAll(adReports);
			}
		}
		

		HttpURLConnection conn = (HttpURLConnection) javaURI.toURL().openConnection();
		byte[] postMessage = null;
		if (persistence.hasKey(InstallReceiver.INSTALL_REFERER)) {
			postParams.put("ad_referer", persistence.getValue(InstallReceiver.INSTALL_REFERER, "null_referrer_found"));
			postParams.put("ad_app", getPackageName());
			postParams.put("ad_udid", DigestUtil.asBase64(UDIDUtil.getDeviceId(getApplicationContext())));
			postParams.put("ad_aid", DigestUtil.asBase64(UDIDUtil.getAndroidID(getApplicationContext())));
		}
		
		if(postParams.size() > 0) {
			postMessage = HTTPRequestData.convertToBytes(postParams);
			YRDLog.i(getClass(), "postMessage:" + new String(postMessage));
			
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Language", "en-US");
		}
		
		String requestAuth = null;
		try {
			requestAuth = generateHmac(javaURI);
			conn.setRequestProperty("X-Request-Auth", requestAuth);
		} catch (Exception e) {
			YRDLog.e(getClass(), "Failed to generate HMAC abandoning call");
			return null;
		}

		// Unable to determine why the following causes issues, needs to be left disabled for now
		// conn.setRequestProperty("Content-Length", Integer.toString(postMessage.length));

		if(postMessage != null) {
			conn.setDoOutput(true);
			conn.setChunkedStreamingMode(0);
			conn.connect();
			BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());
			out.write(postMessage);
			out.close();
		} else {
			conn.connect();
		}

		return conn;
	}
	
	/* (non-Javadoc)
	 * @see com.yerdy.android.services.service.YRDService#getAPIRevision()
	 */
	@Override
	protected String getAPIRevision() {
		return "2";
	}

	/* (non-Javadoc)
	 * @see com.yerdy.android.services.service.YRDService#processResult(java.net.HttpURLConnection, int)
	 */
	@Override
	protected void processResult(HttpURLConnection conn, int responseCode) throws IOException, JSONException {
		YRDLaunchProcessor processor = new YRDLaunchProcessor();
		processor.parseJSON(convertStreamToJson(conn.getInputStream()));
		YRDUserInfo.getInstance(getApplicationContext()).setUserType(processor.getUserType());
		YRDUserInfo.getInstance(getApplicationContext()).setCurrentABTag(processor.getTag());
	}

	/* (non-Javadoc)
	 * @see com.yerdy.android.services.service.YRDService#getPath()
	 */
	@Override
	protected String getPath() {
		return "stats/launch.php";
	}

	/* (non-Javadoc)
	 * @see com.yerdy.android.services.service.YRDService#didFailWithError(com.yerdy.android.services.service.YRDClient, java.lang.Exception)
	 */
	@Override
	protected void didFailWithError(YRDClient client, Exception error, int responseCode) {
		if (null != client) {
			YRDLog.w(getClass(), "didFailWithError: client.launchReportFailed();");
			((YRDLaunchClient) client).launchReportFailed(error);
		} else {
			YRDLog.w(getClass(), "didFailWithError: client == null");
		}
	}

	/* (non-Javadoc)
	 * @see com.yerdy.android.services.service.YRDService#didFinishLoadingWithResult(com.yerdy.android.services.service.YRDClient, java.net.HttpURLConnection)
	 */
	@Override
	protected void didFinishLoadingWithResult(YRDClient client, HttpURLConnection conn) {
		if (null != client) {
			if (((YRDLaunchClient) client).isAttemptingInstallationReport()) {
				YRDPersistence persistence = InstallReceiver.getPersistence(getApplicationContext(),
						Yerdy.getInstance().getAppPackage());

				// Submitted successful
				persistence.clear();
				persistence.save();
			}

			((YRDLaunchClient) client).launchReported();
		} else {
			YRDLog.w(getClass(), "didFinishLoadingWithResult: client == null");
		}

	}


}
