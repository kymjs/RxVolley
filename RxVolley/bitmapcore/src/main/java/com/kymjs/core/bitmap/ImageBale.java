/*
 * Copyright (c) 2014, 张涛.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kymjs.core.bitmap;


import android.graphics.Bitmap;

import com.kymjs.rxvolley.client.HttpCallback;

import java.util.HashMap;

/**
 * 对一个图片的封装，包含了这张图片所需要携带的信息,摘取自Volley
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
