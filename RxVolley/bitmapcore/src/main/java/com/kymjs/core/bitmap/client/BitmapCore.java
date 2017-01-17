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
package com.kymjs.core.bitmap.client;

import android.annotation.SuppressLint;
import android.database.Observable;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.kymjs.common.DensityUtils;
import com.kymjs.common.Log;
import com.kymjs.core.bitmap.BitmapMemoryCache;
import com.kymjs.core.bitmap.DiskImageDisplayer;
import com.kymjs.core.bitmap.ImageBale;
import com.kymjs.core.bitmap.ImageDisplayer;
import com.kymjs.core.bitmap.interf.IBitmapCache;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.http.RetryPolicy;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 入口类
 *
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public final class BitmapCore {

    private BitmapCore() {
    }

    private static ImageDisplayer sDisplayer;
    private static DiskImageDisplayer sDiskImageDisplayer;

    private final static ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    private static LinkedList<ImageBale> requestArray = new LinkedList<>();

    /**
     * 获取一个请求队列(单例)
     */
    public synchronized static ImageDisplayer getDisplayer() {
        if (sDisplayer == null) {
            createDisplayer(null, null);
        }
        return sDisplayer;
    }

    public synchronized static DiskImageDisplayer getDiskDisplayer() {
        if (sDiskImageDisplayer == null) {
            createDisplayer(null, null);
        }
        return sDiskImageDisplayer;
    }

    public static ExecutorService getExecutorService() {
        return DEFAULT_EXECUTOR_SERVICE;
    }

    /**
     * 设置请求队列,必须在调用BitmapCore#getDisplayer()之前设置
     *
     * @return 是否设置成功
     */
    public synchronized static boolean createDisplayer(RequestQueue queue, IBitmapCache
            mMemoryCache) {
        if (queue == null)
            queue = RxVolley.getRequestQueue();
        if (mMemoryCache == null) mMemoryCache = new BitmapMemoryCache();
        if (sDiskImageDisplayer == null) {
            sDiskImageDisplayer = new DiskImageDisplayer(mMemoryCache);
        }
        if (sDisplayer == null) {
            sDisplayer = new ImageDisplayer(queue, mMemoryCache);
            return true;
        } else {
            return false;
        }
    }

    public static class Builder {
        private HttpCallback realCallback;
        private HttpCallback callback;
        private View view;
        private BitmapRequestConfig config = new BitmapRequestConfig();

        /**
         * 请求回调,不需要可以为空
         */
        public Builder callback(HttpCallback callback) {
            this.callback = callback;
            return this;
        }

        /**
         * 要显示图片的view
         */
        public Builder view(View view) {
            this.view = view;
            return this;
        }

        /**
         * HttpRequest的配置器
         */
        public Builder config(BitmapRequestConfig config) {
            this.config = config;
            return this;
        }

        public Builder loadResId(int loadResId) {
            this.config.loadRes = loadResId;
            return this;
        }

        public Builder errorResId(int errorResId) {
            this.config.errorRes = errorResId;
            return this;
        }

        public Builder loadDrawable(Drawable loadDrawable) {
            this.config.loadDrawable = loadDrawable;
            return this;
        }

        public Builder errorDrawable(Drawable errorDrawable) {
            this.config.errorDrawable = errorDrawable;
            return this;
        }

        public Builder putHeader(String k, String v) {
            this.config.putHeader(k, v);
            return this;
        }

        /**
         * 使用并行访问本地图片
         *
         * @param useAsync true为并行,false为串行
         */
        public Builder useAsyncLoadDiskImage(boolean useAsync) {
            this.config.useAsyncLoadDisk = useAsync;
            return this;
        }

        /**
         * 请求超时时间,如果不设置则使用重连策略的超时时间,默认2500ms
         */
        public Builder timeout(int timeout) {
            this.config.mTimeout = timeout;
            return this;
        }

        /**
         * 为了更真实的模拟网络,如果读取缓存,延迟一段时间再返回缓存内容
         */
        public Builder delayTime(int delayTime) {
            this.config.mDelayTime = delayTime;
            return this;
        }

        /**
         * 显示图片的最大宽高,若图片高于这个值则压缩,否则不作处理
         */
        public Builder size(int w, int h) {
            this.config.maxWidth = w;
            this.config.maxHeight = h;
            return this;
        }

        /**
         * 是否使用服务器控制的缓存有效期(如果使用服务器端的,则无视#cacheTime())
         */
        public Builder useServerControl(boolean useServerControl) {
            this.config.mUseServerControl = useServerControl;
            return this;
        }

        /**
         * 是否启用缓存
         */
        public Builder shouldCache(boolean shouldCache) {
            this.config.mShouldCache = shouldCache;
            return this;
        }

        /**
         * 网络请求接口url
         */
        public Builder url(String url) {
            this.config.mUrl = url;
            return this;
        }

        /**
         * 重连策略,不传则使用默认重连策略
         */
        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.config.mRetryPolicy = retryPolicy;
            return this;
        }

        /**
         * 编码,默认UTF-8
         */
        public Builder encoding(String encoding) {
            this.config.mEncoding = encoding;
            return this;
        }

        /**
         * 安全校验
         */
        private synchronized void build() {
            if (TextUtils.isEmpty(config.mUrl)) {
                Log.d("image url is empty");
                doFailure(view, config.errorDrawable, config.errorRes);
                if (callback != null)
                    callback.onFailure(-1, "image url is empty");
                return;
            }

            if (config.mShouldCache == null) {
                config.mShouldCache = Boolean.TRUE;
            }

            if (view != null) {
                if (config.maxWidth == BitmapRequestConfig.DEF_WIDTH_HEIGHT &&
                        config.maxHeight == BitmapRequestConfig.DEF_WIDTH_HEIGHT) {
                    config.maxWidth = view.getWidth();
                    config.maxHeight = view.getHeight();
                } else if (config.maxWidth == BitmapRequestConfig.DEF_WIDTH_HEIGHT) {
                    config.maxWidth = DensityUtils.getScreenW();
                } else if (config.maxHeight == BitmapRequestConfig.DEF_WIDTH_HEIGHT) {
                    config.maxHeight = DensityUtils.getScreenH();
                }
            }

            if (config.loadRes == 0 && config.loadDrawable == null) {
                config.loadDrawable = new ColorDrawable(0xFFCFCFCF);
            }
            if (config.errorRes == 0 && config.errorDrawable == null) {
                config.errorDrawable = new ColorDrawable(0xFFCFCFCF);
            }

            if (realCallback == null)
                realCallback = new HttpCallback() {
                    @Override
                    public void onPreStart() {
                        if (view != null)
                            view.setTag(config.mUrl);
                        if (callback != null) callback.onPreStart();
                    }

                    @Override
                    public void onPreHttp() {
                        setImageWithResource(view, config.loadDrawable, config.loadRes);
                        if (callback != null) callback.onPreHttp();
                    }

                    @Override
                    public void onSuccessInAsync(byte[] t) {
                        if (callback != null) callback.onSuccessInAsync(t);
                        if (config.animation != null
                                && config.anim != 0) {
                            config.animation = AnimationUtils.loadAnimation(view.getContext(), config.anim);
                        }
                    }

                    @Override
                    public void onFailure(int errorNo, String strMsg) {
                        if (view != null && config.mUrl.equals(view.getTag())) {
                            setImageWithResource(view, config.errorDrawable, config.errorRes);
                        }
                        if (callback != null) callback.onFailure(errorNo, strMsg);
                    }

                    @Override
                    public void onFinish() {
                        if (callback != null) callback.onFinish();
                        //从正在请求的列表中移除
                        for (ImageBale bale : requestArray) {
                            if (config.mUrl.equals(bale.getRequestUrl())) {
                                requestArray.remove(bale);
                                break;
                            }
                        }
                    }

                    @Override
                    public void onSuccess(Map<String, String> headers, Bitmap bitmap) {
                        if (view != null && config.mUrl.equals(view.getTag())) {
                            setViewImage(view, bitmap);
                        }
                        if (config.animation != null) {
                            view.startAnimation(config.animation);
                        }
                        if (callback != null) {
                            callback.onSuccess(headers, bitmap);
                        }
                    }
                };
        }

        public Observable<Bitmap> getResult() {
            //// TODO: 12/28/16  
//            doTask();
//            return config.mSubject
//                    .map(new Function<Result, Bitmap>() {
//                        @Override
//                        public Bitmap apply(Result result) throws Exception {
//                            return CreateBitmap.create(result.data, config.maxWidth, config.maxHeight);
//                        }
//                    })
//                    .subscribeOn(Schedulers.io());
            return null;
        }

        public void doTask() {
            build();
            if (config.mUrl.startsWith("http")) {
                ImageBale bale = getDisplayer().get(config, realCallback);
                requestArray.add(bale);
            } else {
                getDiskDisplayer().load(config, realCallback, config.useAsyncLoadDisk);
            }
        }
    }

    /**
     * 取消一个请求
     *
     * @return 是否成功
     */
    public static boolean cancle(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        for (ImageBale bale : requestArray) {
            if (url.equals(bale.getRequestUrl())) {
                bale.cancelRequest();
                requestArray.remove(bale);
                return true;
            }
        }
        return false;
    }

    public static Bitmap getMemoryBitmap(String url) {
        return getDisplayer().getMemoryCache().getBitmap(url);
    }

    /**
     * 按照优先级为View设置图片资源
     * 优先使用drawable，仅当drawable无效时使用bitmapRes，若两值均无效，则不作处理
     *
     * @param view          要设置图片的控件(View设置bg，ImageView设置src)
     * @param errorImage    优先使用项
     * @param errorImageRes 次级使用项
     */
    public static void doFailure(View view, Drawable errorImage, int errorImageRes) {
        setImageWithResource(view, errorImage, errorImageRes);
    }

    /**
     * 按照优先级为View设置图片资源
     *
     * @param view          要设置图片的控件(View设置bg，ImageView设置src)
     * @param bitmap        优先使用项
     * @param errorImage    二级使用项
     * @param errorImageRes 三级使用项
     */
    public static void doSuccess(View view, Bitmap bitmap, Drawable errorImage,
                                 int errorImageRes) {
        if (bitmap != null) {
            setViewImage(view, bitmap);
        } else {
            setImageWithResource(view, errorImage, errorImageRes);
        }
    }

    /**
     * 按照优先级为View设置图片资源
     * 优先使用drawable，仅当drawable无效时使用bitmapRes，若两值均无效，则不作处理
     *
     * @param imageView 要设置图片的控件(View设置bg，ImageView设置src)
     * @param drawable  优先使用项
     * @param bitmapRes 次级使用项
     */
    public static void setImageWithResource(View imageView, Drawable drawable,
                                            int bitmapRes) {
        if (drawable != null) {
            setViewImage(imageView, drawable);
        } else if (bitmapRes > 0) { //大于0视为有效ImageResource
            setViewImage(imageView, bitmapRes);
        }
    }

    public static void setViewImage(View view, int background) {
        if (view == null) return;
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(background);
        } else {
            view.setBackgroundResource(background);
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static void setViewImage(View view, Bitmap background) {
        if (view == null) return;
        if (view instanceof ImageView) {
            ((ImageView) view).setImageBitmap(background);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                view.setBackground(new BitmapDrawable(view.getResources(),
                        background));
            } else {
                view.setBackgroundDrawable(new BitmapDrawable(view
                        .getResources(), background));
            }
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static void setViewImage(View view, Drawable background) {
        if (view == null) return;
        if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(background);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                view.setBackground(background);
            } else {
                view.setBackgroundDrawable(background);
            }
        }
    }
}
