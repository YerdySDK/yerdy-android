/**
 * Utility class for generating hashes from Strings given certain Digest types
 * and similar functionality.
 */
package com.yerdy.services.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.yerdy.services.logging.YRDLog;

import android.util.Base64;

/**
 * @author m2
 * 
 */
public class DigestUtil {

	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	/**
	 * Create an md5 hash given a string to digest. Note the string is converted
	 * to UTF8 first to maintain compatibility with IOS implementations
	 * 
	 * @param toConvert
	 *            source string
	 * @return Hex encoded string of md5 hash
	 */
	static public String md5Hash(String toConvert) {

		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return hashFromDigest(toConvert, digest);

		} catch (NoSuchAlgorithmException e) {
			YRDLog.e(DigestUtil.class, "No MD5 MessageDigest");
			e.printStackTrace();
			return toConvert;
		}
	}

	/**
	 * Create an sha1 hash given a string to digest. Note the string is
	 * converted to UTF8 first to maintain compatibility with IOS
	 * implementations
	 * 
	 * @param toConvert
	 *            source string
	 * @return Hex encoded string of sha1 hash, On error, returns raw value of
	 *         toConvert
	 */
	static public String sha1Hash(String toConvert) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			return hashFromDigest(toConvert, digest);

		} catch (NoSuchAlgorithmException e) {
			YRDLog.e(DigestUtil.class, "No SHA-1 MessageDigest");
			e.printStackTrace();
			return toConvert;
		}
	}

	/**
	 * Create a hash given a string to digest and an arbitrary MessageDigest
	 * algorithm to apply Note the string is converted to UTF8 first to maintain
	 * compatibility with IOS implementations
	 * 
	 * @param toConvert
	 *            source string
	 * @param digest
	 *            algorithm to apply when generating hash
	 * @return Hex encoded string of arbitrary algorithm's hash. On error,
	 *         returns raw value of toConvert
	 */
	static public String hashFromDigest(String toConvert, MessageDigest digest) {
		digest.reset();
		try {
			digest.update(toConvert.getBytes(HTTPRequestData.UTF8));
			return asHex(digest.digest());
		} catch (UnsupportedEncodingException e) {
			YRDLog.e(DigestUtil.class, "No UTF8 encodings");
			e.printStackTrace();
			return toConvert;
		}
	}

	public static String asBase64(String toConvert) {
		try {
			String response = Base64.encodeToString(toConvert.getBytes(HTTPRequestData.UTF8), Base64.DEFAULT);
			if(response != null)
				response = response.trim();
			return response;
		} catch (UnsupportedEncodingException e) {
			YRDLog.e(DigestUtil.class, "No UTF8 encodings");
			e.printStackTrace();
			return toConvert;
		}

	}

	/**
	 * Crazy fast way to turn byte[] into a String hexadecimal representation
	 * using Math! for example:
	 * <code>asHex( { 0xde, 0xad, 0xbe, 0xef, 0xd0, 0x0d})</code> will return
	 * the String "deadbeefd00d"
	 * 
	 * @param buf
	 *            raw bytes to convert
	 * @return String representation of bytes in Hex
	 */
	public static String asHex(byte[] buf) {
		char[] chars = new char[2 * buf.length];
		for (int i = 0; i < buf.length; ++i) {
			chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
			chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
		}
		return new String(chars);
	}

}
