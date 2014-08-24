package com.yerdy.services.purchases;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;

import org.json.JSONException;

import android.content.Context;
import android.net.Uri;

import com.yerdy.services.Yerdy;
import com.yerdy.services.core.YRDClient;
import com.yerdy.services.core.YRDService;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.util.DigestUtil;
import com.yerdy.services.util.HTTPRequestData;
import com.yerdy.services.util.YerdyUtil;

public class YRDReportIAPService extends YRDService {

	int resultCode;

	/**
	 * Track purchase with real currency
	 * 
	 * @param purchaseData
	 *            Meta statistics about player activities when purchase was made
	 * @see https 
	 */
	public void savePurchase(Context cxt, YRDReportIAPClient client) {

		// getStandardURIBuilder calls back to prepConnection
		Uri.Builder builder = getStandardURIBuilder(cxt);
		String udid = YerdyUtil.getUDID(cxt.getApplicationContext());
		PurchaseData purchaseData = ((YRDReportIAPClient) client).getPurchaseData();
		builder.appendQueryParameter("publisherid", Yerdy.getInstance().getPublisherKey());
		builder.appendQueryParameter("bundleid", Yerdy.getInstance().getAppPackage() + "." + Yerdy.getInstance().getPlatform().getName());
		builder.appendQueryParameter("deviceid", udid);
		builder.appendQueryParameter("v", Yerdy.getInstance().getAppVersion());
		builder.appendQueryParameter("os", YerdyUtil.getOS());
		//builder.appendQueryParameter("cc", YerdyUtil.getCountry(getApplicationContext()));
		builder.appendQueryParameter("currency", purchaseData.getProductCurrency().toString());
		builder.appendQueryParameter("value", purchaseData.getProductValue());
		
		if(purchaseData.isOnSale())
			builder.appendQueryParameter("sale", "1");

		
		builder.appendQueryParameter("fmt", "json");
		if(purchaseData.getMessageId() != -1)
			builder.appendQueryParameter("msgid", Integer.toString(purchaseData.getMessageId()));

		Uri uri = builder.build();

		executeWithRequest(cxt, uri, client);
	}

	@Override
	protected HttpURLConnection prepConnection(YRDClient client, URI javaURI) throws IOException {
		HashMap<String, Object> postParams = new HashMap<String, Object>();

		PurchaseData purchaseData = ((YRDReportIAPClient) client).getPurchaseData();

		if (PurchaseData.AMAZON_STORE.equals(purchaseData.getStore())) {
			postParams.put("amazon_receipt", purchaseData.getReceipt());
			postParams.put("amazon_user", purchaseData.getUser());
			postParams.put("amazon_product", purchaseData.getProductID());
		} else if (PurchaseData.GOOGLE_STORE.equals(purchaseData.getStore())) {
			postParams.put("google_receipt_encoded", DigestUtil.asBase64(purchaseData.getReceipt()));
			postParams.put("google_product", purchaseData.getProductID());
			postParams.put("google_signature", purchaseData.getSignature());
		}

		if (purchaseData.isSandboxed()) {
			postParams.put("sandbox", 1);
		}
		postParams.put("launch_count", purchaseData.getLauches());
		postParams.put("playtime", purchaseData.getTotalSecondsPlayed());
		postParams.put("currency", purchaseData.getTotalCurrency());
		postParams.put("currency_earned", purchaseData.getCurrencyEarned());
		postParams.put("currency_bought", purchaseData.getCurrencyPurchased());
		postParams.put("currency_spent", purchaseData.getCurrencySpent());
		postParams.put("items", purchaseData.getTotalItemsPurchased());
		addArrayParams(postParams, "last_nav", purchaseData.getLastScreenVisits());
		addArrayParams(postParams, "last_item", purchaseData.getLastItemPurchases());
		addArrayParams(postParams, "last_msg", purchaseData.getLastMessages());
		addArrayParams(postParams, "last_player_keys", purchaseData.getLastPlayerProgressionCategories());
		addArrayParams(postParams, "last_player_values", purchaseData.getLastPlayerProgressionMilestones());

		HttpURLConnection conn = (HttpURLConnection) javaURI.toURL().openConnection();
		byte[] postMessage = HTTPRequestData.convertToBytes(postParams);

		YRDLog.i(getClass(), "postMessage:" + new String(postMessage));

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Language", "en-US");
		try {
			conn.setRequestProperty("X-Payload-Auth", generateHmac(postMessage));
		} catch (Exception e) {
			YRDLog.e(getClass(), "Failed to generate HMAC Payload abandoning call");
			return null;
		}

		String requestAuth = null;
		try {
			requestAuth = generateHmac(javaURI);
			conn.setRequestProperty("X-Request-Auth", requestAuth);
		} catch (Exception e) {
			YRDLog.e(getClass(), "Failed to generate HMAC Request abandoning call");
			return null;
		}
		
		// This doesn't work. Rather than sorting out why, easier to remove.
		// conn.setRequestProperty("Content-Length",
		// Integer.toString(postMessage.length));
		conn.setDoOutput(true);
		conn.setChunkedStreamingMode(0);

		conn.connect();

		BufferedOutputStream out = new BufferedOutputStream(conn.getOutputStream());

		out.write(postMessage);
		out.close();
		return conn;

	}

	@Override
	protected String getAPIRevision() {
		return "3";
	}

	@Override
	protected void processResult(HttpURLConnection conn, int responseCode) throws IOException, JSONException {
		YRDResultProcessor handler = new YRDResultProcessor();
		handler.parseJSON(convertStreamToJson(conn.getInputStream()));
		resultCode = handler.getResultCode();
	}

	@Override
	protected String getPath() {
		return "stats/trackPurchase.php";
	}

	@Override
	protected void didFailWithError(YRDClient client, Exception error, int responseCode) {
		if (null != client) {
			((YRDReportIAPClient) client).savePurchaseServiceFailed(error, responseCode);
		}
	}

	@Override
	protected void didFinishLoadingWithResult(YRDClient client, HttpURLConnection conn) {
		if (null != client) {
			((YRDReportIAPClient) client).savePurchaseServiceSucceeded(resultCode);
		}
	}

	
	// adds params in form '<key>[<i>]=<value>'
	private void addArrayParams(Map<String, Object> params, String key, List<String> array) {
		if (array == null)
			return;
		
		for (int i = 0; i < array.size(); i++) {
			YRDLog.d(getClass(), "params: " + key +":" + array.get(i));
			params.put(key + "[" + i + "]", array.get(i));
		}
	}
}
