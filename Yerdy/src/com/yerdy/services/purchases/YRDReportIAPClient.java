package com.yerdy.services.purchases;

import com.yerdy.services.core.YRDClient;

public abstract class YRDReportIAPClient extends YRDClient {

	PurchaseData purchaseData;

	public abstract void savePurchaseServiceSucceeded(int resultCode);

	public abstract void savePurchaseServiceFailed(Exception e, int resultCode);

	public PurchaseData getPurchaseData() {
		return purchaseData;
	}

	public void setPurchaseData(PurchaseData purchaseData) {
		this.purchaseData = purchaseData;
	}

}