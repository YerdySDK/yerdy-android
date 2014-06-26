package com.yerdy.services.purchases;

/**
 * Purchase object used in reporting purchases via {@link com.yerdy.services.Yerdy #purchasedInApp(YRDPurchase, java.util.Map)} specifically for Google Play Store based purchases
 * 
 * purchases cannot be properly validated against cheaters unless all information is provided
 * 
 * @author Chris
 *
 */
public class YRDPurchaseGoogle extends YRDPurchase {
	/**
	 * @param sku purchase sku
	 * @param productValue purchase value in real currency
	 * @param googleReceipt google purchase receipt
	 * @param googleSignature google purchase signature
	 * @param isSandbox if it is a testing purchase purchase
	 */
	public YRDPurchaseGoogle(String sku, String productValue, String googleReceipt, String googleSignature, boolean isSandbox) {
		super(sku, productValue, googleReceipt, googleSignature, null, isSandbox);
	}
}
