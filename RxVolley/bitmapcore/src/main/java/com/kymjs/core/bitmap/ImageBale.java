package com.kymjs.core.bitmap;


import android.graphics.Bitmap;

import com.kymjs.rxvolley.client.HttpCallback;

import java.util.HashMap;

/**
 * 对一个图片的封装，包含了这张图片所需要携带的信息
 *
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class ImageBale {
    public Bitmap mBitmap;
    private final String mRequestUrl;
    public final HttpCallback mCallback;
    private final HashMap<String, ImageRequestEven> mRequestsMap;
    private final HashMap<String, ImageRequestEven> mResponsesMap;

    public ImageBale(Bitmap bitmap, String requestUrl, HttpCallback callback,
                     HashMap<String, ImageRequestEven> requestsMap,
                     HashMap<String, ImageRequestEven> responsesMap) {
        mBitmap = bitmap;
        mRequestUrl = requestUrl;
        mCallback = callback;
        mRequestsMap = requestsMap;
        mResponsesMap = responsesMap;
    }

    public void cancelRequest() {
        ImageRequestEven request = mRequestsMap.get(mRequestUrl);
        if (request != null) {
            boolean canceled = request.removeBale(this);
            if (canceled) {
                mRequestsMap.remove(mRequestUrl);
            }
        } else {
            ImageRequestEven responses = mResponsesMap.get(mRequestUrl);
            if (responses != null) {
                responses.removeBale(this);
                if (responses.mImageBales.size() == 0) {
                    mResponsesMap.remove(mRequestUrl);
                }
            }
        }
    }

    public String getRequestUrl() {
        return mRequestUrl;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

}
