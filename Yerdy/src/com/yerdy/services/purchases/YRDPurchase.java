package com.yerdy.services.purchases;

/**
 * Root object for in-app purchase tracking that does not contain store specific data
 * @author Chris
 */
public class YRDPurchase {

	private String _productId = null;
	private String _productValue = null;
	private boolean _sandboxed = false;
	private String _receipt = null;
	private String _signature = null;
	private String _user = null;
	private boolean _onSale = false;
	
	protected YRDPurchase(String productId, String productValue, String receipt, String signature, String user, boolean sandboxed) {
		_productId = productId;
		_productValue = productValue;
		_sandboxed = sandboxed;
		_receipt = receipt;
		_signature = signature;
		_user = user;
	}
	
	public String getSku() {
		return _productId;
	}
	
	public String getProductValue() {
		return _productValue;
	}
	
	public boolean isSandbox() {
		return _sandboxed;
	}
	
	public String getReceipt() {
		return _receipt;
	}
	
	public String getSignature() {
		return _signature;
	}
	
	public String getUser() {
		return _user;
	}
	
	public boolean isOnSale() {
		return _onSale;
	}
	
	public void onSale(boolean flag) {
		_onSale = flag;
	}
}
