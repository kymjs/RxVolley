package com.kymjs.core.bitmap;

import android.graphics.Bitmap;

import com.kymjs.rxvolley.http.Request;
import com.kymjs.rxvolley.http.VolleyError;

import java.util.LinkedList;

/**
 * 图片从网络请求并获取到相应的事件
 *
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class ImageRequestEven {
    public final Request<?> mRequest;
    public Bitmap mBitmapRespond;
    private VolleyError mError;
    public final LinkedList<ImageBale> mImageBales = new LinkedList<>();

    public ImageRequestEven(Request<?> request, ImageBale imageBale) {
        mRequest = request;
        mImageBales.add(imageBale);
    }

    public void setError(VolleyError error) {
        mError = error;
    }

    public VolleyError getError() {
        return mError;
    }

    public void addImageBale(ImageBale imageBale) {
        mImageBales.add(imageBale);
    }

    public boolean removeBale(ImageBale imageBale) {
        mImageBales.remove(imageBale);
        if (mImageBales.size() == 0) {
            mRequest.cancel();
            return true;
        }
        return false;
    }
}
