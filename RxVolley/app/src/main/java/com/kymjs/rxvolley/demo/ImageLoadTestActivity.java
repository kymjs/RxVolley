package com.kymjs.rxvolley.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.widget.ImageView;

import com.kymjs.core.bitmap.client.BitmapCore;
import com.kymjs.okhttp3.OkHttpStack;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.toolbox.FileUtils;
import com.kymjs.rxvolley.toolbox.Loger;

import java.io.File;
import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * Created by kymjs on 2/23/16.
 */
public class ImageLoadTestActivity extends Activity {

    HttpCallback callback;
    public ImageView contentView;

    protected void setUp() {
        RxVolley.setRequestQueue(RequestQueue.newRequestQueue(RxVolley.CACHE_FOLDER,
                new OkHttpStack(new OkHttpClient())));

        callback = new HttpCallback() {
            @Override
            public void onPreStart() {
                Loger.debug("=====onPreStart");
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            //访问网络之前会回调
            @Override
            public void onPreHttp() {
                super.onPreHttp();
                Loger.debug("=====onPreHttp");
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            //仅在Bitmap回调有效
            @Override
            public void onSuccess(Map<String, String> headers, Bitmap bitmap) {
                super.onSuccess(headers, bitmap);
                Loger.debug("=====onSuccessBitmap" + headers.size());
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                Loger.debug("=====onFailure" + strMsg);
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Loger.debug("=====onFinish");
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }
        };

        contentView = (ImageView) findViewById(R.id.imageView);
    }

    protected void tearDown() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUp();

        testBitmapWithDiskLoader();

        tearDown();
    }

    public void test() {
        new BitmapCore.Builder()
                .view(contentView)
                .url("http://kymjs.com/image/logo.jpg")
                .callback(callback).doTask();
    }

    /**
     * 定制加载中、加载出错的图片
     */
    public void testBitmapWithLoadImage() {
        new BitmapCore.Builder()
                .url("http://kymjs.com/image/logo.jpg")
                .callback(callback)
                .view(contentView)
                .loadResId(R.mipmap.ic_launcher)
                .errorResId(R.mipmap.ic_launcher)
                .doTask();
    }

    /**
     * 从本地SD卡加载图片(可直接传路径)
     */
    public void testBitmapWithDiskLoader() {
        new BitmapCore.Builder()
                .url(FileUtils.getSDCardPath() + File.separator + "request.png")
                .callback(callback)
                .view(contentView)
                .loadResId(R.mipmap.ic_launcher)
                .errorResId(R.mipmap.ic_launcher)
                .doTask();
    }

    /**
     * 使用并行模式访问本地图片(默认为串行)
     */
    public void testBitmapWithDiskLoader2() {
        new BitmapCore.Builder()
                .url(FileUtils.getSDCardPath() + File.separator + "request.png")
                .callback(callback)
                .view(contentView)
                .loadResId(R.mipmap.ic_launcher)
                .errorResId(R.mipmap.ic_launcher)
                //并行访问本地图片
                .useAsyncLoadDiskImage(true)
                .doTask();
    }
}
