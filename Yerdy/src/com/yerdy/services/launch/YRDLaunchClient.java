package com.yerdy.services.launch;

import java.util.Map;

import org.json.JSONObject;

import com.yerdy.services.core.YRDClient;

/**
 * Extension of YRDClient class for launch service requests
 * @author m2
 */
public abstract class YRDLaunchClient extends YRDClient {

	private boolean _attemptingInstallationReport = false;
	private JSONObject _navigationEvents = new JSONObject();
	private Map<String, String> _adReport = null;

	public abstract void launchReported();

	public abstract void launchReportFailed(Exception error);
	
	public void setAttemptingInstallationReport(boolean attemptingInstallationReport) {
		this._attemptingInstallationReport = attemptingInstallationReport;
	}

	public boolean isAttemptingInstallationReport() {
		return _attemptingInstallationReport;
	}
	
	public abstract void launchSkipped();
	
	public JSONObject getScreenVisits() {
		return _navigationEvents;
	}
	
	public void setScreenVisits(JSONObject value) {
		_navigationEvents = value;
	}
	
	public void setAdPerformance(Map <String, String> adReport) {
		_adReport = adReport;
	}
	
	public Map<String, String> getAdPerformance() {
		return _adReport;
	}

}