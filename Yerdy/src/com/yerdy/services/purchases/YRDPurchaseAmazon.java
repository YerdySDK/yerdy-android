package com.yerdy.services.purchases;

/**
 * Purchase object used in reporting purchases via {@link com.yerdy.services.Yerdy #purchasedInApp(YRDPurchase, java.util.Map)} specifically for Amazon Appstore based purchases
 * 
 * purchases cannot be properly validated against cheaters unless all information is provided
 * 
 * @author Chris
 */
public class YRDPurchaseAmazon extends YRDPurchase {
	/**
	 * @param sku purchase sku
	 * @param productValue purchase value in real currency
	 * @param amazonReceipt amazon purchase receipt
	 * @param amazonUser amazon user name
	 * @param isSandbox if it is a testing purchase purchase
	 */
	public YRDPurchaseAmazon(String sku, String productValue, String amazonReceipt, String amazonUser, boolean isSandbox) {
		super(sku, productValue, amazonReceipt, null, amazonUser, isSandbox);
	}
}
