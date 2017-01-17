package com.kymjs.rxvolley;

import android.os.Looper;
import android.test.AndroidTestCase;

import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.ProgressListener;

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
        callback = new HttpCallback() {
            @Override
            public void onPreStart() {
                Loger.debug("=====onPreStart");

//                测试类是运行在异步的,所以此处断言会异常
//                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onPreHttp() {
                super.onPreHttp();
                Loger.debug("=====onPreHttp");
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onSuccessInAsync(byte[] t) {
                Loger.debug("=====onSuccessInAsync" + new String(t));

                assertNotNull(t);

                //onSuccessInAsync 一定是运行在异步
                assertFalse(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            public void onSuccess(File file) {
                Loger.debug("=====onSuccess" + file);
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onSuccess(Map<String, String> headers, byte[] t) {
                assertNotNull(t);
                Loger.debug("=====onSuccessWithHeader" + headers.size() + new String(t));
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                Loger.debug("=====onFailure" + strMsg);
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Loger.debug("=====onFinish");
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
                Loger.debug(transferredBytes + "======" + totalSize);
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }
        }, callback);
    }
}
