package com.yerdy.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.unity3d.player.UnityPlayer;
import com.yerdy.services.core.YRDAnalytics;
import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.logging.YRDLogLevel;
import com.yerdy.services.lvl.LVLIHandler;
import com.yerdy.services.lvl.LVLStatus;
import com.yerdy.services.messaging.YRDInAppPurchase;
import com.yerdy.services.messaging.YRDItemPurchase;
import com.yerdy.services.messaging.YRDReward;
import com.yerdy.services.messaging.YRDRewardItem;
import com.yerdy.services.purchases.YRDPurchase;
import com.yerdy.services.util.YRDPlatform;

public class YerdyUnity {
	private static Activity getRootActivity() {
		return UnityPlayer.currentActivity;
	}
	
	public static void onDestroy() {
		YRDLog.i(YerdyUnity.class, "onDestroy");
		Yerdy.getInstance().onDestroy();
	}

	public static void onResume() {
		Activity activity = getRootActivity();
		YRDLog.i(YerdyUnity.class, "onResume:"+activity.hasWindowFocus());
		Yerdy.getInstance().onResume(activity);
	}
	
	public static void onPause() {
		YRDLog.i(YerdyUnity.class, "onPause");
		Yerdy.getInstance().onPause();
	}
	
	//Prime31 Activity Sharing plug-in doesn't appear to be correctly calling onWindowFocusChanged with current version.
	//Renamed to _name and removed static declaration until plug-in properly calls function and unity layer can be bypassed
	public void _onWindowFocusChanged(boolean hasFocus) {
		YRDLog.i(YerdyUnity.class, "onWindowFocusChanged:"+hasFocus);
		Activity activity = getRootActivity();
		Yerdy.getInstance().onWindowFocusChanged(hasFocus, activity);
	}
	
	public static void onKeyDown( int keyCode, KeyEvent event ) {
		YRDLog.i(YerdyUnity.class, "onKeyDown:"+keyCode);
	}
	 
	public static void onKeyUp( int keyCode, KeyEvent event ) {
		YRDLog.i(YerdyUnity.class, "onKeyDown:"+keyCode);
	}
	
	public static void onBackPressed() {
		YRDLog.i(YerdyUnity.class, "onBackPressed");
	}
	
	public void startWithPublisherKey(String publisherKey) {
		YRDLog.i(YerdyUnity.class, "startWithPublisherKey");
		Yerdy.getInstance().configureDelegate(this.yerdyDelegate);
		Yerdy.getInstance().configureMessageDelegate(this.messageDelegate);
		Yerdy.getInstance().startWithPublisherKey(getRootActivity(), publisherKey);
	}
	
	public void configureAppPlatform(YRDPlatform platform) {
		YRDLog.i(YerdyUnity.class, "configureAppPlatform:"+platform.getName());
		Yerdy.getInstance().configureAppPlatform(platform);
	}
	
	public void configureCurrencies(String c1, String c2, String c3, String c4, String c5, String c6) {
		YRDLog.i(YerdyUnity.class, "configureCurrencies:"+c1+","+c2+","+c3+","+c4+","+c5+","+c6);
		Yerdy.getInstance().configureCurrencies(getRootActivity(), new String[]{c1, c2, c3, c4, c5, c6});
	}
	
	public void setExistingCurrenciesForPreYerdyUser(Map<String, Integer> currencies) {
		YRDLog.i(YerdyUnity.class, "setExistingCurrenciesForPreYerdyUser:"+currencies.toString());
		Yerdy.getInstance().setExistingCurrenciesForPreYerdyUser(currencies);
	}
	
	public void setShouldTrackPreYerdyUserProgression(boolean flag) {
		YRDLog.i(YerdyUnity.class, "setShouldTrackPreYerdyUserProgression:"+flag);
		Yerdy.getInstance().setShouldTrackPreYerdyUserProgression(flag);
	}
	
	public void logScreenVisit(String name) {
		YRDLog.i(YerdyUnity.class, "logScreenVisit:"+name);
		Yerdy.getInstance().logScreenVisit(name);
	}
	
	public void configureGoogleLVLKey(String lvlKey) {
		YRDLog.i(YerdyUnity.class, "configureGoogleLVLKey");
		Yerdy.getInstance().configureGoogleLVLKey(getRootActivity(), lvlKey, lvlHandler);
	}
	
	public boolean isMessageAvailiable(String placement) {
		YRDLog.i(YerdyUnity.class, "isMessageAvailiable:"+placement);
		return Yerdy.getInstance().isMessageAvailiable(placement);
	}
	
	public void purchasedItem(String item, Map<String, Integer> currencies, boolean onSale) {
		YRDLog.i(YerdyUnity.class, "purchasedItem:"+item+","+currencies.toString());
		Yerdy.getInstance().purchasedItem(item, currencies, onSale);
	}
	
	public void purchasedInApp(YRDPurchase purchase, Map<String, Integer> currencies) {
		YRDLog.i(YerdyUnity.class, "purchasedInApp:"+purchase.getSku()+", "+ currencies.toString());
		Yerdy.getInstance().purchasedInApp(purchase, currencies);
	}

	public void earnedCurrency(Map<String, Integer> currencies) {
		YRDLog.i(YerdyUnity.class, "earnedCurrency:"+currencies.toString());
		Yerdy.getInstance().earnedCurrency(currencies);
	}
	
	public boolean showMessage(String placement) {
		YRDLog.i(YerdyUnity.class, "showMessage:"+placement);
		return Yerdy.getInstance().showMessage(getRootActivity(), placement);
	}
	
	public void dismissMessage() {
		YRDLog.i(YerdyUnity.class, "dismissMessage");
		Yerdy.getInstance().dismissMessage();
	}
	
	public boolean getIsPremiumUser() {
		YRDLog.i(YerdyUnity.class, "getIsPremiumUser");
		return Yerdy.getInstance().getIsPremiumUser(getRootActivity());
	}
	
	public void playerProgression(String category, String milestone) {
		YRDLog.i(YerdyUnity.class, "playerProgression:"+category+","+milestone);
		Yerdy.getInstance().logPlayerProgression(category, milestone);
	}
	
	public void logEvent(String name, Map<String, String>map) {
		YRDLog.i(YerdyUnity.class, "logEvent:"+map);
		Yerdy.getInstance().logEvent(name, map);
	}
	
	public void setMaxFailoverCount(int count, String placement) {
		YRDLog.i(YerdyUnity.class, "setMaxFailoverCount:"+count+","+placement);
		Yerdy.getInstance().setMaxFailoverCount(count, placement);
	}
	
	public void setLogLevel(YRDLogLevel level) {
		YRDLog.i(YerdyUnity.class, "setLogLevel:" + level.name());
		YRDLog.SetLogLevel(level);
	}
	
	YerdyDelegate yerdyDelegate = new YerdyDelegate() {
		@Override
		public void yerdyConnected(boolean success) {
			if(success)
				UnityPlayer.UnitySendMessage("YerdyCallbacks", "_YerdyConnected", "success");
			else
				UnityPlayer.UnitySendMessage("YerdyCallbacks", "_YerdyConnected", "error");
			// TODO Auto-generated method stub
		}
	};
	
	YerdyMessageDelegate messageDelegate = new YerdyMessageDelegate() {
		
		@Override
		public void willPresentMessageForPlacement(String placement) {
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_WillPresentMessage", (placement != null)?(placement):(""));
		}
		
		@Override
		public void willDismissMessageForPlacement(String placement) {
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_WillDismissMessage", (placement != null)?(placement):(""));
		}
		
		@Override
		public void didPresentMessageForPlacement(String placement) {
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_DidPresentMessage", (placement != null)?(placement):(""));
		}
		
		@Override
		public void didDismissMessageForPlacement(String placement) {
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_DidDismissMessage", (placement != null)?(placement):(""));
		}
		
		@Override
		public void handleReward(YRDReward reward) {
			List<String> message = new ArrayList<String>();
			for(YRDRewardItem rewardItem : reward.getRewards()) {
				message.add(rewardItem.getName()+','+rewardItem.getAmountString());
			}
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_HandleRewards", TextUtils.join(";", message));
		}
		
		@Override
		public void handleItemPurchase(YRDItemPurchase purchase) {
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_HandleItemPurchase", purchase.getItem());
		}
		
		@Override
		public void handleInAppPurchase(YRDInAppPurchase purchase) {
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_HandleInAppPurchase", purchase.getProductIdentifier());
		}
		
		@Override
		public void handleNavigation(String screen) {
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_HandleNavigation", screen);
		};
	};
	
	private LVLIHandler lvlHandler = new LVLIHandler() {
		@Override
		public void statusChanged(LVLStatus result) {
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_GoogleLVLStatusChanged", result.name());
		}
		
		@Override
		public void configurationFailed() {
			UnityPlayer.UnitySendMessage("YerdyCallbacks", "_GoogleLVLStatusChanged", "ERROR");
		}
	};
	
	public String getPushToken() {
		return YRDAnalytics.getInstance().getPushToken();
	}
	
	public String getPushTokenType() {
		return YRDAnalytics.getInstance().getPushTokenType();
	}
	
	public void logAdRequest(String network) {
		YRDLog.i(YerdyUnity.class, "logAdRequest:"+network);
		Yerdy.getInstance().logAdRequest(network);
	}
	
	public void logAdFill(String network) {
		YRDLog.i(YerdyUnity.class, "logAdFill:"+network);
		Yerdy.getInstance().logAdFill(network);
	}
}
