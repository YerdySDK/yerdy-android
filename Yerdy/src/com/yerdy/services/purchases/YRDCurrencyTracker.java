package com.yerdy.services.purchases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.yerdy.services.core.YRDPersistence;
import com.yerdy.services.core.YRDPersistence.AnalyticKey;
import com.yerdy.services.logging.YRDLog;

public class YRDCurrencyTracker {
	private static final Integer MAX_CURRENCIES = 6;
	private static final String TOKEN = ";";
	private static final String DEFAULT_ZERO = "0;0;0;0;0;0";
	
	private List<String> _names = new ArrayList<String>(MAX_CURRENCIES);
	private List<Integer> _earned = new ArrayList<Integer>(MAX_CURRENCIES);
	private List<Integer> _spent = new ArrayList<Integer>(MAX_CURRENCIES);
	private List<Integer> _purchased = new ArrayList<Integer>(MAX_CURRENCIES);
	
	//Using json objects over maps due to ease of persistance
	private JSONObject _timedCurrency = new JSONObject();
	
	private YRDPersistence _persistance;
	
	public YRDCurrencyTracker(Context cxt) {
		_persistance = new YRDPersistence(cxt, cxt.getApplicationInfo().packageName, false);
		String conversion = _persistance.getValue(AnalyticKey.EARNED_CURRENCY_LIFETIME, DEFAULT_ZERO);
		_earned = parse(conversion);
		YRDLog.i(getClass(), "Earned: " + conversion);
		
		conversion = _persistance.getValue(AnalyticKey.SPENT_CURRENCY_LIFETIME, DEFAULT_ZERO);
		_spent = parse(conversion);
		YRDLog.i(getClass(), "Spent: " + conversion);
		
		conversion = _persistance.getValue(AnalyticKey.PURCHASED_CURRENCY_LIFETIME, DEFAULT_ZERO);
		_purchased = parse(conversion);
		YRDLog.i(getClass(), "Purchased: " + conversion);
		
		_timedCurrency = _persistance.getCounter(AnalyticKey.CURRENCY_TIMED);
	}
	
	public void configure(String[] names) {
		for(int i = 0; i < names.length; i++) {
			String name = names[i];
			if(i >= MAX_CURRENCIES) {
				YRDLog.e(this.getClass(), String.format("Outside range of possible currencies ignoring \"%s\"", name));
				continue;
			} else {
				_names.add(i, name);
			}
		}
	}
	
	private int indexForCurrency(String name) {
		return _names.indexOf(name);
	}
	
	public void spentCurrencies(String name, int value) {
		updateCurrency(_spent, name, value, "spent");
		String conversion = currencySpent();
		_persistance.setValue(AnalyticKey.SPENT_CURRENCY_LIFETIME, conversion);
		_persistance.save();
		YRDLog.i(getClass(), "Spent: " + conversion);
	}
	
	public void spentCurrencies(Map<String, Integer> currencies) {
		updateCurrency(_spent, currencies, "spent");
		String conversion = currencySpent();
		_persistance.setValue(AnalyticKey.SPENT_CURRENCY_LIFETIME, conversion);
		_persistance.save();
		YRDLog.i(getClass(), "Spent: " + conversion);
	}
	
	public void earnedCurrencies(String name, int value) {
		updateCurrency(_earned, name, value, "earned");
		String conversion = currencyEarned();
		_persistance.setValue(AnalyticKey.EARNED_CURRENCY_LIFETIME, conversion);
		_persistance.save();
		YRDLog.i(getClass(), "Earned: " + conversion);
	}
	
	public void earnedCurrencies(Map<String, Integer> currencies) {
		updateCurrency(_earned, currencies, "earned");
		String conversion = currencyEarned();
		_persistance.setValue(AnalyticKey.EARNED_CURRENCY_LIFETIME, conversion);
		_persistance.save();
		YRDLog.i(getClass(), "Earned: " + conversion);
	}
	
	public void boughtCurrencies(String name, int value) {
		updateCurrency(_purchased, name, value, "purchased");
		String conversion = currencyPurchased();
		_persistance.setValue(AnalyticKey.PURCHASED_CURRENCY_LIFETIME, conversion);
		_persistance.save();
		YRDLog.i(getClass(), "Bought: " + conversion);
	}
	
	public void boughtCurrencies(Map<String, Integer> currencies) {
		updateCurrency(_purchased, currencies, "purchased");
		String conversion = currencyPurchased();
		_persistance.setValue(AnalyticKey.PURCHASED_CURRENCY_LIFETIME, conversion);
		_persistance.save();
		YRDLog.i(getClass(), "Bought: " + conversion);
	}
	
	private void updateCurrency(List<Integer> target, String name, int value, String counterLabel) {
		if(target == null) {
			YRDLog.e(this.getClass(), "Critical error loading currency");
		} else if(name == null) {
			YRDLog.e(this.getClass(), "Currency is null and being ignored");
		} else {
			int index = indexForCurrency(name);
			if(index >= 0 && index < MAX_CURRENCIES) {
				target.set(index, target.get(index) + value);
				updateMilestones(counterLabel, index, value);
				_persistance.setCounter(AnalyticKey.CURRENCY_TIMED, _timedCurrency);
				_persistance.save();
			} else {
				YRDLog.e(this.getClass(), String.format("'%s' unrecognized as a valid currency", name));
			}
		}
	}
	
	private void updateCurrency(List<Integer> target, Map<String, Integer> currencies, String counterLabel) {
		if(target == null) {
			YRDLog.e(this.getClass(), "Critical error loading currency");
		} else if(currencies == null || currencies.size() <= 0) {
			YRDLog.e(this.getClass(), "Invalid currency collection");
		} else {
			boolean updatedMilestones = false;
			for(String name : currencies.keySet()) {
				int value = currencies.get(name);
				int index = indexForCurrency(name);
				if(index >= 0 && index < MAX_CURRENCIES) {
					target.set(index, target.get(index) + value);
					updateMilestones(counterLabel, index, value);
					updatedMilestones = true;
				} else {
					YRDLog.e(this.getClass(), String.format("'%s' unrecognized as a valid currency", name));
				}
			}
			
			if(updatedMilestones) {
				_persistance.setCounter(AnalyticKey.CURRENCY_TIMED, _timedCurrency);
				_persistance.save();
			}
		}
	}
	
	private void updateMilestones(String label, int index, int value) {
		String jsonLabel = label + "-" + (index + 1);
		int currentValue = 0;
		currentValue = _timedCurrency.optInt(jsonLabel, 0);
		try {
			_timedCurrency.put(jsonLabel, (currentValue + value));
		} catch (JSONException e) { }
	}
	
	private List<Integer> parse(String currencyString) {
		String[] values = currencyString.split(TOKEN);
		Integer[] intValues = {0,0,0,0,0,0};
		for(int i = 0; i < values.length && i < intValues.length; i++) {
			try {
				intValues[i] = Integer.parseInt(values[i]);
			} catch (Exception e) {
				
			}
		}
		return Arrays.asList(intValues);
	}
	
	private String parse(Map<String, Integer> currencies) {
		List<Integer> tempValues = new ArrayList<Integer>(Arrays.asList(0,0,0,0,0,0));
		if(currencies != null) {
			for(String key : currencies.keySet()) {
				int index = _names.indexOf(key);
				if(index >= 0)
					tempValues.set(index, currencies.get(key));
				else
					cannotParse(key);
			}
		} else {
			return DEFAULT_ZERO;
		}
		return TextUtils.join(TOKEN, tempValues);
	}
	
	private static void cannotParse(String label) {
		YRDLog.e(YRDCurrencyTracker.class, "Cannot parse currency of " + label);
	}
	
	public YRDCurrencyReport generateCurrencyReport(Map<String, Integer> currencies) {
		YRDCurrencyReport report = new YRDCurrencyReport(parse(currencies), currencyBalance(), currencyEarned(), currencyPurchased(), currencySpent());
		return report;
	}
	
	public JSONObject getAndResetTimedCurrency() {
		JSONObject response = new JSONObject();
		try {
			response = new JSONObject(_timedCurrency.toString());
		} catch (JSONException e) { }
		_timedCurrency = new JSONObject();
		_persistance.deleteKey(AnalyticKey.CURRENCY_TIMED);
		_persistance.save();
		return response;
	}
	
	private String currencyEarned() {
		return TextUtils.join(TOKEN, _earned);
	}
	
	private String currencySpent() {
		return TextUtils.join(TOKEN, _spent);
	}
	
	private String currencyPurchased() {
		return TextUtils.join(TOKEN, _purchased);
	}
	
	public String currencyBalance() {
		List<Integer> totals = new ArrayList<Integer>(Arrays.asList(0,0,0,0,0,0));
		for(int i = 0; i < totals.size(); i++) {
			totals.set(i, _earned.get(i) + _purchased.get(i) - _spent.get(i));
		}
		return TextUtils.join(TOKEN, totals);
	}

	public boolean setInitialCurrencies(Map<String, Integer> currencies) {
		boolean isSet = _persistance.getValue(AnalyticKey.SET_INITIAL_CURRENCY, false);
		if(!isSet) {
			earnedCurrencies(currencies);
			_persistance.setValue(AnalyticKey.SET_INITIAL_CURRENCY, true);
			_persistance.save();
			return true;
		}
		return false;
	}

}
