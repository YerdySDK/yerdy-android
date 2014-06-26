package com.yerdy.services.purchases;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;

import org.json.JSONException;

import android.content.Context;
import android.net.Uri;

import com.yerdy.services.Yerdy;
import com.yerdy.services.core.YRDClient;
import com.yerdy.services.core.YRDService;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.util.YerdyUtil;

public class YRDReportVirtualPurchaseService extends YRDService {

	int resultCode;

	/**
	 * Report a virtual purchase back to server.
	 * 
	 * @param itemIdentifier
	 *            type of virtual item purchased
	 * @param currency
	 *            number of virtual items purchased
	 * @param isFirstPurchase
	 *            flag indicating this is user's first time spending money
	 * @see 
	 */
	public void reportVirtualPurchase(Context cxt, String itemIdentifier, YRDCurrencyReport currencyReport, boolean isFirstPurchase, int postIapIndex,
					int messageId, boolean onSale, YRDReportVirtualPurchaseClient client) {

		Uri.Builder builder = getStandardURIBuilder(cxt);
		String udid = YerdyUtil.getUDID(cxt.getApplicationContext());
		builder.appendQueryParameter("publisherid", Yerdy.getInstance().getPublisherKey());
		builder.appendQueryParameter("bundleid", Yerdy.getInstance().getAppPackage() + "." + Yerdy.getInstance().getPlatform().getName());
		builder.appendQueryParameter("deviceid", udid);
		builder.appendQueryParameter("v", Yerdy.getInstance().getAppVersion());
		builder.appendQueryParameter("itemid", itemIdentifier);

		builder.appendQueryParameter("currency", currencyReport.getTransactionAmount());
		builder.appendQueryParameter("fmt", "json");
		if (isFirstPurchase) {
			builder.appendQueryParameter("first", "1");
		}
		if (postIapIndex > 0) {
			builder.appendQueryParameter("indexiap", Integer.toString(postIapIndex));
		}
		if (messageId >= 0)
			builder.appendQueryParameter("msgid", Integer.toString(messageId));
		if (onSale)
			builder.appendQueryParameter("sale", "1");
			

		Uri uri = builder.build();
		executeWithRequest(cxt, uri, client);
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
		return "stats/trackVirtualPurchase.php";
	}

	@Override
	protected void didFailWithError(YRDClient client, Exception error, int responseCode) {
		if (null != client) {
			((YRDReportVirtualPurchaseClient) client).saveVirtualPurchaseServiceFailed(error, responseCode);
		}
	}

	@Override
	protected void didFinishLoadingWithResult(YRDClient client, HttpURLConnection conn) {
		if (null != client) {
			((YRDReportVirtualPurchaseClient) client).saveVirtualPurchaseServiceSucceeded(resultCode);
		}
	}

}
