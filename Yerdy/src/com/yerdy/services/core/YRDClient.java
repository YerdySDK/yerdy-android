package com.yerdy.services.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.yerdy.services.logging.YRDLog;

/**
 * This is a marker for Services to be passed via the intent mechasnism.
 * Originally, the client was meant to be passed around with the Intent it 
 * was meant to address, and was made Serializable.  
 * This turned out to be agonizingly impractical when the majority of 
 * implementing subclasses were found to be anonymous, requiring their housing 
 * classes be Serializable too.  
 * Ultimately, a lookup value to a client via a static collection was used in lieu of this mechanism.
 * 
 * @author m2
 * 
 */
public abstract class YRDClient {

	/**
	 * Maintain a pool of clients as Intents and Services are prone to recreating themselves without their associated clients
	 */
	private static Map<Long, YRDClient> _clientPool = Collections.synchronizedMap(new HashMap<Long, YRDClient>());
	private long _poolId;

	public YRDClient() {
		_poolId = hashCode();
		_clientPool.put(_poolId, this);
	}

	public long getLookupValue() {
		YRDLog.d(getClass(), "getLookupValue():" + _poolId );
		
		return _poolId;
	}

	public static YRDClient doLookup(long lookupValue) {
		YRDLog.d(YRDClient.class, "doLookup("+ lookupValue +")");
		return _clientPool.get(lookupValue);
	}

	public static void clean(long lookupValue) {
		YRDLog.d(YRDClient.class, "clean("+ lookupValue +")");
		_clientPool.remove(lookupValue);
		
	}
}
