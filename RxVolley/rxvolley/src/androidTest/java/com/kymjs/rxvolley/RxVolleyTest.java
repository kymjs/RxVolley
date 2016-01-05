package com.kymjs.rxvolley;

import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.toolbox.Loger;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * @author kymjs (http://www.kymjs.com/) on 1/5/16.
 */
public class RxVolleyTest {

    HttpCallback callback;

    @Before
    public void setUp() throws Exception {
        callback = new HttpCallback() {
            @Override
            public void onPreStart() {
                super.onPreStart();
                Loger.debug("=====onPreStart");
            }

            @Override
            public void onPreHttp() {
                super.onPreHttp();
                Loger.debug("=====onPreHttp");
            }

            @Override
            public void onSuccessInAsync(byte[] t) {
                super.onSuccessInAsync(t);
                Loger.debug("=====onSuccessInAsync" + t);
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Loger.debug("=====onSuccess" + t);
            }

            @Override
            public void onSuccess(Map<String, String> headers, byte[] t) {
                super.onSuccess(headers, t);
                Loger.debug("=====onSuccessWithHeader" + headers.size() + t);
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                Loger.debug("=====onFailure" + strMsg);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Loger.debug("=====onFinish");
            }
        };
    }

    @Test
    public void testGet() throws Exception {
        RxVolley.get("http://www.baidu.com/", callback);
    }

    @Test
    public void testGet1() throws Exception {

    }

    @Test
    public void testPost() throws Exception {

    }

    @Test
    public void testJsonGet() throws Exception {

    }

    @Test
    public void testJsonPost() throws Exception {

    }

    @Test
    public void testGetCache() throws Exception {

    }

    @Test
    public void testDownload() throws Exception {

    }
}