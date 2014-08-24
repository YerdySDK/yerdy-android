package com.yerdy.services.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import com.yerdy.services.Yerdy;
import com.yerdy.services.launch.YRDUserInfo;
import com.yerdy.services.logging.YRDLog;

/**
 * Base class for Services that discuss data via http. We use explicit Intents
 * for our Services, this allows up to bypass Intent filters Android manifest
 * must include: <uses-permission android:name="android.permission.INTERNET"/>
 * 
 * @author m2
 * 
 */
abstract public class YRDService extends IntentService {

	private static final String EXTRA_CLIENT = "extra_client";
	private static final String CRYPTO_ALG = "HmacSHA1";
	private static final String CRYPTO_ENC = "UTF-8";
	
	// Base URL should end in '/'
	// 	Production server: http://services.yerdy.com/
	// 	Internal test server:  http://10.189.165.237/~michal/
	// 	Klaus test server: http://10.189.165.104/~krubba/FluikServices/httpdocs/
	// 	Klaus test server (Mac mini): http://10.189.165.234/~user/FluikServices/httpdocs/
	// 	Darren test server: http://10.189.165.207/~darrenclark/FluikServices/httpdocs/
	private static final String ROOT = "http://services.yerdy.com/";
	
	public YRDService() {
		super("YerdyService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		onHandleIntentAttempt(intent, 0);
	}

	private void onHandleIntentAttempt(Intent intent, int attempt) {
		Uri androidUri = intent.getData();
		long clientLookup = intent.getLongExtra(EXTRA_CLIENT, 0L);
		boolean success = false;
		int responseCode = -1;
		HttpURLConnection conn = null;

		YRDClient client = null;
		if (0L != clientLookup) {
			client = YRDClient.doLookup(clientLookup);
			if (null == client) {
				YRDLog.w(getClass(),
						"Make sure NEW client is provided to Service every time intent is called.");
			}
		}

		YRDLog.v(getClass(), "Handling intent for " + androidUri);
		try {
			// This should occur on the "YerdyService" messaging thread
			// --as such we will never dispatch more than one Service call of
			// any kind per second
			Thread.sleep(1000);

			conn = prepConnection(client, new URI(androidUri.toString()));
			responseCode = conn.getResponseCode();
		} catch (Exception e) {
			// Network retry
			if (attempt < 3) {
				try {
					e.printStackTrace();
					Thread.sleep(500);
				} catch (InterruptedException e1) {

				}
				onHandleIntentAttempt(intent, (attempt + 1));
				return;
			}
		}

		if (responseCode == HttpURLConnection.HTTP_OK) {
			try {
				processResult(conn, responseCode);
				success = true;
			} catch (Exception e) {
				YRDLog.e(this.getClass(), "Failed to parse server response");
				e.printStackTrace();
			}
		}

		try {
			if (success) {
				didFinishLoadingWithResult(client, conn);
			} else {
				errorOut(client, new Exception("HTTP Reponse Code: "
						+ responseCode), responseCode);
			}
		} catch (Exception e) {
			// TODO: Silently fail with exception
		}

		if (0L != clientLookup) {
			YRDClient.clean(clientLookup);
		}
	}

	void errorOut(YRDClient client, Exception error, int responseCode) {
		String errorMessage = "";
		if (null != error) {
			errorMessage = error.getLocalizedMessage();
		}

		YRDLog.e(getClass(), error.getClass().getSimpleName() + " : "
				+ errorMessage);
		didFailWithError(client, error, responseCode);

	}

	/**
	 * Convenience method to provide a URI builder to Subclasses with root path,
	 * and normalized variables prepopulated
	 * 
	 * @return
	 */
	protected Uri.Builder getStandardURIBuilder(Context cxt) {
		Uri.Builder builder = Uri.parse(ROOT).buildUpon();
		builder.appendEncodedPath(getPath());

		String api = getAPIRevision();
		if (api.length() > 0) {
			builder.appendQueryParameter("api", api);
		}

		if (YRDUserInfo.getInstance(cxt).isInTest()) {
			builder.appendQueryParameter("tag", YRDUserInfo.getInstance(cxt)
					.getCurrentABTag());
		}

		return builder;
	}

	/**
	 * Reports the API revision services should be using so that the server is
	 * able to better interpret what the client is trying to say
	 * 
	 * @return an int representing the API protocol revision
	 */
	protected String getAPIRevision() {
		return "";
	}

	/**
	 * Fork off service onto worker thread and apply uri as endpoint
	 * 
	 * @param uri
	 *            Uri this service communicates with. getActivityRoot() and
	 *            getPath() should be applied to this URI
	 * 
	 */
	public void executeWithRequest(Context cxt, Uri uri, YRDClient client) {
		long clientLookup = 0;
		if (null != client) {
			clientLookup = client.getLookupValue();
		} else {
			YRDLog.w(getClass(), "executeWithRequest has null client");
		}

		YRDLog.i(getClass(), "Preparing intent for " + uri.toString());
		// Map intent as "property of rootActivity"
		Intent i = new Intent(cxt.getApplicationContext(), getClass());
		i.setPackage(Yerdy.getInstance().getAppPackage());
		i.setData(uri);
		i.putExtra(EXTRA_CLIENT, clientLookup);
		ComponentName response = cxt.getApplicationContext().startService(i);
		if (response == null) {
			YRDLog.wtf(getClass(),
					"Unable to start service, Please check your manifest");
		}
	}

	/**
	 * Convenience method for key generation to return a String consisting of
	 * the first 6 characters in a passed String
	 * 
	 * @param inString
	 * @return
	 */
	protected String first6(String inString) {
		if (inString.length() < 6) {
			return inString;
		}
		return inString.substring(0, 6);
	}

	/**
	 * Convenience method for key generation to return a String consisting of
	 * the last 6 characters in a passed String
	 * 
	 * @param inString
	 * @return
	 */
	protected String last6(String inString) {
		if (inString.length() < 6) {
			return inString;
		}
		return inString.substring(inString.length() - 6);
	}

	/**
	 * Subclasses should override. This method is for consistency with FLIP
	 * 
	 * @param client
	 *            Handles behaviours of the service
	 * @param error
	 *            Best explanation of what happened, not this may be null
	 */
	protected abstract void didFailWithError(YRDClient client, Exception error,
			int responseCode);

	/**
	 * Subclasses should override. This method is for consistency with FLIP
	 * 
	 * @param client
	 *            Handles behaviours of the service
	 * @param conn
	 *            Apply result with something like this:
	 *            <code>InputStream is = conn.getInputStream(); 
	 *            BufferedInputStream bis = new BufferedInputStream(is, 8192);</code>
	 *            and close is and bis when done
	 */
	abstract protected void didFinishLoadingWithResult(YRDClient client,
			HttpURLConnection conn);

	/**
	 * Subclasses *may* override. This method is for consistency with FLIP
	 * 
	 * @param conn
	 *            Apply result with something like this:
	 *            <code>InputStream is = conn.getInputStream(); 
	 *            BufferedInputStream bis = new BufferedInputStream(is, 8192);</code>
	 *            and close is and bis when done
	 * @param responseCode
	 *            Connection response code. Code extracted without subclasses
	 *            having to handle IOException
	 */
	protected void processResult(HttpURLConnection conn, int responseCode)
			throws Exception {
	}

	/**
	 * If Connection needs to be Authenticated or changed to a POST or similar,
	 * do it here. Typically, this does nothing
	 * 
	 * @param client
	 * 
	 * @param conn
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	protected HttpURLConnection prepConnection(YRDClient client, URI javaURI)
			throws MalformedURLException, IOException {
		HttpURLConnection conn = (HttpURLConnection) javaURI.toURL()
				.openConnection();
		conn.connect();

		return conn;
	}

	/**
	 * 
	 * @return portion of the path an activity applies when called
	 */
	protected abstract String getPath();

	protected JSONObject convertStreamToJson(InputStream is)
			throws UnsupportedEncodingException, IOException, JSONException {
		BufferedReader streamReader = new BufferedReader(new InputStreamReader(
				is, "UTF-8"));
		StringBuilder responseStrBuilder = new StringBuilder();

		String inputStr;
		while ((inputStr = streamReader.readLine()) != null)
			responseStrBuilder.append(inputStr);
		return new JSONObject(responseStrBuilder.toString());
	}

	protected String generateHmac(URI javaUri) throws Exception {
		String val = javaUri.getRawPath() + javaUri.getRawQuery();
		YRDLog.i("HMAC", "Val: " + val);
		return generateHmac(val.getBytes(CRYPTO_ENC));
	}

	protected String generateHmac(byte[] val) throws Exception {
		String secret = Yerdy.getInstance().getPublisherSecret();
		YRDLog.i("HMAC", "Bytes: " + val);
		String derived = null;
		SecretKeySpec key = new SecretKeySpec(secret.getBytes(CRYPTO_ENC),
				CRYPTO_ALG);
		Mac hmac = Mac.getInstance(CRYPTO_ALG);
		hmac.init(key);
		byte[] bytes = hmac.doFinal(val);
		derived = new String(Base64.encode(bytes, Base64.DEFAULT), CRYPTO_ENC);
		YRDLog.i("HMAC", "derived: " + derived);
		return derived.trim();
	}

}
