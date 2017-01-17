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
package com.kymjs.rxvolley;


import android.text.TextUtils;

import com.kymjs.common.FileUtils;
import com.kymjs.rxvolley.client.FileRequest;
import com.kymjs.rxvolley.client.FormRequest;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.kymjs.rxvolley.client.JsonRequest;
import com.kymjs.rxvolley.client.ProgressListener;
import com.kymjs.rxvolley.client.RequestConfig;
import com.kymjs.rxvolley.http.DefaultRetryPolicy;
import com.kymjs.rxvolley.http.Request;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.http.RetryPolicy;
import com.kymjs.rxvolley.interf.ICache;
import com.kymjs.rxvolley.rx.Result;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;


/**
 * 主入口
 *
 * @author kymjs (http://www.kymjs.com/) on 12/17/15.
 */
public class RxVolley {

    private RxVolley() {
    }

    public final static File CACHE_FOLDER = FileUtils.getExternalCacheDir("RxVolley");

    private static RequestQueue sRequestQueue;

    /**
     * 获取一个请求队列(单例)
     */
    public synchronized static RequestQueue getRequestQueue() {
        if (sRequestQueue == null) {
            sRequestQueue = RequestQueue.newRequestQueue(CACHE_FOLDER);
        }
        return sRequestQueue;
    }

    /**
     * 设置请求队列,必须在调用Core#getRequestQueue()之前设置
     *
     * @return 是否设置成功
     */
    public synchronized static boolean setRequestQueue(RequestQueue queue) {
        if (sRequestQueue == null) {
            sRequestQueue = queue;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 请求方式:FORM表单,或 JSON内容传递
     */
    public interface ContentType {
        int FORM = 0;
        int JSON = 1;
    }

    /**
     * 支持的请求方式
     */
    public interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }

    /**
     * 构建器
     */
    public static class Builder {
        private HttpParams params;
        private int contentType;
        private HttpCallback callback;
        private Request<?> request;
        private ProgressListener progressListener;
        private RequestConfig httpConfig = new RequestConfig();

        /**
         * Http请求参数
         */
        public Builder params(HttpParams params) {
            this.params = params;
            return this;
        }

        /**
         * 参数的类型:FORM表单,或 JSON内容传递
         */
        public Builder contentType(int contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * 请求回调,不需要可以为空
         */
        public Builder callback(HttpCallback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * HttpRequest
         */
        public Builder setRequest(Request<?> request) {
            this.request = request;
            return this;
        }

        /**
         * 每个request可以设置一个标志,用于在cancel()时找到
         */
        public Builder setTag(Object tag) {
            this.httpConfig.mTag = tag;
            return this;
        }


        /**
         * HttpRequest的配置器
         */
        public Builder httpConfig(RequestConfig httpConfig) {
            this.httpConfig = httpConfig;
            return this;
        }

        /**
         * 请求超时时间,如果不设置则使用重连策略的超时时间,默认3000ms
         */
        public Builder timeout(int timeout) {
            this.httpConfig.mTimeout = timeout;
            return this;
        }

        /**
         * 上传进度回调
         *
         * @param listener 进度监听器
         */
        public Builder progressListener(ProgressListener listener) {
            this.progressListener = listener;
            return this;
        }

        /**
         * 为了更真实的模拟网络,如果读取缓存,延迟一段时间再返回缓存内容
         */
        public Builder delayTime(int delayTime) {
            this.httpConfig.mDelayTime = delayTime;
            return this;
        }

        /**
         * 缓存有效时间,单位分钟
         */
        public Builder cacheTime(int cacheTime) {
            this.httpConfig.mCacheTime = cacheTime;
            return this;
        }

        /**
         * 缓存有效时间,单位分钟
         */
        public Builder cacheTime(int cacheTime, TimeUnit timeUnit) {
            this.httpConfig.mCacheTime = cacheTime;
            return this;
        }

        /**
         * 是否使用服务器控制的缓存有效期(如果使用服务器端的,则无视#cacheTime())
         */
        public Builder useServerControl(boolean useServerControl) {
            this.httpConfig.mUseServerControl = useServerControl;
            return this;
        }

        /**
         * 查看RequestConfig$Method
         * GET/POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH
         */
        public Builder httpMethod(int httpMethod) {
            this.httpConfig.mMethod = httpMethod;
            if (httpMethod == Method.POST) {
                this.httpConfig.mShouldCache = false;
            }
            return this;
        }

        /**
         * 是否启用缓存
         */
        public Builder shouldCache(boolean shouldCache) {
            this.httpConfig.mShouldCache = shouldCache;
            return this;
        }

        /**
         * 网络请求接口url
         */
        public Builder url(String url) {
            this.httpConfig.mUrl = url;
            return this;
        }

        /**
         * 重连策略,不传则使用默认重连策略
         */
        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.httpConfig.mRetryPolicy = retryPolicy;
            return this;
        }

        /**
         * 编码,默认UTF-8
         */
        public Builder encoding(String encoding) {
            this.httpConfig.mEncoding = encoding;
            return this;
        }

        private Builder build() {
            if (request == null) {
                if (params == null) {
                    params = new HttpParams();
                } else {
                    if (httpConfig.mMethod == Method.GET)
                        httpConfig.mUrl += params.getUrlParams();
                }

                if (httpConfig.mShouldCache == null) {
                    //默认情况只对get请求做缓存
                    if (httpConfig.mMethod == Method.GET) {
                        httpConfig.mShouldCache = Boolean.TRUE;
                    } else {
                        httpConfig.mShouldCache = Boolean.FALSE;
                    }
                }

                if (contentType == ContentType.JSON) {
                    request = new JsonRequest(httpConfig, params, callback);
                } else {
                    request = new FormRequest(httpConfig, params, callback);
                }

                request.setTag(httpConfig.mTag);
                request.setOnProgressListener(progressListener);

                if (TextUtils.isEmpty(httpConfig.mUrl)) {
                    throw new RuntimeException("Request url is empty");
                }
            }
            if (callback != null) {
                callback.onPreStart();
            }
            return this;
        }

        /**
         * 执行请求任务,并返回一个RxJava的Observable类型
         */
        public Observable<Result> getResult() {
            doTask();
            return httpConfig.mSubject;
        }

        /**
         * 执行请求任务
         */
        public void doTask() {
            build();
            getRequestQueue().add(request);
        }
    }

    public static void get(String url, HttpCallback callback) {
        new Builder().url(url).callback(callback).doTask();
    }

    public static void get(String url, HttpParams params, HttpCallback callback) {
        new Builder().url(url).params(params).callback(callback).doTask();
    }

    public static void post(String url, HttpParams params, HttpCallback callback) {
        new Builder().url(url).params(params).httpMethod(Method.POST).callback(callback).doTask();
    }

    public static void post(String url, HttpParams params, ProgressListener listener,
                            HttpCallback callback) {
        new Builder().url(url).params(params).progressListener(listener).httpMethod(Method.POST)
                .callback(callback).doTask();
    }

    public static void jsonGet(String url, HttpParams params, HttpCallback callback) {
        new Builder().url(url).params(params).contentType(ContentType.JSON)
                .httpMethod(Method.GET).callback(callback).doTask();
    }

    public static void jsonPost(String url, HttpParams params, HttpCallback callback) {
        new Builder().url(url).params(params).contentType(ContentType.JSON)
                .httpMethod(Method.POST).callback(callback).doTask();
    }

    /**
     * 获取到在本地的二进制缓存
     *
     * @param url 缓存的key
     * @return 缓存内容
     */
    public static byte[] getCache(String url) {
        ICache cache = getRequestQueue().getCache();
        if (cache != null) {
            ICache.Entry entry = cache.get(url);
            if (entry != null) {
                return entry.data;
            }
        }
        return new byte[0];
    }

    /**
     * 下载
     *
     * @param storeFilePath    本地存储绝对路径
     * @param url              要下载的文件的url
     * @param progressListener 下载进度回调
     * @param callback         回调
     */
    public static FileRequest download(String storeFilePath, String url, ProgressListener
            progressListener, HttpCallback callback) {
        RequestConfig config = new RequestConfig();
        config.mUrl = url;
        config.mRetryPolicy = new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                20, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        FileRequest request = new FileRequest(storeFilePath, config, callback);
        request.setTag(url);
        request.setOnProgressListener(progressListener);
        new Builder().setRequest(request).doTask();
        return request;
    }
}
