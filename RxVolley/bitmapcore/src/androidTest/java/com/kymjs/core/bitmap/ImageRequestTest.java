package com.kymjs.core.bitmap;

import android.graphics.Bitmap;
import android.os.Looper;
import android.test.AndroidTestCase;
import android.widget.ImageView;

import com.kymjs.core.bitmap.client.BitmapCore;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.toolbox.FileUtils;
import com.kymjs.rxvolley.toolbox.Loger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;

/**
 * @author kymjs (http://www.kymjs.com/) on 1/5/16.
 */
public class ImageRequestTest extends AndroidTestCase {

    HttpCallback callback;
    ImageView contentView;

    public static final String SDCARD_PATH = FileUtils.getSDCardPath() + File.separator + "request.png";

    @Before
    public void setUp() throws Exception {
        callback = new HttpCallback() {
            @Override
            public void onPreStart() {
                Loger.debug("=====onPreStart");
                // 测试类AndroidTestCase是运行在异步的,所以此处断言会异常
                assertEquals(Thread.currentThread(), Looper.getMainLooper().getThread());
            }

            @Override
            public void onPreHttp() {
                Loger.debug("=====onPreHttp");
                assertEquals(Thread.currentThread(), Looper.getMainLooper().getThread());
            }

            //仅在Bitmap回调有效
            @Override
            public void onSuccess(Map<String, String> headers, Bitmap bitmap) {
                super.onSuccess(headers, bitmap);
                Loger.debug("=====onSuccess");
                assertEquals(contentView.getTag(), SDCARD_PATH);
                assertEquals(Thread.currentThread(), Looper.getMainLooper().getThread());
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                Loger.debug("=====onFailure" + strMsg);
                assertEquals(Thread.currentThread(), Looper.getMainLooper().getThread());
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Loger.debug("=====onFinish");
                assertEquals(Thread.currentThread(), Looper.getMainLooper().getThread());
            }
        };
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testBitmapWithDiskLoader() {
        contentView = new ImageView(getContext());
        new BitmapCore.Builder()
                .url(SDCARD_PATH)
                .callback(callback)
                .view(contentView)
                .doTask();
    }
}