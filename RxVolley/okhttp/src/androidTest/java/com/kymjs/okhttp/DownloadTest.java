package com.kymjs.okhttp;

import android.os.Looper;
import android.test.AndroidTestCase;

import com.kymjs.common.Log;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.ProgressListener;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.toolbox.FileUtils;
import com.squareup.okhttp.OkHttpClient;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * Created by kymjs on 1/21/16.
 */
public class DownloadTest extends AndroidTestCase {

    HttpCallback callback;

    @Before
    public void setUp() throws Exception {
        RxVolley.setRequestQueue(RequestQueue.newRequestQueue(RxVolley.CACHE_FOLDER,
                new OkHttpStack(new OkHttpClient())));

        callback = new HttpCallback() {
            @Override
            public void onPreStart() {
                Log.d("=====onPreStart");

//                测试类是运行在异步的,所以此处断言会异常
//                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onPreHttp() {
                super.onPreHttp();
                Log.d("=====onPreHttp");
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onSuccessInAsync(byte[] t) {
                Log.d("=====onSuccessInAsync" + new String(t));

                assertNotNull(t);

                //onSuccessInAsync 一定是运行在异步
                assertFalse(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            public void onSuccess(File file) {
                Log.d("=====onSuccess" + file);
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onSuccess(Map<String, String> headers, byte[] t) {
                assertNotNull(t);
                Log.d("=====onSuccessWithHeader" + headers.size() + new String(t));
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                Log.d("=====onFailure" + strMsg);
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Log.d("=====onFinish");
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }
        };
    }

    public static final String URL = "https://www.oschina.net/uploads/osc-android-app-2.4.apk";
    public static final String SAVE_PATH = FileUtils.getSDCardPath() + "/a.apk";

    @Test
    public void testDownload() throws Exception {
        RxVolley.download(SAVE_PATH, URL, new ProgressListener() {
            @Override
            public void onProgress(long transferredBytes, long totalSize) {
                Log.d(transferredBytes + "======" + totalSize);
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }
        }, callback);
    }
}
