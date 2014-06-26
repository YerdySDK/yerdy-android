package com.yerdy.services.messaging;

public enum YRDWatermarkPosition {
	TOP_LEFT("tl"),
	TOP_CENTER("tc"),
	TOP_RIGHT("tr"),
	MIDDLE_LEFT("ml"),
	MIDDLE_CENTER("mc"),
	MIDDLE_RIGHT("mr"),
	BOTTOM_LEFT("bl"),
	BOTTOM_CENTER("bc"),
	BOTTOM_RIGHT("br"),
	HIDDEN(null);
	
	private String _position = null;
	private YRDWatermarkPosition(String value) {
		_position = value;
	}
	
	public static YRDWatermarkPosition parse(String value) {
		for(YRDWatermarkPosition pos : YRDWatermarkPosition.values()) {
			if(pos._position != null && pos._position.equals(value)) {
				return pos;
			}
		}
		
		return YRDWatermarkPosition.HIDDEN;
	}
}
