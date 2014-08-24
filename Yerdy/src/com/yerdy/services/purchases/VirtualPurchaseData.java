package com.yerdy.services.purchases;

import org.json.*;

public class VirtualPurchaseData {
	
	private String itemIdentifier;
	private String transactionAmount;
	private boolean firstPurchase;
	private int postIapIndex;
	private int messageId;
	private boolean onSale;
	
	public VirtualPurchaseData() {
	}
	
	public VirtualPurchaseData(String itemIdentifier, String transactionAmount, boolean firstPurchase, int postIapIndex, int messageId, boolean onSale) {
		this.itemIdentifier = itemIdentifier;
		this.transactionAmount = transactionAmount;
		this.firstPurchase = firstPurchase;
		this.postIapIndex = postIapIndex;
		this.messageId = messageId;
		this.onSale = onSale;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject retVal = new JSONObject();
		retVal.putOpt("itemIdentifier", itemIdentifier);
		retVal.putOpt("transactionAmount", transactionAmount);
		retVal.put("firstPurchase", firstPurchase);
		retVal.put("postIapIndex", postIapIndex);
		retVal.put("messageId", messageId);
		retVal.put("onSale", onSale);
		return retVal;
	}
	
	public static VirtualPurchaseData fromJSON(JSONObject json) throws JSONException {
		VirtualPurchaseData retVal = new VirtualPurchaseData();
		retVal.itemIdentifier = json.optString("itemIdentifier");
		retVal.transactionAmount = json.optString("transactionAmount");
		retVal.firstPurchase = json.optBoolean("firstPurchase");
		retVal.postIapIndex = json.optInt("postIapIndex");
		retVal.messageId = json.optInt("messageId");
		retVal.onSale = json.optBoolean("onSale");
		return retVal;
	}

	public String getItemIdentifier() {
		return itemIdentifier;
	}

	public void setItemIdentifier(String value) {
		itemIdentifier = value;
	}

	public String getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(String value) {
		transactionAmount = value;
	}

	public boolean getFirstPurchase() {
		return firstPurchase;
	}

	public void setFirstPurchase(boolean value) {
		firstPurchase = value;
	}

	public int getPostIapIndex() {
		return postIapIndex;
	}

	public void setPostIapIndex(int value) {
		postIapIndex = value;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int value) {
		messageId = value;
	}

	public boolean getOnSale() {
		return onSale;
	}

	public void setOnSale(boolean value) {
		onSale = value;
	}
	
}
