package com.yerdy.services.util;

import java.util.Locale;

/**
 * Used to override platform detection
 * 
 * @author Chris Pritchard
 */
public enum YRDPlatform {
	GOOGLE("Google"),
	AMAZON("Amazon"),
	AUTO("Google");
	
	private String _name;
	
	private YRDPlatform(String name) {
		_name = name;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public boolean contains(String value)
	{
		return (_name.toLowerCase(Locale.getDefault()).contains(value.toLowerCase(Locale.getDefault())));
	}
}
