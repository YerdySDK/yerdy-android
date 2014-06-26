package com.yerdy.Sample;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * @author Chis
 * 
 * This is for persistance of currency balances for demo purposes
 */

public class CurrencyManager {

	private SharedPreferences prefs;
	
	private int _gold = 0;
	private int _silver = 0;
	private int _bronze = 0;
	private int _diamonds = 0;
	private int _pearls = 0;
	private int _rubies = 0;
	
	public enum CURRENCIES {
		GOLD("Gold"),
		SILVER("Silver"),
		BRONZE("Bronze"),
		DIAMONDS("Diamonds"),
		PEARLS("Pearls"),
		RUBIES("Rubies");
		
		private String _name = null;
		
		private CURRENCIES(String name) {
			_name = name;
		}
		
		public String getName() {
			return _name;
		}
		
		public boolean is(String value) {
			return (_name.equals(value));
		}
		
		public static CURRENCIES match(String name) {
			for(CURRENCIES currency : CURRENCIES.values()) {
				if(currency.is(name))
					return currency;
			}
			
			return null;
		}
	}
	
	public CurrencyManager(Context cxt) {
		prefs = cxt.getSharedPreferences("sampleCurrencyManager", Context.MODE_PRIVATE);
		_gold = prefs.getInt(CURRENCIES.GOLD.getName(), 0);
		_silver = prefs.getInt(CURRENCIES.SILVER.getName(), 0);
		_bronze = prefs.getInt(CURRENCIES.BRONZE.getName(), 0);
		_diamonds = prefs.getInt(CURRENCIES.DIAMONDS.getName(), 0);
		_pearls = prefs.getInt(CURRENCIES.PEARLS.getName(), 0);
		_rubies = prefs.getInt(CURRENCIES.RUBIES.getName(), 0);
	}
	
	public void add(int g, int s, int b, int d, int p, int r)
	{
		_gold += g;
		_silver += s;
		_bronze += b;
		_diamonds += d;
		_pearls += p;
		_rubies += r;
		commitCurrencies();
	}
	
	public boolean subtract(int g, int s, int b, int d, int p, int r)
	{
		if(_gold >= g && _silver >= s && _bronze >= b && _diamonds >= d && _pearls >= p && _rubies >= r)
		{
			_gold -= g;
			_silver -= s;
			_bronze -= b;
			_diamonds -= d;
			_pearls -= p;
			_rubies -= r;
			commitCurrencies();
			
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void commitCurrencies() {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(CURRENCIES.GOLD.getName(), _gold);
		editor.putInt(CURRENCIES.SILVER.getName(), _silver);
		editor.putInt(CURRENCIES.BRONZE.getName(), _bronze);
		editor.putInt(CURRENCIES.DIAMONDS.getName(), _diamonds);
		editor.putInt(CURRENCIES.PEARLS.getName(), _pearls);
		editor.putInt(CURRENCIES.RUBIES.getName(), _rubies);
		editor.commit();
	}
	
	public int getGold() {
		return _gold;
	}
	
	public int getSilver() {
		return _silver;
	}
	
	public int getBronze() {
		return _bronze;
	}
	
	public int getDiamonds() {
		return _diamonds;
	}
	
	public int getPearls() {
		return _pearls;
	}
	
	public int getRubies() {
		return _rubies;
	}
}
