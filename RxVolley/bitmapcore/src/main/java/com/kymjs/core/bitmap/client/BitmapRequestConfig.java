package com.kymjs.core.bitmap.client;

import android.graphics.drawable.Drawable;

import com.kymjs.rxvolley.client.RequestConfig;
import com.kymjs.rxvolley.toolbox.HttpParamsEntry;

import java.util.ArrayList;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class BitmapRequestConfig extends RequestConfig {

    public static final int DEF_WIDTH_HEIGHT = -100;

    public int maxWidth = DEF_WIDTH_HEIGHT;
    public int maxHeight = DEF_WIDTH_HEIGHT;

    public Drawable loadDrawable;
    public Drawable errorDrawable;
    public int loadRes;
    public int errorRes;

    private final ArrayList<HttpParamsEntry> mHeaders = new ArrayList<>();

    public ArrayList<HttpParamsEntry> getHeaders() {
        return mHeaders;
    }

    public void putHeader(String k, String v) {
        mHeaders.add(new HttpParamsEntry(k, v));
    }
}
