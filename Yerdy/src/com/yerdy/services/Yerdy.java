package com.yerdy.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.SystemClock;

import com.yerdy.services.ads.YRDAdRequestTracker;
import com.yerdy.services.core.YRDAnalytics;
import com.yerdy.services.core.YRDEventTracker;
import com.yerdy.services.core.YRDProgressionTracker;
import com.yerdy.services.core.YRDTaskScheduler;
import com.yerdy.services.core.YRDTimerTask;
import com.yerdy.services.launch.YRDLaunchClient;
import com.yerdy.services.launch.YRDUserInfo;
import com.yerdy.services.launch.YRDUserType;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.lvl.LVLIHandler;
import com.yerdy.services.lvl.LVLManager;
import com.yerdy.services.messaging.YRDAppActionParser;
import com.yerdy.services.messaging.YRDConversionTracker;
import com.yerdy.services.messaging.YRDInAppPurchase;
import com.yerdy.services.messaging.YRDItemPurchase;
import com.yerdy.services.messaging.YRDMessage;
import com.yerdy.services.messaging.YRDMessageActionType;
import com.yerdy.services.messaging.YRDMessagePreseneter;
import com.yerdy.services.messaging.YRDMessagePresenterDelegate;
import com.yerdy.services.messaging.YRDMessagingClient;
import com.yerdy.services.messaging.YRDMessagingService;
import com.yerdy.services.messaging.YRDReward;
import com.yerdy.services.purchases.YRDCurrencyReport;
import com.yerdy.services.purchases.YRDCurrencyTracker;
import com.yerdy.services.purchases.YRDPurchase;
import com.yerdy.services.push.YRDPushManager;
import com.yerdy.services.util.YRDPlatform;
import com.yerdy.services.util.YerdyUtil;

/**
 * Public interface to Yerdy
 * <h2>Messaging</h2>
 * <hr>
 * <p>Yerdy can be used to show users messages configured in the dashboard. See below for implementation details.</p>
 * 
 * <h3>Placements</h3>
 * <p>Using placements you can target different messages to different places inside your game. For example, you could add a level_complete placement and then setup a daily message rewarding the user for completing a level to encourage users to complete a level daily.</p>
 * <p>Some examples placements could include launch, game_over, level_complete, and achievement_unlocked</p>
 * <p><strong>Note:</strong>Placements are optional. If you wish to show any message regardless of placement you can simply pass in null to most methods taking a placement.</p>
 * 
 * <h3>Showing messages</h3>
 * <p>To show a message, simply call {@link #showMessage(Activity, String)}, passing in a context and placement:</p>
 * <pre>{@code Yerdy.getInstance().showMessage(this, "launch");}</pre>
 * <p>If you wish to check if a message is available before trying to show one, you can use {@link #isMessageAvailiable(String)}</p>
 * <pre>{@code 
	if(Yerdy.getInstance().isMessageAvailiable("launch"))
 		Yerdy.getInstance().showMessage(this, "launch");}</pre>
 *   
 * <h3>Handling message triggered rewards and purchases</h3>
 * <p>An important part of Yerdy�s messaging features is being able to reward the user or trigger a purchase inside the app (for example, when an item is on sale). To support this, you need to implement the {@link com.yerdy.services.YerdyMessageDelegate YerdyMessageDelegate}</p>
 * <pre>{@code
	&#064;Override
	public void handleInAppPurchase(YRDInAppPurchase purchase) {
		// 'purchase' contains a product identifier you can use to start an in-app purchase.
		// For example, if you have a class 'InAppManager' used for handling in app purchases,
		// you could do something like:
		startInAppPurchase(purchase.getProductIdentifier());
	}

	&#064;Override
	public void handleItemPurchase(YRDItemPurchase purchase) {
	    // 'purchase' contains a product identifier you can use to start an in-game item purchase
	    // For example, if you have a class 'StoreManager' used for purchasing in game items,
	    // you could do something like:
		startItemPurchase(purchase.getItem());
	}

	&#064;Override
	public void handleReward(YRDReward reward) {
	    // 'rewards' is a dictionary contain reward names & amounts, like:
	    // { "bricks" : 5 }
	    // For example, if you have a class 'InventoryManager' used for tracking the user's inventory,
	    // you could do something like:
	    for(YRDRewardItem item : reward.getRewards()) {
			inventoryManager.addItem(item.getName(), item.getAmount());
		}
	}}</pre>
 * <h2>In-Game Currency</h2>
 * <hr>
 * <p>Yerdy can be used to track the economy of your game. See below for implementation details.</p>
 * <h3>Registering Currencies</h3>
 * <p>You can register up to 6 currencies via the {@link #configureCurrencies(Context, String[])} method. For example:</p>
 * <pre>{@code Yerdy.getInstance().configureCurrencies(this, new String[] { "platinum", "gold", "silver", "copper" }); }></pre>
 * <p><strong>Note:</strong> The order of the currencies is important. You <strong>MUST NOT</strong> reorder them. However, you can append new currencies. For example, if we add a new currency to our game in a later release, we would update the array to: { "platinum", "gold", "silver", "copper", "rubies" }</p>
 * <h3>Registering Currencies</h3>
 * <p>Yerdy supports three types of transactions:</p>
 * <ul>
 * <li>User earned currency {@link #earnedCurrency(Context, Map)}</li>
 * <li>User purchased an in game item {@link #purchasedItem(String, Map)}</li>
 * <li>User made an in-app purchase {@link #purchasedInApp(YRDPurchase, Map)}</li></ul>
 * <p>All three of these methods accept a maps of currencies and their amounts. Some examples:</p>
 * <h3>User earned currency</h3>
 * <pre>{@code
	Map<String, Integer> currencies = new HashMap<String, Integer>();
	currencies.put("gold", 5);
	currencies.put("silver", 10);
	Yerdy.getInstance().earnedCurrency(this, currencies);
 * }</pre>
 * <h3>User purchase an in game item</h3>
 * <pre>{@code
	Map<String, Integer> currencies = new HashMap<String, Integer>();
	currencies.put("silver", 5);
	Yerdy.getInstance().purchasedItem("Superboost", currencies);
 * }</pre>
 * <h3>User made an in-app purchase</h3>
 * <pre>{@code
	Map<String, Integer> currencies = new HashMap<String, Integer>();
	currencies.put("gold", 5);
	currencies.put("silver", 10);
	currencies.put("bronze", 5);
	YRDPurchaseGoogle purchase = new YRDPurchaseGoogle(...);
	Yerdy.getInstance().purchasedInApp(purchase, currencies);
 * }</pre>
 * 
 * 
 */
public class Yerdy {

	private static Yerdy _instance = null;
	private LVLManager _lvlManager = null;
	private List<YRDMessage> _messages = new ArrayList<YRDMessage>();
	private YRDMessagePreseneter _activeMessage = null;
	private String _publisherKey = null;
	private String _publisherSecret = null;
	private YRDMessagingService _messagingService = null;
	private String _appPacakge = null;
	private String _appVersionName = null;
	private int _appVersionCode = 0;
	private YRDTaskScheduler _timeTracker = new YRDTaskScheduler();
	private YRDPlatform _platform = YRDPlatform.AUTO;
	private String _installerPackage = null;
	private Context _applicationContext = null;
	private YerdyMessageDelegate _yerdyMessageDelegate = null;
	private YerdyDelegate _yerdyDelegate = null;
	private boolean _forceMessageFetchNextResume = false;
	private int _messagesPresentedInRow = 1;
	private boolean _didDismissMessage = false;
	private String _currentPlacement = null;
	private long _lastProgressionReport = -1;
	private int _defaultMaxFailverCount = Integer.MAX_VALUE;
	private Map<String, Integer> _maxFailverCounts = new HashMap<String, Integer>();

	private final long PROGRESSION_THRESHOLD = 120000;
	
	private YRDConversionTracker _conversionTracker = new YRDConversionTracker();
	private YRDCurrencyTracker _currencyTracker;
	private YRDProgressionTracker _progressionTracker;
	private YRDEventTracker _eventTracker;
	private YRDAdRequestTracker _adRequestTracker;
	private boolean _initialized = false;

	/**
	 * Gets the singleton instance of Yerdy.
	 * 
	 * @return Yerdy singleton
	 */
	public static Yerdy getInstance() {
		if(_instance == null)
			_instance = new Yerdy();
		return _instance;
	}
	
	/**
	 * Executes necessary clean up of Yerdy singleton
	 * <pre>{@code
	 *&#064;Override
	 *protected void onDestroy() { 
	 *  super.onDestroy();
	 *  Yerdy.getInstance().onDestroy();
	 *}</pre>
	 * @category Application Life Cycle
	 */
	public void onDestroy() {
		if(_lvlManager != null)
			_lvlManager.onDestroy();
		YRDPushManager.onDestroy(_applicationContext);
		YRDAnalytics.getInstance().appHandleDeactivate();
	}
	
	/**
	 * Resumes Yerdy tracking of current application, should be called from activity onResume(), also confirms application is in focus
	 * 
	 * <pre>{@code
	 *&#064;Override
	 *protected void onResume() { 
	 *  super.onResume();
	 *  Yerdy.getInstance().onResume(this);
	 *}</pre>
	 * 
	 * @param activity current activity
	 * @category Application Life Cycle
	 */
	public void onResume(Activity activity) {
		boolean hasFocus = activity.hasWindowFocus();
		boolean finishing = activity.isFinishing();
		if(hasFocus && !finishing)
			activate(activity, true);
	}
	
	/**
	 * Pauses Yerdy tracking of current application, should be called from activity onPause()
	 * 
	 * <pre>{@code
	 *&#064;Override
	 *protected void onPause() { 
	 *  super.onResume();
	 *  Yerdy.getInstance().onPause();
	 *}</pre>
	 * 
	 * @category Application Life Cycle
	 */
	public void onPause() {
		deactivate();
	}
	
	/**
	 * Confirms focus of application for Yerdy pause/resume, should be called from activity onWindowFocusChanged()
	 * 
	 * <pre>{@code
	 *&#064;Override
	 *public void onWindowFocusChanged(boolean hasFocus) {
	 *  super.onWindowFocusChanged(hasFocus);
	 *  Yerdy.getInstance().onWindowFocusChanged(hasFocus, this);
	 *}</pre>
	 * 
	 * @param hasFocus if activity has focus
	 * @param activity current activity
	 * @category Application Life Cycle
	 */
	public void onWindowFocusChanged(boolean hasFocus, Activity activity) {
		if(hasFocus && !activity.isFinishing())
			activate(activity, true);
		else
			deactivate();
	}
	
	/**
	 * Starts/Resumes Yerdy tracking different behaviors for launch tracking if hard or soft launch. Hard launch is considered to be a fresh start. Soft launch considered to be a resume from background etc.
	 * 
	 * @param activity current activity
	 * @param soft soft or hard launch
	 * @category Application Life Cycle
	 */
	protected void activate(Activity activity, boolean soft) {
		sendEvents(activity);
		boolean newSession = YRDAnalytics.getInstance().appHandleActivate(activity, _currencyTracker.generateCurrencyReport(null), _adRequestTracker.generateReport(), true);

		if(!newSession)
			_timeTracker.resume();
		else {
			_adRequestTracker.reset();
			_timeTracker.dequeueAll();
			queueTimeMilestone();
		}
	}
	
	/**
	 * Stops Yerdy Tracking
	 * @category Application Life Cycle
	 */
	private void deactivate() {
		YRDAnalytics.getInstance().appHandleDeactivate();
		_timeTracker.pause();
	}
	
	/**
	 * Initializes Yerdy
	 * 
	 * @param context	current activity context
	 * @param publisherKey	Your publisher key (get one from <a href="http://www.yerdy.com">here</a>)
	 * @category Configuration
	 */
	public void startWithPublisherKey (Context context, String publisherKey) {
		_initialized = true;
		Resources res = context.getResources();
		int icon = res.getIdentifier("notification_icon", "drawable", Yerdy.getInstance().getAppPackage());
		YRDPushManager.Register(context, icon);
		
		_publisherKey = publisherKey.substring(0, 16);
		_publisherSecret = publisherKey.substring(16);
		_appPacakge = context.getPackageName();
		_appVersionName = "0.0.1";
		_appVersionCode = 0;
		if(_currencyTracker == null)
			_currencyTracker = new YRDCurrencyTracker(context.getApplicationContext());
		if(_progressionTracker == null)
			_progressionTracker = new YRDProgressionTracker(context.getApplicationContext());
		if(_eventTracker == null)
			_eventTracker = new YRDEventTracker(context.getApplicationContext());
		if(_adRequestTracker == null)
			_adRequestTracker = new YRDAdRequestTracker(context.getApplicationContext());
				
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(_appPacakge, 0);
			_appVersionName = packageInfo.versionName;
			_appVersionCode = packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		_applicationContext = context.getApplicationContext();
		_installerPackage = context.getPackageManager().getInstallerPackageName(_appPacakge);
		YRDAnalytics.getInstance().setLaunchClient(context, new YRDLaunchClient() {
			@Override
			public void launchReported() {
				fetchMessages(_applicationContext);
			}
			
			@Override
			public void launchReportFailed(Exception error) {
				fetchMessages(_applicationContext);
			}

			@Override
			public void launchSkipped() {
				if(_forceMessageFetchNextResume)
					fetchMessages(_applicationContext);
			}
		});

		boolean newVersion = YRDAnalytics.getInstance().versionMismatch();
		if(newVersion)
			_adRequestTracker.reset();
		
		YRDAnalytics.getInstance().appHandleActivate(context, _currencyTracker.generateCurrencyReport(null), _adRequestTracker.generateReport(), false);
		queueTimeMilestone();
		_adRequestTracker.reset();
		
		YRDLog.i(this.getClass(), "To enable test mode for this device, copy/paste the device ID below into the Yerdy dashboard.");
		YRDLog.i(this.getClass(), "Device ID: " + YerdyUtil.getUDID(context));
	}
	
	/**
	 * Used to add milestones to the time tracker
	 * @category Milestones
	 */
	private void queueTimeMilestone() {
		long currentTime = YRDAnalytics.getInstance().getPlaytimeMS(false);
		long delay = getNextMilestoneReportInterval(currentTime);
		queueTimeMilestone(currentTime, delay);
	}
	
	/**
	 * Used to add milestones to the time tracker
	 * @category Milestones
	 */
	private void queueTimeMilestone(final long currentPlaytime, final long delay) {
		_timeTracker.dequeueAll();
		
		YRDLog.i("Time_Progression", String.format("Current Time: %,d, Delay: %,d", currentPlaytime, delay));
		
 		_timeTracker.queueAndStart(new YRDTimerTask(delay, new Runnable() {
			@Override
			public void run() {
				long execTime = YRDAnalytics.getInstance().getPlaytimeMS(false);
				if(execTime < (currentPlaytime + delay)) {
					YRDLog.i("Time_Progression", String.format("Exec Time: %,d, Limiter: %,d", execTime, (currentPlaytime + delay)));
					queueTimeMilestone(execTime, getNextMilestoneReportInterval(execTime));
				} else {
					YRDLog.i("Time_Progression", String.format("Report Time: %,d, Delay: %,d, Limiter: %,d", execTime, delay, (currentPlaytime + delay)));
					if(YRDAnalytics.getInstance().shouldTrackUser()) {
						YRDAnalytics.getInstance().reportTimeMilestone(_applicationContext, execTime, _currencyTracker.getAndResetTimedCurrency());
					}
					queueTimeMilestone(execTime, getNextMilestoneReportInterval(execTime));
				}
			}
		}));
	}
	
	private final long MINUTE = 60000;
	/**
	 * Creates next milestone for time tracking
	 * @param currentPlaytime	current play time used to determine next milestone
	 * @return	next reporting milestone
	 * @category Milestones
	 */
	private long getNextMilestoneReportInterval(long currentPlaytime) {
		if(currentPlaytime <= 0) {
			return 2 * MINUTE;
		} else if(currentPlaytime < (10 * MINUTE)) {
			return milestoneConversion(currentPlaytime, 2);
		} else if(currentPlaytime < (30 * MINUTE)) {
			return milestoneConversion(currentPlaytime, 5);
		} else if(currentPlaytime < (60 * MINUTE)){
			return milestoneConversion(currentPlaytime, 10);
		} else {
			return milestoneConversion(currentPlaytime, 30);
		}
	}
	
	/**
	 * Utility function to process milestone time
	 * @param currentPlaytime	Current lifetime play time of application
	 * @param minutes	Expected minute based interval 
	 * @return	calculated milestone in milliseconds
	 * @category Milestones
	 */
	private long milestoneConversion(long currentPlaytime, int minutes) {
		long time = (minutes * MINUTE);
		return time - (currentPlaytime % time);
	}
	
	/**
	 * Used to override detection of current platform (Google/Amazon) as the auto detection will only work when loaded from the store, will assume Google is side loaded
	 * @param platform	platform override
	 * @throws YerdyConfigurationException 
	 * @see com.yerdy.services.util.YRDPlatform YRDPlatform
	 * @category Configuration
	 */
	public void configureAppPlatform(YRDPlatform platform) {
		if(_initialized) {
			throw new RuntimeException("configureAppPlatform must be called before startWithPublisherKey");
		}
		
		_platform = platform;
	}
	
	/**
	 * Registers up to 6 currencies used in the app.
	 * 
	 * <br/><br/><strong>Warning:</strong> You <strong>MUST NOT</strong> change the order of currencies. However, you may append new currencies.
	 * 
	 * <pre>{@code Yerdy.getInstance().configureCurrencies(this, new String[] { "platinum", "gold", "silver", "copper" });}</pre>
	 * @param context application context
	 * @param names array of currency names
	 * @throws YerdyConfigurationException 
	 * @category Configuration
	 */
	public void configureCurrencies(Context context, String[] names) {
		if(_initialized) {
			throw new RuntimeException("configureCurrencies must be called before startWithPublisherKey");
		}
		
		if(_currencyTracker == null)
			_currencyTracker = new YRDCurrencyTracker(context.getApplicationContext());
		_currencyTracker.configure(names);
	}
	
	/**
	 * Marks a user as existing and sets their existing currency
	 * 
	 * <pre>{@code Yerdy.getInstance().configureCurrencies(this, new String[] { "platinum", "gold", "silver", "copper" });}</pre>
	 * @param currencies The users current currency balance
	 * @category Configuration
	 */
	public void setExistingCurrenciesForPreYerdyUser(Map<String, Integer> currencies) {
		boolean wasConfigured = _currencyTracker.setInitialCurrencies(currencies);
		if(wasConfigured) {
			YRDAnalytics.getInstance().setIsPreYerdyUser(true);
			YRDAnalytics.getInstance().setTrackPreYerdyUser(false);
			if(currencies.size() > 0) {
				for(String key : currencies.keySet()) {
					int amount = currencies.get(key);
					if(amount != 0) {
						YRDCurrencyReport currencyReport = _currencyTracker.generateCurrencyReport(null);
						YRDAnalytics.getInstance().reportLaunch(_applicationContext, currencyReport, _adRequestTracker.generateReport(), true);
						return;
					}
				}
			}
		}
	}
	
	/**
	 * Overrides the default behavior of not tracking certain metrics when a user is flag as a pre-yerdy user
	 * 
	 * @param flag
	 * @category Configuration
	 */
	public void setShouldTrackPreYerdyUserProgression(boolean flag) {
		YRDAnalytics.getInstance().setTrackPreYerdyUser(false);
	}
	
	/**
	 * Tracks a screen visit
	 * 
	 * @param name The name of the screen (for example: �settings�, �store�, etc�)
	 * @category Event Tracking
	 */
	public void logScreenVisit(String name) {
		YRDAnalytics.getInstance().logScreenVisit(scrubName(name));
	}
	
	/**
	 * @return currently detected or manually specified platform
	 * @see com.yerdy.services.util.YRDPlatform YRDPlatform
	 * @category Configuration
	 */
	public YRDPlatform getPlatform() {
		if(_platform == YRDPlatform.AUTO) {
			if(_installerPackage != null) {
				if(_installerPackage.toLowerCase(Locale.getDefault()).contains("com.amazon.venezia")) {
					return YRDPlatform.AMAZON;
				} else if (_installerPackage.toLowerCase(Locale.getDefault()).contains("com.android.vending")) {
					return YRDPlatform.GOOGLE;
				}
			}
			
			return YRDPlatform.GOOGLE;
		} else {
			return _platform;
		}
	}
	
	/**
	 * Retrieves pull messages for display
	 * @param cxt application context
	 * @category Messaging
	 */
	private void fetchMessages(Context cxt) {
		if(_messagingService == null) {
			_messagingService = new YRDMessagingService();
		}

		_forceMessageFetchNextResume = false;
		_messagingService.fetchMessages(cxt, new YRDMessagingClient() {
			@Override
			public void onSuccess(List<YRDMessage> messages) {
				_messages = messages;
				_yerdyDelegate.yerdyConnected(true);
			}

			@Override
			public void onError(Exception e) {
				YRDLog.e(this.getClass(), e.getMessage());
				_yerdyDelegate.yerdyConnected(false);
			}
		});
	}
	
	/**
	 * Used to track Google LVL validation for metric purposes, we don't stop application execution for failed LVL checks
	 * 
	 * <pre>{@code <uses-permission android:name="com.android.vending.CHECK_LICENSE" />}</pre>
	 * @param cxt - Context
	 * @param lvlKey - Google LVL key
	 * @category Configuration
	 */
	public void configureGoogleLVLKey(Context cxt, String lvlKey) {
		configureGoogleLVLKey(cxt, lvlKey, null);
	}
	
	/**
	 * Used to track Google LVL validation for metric purposes, we don't stop application execution for failed LVL checks
	 * 
	 * <pre>{@code <uses-permission android:name="com.android.vending.CHECK_LICENSE" />}</pre>
	 * @param cxt - Context
	 * @param lvlKey - Google LVL key
	 * @param handler - response handler, will allow you to access results of the LVL check
	 * @category Configuration
	 */
	public void configureGoogleLVLKey(Context cxt, String lvlKey, LVLIHandler handler) {
		_lvlManager = LVLManager.getInstance();
		_lvlManager.configureAndRunIfNeeded(cxt, lvlKey, handler);
	}
	
	/**
	 * Reports Yerdy connection status (optional)
	 * @param delegate YerdyDelegate
	 * @see com.yerdy.services.YerdyDelegate
	 * @category Configuration
	 */
	public void configureDelegate(YerdyDelegate delegate) {
		_yerdyDelegate = delegate;
	}
	
	/**
	 * Reports Yerdy pull message status & actions
	 * @param delegate YerdyMessageDelegate
	 * @see com.yerdy.services.YerdyMessageDelegate
	 * @category Messaging
	 */
	public void configureMessageDelegate(YerdyMessageDelegate delegate) {
		_yerdyMessageDelegate = delegate;
	}
	
	/**
	 * Returns messages currently available at specified placement
	 * @param placement The placement (for example, you could have �launch�, �gameover�, and �store�). Pass in nil for any placement.
	 * @return messages available in placement
	 * @category Messaging
	 */
	private YRDMessage getMessageForPlacement(String placement) {
		if(_messages != null && _messages.size() > 0) {
			for(YRDMessage m : _messages) {
				if(placement == null || m.placement.toLowerCase(Locale.getDefault()).equals(placement.toLowerCase(Locale.getDefault())))
					return m;
			}
		}
		
		return null;
	}
	
	/**
	 * Checks if a message is available for the given placement
	 * @param placement The placement (for example, you could have �launch�, �gameover�, and �store�). Pass in nil for any placement.
	 * @return Whether or not a message is available
	 * @category Messaging
	 */
	public boolean isMessageAvailiable(String placement) {
		return this.getMessageForPlacement(placement) != null;
	}
	
	/**
	 * Tracks in-game item purchase, short version if purchase only required a single currency
	 * @param item The name of the item
	 * @param currency single currency name
	 * @param amount single currency amount
	 * @category Currency
	 */
	public void purchasedItem(String item, String currency, int amount) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put(currency, amount);
		purchasedItem(item, map, false);
	}
	
	/**
	 * Tracks in-game item purchase, 
	 * @param item The name of the item
	 * @param currencies mapping of currency names (String) and amounts (Integer)
	 * @category Currency
	 */
	public void purchasedItem(String item, Map<String, Integer> currencies) {
		purchasedItem(item, currencies, false);
	}
	
	/**
	 * Tracks in-game item purchase, short version if purchase only required a single currency, allows specification if price was regular pricing or sale price
	 * @param item The name of the item
	 * @param currency single currency name
	 * @param amount single currency amount
	 * @param onSale Whether or not the item is on sale
	 * @category Currency
	 */
	public void purchasedItem(String item, String currency, int amount, boolean onSale) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put(currency, amount);
		purchasedItem(item, map, onSale);
	}
	
	/**
	 * Tracks in-game item purchase, 
	 * @param item The name of the item
	 * @param currencies mapping of currency names (String) and amounts (Integer)
	 * @param onSale Whether or not the item is on sale
	 * @category Currency
	 */
	public void purchasedItem(String item, Map<String, Integer> currencies, boolean onSale) {
		int msgId = _conversionTracker.check(item);
		YRDCurrencyReport report = _currencyTracker.generateCurrencyReport(currencies);
		YRDAnalytics.getInstance().reportVirtualPurchase(_applicationContext, item, report, msgId, onSale);
		_currencyTracker.spentCurrencies(currencies);
	}
	
	/**
	 * Tracks in-app purchases (IAP) when in-game currency <strong>is not</strong> rewarded<br/>
	 * If the in-app purchase was for in-game currency, use {@link #purchasedInApp(YRDPurchase, Map)} or {@link #purchasedInApp(YRDPurchase, String, int)} instead.
	 * 
	 * @param purchase - YRDPurchaseGoogle or YRDPurchaseAmazon object
	 * @see com.yerdy.services.purchases.YRDPurchaseAmazon YRDPurchaseAmazon
	 * @see com.yerdy.services.purchases.YRDPurchaseGoogle YRDPurchaseGoogle
	 * @see com.yerdy.services.purchases.YRDPurchase YRDPurchase
	 * @category Currency
	 */
	public void purchasedInApp(YRDPurchase purchase) {
		purchasedInApp(purchase, null);
	}
	
	/**
	 * Tracks in-app purchases (IAP) when in-game currency <strong>is</strong> rewarded<br/>
	 * If the in-app purchase not for in-game currency, use {@link #purchasedInApp(YRDPurchase)} instead.
	 * 
	 * @param purchase YRDPurchaseGoogle or YRDPurchaseAmazon object
	 * @param currency 
	 * @param amount
	 * @see com.yerdy.services.purchases.YRDPurchaseAmazon YRDPurchaseAmazon
	 * @see com.yerdy.services.purchases.YRDPurchaseGoogle YRDPurchaseGoogle
	 * @see com.yerdy.services.purchases.YRDPurchase YRDPurchase
	 * @category Currency
	 */
	public void purchasedInApp(YRDPurchase purchase, String currency, int amount) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put(currency, amount);
		purchasedInApp(purchase, map);
	}
	
	/**
	 * Tracks in-app purchases (IAP) when in-game currency <strong>is</strong> rewarded<br/>
	 * If the in-app purchase not for in-game currency, use {@link #purchasedInApp(YRDPurchase)} instead.
	 * 
	 * @param purchase YRDPurchaseGoogle or YRDPurchaseAmazon object
	 * @param currencies mapping of currency names (String) and amounts (Integer)
	 * @see com.yerdy.services.purchases.YRDPurchaseAmazon YRDPurchaseAmazon
	 * @see com.yerdy.services.purchases.YRDPurchaseGoogle YRDPurchaseGoogle
	 * @see com.yerdy.services.purchases.YRDPurchase YRDPurchase
	 * @category Currency
	 */
	public void purchasedInApp(YRDPurchase purchase, Map<String, Integer> currencies) {
		int msgId = _conversionTracker.check(purchase.getSku());
		YRDCurrencyReport report = _currencyTracker.generateCurrencyReport(currencies);
		YRDAnalytics.getInstance().reportInAppPurchase(_applicationContext, purchase, report, msgId);
		_currencyTracker.boughtCurrencies(currencies);
	}
	
	/**
	 * Tracks currency earned by the user.
	 * @param context activity context
	 * @param currency single currency name
	 * @param amount single currency amount
	 * @see #earnedCurrency(Context, Map) for multiple currencies
	 * @category Currency
	 */
	public void earnedCurrency(Context context, String currency, int amount) {
		_currencyTracker.earnedCurrencies(currency, amount);
	}
	
	/**
	 * Tracks currency earned by the user.
	 * @param context activity context
	 * @param currencies mapping of currency names (String) and amounts (Integer)
	 * @see #earnedCurrency(Context, String, int)
	 * @category Currency
	 */
	public void earnedCurrency(Context context, Map<String, Integer> currencies) {
		_currencyTracker.earnedCurrencies(currencies);
	}
	
	/**
	 * Shows a message (if available)
	 * @param activity activity context that the message will be shown in
	 * @param placement The placement (for example, you could have �launch�, �gameover�, and �store�). Pass in null for any placement.
	 * @return Whether or not a message was shown
	 * @category Messaging
	 */
	public boolean showMessage(Activity activity, String placement) {
		return internalShowMessage(activity, placement, true);
	}
	
	private boolean internalShowMessage(Activity activity, String placement, boolean first) {
		if(activity == null || activity.isFinishing())
			return false;
		
		if(_activeMessage != null) {
			if(_activeMessage.isVisible() || !_activeMessage.hasReportedAction())
				return false;
		}
		
		YRDMessage msgData = getMessageForPlacement(placement);
		if(msgData == null)
			return false;

		YRDMessagePreseneter msg = new YRDMessagePreseneter(msgData, activity.getApplicationContext());
		msg.setMessageDelegate(new PresenterDelegate());

		if(first) {
			_messagesPresentedInRow = 1;
			_didDismissMessage = false;
		} else {
			_messagesPresentedInRow++;
		}

		_currentPlacement = placement;
		msg.show(activity);
		_activeMessage = msg;

		synchronized(_messages) {
			_messages.remove(msgData);
		}
		
		return true;
	}
	
	/** 
	 * Dismisses any open messages
	 * @category Messaging
	 */
	public void dismissMessage() {
		_didDismissMessage = true;
		_activeMessage.dismiss();
		_activeMessage = null;
	}
	
	/**
	 * @return detected application package
	 * @category Utilities
	 */
	public String getAppPackage() {
		return _appPacakge;
	}
	
	/**
	 * @return detected application version label
	 * @category Utilities
	 */
	public String getAppVersion() {
		return _appVersionName;
	}
	
	/**
	 * @return detected application version code
	 * @category Utilities
	 */
	public int getAppCode() {
		return _appVersionCode;
	}
	
	/**
	 * @return detected publisher key
	 * @category Utilities
	 */
	public String getPublisherKey() {
		return _publisherKey;
	}

	/**
	 * @return detected publisher secret
	 * @category Utilities
	 */
	public String getPublisherSecret() {
		return _publisherSecret;
	}
	
	/**
	 * Is the user a premium user? (Do they have any validated IAP purchases?)
	 * @param cxt activity context
	 * @return is premium user
	 * @category Utilities
	 */
	public boolean getIsPremiumUser(Context cxt) {
		return YRDUserInfo.getInstance(cxt).getUserType() == YRDUserType.PAY;
	}
	
	private class PresenterDelegate implements YRDMessagePresenterDelegate {
		@Override
		public void willPresentMessage(Activity activity, YRDMessagePreseneter presenter, YRDMessage message) {
			if(_messagesPresentedInRow > 1) {
				return;
			}
			
			if(_yerdyMessageDelegate != null)
				_yerdyMessageDelegate.willPresentMessageForPlacement(_currentPlacement);
		}
		
		@Override
		public void didPresentMessage(Activity activity, YRDMessagePreseneter presenter, YRDMessage message) {
			if (_messagesPresentedInRow > 1)
				return;

			if(_yerdyMessageDelegate != null)
				_yerdyMessageDelegate.didPresentMessageForPlacement(_currentPlacement);
		}
		
		@Override
		public void willDismissMessage(Activity activity, YRDMessagePreseneter presenter, YRDMessage message, YRDMessageActionType action, String actionParamter) {
			if(activity == null || activity.isFinishing())
				return;
			
			if((action == null || action == YRDMessageActionType.NONE) && shouldShowAnotherMessage()) {
				_activeMessage = null;
				if(internalShowMessage(activity, _currentPlacement, false))
					return;
				else
					_activeMessage = presenter;
			}
			
			if(_yerdyMessageDelegate != null)
				_yerdyMessageDelegate.willDismissMessageForPlacement(_currentPlacement);
		}
		
		@Override
		public void didDismissMessage(Activity activity, YRDMessagePreseneter presenter, YRDMessage message, YRDMessageActionType action, String actionParameter) {
			if(_activeMessage != presenter)
				return;
			
			_activeMessage = null;
			
			if(_yerdyMessageDelegate != null)
				_yerdyMessageDelegate.didDismissMessageForPlacement(_currentPlacement);
			
			if(action != null && action != YRDMessageActionType.NONE) {
				if(message.forceRefresh)
					_forceMessageFetchNextResume = true;
				
				switch(action) {
				case EXTERNAL_BROWSER:
				case INTERNAL_BROWSER:
					if(actionParameter != null && actionParameter.length() > 0) {
						Uri uri = Uri.parse(actionParameter);
						Intent myIntent = new Intent(Intent.ACTION_VIEW, uri);
						activity.startActivity(myIntent);
					}
					break;
				case APP:
					YRDAppActionParser parser = YRDAppActionParser.parseAction(actionParameter);
					if(parser != null) {
						switch(parser.getActionType()) {
							case IN_APP_PURCHASE:
								this.handleInAppPurchase(new YRDInAppPurchase(parser.getActionInfo(), message.id));
								break;
							case ITEM_PURCHASE:
								this.handleItemPurchase(new YRDItemPurchase(parser.getActionInfo(), message.id));
								break;
							case REWARD:
								this.handleReward(new YRDReward(parser.getActionInfo()));
								break;
							case NAVIGATION:
								this.handleNavigation(parser.getActionInfo());
								break;
						}
					} else {
						YRDLog.e(this.getClass(), "Failed to parse app action '" + actionParameter + "'");
					}
					break;
				}
			}
		}

		protected void handleInAppPurchase(YRDInAppPurchase purchase) {
			_conversionTracker.track(purchase);
			if(!verifyMessageDelegateSetupFor("in app purchase"))
			{
				return;
			} else {
				_yerdyMessageDelegate.handleInAppPurchase(purchase);
			}
		}
		
		protected void handleItemPurchase(YRDItemPurchase purchase) {
			_conversionTracker.track(purchase);
			if(!verifyMessageDelegateSetupFor("item purchase"))
			{
				return;
			} else {
				_yerdyMessageDelegate.handleItemPurchase(purchase);
			}
		}

		protected void handleReward(YRDReward reward) {
			if(!verifyMessageDelegateSetupFor("rewards"))
			{
				return;
			} else {
				_yerdyMessageDelegate.handleReward(reward);
			}
		}
		
		protected void handleNavigation(String screen) {
			if(!verifyMessageDelegateSetupFor("navigation"))
			{
				return;
			} else {
				_yerdyMessageDelegate.handleNavigation(screen);
			}
		}
		
		private boolean shouldShowAnotherMessage() {
			int max = _defaultMaxFailverCount;
			if(_currentPlacement != null && _maxFailverCounts.containsKey(_currentPlacement))
				max = _maxFailverCounts.get(_currentPlacement);
			
			if (_messagesPresentedInRow > max)
				return false;
			
			return (!_didDismissMessage && isMessageAvailiable(_currentPlacement));
		}
	}
	
	/**
	 * Checks to make sure the is an application side handler setup for messages
	 * @param msg - type of handler
	 * @return - if configured
	 * @category Messaging
	 */
	private boolean verifyMessageDelegateSetupFor(String msg) {
		if(_yerdyMessageDelegate == null) {
			YRDLog.e(this.getClass(), "Failed handling " + msg + " you haven't set [Yerdy].configureMessageDelegate.");
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Logs a player progression event.
	 * <p>Milestones are grouped by category. For example, you may have a �map� category and your milestones could be �unlocked world 1�, �unlocked world 2�, �unlocked world 3�, etc�</p>
	 * @param category The category for this progression event
	 * @param milestone The milestone the user reached
	 * @category Event Tracking
	 */
	public void playerProgression(String category, String milestone) {
		if(YRDAnalytics.getInstance().shouldTrackUser())
			_progressionTracker.trackProgression(scrubName(category), "_"+milestone, YRDAnalytics.getInstance().getLaunches(false), YRDAnalytics.getInstance().getPlaytimeMS(false));
	}
	
	private void sendEvents(Context cxt) {
		if(Math.abs(_lastProgressionReport - SystemClock.elapsedRealtime()) > PROGRESSION_THRESHOLD || _lastProgressionReport < 0) {
			_lastProgressionReport = SystemClock.elapsedRealtime();
			if(!YerdyUtil.networkUnreachable(cxt)) {
				if(_progressionTracker != null && _progressionTracker.isReadyToReport())
					YRDAnalytics.getInstance().reportProgressionMilestone(cxt, _progressionTracker.getAndResetProgressionEvents());
				
				if(_eventTracker != null && _eventTracker.isReadyToReport())
					YRDAnalytics.getInstance().reportCustomEvent(_applicationContext, _eventTracker.getAndResetCustomEvents());

				//YRDAnalytics.getInstance().reportCustomEvent(_applicationContext, name, parameterName, bucketName, 1);
			}
		}
	}
	

	/**
	 * Tracks a user-defined event
	 * 
	 * <p><strong>Note:</strong> This feature is currently only supported by Premium Yerdy Accounts. </p>
	 * 
	 * <p>Used to track any other metrics you may find interesting</p>
	 * <pre>{@code Yerdy.getInstance().logEvent("ChangedCharacter");}
	 * @param name
	 * @category Event Tracking
	 */
	public void logEvent(String name) {
		logEvent(name, null);
	}

	/**
	 * Tracks a user-defined event
	 * 
	 * <p><strong>Note:</strong> This feature is currently only supported by Premium Yerdy Accounts. </p>
	 * 
	 * <p>Used to track any other metrics you may find interesting. For example, you could have an event to see which character is selected most often:</p>
	 * <pre>{@code 
	 * Map<String, String> map = new HashMap<String, String>();
	 * map.put("Name", "Bob");
	 * Yerdy.getInstance().logEvent("Change", map);}</pre>
	 * @param name event name
	 * @param map event mappings
	 * @category Event Tracking
	 */
	public void logEvent(String name, Map<String, String>map) {
		if(_eventTracker != null) {
			try {
				_eventTracker.trackEvent(name, map, 1);
			} catch (JSONException e) {
				YRDLog.e(getClass(), "Error recording custom event");
				e.printStackTrace();
			}
		} else {
			YRDLog.e(getClass(), "Cannot log event SDK not initialized");
		}
	}
	
	private String scrubName(String name) {
		return name.replaceAll("[^a-zA-Z0-9 ._-]", "");
	}
	
	/**
	 * Sets a limit to the number of �failover� messages that can be shown
	 * <p>If the user clicks �cancel� (or �ok� on a non actionable message), we try and show another message for that placement (until we run out of messages). You can set a limit here. (for example, if you wanted to only show 1 message no matter what, you can call:</p>
	 * <pre>{@code Yerdy.getInstance().setMaxFailoverCount(0, "myPlacement")}</pre>
	 * <p>If you would like to apply it to all placements in your app, pass in nil for placement:</p>
	 * <pre>{@code Yerdy.getInstance().setMaxFailoverCount(0, "myPlacement")}</pre>
	 * @param count
	 * @param placement
	 * @category Configuration
	 */
	public void setMaxFailoverCount(int count, String placement) {
		if(placement == null) {
			_defaultMaxFailverCount = count;
		} else {
			_maxFailverCounts.put(placement, count);
		}
	}
	
	/**
	 * Tracks an ad request.
	 * When the ad network comes back with an ad, you need to call logAdFill: with the same ad network name.
	 * <p><strong>Note:</strong> Must be the exact same value when calling logAdFill</p>
	 * @param network The name of ad network
	 */
	public void logAdRequest(String network) {
		_adRequestTracker.logAdRequest(network);
	}
	
	/**
	 * Tracks an ad fill.
	 * When the ad network comes back with an ad you need to call this with the same ad network name as used in logAdRequest.
	 * <p><strong>Note:</strong> Must be the exact same value when calling logAdRequest</p>
	 * @param network The name of ad network
	 */
	public void logAdFill(String network) {
		_adRequestTracker.logAdFill(network);
	}
}