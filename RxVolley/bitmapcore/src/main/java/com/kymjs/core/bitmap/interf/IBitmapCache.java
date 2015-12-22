package com.kymjs.core.bitmap.interf;

import android.graphics.Bitmap;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public interface IBitmapCache {
    Bitmap getBitmap(String url);

    void remove(String key);

    void clean();

    void putBitmap(String url, Bitmap bitmap);
}
