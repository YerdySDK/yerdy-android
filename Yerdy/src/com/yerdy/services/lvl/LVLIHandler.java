package com.yerdy.services.lvl;

/**
 * Interface used for Google LVL validation
 * @author Chris
 *
 */
public interface LVLIHandler {
	public void configurationFailed();
	public void statusChanged(LVLStatus result);
}
