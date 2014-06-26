package com.yerdy.services;

/**
 * Defines global {@link com.yerdy.services.Yerdy Yerdy} callback
 * @author Chris
 *
 */
public interface YerdyDelegate {
	/**
	 * Called when a launch has been successfully reported to the {@link com.yerdy.services.Yerdy Yerdy} servers
	 * @param success
	 */
	public void yerdyConnected(boolean success);
}
