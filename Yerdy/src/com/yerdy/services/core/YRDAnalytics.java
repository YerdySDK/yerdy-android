package com.yerdy.services.core;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.SystemClock;

import com.yerdy.services.Yerdy;
import com.yerdy.services.core.YRDPersistence.AnalyticKey;
import com.yerdy.services.launch.YRDLaunchClient;
import com.yerdy.services.launch.YRDLaunchService;
import com.yerdy.services.launch.YRDUserInfo;
import com.yerdy.services.launch.YRDUserType;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.purchases.PurchaseData;
import com.yerdy.services.purchases.VirtualPurchaseData;
import com.yerdy.services.purchases.YRDCurrencyReport;
import com.yerdy.services.purchases.YRDHistoryTracker;
import com.yerdy.services.purchases.YRDPurchase;
import com.yerdy.services.purchases.YRDReportIAPClient;
import com.yerdy.services.purchases.YRDReportIAPService;
import com.yerdy.services.purchases.YRDReportVirtualPurchaseClient;
import com.yerdy.services.purchases.YRDReportVirtualPurchaseService;
import com.yerdy.services.util.JSONUtil;
import com.yerdy.services.util.YerdyUtil;

public class YRDAnalytics {

	public static final String PLAYER_PROGRESSION_TYPE = "player";
	public static final String FEATURE_MASTERY_TYPE = "feature";
	
	YRDPersistence keychainData;

	YRDLaunchClient launchClient;

	YRDReportIAPService currentSaveService;

	// Synchronized to disk - DO NOT CHANGE EXISTING ENUM VALUES
	private enum ValidationState {
		DEFAULT(0),  // never submitted a purchase
		PENDING(1),  // submitted a purchase, waiting for it to be validated
		VALIDATED(2);  // user has validated at least 1 purchase
		
		private final int idx;
		private ValidationState(int idx) {
			this.idx = idx;
		}
		public int idx() {
			return idx;
		}
	}
	
	//Looks like in certain situations stop may fire before start thus value was 0 which is incorrect
	private long appStart = -1;
	private boolean _activated = false;

	/**
	 * 15 minutes between suspend and resume counts as a new login
	 */
	public static long INTERVAL_FOR_SOFT_RESUME = (15 * 60 * 1000);

	/**
	 * Calculation variables for PurchaseService retries
	 * 
	 * @see 
	 */
	static int currentTry = 1;
	static final int minTry = 1;
	static final int maxTry = 5;
	static final int baseFactor = 8;
	static final long slotTime = 1;

	Timer purchaseServiceTimer = new Timer("PurchaseServiceTimer");

	static boolean countedLaunch = false;
	static boolean countedExit = true;

	String defaultZeroCurrecy;

	// Singleton
	static YRDAnalytics _instance = null;
	private Context _cxt = null;
	private JSONObject _screenVisits = new JSONObject();

	public void setLaunchClient(Context cxt, YRDLaunchClient launchClient) {
		this.launchClient = launchClient;
		_cxt = cxt;
		keychainData = new YRDPersistence(cxt, Yerdy.getInstance().getAppPackage(), true);
	}

	// Singleton
	public static YRDAnalytics getInstance() {
		if(_instance == null)
			_instance = new YRDAnalytics();
		return _instance;
	}

	// Private to enforce singleton
	private YRDAnalytics() {}

	public void logEvent(CharSequence event) {
		keychainData.setValue(event, true);
	}

	public void countEvent(CharSequence event, int count, boolean versionSensitive) {
		// reset version sensitive data if needed
		if (versionMismatch() && versionSensitive) {
			reset();
		}

		int eventCount = keychainData.getValue(event, 0);
		if (eventCount < 0) {
			eventCount = 0;
		}

		keychainData.setValue(event, count + eventCount);
		keychainData.save();
	}

	public boolean versionMismatch() {
		String currentVersion = Yerdy.getInstance().getAppVersion();
		String storedVersion = keychainData.getValue(AnalyticKey.VERSION, "");
		return !currentVersion.equals(storedVersion);
	}

	private void reset() {
		keychainData.setValue(AnalyticKey.VERSION, Yerdy.getInstance().getAppVersion());
		YRDLog.i(getClass(), "Resetting Version sensitive data");
		keychainData.setValue(AnalyticKey.LAUNCHES_VERSIONED, 0);
		keychainData.setValue(AnalyticKey.ENTERS_VERSIONED, 0);
		keychainData.setValue(AnalyticKey.EXITS_VERSIONED, 0);
		keychainData.setValue(AnalyticKey.PURCHASED_ITEMS_VERSIONED, 0);
		keychainData.setValue(AnalyticKey.PLAY_TIME_VERSIONED, 0L);
		keychainData.save();
	}

	public void itemPurchased() {
		countEvent(AnalyticKey.PURCHASED_ITEMS_LIFETIME, 1, false);
		countEvent(AnalyticKey.PURCHASED_ITEMS_VERSIONED, 1, true);
	}

	public void setTotalItemsPurchased(int totalPurchased) {
		keychainData.setValue(AnalyticKey.PURCHASED_ITEMS_LIFETIME, totalPurchased);
		keychainData.save();
	}

	public int getTotalItemsPurchased() {
		return keychainData.getValue(AnalyticKey.PURCHASED_ITEMS_LIFETIME, 0);
	}

	public long getPlaytimeMS(boolean versioned) {
		if(_activated)
			updatePlaytime();
		
		if(versioned)
			return keychainData.getValue(AnalyticKey.PLAY_TIME_VERSIONED, 0L);
		else
			return keychainData.getValue(AnalyticKey.PLAY_TIME_LIFETIME, 0L);
	}

	public long getPlaytimeS(boolean versioned) {
		return getPlaytimeMS(versioned) / 1000L;
	}

	private void updatePlaytime() {
		stopTimer();
		startTimer();
	}

	public void reportLaunch(Context cxt, YRDCurrencyReport currencyReport, Map<String, String> adReport, YRDHistoryTracker historyTracker, boolean isRefresh) {
		YRDLaunchService launchService = new YRDLaunchService();

		final Map<String, Object> info = new HashMap<String, Object>();
		info.put("launches", getLaunches(true));
		
		// if counted launch = true then exit has not yet been called and is
		// waiting for an exit call thus hey are not balanced
		int crashes;
		if (countedLaunch) {
			crashes = getEnters() - getExits() - 1;
		} else {
			crashes = getEnters() - getExits();
		}
		info.put("crashes", crashes);

		// report last feature before crash (if this was a crash)
		if (!isRefresh) {
			int lastReported = keychainData.getValue(AnalyticKey.LAST_REPORTED_CRASH_COUNT, 0);

			if (crashes > lastReported) { // only check greater than, since crashes gets set back to 0 on new version
				if (historyTracker != null) {
					List<String> lastFeatures = historyTracker.getLastFeatureUses();
					if (lastFeatures != null && lastFeatures.size() > 0) {
						info.put("feature_before_crash", lastFeatures.get(0));
					}
				}
			}
			
			lastReported = crashes;
			keychainData.setValue(AnalyticKey.LAST_REPORTED_CRASH_COUNT, lastReported);
		}
		
		if(isRefresh) {
			info.put("refresh", "1");
		}
		
		info.put("playtime", getPlaytimeS(true));
		info.put("currency", currencyReport.getTotals());
		
		JSONObject loggedScreenVisits = keychainData.getJSON(AnalyticKey.SCREEN_VISITS);
		keychainData.deleteKey(AnalyticKey.SCREEN_VISITS);
		keychainData.save();  // also saves LAST_REPORTED_CRASH_COUNT above
		_screenVisits = new JSONObject();
		
		MetaLaunchClient client = new MetaLaunchClient(cxt);
		client.setAdPerformance(adReport);
		client.setScreenVisits(loggedScreenVisits);
		client.setIsRefresh(isRefresh);
		launchService.reportLaunch(cxt, this.getPushToken(), this.getPushTokenType(), info, client);
	}

	public int getLaunches(boolean versioned) {
		if(versioned)
			return keychainData.getValue(AnalyticKey.LAUNCHES_VERSIONED, 0);
		else
			return keychainData.getValue(AnalyticKey.LAUNCHES_LIFETIME, 0);
	}

	public int getExits() {
		return keychainData.getValue(AnalyticKey.EXITS_VERSIONED, 0);
	}

	public int getEnters() {
		return keychainData.getValue(AnalyticKey.ENTERS_VERSIONED, 0);
	}

	public void reportVirtualPurchase(Context cxt, String itemIdentifier, YRDCurrencyReport report, int messageId, boolean onSale) {
		updatePlaytime();

		
		boolean firstPurchase = keychainData.getValue(AnalyticKey.REPORT_FIRST_PURCHASE, true);
		int postIapIndex = keychainData.getValue(AnalyticKey.POST_IAP_INDEX, 0);

		if (firstPurchase) {
			keychainData.setValue(AnalyticKey.REPORT_FIRST_PURCHASE, false);
			keychainData.save();
		}
		
		if (postIapIndex > 0) {
			keychainData.setValue(AnalyticKey.POST_IAP_INDEX, (postIapIndex + 1));
			keychainData.save();
		}

		int count = keychainData.getValue(AnalyticKey.VIRTUAL_PURCHASE_COUNT_TIMED, 0);
		keychainData.setValue(AnalyticKey.VIRTUAL_PURCHASE_COUNT_TIMED, count + 1);
		itemPurchased();
		keychainData.save();
		
		
		VirtualPurchaseData data = new VirtualPurchaseData(
				itemIdentifier,
				report.getTransactionAmount(), 
				firstPurchase,
				postIapIndex,
				messageId,
				onSale);
		
		int validationState = keychainData.getValue(AnalyticKey.PURCHASE_VALIDATION_STATE, ValidationState.DEFAULT.idx());
		// if we are in the middle of validating the first purchase, hold off on submitting
		if (validationState != ValidationState.PENDING.idx()) {
			// not validating the first purchase - submit now
			
			// if we have internet, submit right away.  otherwise, cache it for later.
			if (!YerdyUtil.networkUnreachable(cxt)) {
				uploadVirtualPurchase(cxt, data, new MetaSaveVirtaulPurchaseClient());
			} else {
				YRDLog.i(getClass(), "Saving virtual purchase for reporting later");
				JSONArray unpushedVirtual = keychainData.getJSONArray(AnalyticKey.UNPUSHED_VIRTUAL_PURCHASES);
				try
				{
					unpushedVirtual.put(data.toJSON());
					keychainData.setJSONArray(AnalyticKey.UNPUSHED_VIRTUAL_PURCHASES, unpushedVirtual);
					keychainData.save();
				}
				catch (JSONException ex)
				{
					YRDLog.e(getClass(), "Failed to add cached virtual purchase");
					ex.printStackTrace();
				}
			}
		} else {
			// validating first purchase - hold for later
			YRDLog.i(getClass(), "Saving virtual purchase for reporting later (first IAP is currently being validated)");
			JSONArray pendingVirtual = keychainData.getJSONArray(AnalyticKey.PENDING_VIRTUAL_PURCHASES);
			try
			{
				pendingVirtual.put(data.toJSON());
				keychainData.setJSONArray(AnalyticKey.PENDING_VIRTUAL_PURCHASES, pendingVirtual);
				keychainData.save();
			}
			catch (JSONException ex)
			{
				YRDLog.e(getClass(), "Failed to add pending virtual purchase");
				ex.printStackTrace();
			}
		}
	}
	
	private void uploadVirtualPurchase(Context ctx, VirtualPurchaseData purchaseData, YRDReportVirtualPurchaseClient client) {
		YRDReportVirtualPurchaseService purchaseService = new YRDReportVirtualPurchaseService();		
		purchaseService.reportVirtualPurchase(ctx, 
				purchaseData.getItemIdentifier(),
				purchaseData.getTransactionAmount(), 
				purchaseData.getFirstPurchase(),
				purchaseData.getPostIapIndex(), 
				purchaseData.getMessageId(), 
				purchaseData.getOnSale(),
				client);
	}
	
	public void reportInAppPurchase(Context context, YRDPurchase purchase, YRDCurrencyReport currencyReport, int messageId, YRDHistoryTracker historyTracker) {
		PurchaseData purchaseData = new PurchaseData(purchase, currencyReport);
		if(!purchaseData.isValid()) {
			YRDLog.wtf(this.getClass(), "Attempted to make a purchase report with an invalid YRDpurchase object");
			return;
		}
		
		updatePlaytime();
		List<PurchaseData> values = getPurchasesToReport();
		purchaseData.setTotalSecondsPlayed(getPlaytimeS(false));
		purchaseData.setLaunches(getLaunches(false));
		purchaseData.setTotalItemsPurchased(getTotalItemsPurchased());
		purchaseData.setMessageId(messageId);
		
		purchaseData.setLastFeatureUses(historyTracker.getLastFeatureUses());
		purchaseData.setLastItemPurchases(historyTracker.getLastItemPurchases());
		purchaseData.setLastMessages(historyTracker.getLastMessages());
		purchaseData.setLastPlayerProgressionCategories(historyTracker.getLastPlayerProgressionCategories());
		purchaseData.setLastPlayerProgressionMilestones(historyTracker.getLastPlayerProgressionMilestones());
		purchaseData.setLastFeatureNames(historyTracker.getLastFeatureNames());
		purchaseData.setLastFeatureLevels(historyTracker.getLastFeatureLevels());
		
		values.add(purchaseData);
		writePurchasesToReport(values);
		
		// if they've never made an IAP before, move them over to the 'pending' state (where we hold any future
		// virtual purchases until they move to the 'validated' state
		int validationState = keychainData.getValue(AnalyticKey.PURCHASE_VALIDATION_STATE, ValidationState.DEFAULT.idx());
		if (validationState == ValidationState.DEFAULT.idx()) {
			keychainData.setValue(AnalyticKey.PURCHASE_VALIDATION_STATE, ValidationState.PENDING.idx());
			keychainData.save();
		}
		
		uploadIfNeeded(context);
	}

	private void writePurchasesToReport(List<PurchaseData> values) {
		if (null == values) {
			values = new LinkedList<PurchaseData>();
		}
		keychainData.setValue(AnalyticKey.UNPUSHED_SAVED_PURCHASES_LIST, values);
		keychainData.save();
	}

	private List<PurchaseData> getPurchasesToReport() {
		return keychainData.getValueList(AnalyticKey.UNPUSHED_SAVED_PURCHASES_LIST);
	}

	/**
	 * @param cxt
	 * @param soft
	 * @return returns true if being reported as a launch
	 */
	public boolean appHandleActivate(Context cxt, YRDCurrencyReport currencyReport, Map<String, String> adReport, YRDHistoryTracker historyTracker, boolean soft) {
		_activated = true;
		long now = System.currentTimeMillis();
		long lastBackground = keychainData.getValue(AnalyticKey.BACKGROUND_TIMER, now);

		// Moved both of these higher due to imbalance of not having a soft/hard
		// enter
		startTimer();
		countEnter();

		long delta = Math.abs(now - lastBackground);
		if (soft && delta < INTERVAL_FOR_SOFT_RESUME) {
			if (launchClient != null) {
				launchClient.launchSkipped();
			}
			return false;
		}

		keychainData.setValue(AnalyticKey.BACKGROUND_TIMER, now);

		countLaunch();

		reportLaunch(cxt, currencyReport, adReport, historyTracker, false);
		uploadIfNeeded(cxt);

		currentTry = minTry;
		keychainData.save();
		return true;
	}
	
	private void submitCachedVGPIfNeeded(Context ctx) {
		JSONArray unpushed = keychainData.getJSONArray(AnalyticKey.UNPUSHED_VIRTUAL_PURCHASES);
		if (unpushed.length() == 0 || YerdyUtil.networkUnreachable(ctx))
			return;
		
		VirtualPurchaseData toSubmit = null;
		
		try
		{
			JSONObject firstUnpushedJSON = unpushed.optJSONObject(0);
			if (firstUnpushedJSON == null)
				return;
			toSubmit = VirtualPurchaseData.fromJSON(firstUnpushedJSON);
			
			unpushed = JSONUtil.removeAtIndex(unpushed, 0);
			keychainData.setJSONArray(AnalyticKey.UNPUSHED_VIRTUAL_PURCHASES, unpushed);
			keychainData.save();
		}
		catch (JSONException ex)
		{
			YRDLog.e(getClass(), "Failed to get unpushed virtual purchase from JSON");
			ex.printStackTrace();
		}
		
		if (toSubmit != null) {
			uploadVirtualPurchase(ctx, toSubmit, new CachedSaveVirtualPurchaseClient(ctx));
		}
	}

	public void uploadIfNeeded(Context cxt) {

		final List<PurchaseData> valuesToReport = getPurchasesToReport();

		if (null != currentSaveService || 0 == valuesToReport.size() || YerdyUtil.networkUnreachable(_cxt)) {
			return;
		}

		// First one
		final PurchaseData purchaseData = valuesToReport.get(0);

		currentSaveService = new YRDReportIAPService();
		MetaSavePurchaseClient client = new MetaSavePurchaseClient(cxt);
		client.setPurchaseData(purchaseData);
		currentSaveService.savePurchase(cxt, client);
		YRDLog.i(getClass(),
				"Reporting purchase (" + purchaseData.getProductID() + ") for " + purchaseData.getProductValue());
	}

	private void countEnter() {
		if (countedLaunch) {
			return;
		}

		countEvent(AnalyticKey.ENTERS_VERSIONED, 1, true);

		countedLaunch = true;
		countedExit = false;

	}

	private void startTimer() {
		appStart = SystemClock.elapsedRealtime();
		YRDLog.i(getClass(), "start timer appstart: " + appStart);
	}

	private void countLaunch() {
		YRDLog.i(getClass(), "Incrementing Launch count ");
		countEvent(AnalyticKey.LAUNCHES_LIFETIME, 1, false);
		countEvent(AnalyticKey.LAUNCHES_VERSIONED, 1, true);
		countEvent(AnalyticKey.LAUNCHES_PROGRESSION, 1, false);
	}

	public void appHandleDeactivate() {
		_activated = false;
		keychainData.setValue(AnalyticKey.BACKGROUND_TIMER, System.currentTimeMillis());

		countExit();
		stopTimer();
		keychainData.save();
	}

	private void countExit() {
		if (countedExit) {
			return;
		}
		countEvent(AnalyticKey.EXITS_VERSIONED, 1, true);

		countedExit = true;
		countedLaunch = false;
	}

	private void stopTimer() {
		if (appStart == -1) {
			return;
		}

		final long deltaRuntime = SystemClock.elapsedRealtime() - appStart;
		
		long lifetimeRuntime = keychainData.getValue(AnalyticKey.PLAY_TIME_LIFETIME, 0L);
		lifetimeRuntime = (lifetimeRuntime < 0)?(0):(lifetimeRuntime);
		lifetimeRuntime += Math.max(0L, deltaRuntime);

		long versionRuntime = keychainData.getValue(AnalyticKey.PLAY_TIME_VERSIONED, 0L);
		versionRuntime = (versionRuntime < 0)?(0):(versionRuntime);
		versionRuntime += Math.max(0L, deltaRuntime);
		
		appStart = -1;
		keychainData.setValue(AnalyticKey.PLAY_TIME_LIFETIME, lifetimeRuntime);
		keychainData.setValue(AnalyticKey.PLAY_TIME_VERSIONED, versionRuntime);
		keychainData.save();
	}
	
	class MetaLaunchClient extends YRDLaunchClient {
		private boolean _isRefresh = false;
		private Context _ctx = null;
		
		public MetaLaunchClient(Context ctx) {
			_ctx = ctx.getApplicationContext();
		}
		
		@Override
		public void launchReported() {
			if(_isRefresh) {
				return;
			}
			
			submitCachedVGPIfNeeded(_ctx);
			
			if (null != launchClient) {
				launchClient.launchReported();
			}
		}

		public void setIsRefresh(boolean isRefresh) {
			_isRefresh = isRefresh;
		}
		
		@Override
		public void launchReportFailed(Exception error) {
			if (_isRefresh) {
				return;
			}
			
			if (null != launchClient) {
				launchClient.launchReportFailed(error);
			}
		}
		
		@Override
		public void launchSkipped() {
			if(_isRefresh) {
				return;
			}
			
			if (null != launchClient) {
				launchClient.launchSkipped();
			}
		}
	}

	class MetaSavePurchaseClient extends YRDReportIAPClient {

		private Context _cxt = null;
		public MetaSavePurchaseClient(Context cxt) {
			_cxt = cxt;
		}
		
		@Override
		public void savePurchaseServiceSucceeded(int resultCode) {
			
			if (0 == resultCode) {
				savePurchaseServiceFailed(new Exception("Server Result code: 0"), HttpURLConnection.HTTP_OK);
				return;
			}
			
			// Code 2 or 3 are invalid purchases and transaction errors,
			// respectively
			keychainData.setValue(AnalyticKey.POST_IAP_INDEX, 1);
			keychainData.save();
			
			int validationState = keychainData.getValue(AnalyticKey.PURCHASE_VALIDATION_STATE, ValidationState.DEFAULT.idx());
			if (validationState == ValidationState.PENDING.idx()) {
				try {
					// update user to VALIDATED
					validationState = ValidationState.VALIDATED.idx();
					keychainData.setValue(AnalyticKey.PURCHASE_VALIDATION_STATE, ValidationState.VALIDATED.idx());
					
					// update postIapIndex on all pending virtual purchases
					JSONArray pending = keychainData.getJSONArray(AnalyticKey.PENDING_VIRTUAL_PURCHASES);
					
					int postIapIndex = 1;
					JSONArray processed = new JSONArray();
					for (int i = 0; i < pending.length(); i++) {
						JSONObject jsonObj = pending.optJSONObject(i);
						if (jsonObj == null)
							continue;
						
						try {
							VirtualPurchaseData vpData = VirtualPurchaseData.fromJSON(jsonObj);
							vpData.setPostIapIndex(postIapIndex);
							postIapIndex++;
							processed.put(vpData.toJSON());
						} catch (JSONException ex) { 
							YRDLog.e(getClass(), "Error processing pending VGP");
							ex.printStackTrace();
						}
					}
					
					// update POST_IAP_INDEX after adjusting the pending purchases
					keychainData.setValue(AnalyticKey.POST_IAP_INDEX, postIapIndex);
					
					// add all updated VGP to UNPUSHED_VIRTUAL_PURCHASES
					JSONArray unpushed = keychainData.getJSONArray(AnalyticKey.UNPUSHED_VIRTUAL_PURCHASES);
					keychainData.setJSONArray(AnalyticKey.UNPUSHED_VIRTUAL_PURCHASES, JSONUtil.concat(unpushed, processed));
					
					// clear pending virtual purchases
					keychainData.setJSONArray(AnalyticKey.PENDING_VIRTUAL_PURCHASES, new JSONArray());
					
					keychainData.save();
					
					// send off VGP to server
					submitCachedVGPIfNeeded(_cxt);
				} catch (Exception ex) {
					YRDLog.e(getClass(), "Error updating user from PENDING -> VALIDATED");
					ex.printStackTrace();
				}
			}

			List<PurchaseData> values = getPurchasesToReport();
			if (values.remove(getPurchaseData())) {
				YRDLog.d(getClass(), "Purchase removed from queue");
			} else {
				YRDLog.d(getClass(), "Purchase left on queue");
			}
			writePurchasesToReport(values);

			YRDLog.i(getClass(), "Reported purchase(" + getPurchaseData().getProductID() + ") resultCode: "
					+ resultCode);
			currentSaveService = null;
			uploadIfNeeded(_cxt);
			currentTry = minTry;
		}

		@Override
		public void savePurchaseServiceFailed(Exception e, int responseCode) {
			switch(responseCode) {
			case HttpURLConnection.HTTP_NOT_IMPLEMENTED:
			case HttpURLConnection.HTTP_PAYMENT_REQUIRED:
			case HttpURLConnection.HTTP_UNAUTHORIZED:
			case HttpURLConnection.HTTP_FORBIDDEN:
				List<PurchaseData> values = getPurchasesToReport();
				if (values.remove(getPurchaseData())) {
					YRDLog.d(getClass(), "Purchase removed from queue");
				} else {
					YRDLog.d(getClass(), "Purchase left on queue");
				}
				writePurchasesToReport(values);

				YRDLog.i(getClass(), "Failed to reported purchase will not retry(" + getPurchaseData().getProductID() + ") responseCode: "
						+ responseCode);
				
				if(responseCode == HttpURLConnection.HTTP_PAYMENT_REQUIRED)
					YRDUserInfo.getInstance(_cxt).userType = YRDUserType.CHEAT;
				
				keychainData.setValue(AnalyticKey.POST_IAP_INDEX, 0);
				keychainData.save();
				
				currentSaveService = null;
				uploadIfNeeded(_cxt);
				currentTry = minTry;
				break;
			default:				
				YRDLog.e(getClass(), "Failed to save purchase with error: " + e.getLocalizedMessage());
				currentSaveService = null;
				final long nextRetry = slotTime * (long) Math.pow(baseFactor, currentTry);
				currentTry++;
				if (currentTry > maxTry) {
					currentTry = maxTry;
				} else if (currentTry < minTry) {
					currentTry = minTry;
				}
	
				purchaseServiceTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						uploadIfNeeded(_cxt);
					}
				}, nextRetry * 1000L);
				break;
			}
		}
	}

	// used when submitting a virtual purchase right away
	class MetaSaveVirtaulPurchaseClient extends YRDReportVirtualPurchaseClient {

		@Override
		public void saveVirtualPurchaseServiceFailed(Exception error, int resultCode) {
			// iOS doesn't handle these, so neither does Android
		}

		@Override
		public void saveVirtualPurchaseServiceSucceeded(int resultCode) {
			// iOS doesn't handle these, so neither does Android
		}
	}
	
	// used when uploading a bunch of cached virtual purchases
	class CachedSaveVirtualPurchaseClient extends YRDReportVirtualPurchaseClient {
		private Context context;
		public CachedSaveVirtualPurchaseClient(Context ctx) {
			context = ctx.getApplicationContext();
		}
		
		@Override
		public void saveVirtualPurchaseServiceFailed(Exception error, int resultCode) {
			YRDLog.d(getClass(), "saveVirtualPurchaseServiceFailed");
			submitCachedVGPIfNeeded(context);
		}

		@Override
		public void saveVirtualPurchaseServiceSucceeded(int resultCode) {
			YRDLog.d(getClass(), "saveVirtualPurchaseServiceSucceeded");
			submitCachedVGPIfNeeded(context);
		}
	}

	public void clearApp() {
		keychainData.clear();
		keychainData.save();
	}

	public void setAge() {
		// TODO Auto-generated method stub

	}

	public void setGender(String gender) {
		// TODO Auto-generated method stub

	}

	public void setPushToken(String id) {
		keychainData.setValue(AnalyticKey.GCM_TOKEN, id);
		keychainData.save();
	}
	
	public String getPushToken() {
		String token = keychainData.getValue(AnalyticKey.GCM_TOKEN, null);
		YRDLog.i(YRDAnalytics.class, "getToken: " + token);
		return token;
	}
	
	public void setPushTokenType(String value) {
		keychainData.setValue(AnalyticKey.GCM_TOKEN_TYPE, value);
		keychainData.save();
	}
	
	public String getPushTokenType() {
		String tokenType = keychainData.getValue(AnalyticKey.GCM_TOKEN_TYPE, null);
		YRDLog.i(YRDAnalytics.class, "getTokenType: " + tokenType);
		return tokenType;
	}
	
	public void setCachedCountry(String str)
	{
		if(keychainData != null)
		{
			keychainData.setValue(AnalyticKey.CACHED_COUNTRY, str);
			keychainData.save();
		}
	}
	
	public String getCachedCountry()
	{
		if(keychainData != null)
		{
			return keychainData.getValue(AnalyticKey.CACHED_COUNTRY, "");
		}
		else
		{
			return "";
		}
	}

	public void logScreenVisit(String name) {
		int value = _screenVisits.optInt(name, 0);
		try {
			_screenVisits.put(name, (value + 1));
		} catch (JSONException e) {
			YRDLog.w(getClass(), "Failed to log screen visit");
		}

		if(keychainData != null) {
			keychainData.setValue(AnalyticKey.SCREEN_VISITS, _screenVisits.toString());
			keychainData.save();
		}
		try {
			YRDLog.i("ScreenVisits", _screenVisits.toString(1));
		} catch (JSONException e) {
			YRDLog.i("ScreenVisits", "error showing visits");
		}
	}

	public void reportTimeMilestone(Context cxt, long playtime, JSONObject currency) {
		YRDCounterService counterService = new YRDCounterService();

		int virtualPurchases = keychainData.getValue(AnalyticKey.VIRTUAL_PURCHASE_COUNT_TIMED, 0);
		keychainData.deleteKey(AnalyticKey.VIRTUAL_PURCHASE_COUNT_TIMED);
		keychainData.save();
		
		int launches = 0;
		if(keychainData != null) {
			launches = keychainData.getValue(AnalyticKey.LAUNCHES_LIFETIME, 0);
		}
		
		int marker = Math.round(playtime / 60000);
		
		counterService.reportTimedMilestone(cxt, marker, currency, virtualPurchases, launches, new MetaMilestoneClient());
	}
	
	public void reportPlayerProgressionOrFeature(Context cxt, JSONObject milestones, String type) {
		YRDCounterService counterService = new YRDCounterService();
		counterService.reportPlayerProgressionOrFeature(cxt, milestones, type, new MetaMilestoneClient());
	}

	protected class MetaMilestoneClient extends YRDCounterClient {

		@Override
		public void counterServiceFailed(Exception error, int responseCode) {
			// TODO Auto-generated method stub
		}

		@Override
		public void counterServiceSucceeded(int resultCode) {
			// TODO Auto-generated method stub
		}

	}

	public void reportCustomEvent(Context cxt, JSONObject events) {
		YRDCounterService counterService = new YRDCounterService();
		if(events != null && events.length() > 0) {
			Iterator<?> eventNames = events.keys();
			while(eventNames.hasNext()) {
				String eventName = (String) eventNames.next();
				JSONObject eventJson = events.optJSONObject(eventName);
				counterService.reportCustomEvent(cxt, eventName, eventJson, new MetaMilestoneClient());
			}
		}
	}

	public void setIsPreYerdyUser(boolean flag) {
		keychainData.setValue(AnalyticKey.IS_PRE_YERDY_USER, flag);
		keychainData.save();
	}

	public void setTrackPreYerdyUser(boolean flag) {
		keychainData.setValue(AnalyticKey.TRACK_PRE_YERDY_USER, flag);
		keychainData.save();
	}
	
	public boolean shouldTrackUser() {
		if(keychainData.getValue(AnalyticKey.IS_PRE_YERDY_USER, false) == true)
		{
			return keychainData.getValue(AnalyticKey.TRACK_PRE_YERDY_USER, false);
		}
		return true;
	}
}
