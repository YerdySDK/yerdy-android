package com.yerdy.services.messaging;

/**
 * Pull message In app Purchase action
 * @author Chris
 */
public class YRDInAppPurchase extends YRDBasePurchaseAction {
	public YRDInAppPurchase(String action, int id) {
		super(action, id);
	}
	
	/**
	 * Returns IAP Product Identifier specified in the pull message
	 * @return - Product Identifier
	 */
	public String getProductIdentifier() {
		return getAction();
	}
}
