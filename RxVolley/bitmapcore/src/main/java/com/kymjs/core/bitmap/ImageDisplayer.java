/*
 * Copyright (c) 2015, 张涛.
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
import android.os.Handler;
import android.os.Looper;

import com.kymjs.core.bitmap.client.BitmapRequestConfig;
import com.kymjs.core.bitmap.client.ImageRequest;
import com.kymjs.core.bitmap.interf.IBitmapCache;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.http.Request;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.http.VolleyError;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片显示器
 *
 * @author kymjs (https://www.kymjs.com/)
 */
public class ImageDisplayer {
    private RequestQueue requestQueue; // 使用Http模块的线程池执行队列去加载图片

    private final IBitmapCache mMemoryCache; // 内存缓存器

    private Runnable mRunnable;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // 正在请求的事件
    private final HashMap<String, ImageRequestEven> mRequestsMap = new HashMap<>();
    // 已经请求完成，待处理的事件
    private final HashMap<String, ImageRequestEven> mResponsesMap = new HashMap<>();

    /**
     * 构造器
     */
    public ImageDisplayer(RequestQueue queue, IBitmapCache memoryCache) {
        requestQueue = queue;
        mMemoryCache = memoryCache;
    }

    public IBitmapCache getMemoryCache() {
        return mMemoryCache;
    }

    /**
     * 加载一张图片
     *
     * @param callback 回调
     * @return 加载的图片封装
     */
    public ImageBale get(BitmapRequestConfig config, final HttpCallback callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onPreStart();
            }
        });

        final Bitmap cachedBitmap = mMemoryCache.getBitmap(config.mUrl);
        if (cachedBitmap != null) {
            ImageBale container = new ImageBale(cachedBitmap, config.mUrl, callback,
                    mRequestsMap, mResponsesMap);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(Collections.<String, String>emptyMap(), cachedBitmap);
                    callback.onFinish();
                }
            });
            return container;
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // 开始加载网络图片的标志
                    callback.onPreHttp();
                }
            });
        }

        ImageBale imageBale = new ImageBale(null, config.mUrl, callback, mRequestsMap,
                mResponsesMap);
        //如果有正在请求的,则修改
        ImageRequestEven request = mRequestsMap.get(config.mUrl);
        if (request != null) {
            request.addImageBale(imageBale);
            return imageBale;
        }

        Request<Bitmap> newRequest = makeImageRequest(config);
        requestQueue.add(newRequest);
        mRequestsMap.put(config.mUrl, new ImageRequestEven(newRequest, imageBale));
        return imageBale;
    }

    /**
     * 创建一个网络请求
     */
    protected Request<Bitmap> makeImageRequest(final BitmapRequestConfig config) {
        return new ImageRequest(config, new HttpCallback() {
            @Override
            public void onSuccess(Map<String, String> header, Bitmap t) {
                onGetImageSuccess(config.mUrl, t);
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                onGetImageError(config.mUrl, new VolleyError(strMsg));
            }
        });
    }

    /**
     * 从网络获取bitmap成功时调用
     *
     * @param url    缓存key
     * @param bitmap 获取到的bitmap
     */
    protected void onGetImageSuccess(String url, Bitmap bitmap) {
        mMemoryCache.putBitmap(url, bitmap);
        // 从正在请求的列表中移除这个已完成的请求
        ImageRequestEven request = mRequestsMap.remove(url);

        if (request != null) {
            request.mBitmapRespond = bitmap;
            batchResponse(url, request);
        }
    }

    /**
     * 从网络获取bitmap失败时调用
     *
     * @param url   缓存key
     * @param error 失败原因
     */
    protected void onGetImageError(String url, VolleyError error) {
        // 从正在请求的列表中移除这个已完成的请求
        ImageRequestEven request = mRequestsMap.remove(url);
        if (request != null) {
            request.setError(error);
            batchResponse(url, request);
        }
    }

    /**
     * 分发这次ImageRequest事件的结果
     */
    private void batchResponse(String url, final ImageRequestEven request) {
        mResponsesMap.put(url, request);
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    for (ImageRequestEven even : mResponsesMap.values()) {
                        for (ImageBale imageBale : even.mImageBales) {
                            if (imageBale.mCallback == null) {
                                continue;
                            }
                            if (even.getError() == null) {
                                imageBale.mBitmap = even.mBitmapRespond;
                                imageBale.mCallback.onSuccess(
                                        Collections.<String, String>emptyMap(), imageBale.mBitmap);
                            } else {
                                imageBale.mCallback.onFailure(-1, even.getError().getMessage());
                            }
                            imageBale.mCallback.onFinish();
                        }
                    }
                    mResponsesMap.clear();
                    mRunnable = null;
                }

            };
            mHandler.postDelayed(mRunnable, request.mRequest.getConfig().mDelayTime);
        }
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
