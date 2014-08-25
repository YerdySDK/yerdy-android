package com.yerdy.services.core;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;

import com.yerdy.services.Yerdy;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.purchases.YRDResultProcessor;
import com.yerdy.services.util.YerdyUtil;

public class YRDCounterService extends YRDService {

	int resultCode;
	
	/**
	 * Report a virtual purchase back to server.
	 * 
	 * @param cxt application context
	 * @param eventName
	 * @param eventJson
	 * @param client
	 */
	public void reportCustomEvent(Context cxt, String eventName, JSONObject eventJson, YRDCounterClient client) {
		//String name, String parameterName, String bucketName, int bucketAmount;
		Uri.Builder builder = getBuilder(cxt, eventName, "custom");
		if(eventJson != null && eventJson.length() > 0) {
			builder.appendQueryParameter(String.format("idx[%s]", eventName), "0");
			builder.appendQueryParameter(String.format("mod[%s]", eventName), Integer.toString(eventJson.optInt("mod")));

			JSONObject paramJson = eventJson.optJSONObject("params");
			if(paramJson != null && paramJson.length() > 0)
			{
				Iterator<?> paramNames = paramJson.keys();
				while(paramNames.hasNext()) {
					String paramName = (String) paramNames.next();
					JSONObject bucketJson = paramJson.optJSONObject(paramName);
					
					if(bucketJson != null && bucketJson.length() > 0) {
						Iterator<?> bucketNames = bucketJson.keys();
						int bucketIndex = 0;
						while(bucketNames.hasNext()) {
							String bucketName = (String) bucketNames.next();
							String bucketCount = Integer.toString(bucketJson.optInt(bucketName, 0));
							
							builder.appendQueryParameter(String.format("idx[%s][%d]", paramName, bucketIndex), bucketName);
							builder.appendQueryParameter(String.format("mod[%s][%d]", paramName, bucketIndex), bucketCount);
							bucketIndex++;
						}
					}
				}
			}
		}

		Uri uri = builder.build();
		executeWithRequest(cxt, uri, client);
	}

	public void reportTimedMilestone(Context cxt, int index, JSONObject currency, int virtualPurchases, int launches, YRDCounterClient client) {
		Uri.Builder builder = getBuilder(cxt, "game-"+index, "time");
		builder.appendQueryParameter(String.format("idx[%s]", "game-"+index), "0");

		//Will generate param[earned-1] = value as needed
		Iterator<?> currencyKeys = currency.keys();
		while(currencyKeys.hasNext()) {
			String currencyKey = (String) currencyKeys.next();
			String currencyValue = currency.optString(currencyKey,"0");
			builder.appendQueryParameter("idx["+currencyKey+"]", "0");
			builder.appendQueryParameter("mod["+currencyKey+"]", currencyValue);
		}
		
		builder.appendQueryParameter("idx[launch_count]", "0");
		builder.appendQueryParameter("mod[launch_count]", Integer.toString(launches));

		//Amount of virtual good purchases conversion to index
		String vpgValue = null;
		if(virtualPurchases <= 0) {//0 = 0
			vpgValue = "0";
		} else if(virtualPurchases <= 5) {//1 = 1-5
			vpgValue = "1";
		} else if(virtualPurchases <= 10) {//2 = 6-10
			vpgValue = "2";
		} else if(virtualPurchases <= 20) {//3 = 11-20
			vpgValue = "3";
		} else {//4 = 21+
			vpgValue = "4";
		}
		if(vpgValue != null)
			builder.appendQueryParameter("idx[vgp]", vpgValue);

		Uri uri = builder.build();
		executeWithRequest(cxt, uri, client);
	}
	
	public void reportPlayerProgressionOrFeature(Context cxt, JSONObject milestones, String type, YRDCounterClient client) {
		Iterator<?> groupKeys = milestones.keys();
		while(groupKeys.hasNext()) {
			String groupKey = (String) groupKeys.next();
			Uri.Builder builder = getBuilder(cxt, groupKey, type);
			JSONObject groupjson = milestones.optJSONObject(groupKey);
			
			if(groupjson != null) {
				Iterator<?> eventKeys = groupjson.keys();
				int itemCounter = 0;
				while(eventKeys.hasNext()) {
					String eventKey = (String) eventKeys.next();
					JSONObject eventJson = groupjson.optJSONObject(eventKey);
					builder.appendQueryParameter(String.format("idx[%s][%d]", groupKey, itemCounter), eventKey);
					builder.appendQueryParameter(String.format("mod[%s][%d]", groupKey, itemCounter), eventJson.optString("counter", "0"));
					builder.appendQueryParameter(String.format("idx[playtime][%d]", itemCounter), eventKey);
					long playtime = eventJson.optLong("playtime", 0l);
					playtime /= 1000l;
					String playtimeStr = Integer.toString(Math.round(playtime));
					builder.appendQueryParameter(String.format("mod[playtime][%d]", itemCounter), playtimeStr);
					builder.appendQueryParameter(String.format("idx[launch_count][%d]", itemCounter), eventKey);
					builder.appendQueryParameter(String.format("mod[launch_count][%d]", itemCounter), eventJson.optString("launches", "0"));
					itemCounter++;
				}
			}
			Uri uri = builder.build();
			executeWithRequest(cxt, uri, client);
		}
	}
	
	private Uri.Builder getBuilder(Context cxt, String name, String type) {
		Uri.Builder builder = getStandardURIBuilder(cxt);
		String udid = YerdyUtil.getUDID(cxt.getApplicationContext());
		builder.appendQueryParameter("publisherid", Yerdy.getInstance().getPublisherKey());
		builder.appendQueryParameter("bundleid", Yerdy.getInstance().getAppPackage() + "." + Yerdy.getInstance().getPlatform().getName());
		builder.appendQueryParameter("deviceid", udid);
		builder.appendQueryParameter("v", Yerdy.getInstance().getAppVersion());
		builder.appendQueryParameter("name", name);
		builder.appendQueryParameter("fmt", "json");
		builder.appendQueryParameter("type", type);
		return builder;
	}
	
	@Override
	protected HttpURLConnection prepConnection(YRDClient client, URI javaURI) throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) javaURI.toURL().openConnection();
		
		String requestAuth = null;
		try {
			requestAuth = generateHmac(javaURI);
		} catch (Exception e) {
			YRDLog.e(getClass(), "Failed to generate HMAC abandoning call");
			return null;
		}
		
		conn.setRequestProperty("X-Request-Auth", requestAuth);
		conn.connect();

		return conn;
	}

	@Override
	protected String getAPIRevision() {
		return "3";
	}

	@Override
	protected void processResult(HttpURLConnection conn, int responseCode) throws IOException, JSONException {
		YRDResultProcessor processor = new YRDResultProcessor();
		processor.parseJSON(convertStreamToJson(conn.getInputStream()));
		resultCode = processor.getResultCode();
	}

	@Override
	protected String getPath() {
		return "stats/trackCounter.php";
	}

	@Override
	protected void didFailWithError(YRDClient client, Exception error, int responseCode) {
		if (null != client) {
			((YRDCounterClient) client).counterServiceFailed(error, responseCode);
		}
	}

	@Override
	public void didFinishLoadingWithResult(YRDClient client, HttpURLConnection conn) {
		if (null != client) {
			((YRDCounterClient) client).counterServiceSucceeded(resultCode);
		}
	}
}
