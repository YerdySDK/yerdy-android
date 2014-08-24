package com.yerdy.services.purchases;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import com.yerdy.services.util.JSONUtil;

import java.util.*;

public class PurchaseData implements Serializable {

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PurchaseData)) {
			return false;
		}
		PurchaseData other = (PurchaseData) o;
		return other._productID.equals(_productID) && other._receipt.equals(_receipt);
	}

	public static final String AMAZON_STORE = "amazon_store";
	public static final String GOOGLE_STORE = "google_store";

	private static final long serialVersionUID = 7838386074227686813L;

	private String _productCurrency;
	private String _currencyEarned;
	private String _currencyPurchased;
	private String _currencySpent;

	private int _lauches;
	private String _productID;
	private String _productValue;
	private String _receipt;
	private String _signature;
	private boolean _sandboxed;
	private String _store;
	private String _totalCurrency;
	private int _totalItemsPurchased;
	private long _totalSecondsPlayed;
	private String _user;
	private int _msgId = -1;
	private boolean _isValid = false;
	private boolean _onSale = false;
	private JSONArray _lastScreenVisits;
	private JSONArray _lastItemPurchases;
	private JSONArray _lastMessages;
	private JSONArray _lastPlayerProgressionCategories;
	private JSONArray _lastPlayerProgressionMilestones;

	public PurchaseData(YRDPurchase purchase, YRDCurrencyReport currencyReport) {
		if(purchase != null && purchase.getSku() != null && purchase.getSku().length() > 0) {
			if(purchase instanceof YRDPurchaseAmazon) {
				_isValid = true;
				_store = AMAZON_STORE;
			} else if (purchase instanceof YRDPurchaseGoogle) {
				_isValid = true;
				_store = GOOGLE_STORE;
			} else {
				_isValid = false;
			}
		}
		
		if(_isValid) {
			_productID = purchase.getSku();
			_productValue = purchase.getProductValue();
			_sandboxed = purchase.isSandbox();
			_onSale = purchase.isOnSale();
			_receipt = purchase.getReceipt();
			_signature = purchase.getSignature();
			_user = purchase.getUser();
			
			_productCurrency = currencyReport.getTransactionAmount();
			_totalCurrency = currencyReport.getTotals();
			_currencyEarned = currencyReport.getEarned();
			_currencyPurchased = currencyReport.getPurchased();
			_currencySpent = currencyReport.getSpent();
		}
	}
	
	public PurchaseData(String store) {
		_store = store;
		_isValid = true;
	}

	public String getProductCurrency() {
		return _productCurrency;
	}

	public String getCurrencyEarned() {
		return _currencyEarned;
	}

	public String getCurrencyPurchased() {
		return _currencyPurchased;
	}

	public String getCurrencySpent() {
		return _currencySpent;
	}
	
	public int getLauches() {
		return _lauches;
	}

	public String getProductID() {
		return _productID;
	}

	public String getProductValue() {
		return _productValue;
	}

	public String getReceipt() {
		return _receipt;
	}
	
	public String getSignature() {
		return _signature;
	}

	public String getStore() {
		return _store;
	}

	public String getTotalCurrency() {
		return _totalCurrency;
	}

	public int getTotalItemsPurchased() {
		return _totalItemsPurchased;
	}

	public long getTotalSecondsPlayed() {
		return _totalSecondsPlayed;
	}

	public String getUser() {
		return _user;
	}
	
	public int getMessageId() {
		return _msgId;
	}

	public void setProductCurrency(String coinCount) {
		this._productCurrency = coinCount;
	}

	public void setCurrencyEarned(String currencyEarned) {
		this._currencyEarned = currencyEarned;
	}

	public void setCurrencyPurchased(String currencyPurchased) {
		this._currencyPurchased = currencyPurchased;
	}

	public void setCurrencySpent(String currencySpent) {
		this._currencySpent = currencySpent;
	}

	public void setLauches(int lauches) {
		this._lauches = lauches;
	}

	public void setLaunches(int launches) {
		this._lauches = launches;
	}

	public void setProductID(String productID) {

		this._productID = productID;
	}

	public void setProductValue(String productValue) {
		this._productValue = productValue;
	}

	public void setReceipt(String receipt) {
		this._receipt = receipt;
	}
	
	public void setSignature(String signature) {
		this._signature = signature;
	}

	public void setTotalCurrency(String totalCurrency) {
		this._totalCurrency = totalCurrency;
	}

	public void setTotalItemsPurchased(int totalItemsPurchased) {
		this._totalItemsPurchased = totalItemsPurchased;
	}

	public void setTotalSecondsPlayed(long totalSecondsPlayed) {
		this._totalSecondsPlayed = totalSecondsPlayed;
	}

	public void setUser(String user) {
		this._user = user;
	}
	
	public void setMessageId(int msgId) {
		this._msgId = msgId;
	}
	
	public void setLastScreenVisits(List<String> lastScreenVisits) {
		_lastScreenVisits = new JSONArray(lastScreenVisits);
	}
	
	public void setLastItemPurchases(List<String> lastItemPurchases) {
		_lastItemPurchases = new JSONArray(lastItemPurchases);
	}
	
	public void setLastMessages(List<String> lastMessages) {
		_lastMessages = new JSONArray(lastMessages);
	}
	
	public void setLastPlayerProgressionCategories(List<String> lastPlayerProgressionCategories) {
		_lastPlayerProgressionCategories = new JSONArray(lastPlayerProgressionCategories);
	}
	
	public void setLastPlayerProgressionMilestones(List<String> lastPlayerProgressionMilestones) {
		_lastPlayerProgressionMilestones = new JSONArray(lastPlayerProgressionMilestones);
	}
	
	public List<String> getLastScreenVisits() {
		return JSONUtil.arrayToStringList(_lastScreenVisits);
	}
	
	public List<String> getLastItemPurchases() {
		return JSONUtil.arrayToStringList(_lastItemPurchases);
	}
	
	public List<String> getLastMessages() {
		return JSONUtil.arrayToStringList(_lastMessages);
	}
	
	public List<String> getLastPlayerProgressionCategories() {
		return JSONUtil.arrayToStringList(_lastPlayerProgressionCategories);
	}
	
	public List<String> getLastPlayerProgressionMilestones() {
		return JSONUtil.arrayToStringList(_lastPlayerProgressionMilestones);
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("store", _store);
		obj.put("productCurrency", _productCurrency);
		obj.put("currencyEarned", _currencyEarned);
		obj.put("currencyPurchased", _currencyPurchased);
		obj.put("currencySpent", _currencySpent);
		obj.put("lauches", _lauches);
		obj.put("productID", _productID);
		obj.put("productValue", _productValue);
		obj.put("receipt", _receipt);
		if(_signature != null)
			obj.put("signature", _signature);
		obj.put("sandboxed", _sandboxed);
		obj.put("onSale", _onSale);
		obj.put("totalCurrency", _totalCurrency);
		obj.put("totalItemsPurchased", _totalItemsPurchased);
		obj.put("totalSecondsPlayed", _totalSecondsPlayed);
		if(_user != null)
			obj.put("user", _user);
		if(_msgId != -1)
			obj.put("msgId", _msgId);

		obj.putOpt("lastScreenVisits", _lastScreenVisits);
		obj.putOpt("lastItemPurchases", _lastItemPurchases);
		obj.putOpt("lastMessages", _lastMessages);
		obj.putOpt("lastPlayerProgressionCategories", _lastPlayerProgressionCategories);
		obj.putOpt("lastPlayerProgressionMilestones", _lastPlayerProgressionMilestones);
		
		return obj;
	}

	public static PurchaseData parseJSON(JSONObject purchaseJSON) throws JSONException {
		PurchaseData data = new PurchaseData(purchaseJSON.getString("store"));
		data._productCurrency = purchaseJSON.getString("productCurrency");
		data._currencyEarned = purchaseJSON.getString("currencyEarned");
		data._currencyPurchased = purchaseJSON.getString("currencyPurchased");
		data._currencySpent = purchaseJSON.getString("currencySpent");
		data._lauches = purchaseJSON.getInt("lauches");
		data._productID = purchaseJSON.getString("productID");
		data._productValue = purchaseJSON.getString("productValue");
		data._receipt = purchaseJSON.getString("receipt");
		if(purchaseJSON.has("signature"))
			data._signature = purchaseJSON.getString("signature");
		data._sandboxed = purchaseJSON.getBoolean("sandboxed");
		data._onSale = purchaseJSON.getBoolean("onSale");
		data._totalCurrency = purchaseJSON.getString("totalCurrency");
		data._totalItemsPurchased = purchaseJSON.getInt("totalItemsPurchased");
		data._totalSecondsPlayed = purchaseJSON.getInt("totalSecondsPlayed");
		if(purchaseJSON.has("user"))
			data._user = purchaseJSON.getString("user");
		if(purchaseJSON.has("msgId"))
			data._msgId = purchaseJSON.getInt("msgId");
		
		data._lastScreenVisits = purchaseJSON.optJSONArray("lastScreenVisits");
		data._lastItemPurchases = purchaseJSON.optJSONArray("lastItemPurchases");
		data._lastMessages = purchaseJSON.optJSONArray("lastMessages");
		data._lastPlayerProgressionCategories = purchaseJSON.optJSONArray("lastPlayerProgressionCategories");
		data._lastPlayerProgressionMilestones = purchaseJSON.optJSONArray("lastPlayerProgressionMilestones");
		
		return data;
	}

	public void setSandboxed(boolean sandboxed) {
		this._sandboxed = sandboxed;
	}

	public boolean isSandboxed() {
		return _sandboxed;
	}

	public boolean isOnSale() {
		return _onSale;
	}
	
	public boolean isValid() {
		return _isValid;
	}


}
