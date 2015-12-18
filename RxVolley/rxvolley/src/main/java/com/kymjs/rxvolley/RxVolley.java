package com.kymjs.rxvolley;

import com.kymjs.rxvolley.client.FormRequest;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.kymjs.rxvolley.client.JsonRequest;
import com.kymjs.rxvolley.client.RequestConfig;
import com.kymjs.rxvolley.http.Request;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.http.RetryPolicy;
import com.kymjs.rxvolley.interf.IRespondAdapter;
import com.kymjs.rxvolley.toolbox.FileUtils;

import java.io.File;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/17/15.
 */
public class RxVolley {

    private RxVolley() {
    }

    public static File CACHE_FOLDER = FileUtils.getSaveFolder("RxVolley");

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
     * 设置请求队列,必须在调用RxVolley#getRequestQueue()之前设置
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

    public static class Builder {
        private HttpParams params;
        private int contentType;
        private HttpCallback callback;
        private Request<?> request;
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
         * HttpRequest的配置器
         */
        public Builder httpConfig(RequestConfig httpConfig) {
            this.httpConfig = httpConfig;
            return this;
        }

        /**
         * 请求超时时间,如果不设置则使用重连策略的超时时间,默认2500ms
         */
        public Builder timeout(int timeout) {
            this.httpConfig.mTimeout = timeout;
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

        /**
         * 本次请求的tag，方便在取消时找到它
         */
        public Builder tag(String tag) {
            this.httpConfig.mTag = tag;
            return this;
        }

        /**
         * 网络请求响应结果类型转换适配器
         */
        public Builder respondAdapter(IRespondAdapter<?> adapter) {
            this.httpConfig.adapter = adapter;
            return this;
        }


        public void doTask() {
            doHttp();
        }

        /**
         * 做Http请求
         */
        private void doHttp() {
            if (request == null) {
                if (params == null) {
                    params = new HttpParams();
                } else {
                    if (httpConfig.mMethod == RxVolley.Method.GET)
                        httpConfig.mUrl += params.getUrlParams();
                }

                if (contentType == ContentType.JSON) {
                    request = new JsonRequest(httpConfig, params, callback);
                } else {
                    request = new FormRequest(httpConfig, params, callback);
                }
            }
            getRequestQueue().add(request);
        }
    }
}
