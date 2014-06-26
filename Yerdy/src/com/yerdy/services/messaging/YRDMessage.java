package com.yerdy.services.messaging;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Data component of YRDMessage. Should be populated by
 * {@link YRDMessageProcessor}
 * 
 * @author m2
 * 
 */
public class YRDMessage {
	public int id;
	public String placement = "";
	public String titleString;
	public String textString;
	public YRDMessageStyle style;
	public String confirmString;
	public String cancelString;
	public long expiresUnixTimestamp = 0L;
	public YRDMessageActionType actionType;
	public String action;
	public Uri clickURI;
	public Uri viewURI;
	//public boolean forceAction;
	public Uri imageURI;
	public Bitmap imageBitmap;
	
	public int cancelDelay;
	public int backgroundColor;
	public int titleColor;
	public int messageColor;
	public int expirationColor;
	public Uri watermarkImageURI;
	public Bitmap watermarkBitmap;
	public YRDWatermarkPosition watermarkLocation;
	public int cancelButtonColor;
	public int cancelTextColor;
	public int confirmButtonColor;
	public int confirmTextColor;
	
	public boolean forceRefresh = false;
	
	public YRDMessage() {
	}
}