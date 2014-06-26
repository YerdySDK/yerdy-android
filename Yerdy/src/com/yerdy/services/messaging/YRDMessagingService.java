package com.yerdy.services.messaging;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.net.Uri;

import com.yerdy.services.Yerdy;
import com.yerdy.services.core.YRDClient;
import com.yerdy.services.core.YRDService;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.util.YerdyUtil;

/**
 * HTTP service that queries server for message to present to user via GUI
 * 
 * @author m2
 * 
 */
public class YRDMessagingService extends YRDService {

	List<YRDMessage> _messages;

	public void fetchMessages(Context cxt, YRDMessagingClient client) {
		Uri.Builder builder = getStandardURIBuilder(cxt);
		builder.appendQueryParameter("deviceid", YerdyUtil.getUDID(cxt));
		builder.appendQueryParameter("bundleid", Yerdy.getInstance().getAppPackage() + "." + Yerdy.getInstance().getPlatform().getName());
		builder.appendQueryParameter("publisherid", Yerdy.getInstance().getPublisherKey());
		builder.appendQueryParameter("v", Yerdy.getInstance().getAppVersion());
		builder.appendQueryParameter("view", "0");
		builder.appendQueryParameter("fmt", "json");

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
	protected void processResult(HttpURLConnection conn, int responseCode) throws IOException, JSONException {
		YRDMessageProcessor processor = new YRDMessageProcessor();
		processor.parseJSON(convertStreamToJson(conn.getInputStream()));
		_messages = processor.getMessages();
	}

	@Override
	protected String getPath() {
		return "app_messages/message.php";
	}

	@Override
	protected void didFailWithError(YRDClient client, Exception error, int responseCode) {
		if (null != client) {
			((YRDMessagingClient) client).onError(error);
		}
	}
	
	@Override
	protected String getAPIRevision() {
		return "2";
	}

	@Override
	protected void didFinishLoadingWithResult(YRDClient client, HttpURLConnection conn) {
		if (null == client) {
			return;
		}
		
		List<YRDMessage> data = new ArrayList<YRDMessage>();
		if(_messages != null) {
			for(YRDMessage msg : _messages) {
				data.add(msg);
			}
		}
		((YRDMessagingClient) client).onSuccess(data);
	}

}
