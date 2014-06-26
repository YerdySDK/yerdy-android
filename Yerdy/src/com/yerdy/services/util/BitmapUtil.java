package com.yerdy.services.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class BitmapUtil {

	private static final int IO_BUFFER_SIZE = 1024 * 16;

	public static Bitmap loadBitmapFromURL(Uri imageURI) throws IOException {
		BufferedInputStream bin = new BufferedInputStream(new URL(imageURI.toString()).openStream(), IO_BUFFER_SIZE);
		Bitmap bitmap = BitmapFactory.decodeStream(bin);
		bin.close();
		return bitmap;
	}

}
