package com.yerdy.services.purchases;

import java.util.StringTokenizer;

import com.yerdy.services.logging.YRDLog;

public class Currency {

	private static final String TOKEN = ";";
	private static Currency defaultZeros;

	static int numCurrencies = 1;

	int[] representation;

	private Currency() {
		representation = new int[numCurrencies];
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(representation[0]);

		for (int i = 1; i < numCurrencies; i++) {
			builder.append(TOKEN).append(representation[i]);
		}
		return builder.toString();
	}

	public static Currency parse(String currencyString) {
		Currency currency = new Currency();

		StringTokenizer tokenizer = new StringTokenizer(currencyString, TOKEN);
		for (int i = 0; i < numCurrencies; i++) {
			if (tokenizer.hasMoreTokens()) {
				currency.representation[i] = Math.abs(Integer
						.parseInt(tokenizer.nextToken()));
			} else {
				currency.representation[i] = 0;
			}
		}

		return currency;
	}

	public static void setCount(int currencyCount) {
		numCurrencies = currencyCount;
		defaultZeros = new Currency();

	}

	public static String defaultZeroCurrencies() {
		// There's edge cases where report launch needs this but numCurrencies (and defaultZeros) is unset
		// In this case, simple zero will suffice
		if( null == defaultZeros ) {
			YRDLog.w( Currency.class , "defaultZeroCurrencies() called but setCount() was not");
			return new Currency().toString();
		}
		return defaultZeros.toString();
	}

	/**
	 * Treat this as an IMMUTABLE object, adding two currencies will result in a
	 * new currency object, which should contain the sum of all currencies
	 * passed in values in absolute terms
	 * 
	 * @param currencyAdded
	 * @return this
	 */
	public Currency add(Currency currencyAdded) {
		Currency currency = new Currency();
		for (int i = 0; i < numCurrencies; i++) {
			currency.representation[i] = currencyAdded.representation[i]
					+ this.representation[i];
		}
		return currency;
	}

}
