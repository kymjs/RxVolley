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
package com.kymjs.rxvolley.http;

import android.net.TrafficStats;
import android.net.Uri;
import android.text.TextUtils;

import com.kymjs.common.Log;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.ProgressListener;
import com.kymjs.rxvolley.client.RequestConfig;
import com.kymjs.rxvolley.interf.ICache;
import com.kymjs.rxvolley.toolbox.HttpParamsEntry;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

/**
 * 一个请求基类
 *
 * @param <T> Http返回类型
 * @author kymjs (http://www.kymjs.com/) .
 */
public abstract class Request<T> implements Comparable<Request<T>> {

    private final RequestConfig mConfig;

    public int mDefaultTrafficStatsTag; // 默认tag {@link TrafficStats}
    public boolean mResponseDelivered = false; // 是否再次分发本次响应
    public boolean mCanceled = false; // 是否取消本次请求

    public Object mTag;
    public Integer mSequence;

    protected final HttpCallback mCallback;
    protected ProgressListener mProgressListener;
    protected RequestQueue mRequestQueue;
    private ICache.Entry mCacheEntry = null;

    public Request(RequestConfig config, HttpCallback callback) {
        if (config == null) {
            config = new RequestConfig();
        }
        mConfig = config;
        mCallback = callback;
        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(config.mUrl);
    }

    /**
     * Set listener for tracking download progress
     *
     * @param listener 进度监听
     */
    public void setOnProgressListener(ProgressListener listener) {
        mProgressListener = listener;
    }

    public int getMethod() {
        return mConfig.mMethod;
    }

    public RequestConfig getConfig() {
        return mConfig;
    }

    /**
     * 设置tag，方便取消本次请求时能找到它
     */
    public Object getTag() {
        return mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public HttpCallback getCallback() {
        return mCallback;
    }

    /**
     * @return A tag for use with {@link TrafficStats#setThreadStatsTag(int)}
     */
    public int getTrafficStatsTag() {
        return mDefaultTrafficStatsTag;
    }

    /**
     * @return The hashcode of the URL's host component, or 0 if there is none.
     */
    private static int findDefaultTrafficStatsTag(String url) {
        if (!TextUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                String host = uri.getHost();
                if (host != null) {
                    return host.hashCode();
                }
            }
        }
        return 0;
    }

    /**
     * 通知请求队列，本次请求已经完成
     */
    public void finish(String log) {
        Log.d("RxVolley", log);
        if (mRequestQueue != null) {
            mRequestQueue.finish(this);
        }
    }

    Request<?> setRequestQueue(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;
        return this;
    }

    public final int getSequence() {
        if (mSequence == null) {
            throw new IllegalStateException(
                    "getSequence called before setSequence");
        }
        return mSequence;
    }

    void setSequence(int sequence) {
        this.mSequence = sequence;
    }

    public String getUrl() {
        return mConfig.mUrl;
    }

    public abstract String getCacheKey();

    Request<?> setCacheEntry(ICache.Entry entry) {
        mCacheEntry = entry;
        return this;
    }

    public ICache.Entry getCacheEntry() {
        return mCacheEntry;
    }

    public void cancel() {
        mCanceled = true;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public ArrayList<HttpParamsEntry> getParams() {
        return null;
    }

    public ArrayList<HttpParamsEntry> getHeaders() {
        return new ArrayList<>();
    }

    protected String getParamsEncoding() {
        return mConfig.mEncoding;
    }

    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }

    /**
     * 返回Http请求的body
     */
    public byte[] getBody() {
        ArrayList<HttpParamsEntry> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    /**
     * 对中文参数做URL转码
     */
    private byte[] encodeParameters(ArrayList<HttpParamsEntry> params,
                                    String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (HttpParamsEntry entry : params) {
                encodedParams.append(URLEncoder.encode(entry.k, paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.v, paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: "
                    + paramsEncoding, uee);
        }
    }

    public boolean shouldCache() {
        return mConfig.mShouldCache == null ? false : mConfig.mShouldCache;
    }

    /**
     * 本次请求的优先级，四种
     */
    public enum Priority {
        LOW, NORMAL, HIGH, IMMEDIATE
    }

    public Priority getPriority() {
        return Priority.NORMAL;
    }

    public final int getTimeoutMs() {
        if (mConfig.mTimeout == 0) {
            return mConfig.mRetryPolicy.getCurrentTimeout();
        } else {
            return mConfig.mTimeout;
        }
    }

    /**
     * Returns the retry policy that should be used for this request.
     */
    public RetryPolicy getRetryPolicy() {
        return mConfig.mRetryPolicy;
    }

    /**
     * 标记为已经分发过的
     */
    public void markDelivered() {
        mResponseDelivered = true;
    }

    /**
     * 是否已经被分发过
     */
    public boolean hasHadResponseDelivered() {
        return mResponseDelivered;
    }

    /**
     * 将网络请求执行器(NetWork)返回的NetWork响应转换为Http响应
     *
     * @param response 网络请求执行器(NetWork)返回的NetWork响应
     * @return 转换后的HttpRespond, or null in the case of an error
     */
    abstract public Response<T> parseNetworkResponse(NetworkResponse response);

    /**
     * 如果需要根据不同错误做不同的处理策略，可以在子类重写本方法
     */
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return volleyError;
    }

    /**
     * 将Http请求结果分发到主线程
     *
     * @param response {@link #parseNetworkResponse(NetworkResponse)}
     */
    abstract protected void deliverResponse(Map<String, String> headers, T response);

    /**
     * 响应Http请求异常的回调
     *
     * @param error 原因
     */
    public void deliverError(final VolleyError error) {
        final int errorNo;
        String strMsg;
        if (error != null) {
            if (error.networkResponse != null) {
                errorNo = error.networkResponse.statusCode;
            } else {
                errorNo = -1;
            }
            strMsg = error.getMessage();
        } else {
            errorNo = -1;
            strMsg = "unknow";
        }
        if (mCallback != null) {
            mCallback.onFailure(errorNo, strMsg);
            mCallback.onFailure(error);
        }
        getConfig().mSubject.onError(error);
    }

    public void deliverStartHttp() {
        if (mCallback != null) {
            mCallback.onPreHttp();
        }
    }

    /**
     * Http请求完成(不论成功失败)
     */
    public void requestFinish() {
        if (mCallback != null) {
            mCallback.onFinish();
        }
        getConfig().mSubject.onComplete();
    }

    /**
     * 用于线程优先级排序
     */
    @Override
    public int compareTo(Request<T> other) {
        Priority left = this.getPriority();
        Priority right = other.getPriority();
        return left == right ? mSequence - other.mSequence : right
                .ordinal() - left.ordinal();
    }

    @Override
    public String toString() {
        String trafficStatsTag = "0x" + Integer.toHexString(getTrafficStatsTag());
        return (mCanceled ? "[X] " : "[ ] ") + getUrl() + " " + trafficStatsTag
                + " " + getPriority() + " " + mSequence;
    }

    public int getCacheTime() {
        return mConfig.mCacheTime;
    }

    public boolean getUseServerControl() {
        return mConfig.mUseServerControl;
    }
}
