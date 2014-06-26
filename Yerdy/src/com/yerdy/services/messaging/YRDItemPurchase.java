package com.yerdy.services.messaging;
/**
 * Pull message Virtual Purchase action
 * @author Chris
 */
public class YRDItemPurchase extends YRDBasePurchaseAction {
	public YRDItemPurchase(String action, int id) {
		super(action, id);
	}
	
	/**
	 * Returns Virtual Product Identifier specified in the pull message
	 * @return - Product Identifier
	 */
	public String getItem() {
		return getAction();
	}
}
