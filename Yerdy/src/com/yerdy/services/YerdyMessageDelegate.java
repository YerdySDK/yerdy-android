package com.yerdy.services;

import com.yerdy.services.messaging.YRDInAppPurchase;
import com.yerdy.services.messaging.YRDItemPurchase;
import com.yerdy.services.messaging.YRDReward;

/**
 * Defines all the messaging related delegate methods, an error will display in logcat if not implemented
 * 
 * <h3>Messaging Lifecycle</h3>
 * <p>App requests that a message be shown</p>
 * <ul><li>yerdy:willPresentMessageForPlacement:</li></ul>
 * <p>Message is presented</p>
 * <ul><li>{@link #didDismissMessageForPlacement(String)}</li></ul>
 * <p>User interacts with message</p>
 * <ul><li>{@link #willDismissMessageForPlacement(String)}</li></ul>
 * <p>Message is dismissed</p>
 * <ul><li>{@link #didDismissMessageForPlacement(String)}</li></ul>
 * <p>If the message has action that the app should handle, one of:</p>
 * <ul><li>{@link #handleInAppPurchase(YRDInAppPurchase)}</li>
 * <li>{@link #handleItemPurchase(YRDItemPurchase)}</li>
 * <li>{@link #handleReward(YRDReward)}</li></ul>
 * @author Chris
 */
public interface YerdyMessageDelegate {

	/**
	 * Called right before a message is presented
	 * @param placement The placement passed in to {@link com.yerdy.services.Yerdy #showMessage(android.app.Activity, String)}
	 * @see #didPresentMessageForPlacement(String)
	 */
	public void willPresentMessageForPlacement(String placement);
	
	/**
	 * Called right after a message is presented (i.e. after it has animated in)
	 * @param placement The placement passed in to {@link com.yerdy.services.Yerdy #showMessage(android.app.Activity, String)}
	 * @see #willPresentMessageForPlacement(String)
	 */
	public void didPresentMessageForPlacement(String placement);

	/**
	 * Called after a user has tapped a button but before the message has been dismissed
	 * @param placement The placement passed in to {@link com.yerdy.services.Yerdy #showMessage(android.app.Activity, String)}
	 * @see #didDismissMessageForPlacement(String)
	 */
	public void willDismissMessageForPlacement(String placement);

	/**
	 * Called after a message has been dismissed (i.e. after it has animated out)
	 * @param placement The placement passed in to {@link com.yerdy.services.Yerdy #showMessage(android.app.Activity, String)}
	 * @see #willDismissMessageForPlacement(String)
	 */
	public void didDismissMessageForPlacement(String placement);

	/**
	 * Called when your app should handle an in-app purchase
	 * @param purchase An object containing the product identifier for the in-app purchase
	 * @see #handleItemPurchase(YRDItemPurchase)
	 * @see #handleReward(YRDReward)
	 * @see #handleNavigation(String)
	 */
	public void handleInAppPurchase(YRDInAppPurchase purchase);

	/**
	 * Called when your app should handle an in-game item purchase
	 * @param purchase An object containing the name of the in-game item to purchase
	 * @see #handleInAppPurchase(YRDInAppPurchase)
	 * @see #handleReward(YRDReward)
	 * @see #handleNavigation(String)
	 */
	public void handleItemPurchase(YRDItemPurchase purchase);

	/**
	 * Called when your app should handle a reward
	 * @param reward An object containing the rewards for the user
	 * @see #handleItemPurchase(YRDItemPurchase)
	 * @see #handleInAppPurchase(YRDInAppPurchase)
	 * @see #handleNavigation(String)
	 */
	public void handleReward(YRDReward reward);

	/**
	 * Called when your app should navigate to a screen
	 * @param screen of screen to navigate to
	 * @see #handleItemPurchase(YRDItemPurchase)
	 * @see #handleInAppPurchase(YRDInAppPurchase)
	 * @see #handleReward(YRDReward)
	 */
	public void handleNavigation(String screen);
	
	
}
