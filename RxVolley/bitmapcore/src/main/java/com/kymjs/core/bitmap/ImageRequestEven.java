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

import com.kymjs.rxvolley.http.Request;
import com.kymjs.rxvolley.http.VolleyError;

import java.util.LinkedList;

/**
 * 图片从网络请求并获取到相应的事件,摘取自Volley
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
