package com.yerdy.services.messaging;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;

import android.content.Context;
import android.net.Uri;

import com.yerdy.services.core.YRDClient;
import com.yerdy.services.core.YRDService;
import com.yerdy.services.logging.YRDLog;

/**
 * This is a fire and forget HTTP GET service, used primarily for reporting stats back to server
 * 
 * @author m2
 * 
 */
public class YRDMessageReportService extends YRDService {

	public void reportAtURI(Context cxt, Uri uri) {
		executeWithRequest(cxt, uri, null);
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
	protected String getPath() {
		return null;
	}

	@Override
	protected void didFailWithError(YRDClient client, Exception error, int responseCode) {
		// do nothing		
	}

	@Override
	protected void didFinishLoadingWithResult(YRDClient client, HttpURLConnection conn) {
		// do nothing
		
	}
}
