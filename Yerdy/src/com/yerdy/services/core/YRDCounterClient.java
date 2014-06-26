package com.yerdy.services.core;

import com.yerdy.services.core.YRDClient;

public abstract class YRDCounterClient extends YRDClient {

	public abstract void counterServiceFailed(Exception error, int responseCode);

	public abstract void counterServiceSucceeded(int resultCode);

}
