package com.yerdy.services.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import com.yerdy.services.logging.YRDLog;
import com.yerdy.services.util.BitmapUtil;
import com.yerdy.services.util.YRDJsonProcessor;

/**
 * Parses server response and Populates {@link YRDMessage}
 * @author m2
 *
 */
public class YRDMessageProcessor extends YRDJsonProcessor {

	List<YRDMessage> _messages = new ArrayList<YRDMessage>();
	Map<String, Bitmap> _bitmaps = new HashMap<String, Bitmap>();

	public List<YRDMessage> getMessages() {
		return _messages;
	}
	
	public void parseJSON(JSONObject json) {
		if(json != null) {
			JSONArray arr = json.optJSONArray("message");
			if(arr != null) {
				for(int i = 0; i < arr.length(); i++) {
					JSONObject obj = arr.optJSONObject(i);
					if(obj != null) {
						try {
							YRDMessage msg = new YRDMessage();
							msg.id = obj.optInt("id", 0);
							msg.style = parseMessageStlye(obj, "style", "short");
							msg.placement = getString(obj, "placement", "");
							msg.titleString = getString(obj, "message_title", "");
							msg.textString = getString(obj, "message_text", "");
							msg.imageURI = parseUri(obj, "image");
							msg.confirmString = getString(obj, "confirm_label", null);
							msg.cancelString = getString(obj, "cancel_label", null);
							msg.clickURI = parseUri(obj, "click");
							msg.viewURI = parseUri(obj, "view");
							msg.expiresUnixTimestamp = obj.optLong("expire_time", 0L);
							msg.actionType = parseMessageActionType(obj, "action_type");
							msg.action = getString(obj, "action", null);
							
							int force = obj.optInt("force_action", 0);
							//msg.forceAction = (force == 1)?(true):(false);
							force = obj.optInt("force_refresh", 0);
							msg.forceRefresh = (force == 1)?(true):(false);
	
							msg.backgroundColor = getColor(obj, "text_background", Color.argb(255, 85, 85, 85));
							msg.titleColor = getColor(obj, "title_color", Color.WHITE);
							msg.messageColor = getColor(obj, "text_color", Color.WHITE);
							msg.cancelButtonColor = getColor(obj, "cancel_background", Color.argb(255, 128, 128, 128));
							msg.cancelTextColor = getColor(obj, "cancel_color", Color.WHITE);
							msg.confirmButtonColor = getColor(obj, "confirm_background", Color.argb(255, 255, 128, 0));
							msg.confirmTextColor = getColor(obj, "confirm_color", Color.WHITE);
							
							//TODO Not yet supported
							msg.cancelDelay = getFloatSecondsAsMilliseconds(json, "cancel_delay", 0);
							msg.expirationColor = obj.optInt("expire_color", Color.argb(255, 255, 255, 0));
							msg.watermarkImageURI = parseUri(obj, "watermark_image");
							String wPos = getString(obj, "watermark", null);
							msg.watermarkLocation = YRDWatermarkPosition.parse(wPos);
							

							msg.imageBitmap = fetchBitmap(msg.imageURI);
							msg.watermarkBitmap = fetchBitmap(msg.watermarkImageURI);

							if(YRDAppActionParser.parseAction(msg.action) != null)
								_messages.add(msg);
						} catch (Exception e) {
							YRDLog.e(this.getClass(), "Failed to parse message, it will be ignored");
						}
					}
				}
			}
		}
	}
	
	private Bitmap fetchBitmap(Uri uri) {
		try {
			Bitmap bmp = _bitmaps.get(uri.getPath());
			if(bmp == null) {
				bmp = BitmapUtil.loadBitmapFromURL(uri);
				_bitmaps.put(uri.getPath(), bmp);
			}
			return bmp;
		} catch (Exception e) {
			YRDLog.e(this.getClass(), "Failed to load bitmap for pull message");
			return null;
		}
	}
	
	private Uri parseUri(JSONObject json, String key) {
		String imgUri = getString(json, key, null);
		if(imgUri != null) {
			try {
				return Uri.parse(imgUri);
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	private YRDMessageStyle parseMessageStlye(JSONObject json, String key, String defVal) {
		String style = getString(json, key, defVal);
		if(style != null)
			return YRDMessageStyle.valueOf(style.toUpperCase(Locale.getDefault()));
		else
			return YRDMessageStyle.LONG;
	}
	
	private YRDMessageActionType parseMessageActionType(JSONObject json, String key) {
		int style = json.optInt(key, -1);
		switch (style) {
		case 0:
			return YRDMessageActionType.EXTERNAL_BROWSER;
		case 1:
			return YRDMessageActionType.INTERNAL_BROWSER;
		case 2:
			return YRDMessageActionType.APP;
		default:
			return YRDMessageActionType.NONE;
		}
	}
}
