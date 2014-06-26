package com.yerdy.services.messaging;

/**
 * Pull Message reward item
 * @author Chris
 */
public class YRDRewardItem {
	
	private String _name;
	private String _amountStr;
	private int _amount;
	public YRDRewardItem(String name, String amount) {
		_name = name;
		_amountStr = amount;
		try {
			_amount = Integer.parseInt(amount);
		} catch (NumberFormatException e) {
			_amount = 0;
		}
	}
	
	public String getName() {
		return _name;
	}
	
	public String getAmountString() {
		return _amountStr;
	}
	
	public int getAmount() {
		return _amount;
	}
}
