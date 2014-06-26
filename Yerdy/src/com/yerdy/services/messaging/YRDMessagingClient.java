package com.yerdy.services.messaging;

import java.util.List;

import com.yerdy.services.core.YRDClient;

/**
 * Delegate/Listener for {@link YRDMessagingService}
 * @author m2
 *
 */
public abstract class YRDMessagingClient extends YRDClient {

	/**
	 * Delegate is notified with this method after successfully retrieving messages from the server 
	 * @param message see {@link YRDMessagePreseneter}
	 */
	public abstract void onSuccess(List<YRDMessage> messages);

	/**
	 * Delegate is notified with this method when service determines there are any number of issues retrieving message from server
	 * @param Exception will describe the error in some detail
	 * 
	 */
	public abstract void onError(Exception e);

}