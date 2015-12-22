/*
 * Copyright (C) 2011 The Android Open Source Project, 张涛
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

package com.kymjs.core.bitmap.client;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.http.HttpHeaderParser;
import com.kymjs.rxvolley.http.NetworkResponse;
import com.kymjs.rxvolley.http.Request;
import com.kymjs.rxvolley.http.Response;
import com.kymjs.rxvolley.http.VolleyError;
import com.kymjs.rxvolley.interf.IPersistence;
import com.kymjs.rxvolley.toolbox.HttpParamsEntry;
import com.kymjs.rxvolley.toolbox.Loger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 从网络请求一张bitmap
 *
 * @author kymjs (https://www.kymjs.com/)
 */
public class ImageRequest extends Request<Bitmap> implements IPersistence {

    private final int mMaxWidth;
    private final int mMaxHeight;
    // 用来保证当前对象只有一个线程在访问
    private static final Object sDecodeLock = new Object();

    public ImageRequest(BitmapRequestConfig config, HttpCallback callback) {
        super(config, callback);
        mMaxWidth = config.maxWidth;
        mMaxHeight = config.maxHeight;
    }

    @Override
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    @Override
    public String getCacheKey() {
        return getUrl();
    }

    @Override
    public ArrayList<HttpParamsEntry> getHeaders() {
        return ((BitmapRequestConfig) getConfig()).getHeaders();
    }

    /**
     * 框架会自动将大于设定值的bitmap转换成设定值，所以需要这个方法来判断应该显示默认大小或者是设定值大小。<br>
     * 本方法会根据maxPrimary与actualPrimary比较来判断，如果无法判断则会根据辅助值判断，辅助值一般是主要值对应的。
     * 比如宽为主值则高为辅值
     *
     * @param maxPrimary      需要判断的值，用作主要判断
     * @param maxSecondary    需要判断的值，用作辅助判断
     * @param actualPrimary   真实宽度
     * @param actualSecondary 真实高度
     * @return 获取图片需要显示的大小
     */
    private static int getResizedDimension(int maxPrimary, int maxSecondary,
                                           int actualPrimary, int actualSecondary) {
        if (maxPrimary == 0 && maxSecondary == 0) {
            return actualPrimary;
        }
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    @Override
    public Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
        synchronized (sDecodeLock) {
            try {
                return doParse(response);
            } catch (OutOfMemoryError e) {
                Loger.debug(String.format("Caught OOM for %d byte image, url=%s",
                        response.data.length, getUrl()));
                return Response.error(new VolleyError(e));
            }
        }
    }

    @Override
    protected void deliverResponse(ArrayList<HttpParamsEntry> headers, Bitmap response) {
        if (mCallback != null) {
            HashMap<String, String> map = new HashMap<>(headers.size());
            for (HttpParamsEntry entry : headers) {
                map.put(entry.k, entry.v);
            }
            mCallback.onSuccess(map, response);
        }
    }

    /**
     * 关于本函数的详细解释，可以看我的博客：
     * http://blog.kymjs.com/code/2014/12/05/02/
     */
    private Response<Bitmap> doParse(NetworkResponse response) {
        byte[] data = response.data;
        BitmapFactory.Options option = new BitmapFactory.Options();
        Bitmap bitmap;
        if (mMaxWidth <= 0 && mMaxHeight <= 0) {
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, option);
        } else {
            option.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, option);
            int actualWidth = option.outWidth;
            int actualHeight = option.outHeight;

            // 计算出图片应该显示的宽高
            int desiredWidth = getResizedDimension(mMaxWidth, mMaxHeight,
                    actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(mMaxHeight, mMaxWidth,
                    actualHeight, actualWidth);

            option.inJustDecodeBounds = false;
            option.inSampleSize = findBestSampleSize(actualWidth, actualHeight,
                    desiredWidth, desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0,
                    data.length, option);

            // 做缩放
            if (tempBitmap != null
                    && (tempBitmap.getWidth() > desiredWidth || tempBitmap
                    .getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth,
                        desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }
        if (bitmap == null) {
            return Response.error(new VolleyError(response));
        } else {
            return Response.success(bitmap, response.headers, HttpHeaderParser
                    .parseCacheHeaders(getUseServerControl(), getCacheTime(), response));
        }
    }

    /**
     * 关于本方法的判断，可以查看我的博客：http://blog.kymjs.com/code/2014/12/05/02/
     */
    static int findBestSampleSize(int actualWidth, int actualHeight,
                                  int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }
        return (int) n;
    }
}
