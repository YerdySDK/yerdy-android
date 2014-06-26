package com.yerdy.services.lvl;

import android.content.Context;

import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.StrictPolicy;
import com.yerdy.services.logging.YRDLog;

/**
 * Class manages the integration with the Gooogle LVL checks
 * @author Chris
 */
public class LVLManager {
	
	private LicenseCheckerCallback _lvlCallback;
	private LicenseChecker _lvlChecker;
	private LVLStatus _lvlStatus = LVLStatus.NOT_APPLICABLE;
	private LVLIHandler _lvlHandler = null;
	
	private static LVLManager _instance = null;
	private String _key = null;
	
	/**
	 * @return
	 */
	public static LVLManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new LVLManager();
		}
		return _instance;
	}
	
	public LVLManager() {}
	
	
	/**
	 * @param cxt - Application context
	 * @param key - Google LVL Key
	 * @param handler - callback handler
	 */
	public void configureAndRunIfNeeded(Context cxt, String key, LVLIHandler handler)
	{
		_lvlHandler = handler;
		if(key != null)
		{
			YRDLog.i(LVLManager.class, "Configure Check");
			if(_key != key)
				_key = key;
						
			if(_lvlCallback == null)
				_lvlCallback = new LVLCheckerCallback();
			
			if(_lvlChecker == null)
			{
				StrictPolicy policy = new StrictPolicy();
				_lvlChecker = new LicenseChecker(cxt, policy, _key);
			}
			runCheck();
		}
		else
		{
			if(_lvlHandler != null)
				_lvlHandler.configurationFailed();
			YRDLog.i(LVLManager.class, "Failed to configure, invalid key");
		}
	}
	
	/**
	 * attempts to prep and check Google LVL
	 */
	private void runCheck()
	{
		setStatus(LVLStatus.NONE);
		if(_key != null && _lvlChecker != null && _lvlCallback != null)
		{
			YRDLog.i(LVLManager.class, "Run Check");
			_lvlChecker.checkAccess(_lvlCallback);
		}
		else
		{
			YRDLog.i(LVLManager.class, "failed to run check");
		}
	}
	
	/**
	 * Sets resulting status
	 * @param status
	 */
	private void setStatus(LVLStatus status)
	{
		YRDLog.i(LVLManager.class, "status = " + status.getKey());
		_lvlStatus = status;
		if(_lvlHandler != null)
			_lvlHandler.statusChanged(status);
	}
	
	/**
	 * Cleanup
	 */
	public void onDestroy() {
		if(_lvlChecker != null)
			_lvlChecker.onDestroy();
		if(_lvlHandler != null)
			_lvlHandler = null;
	}
	
	/**
	 * Returns current Google LVL check status
	 * {@link LVLStatus}
	 * @return
	 */
	public LVLStatus getStatus()
	{
		return _lvlStatus;
	}
	
	/**
	 * Listens and handles result from Google LVL check
	 * @author Chris
	 */
	private class LVLCheckerCallback implements LicenseCheckerCallback
	{
		@Override
		public void allow(int policyReason) {
			YRDLog.i(LVLManager.class, "allow = " + policyReason);
			setStatus((policyReason == Policy.RETRY)?(LVLStatus.NONE):(LVLStatus.LICENSED));
		}

		@Override
		public void dontAllow(int policyReason) {
			YRDLog.i(LVLManager.class, "dontAllow = " + policyReason);
			setStatus((policyReason == Policy.RETRY)?(LVLStatus.NONE):(LVLStatus.UNLICENSED));
		}

		@Override
		public void applicationError(int policyReason) {
			switch (policyReason) {
			case 3:
				YRDLog.i(LVLManager.class, "applicationError = not managed");
				break;
			case 4:
				YRDLog.i(LVLManager.class, "applicationError = server failure");
				break;
			case 5:
				YRDLog.i(LVLManager.class, "applicationError = over quota");
				break;
			case 0x101:
				YRDLog.i(LVLManager.class, "applicationError = error contacting server");
				break;
			case 0x102:
				YRDLog.i(LVLManager.class, "applicationError = invalid package name");
				break;
			case 0x103:
				YRDLog.i(LVLManager.class, "applicationError = non matching uid");
				break;
			default:
				YRDLog.i(LVLManager.class, "applicationError = " + policyReason);
				break;
			}
				
			setStatus(LVLStatus.NONE);
		}
	}
}
