package com.yerdy.services.launch;

/**
 * Interface method for listening for AB tag changes
 * @author Chris
 */

public interface YRDABTagChangeDelegate {

	void abTestingTagChanged(String currentTag);

}
