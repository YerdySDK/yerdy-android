package com.yerdy.services.purchases;

import com.yerdy.services.core.YRDClient;

public abstract class YRDReportVirtualPurchaseClient extends YRDClient {

	public abstract void saveVirtualPurchaseServiceFailed(Exception error, int responseCode);

	public abstract void saveVirtualPurchaseServiceSucceeded(int resultCode);

}
