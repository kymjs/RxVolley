package com.kymjs.rxvolley;

import android.os.Looper;
import android.test.AndroidTestCase;

import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.ProgressListener;
import com.kymjs.rxvolley.toolbox.FileUtils;
import com.kymjs.rxvolley.toolbox.Loger;

import org.junit.Before;
import org.junit.Test;

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
            }

            @Override
            public void onPreHttp() {
                super.onPreHttp();
                Loger.debug("=====onPreHttp");
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            @Override
            public void onSuccessInAsync(byte[] t) {
                super.onSuccessInAsync(t);
                Loger.debug("=====onSuccessInAsync" + new String(t));
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Loger.debug("=====onSuccess" + t);
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            @Override
            public void onSuccess(Map<String, String> headers, byte[] t) {
                super.onSuccess(headers, t);
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
                Loger.debug("=====onSuccessWithHeader" + headers.size() + new String(t));
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
                Loger.debug("=====onFailure" + strMsg);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
                Loger.debug("=====onFinish");
            }
        };
    }


    @Test
    public void testDownload() throws Exception {
        RxVolley.download(FileUtils.getSDCardPath() + "/a.apk",
                "https://www.oschina.net/uploads/osc-android-app-2.4.apk",
                new ProgressListener() {
                    @Override
                    public void onProgress(long transferredBytes, long totalSize) {
                        Loger.debug(transferredBytes + "======" + totalSize);
                        Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper
                                .getMainLooper().getThread()));
                    }
                }, callback);
    }
}
