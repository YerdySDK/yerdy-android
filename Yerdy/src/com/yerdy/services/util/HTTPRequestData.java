package com.yerdy.services.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

public class HTTPRequestData {

	public static final String UTF8 = "UTF-8";

	public static byte[] convertToBytes(Map<String, ? extends Object> formData) {
		try {
			return converToString(formData).getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String converToString(Map<String, ? extends Object> formData) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Entry<String, ? extends Object> entry : formData.entrySet()) {
			if (first) {
				first = false;
			} else {
				sb.append('&');
			}
			sb.append(URLEncoder.encode(entry.getKey(), UTF8));
			sb.append('=');
			sb.append(URLEncoder.encode(entry.getValue().toString(), UTF8));
		}
		return sb.toString();

	}

}
