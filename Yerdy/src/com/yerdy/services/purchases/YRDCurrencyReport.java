package com.yerdy.services.purchases;

public class YRDCurrencyReport {
	private String _transationAmount;
	private String _totals;
	private String _earned;
	private String _purchased;
	private String _spent;
	
	public YRDCurrencyReport(String transactionAmount, String totals, String earned, String purchased, String spent) {
		_transationAmount = transactionAmount;
		_totals = totals;
		_earned = earned;
		_purchased = purchased;
		_spent = spent;
	}
	
	public String getTransactionAmount() {
		return _transationAmount;
	}
	
	public String getTotals() {
		return _totals;
	}
	
	public String getEarned() {
		return _earned;
	}
	
	public String getPurchased() {
		return _purchased;
	}
	
	public String getSpent() {
		return _spent;
	}
}
