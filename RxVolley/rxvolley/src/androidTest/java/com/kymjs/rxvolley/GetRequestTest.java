package com.kymjs.rxvolley;

import android.test.AndroidTestCase;

import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.kymjs.rxvolley.toolbox.Loger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * @author kymjs (http://www.kymjs.com/) on 1/5/16.
 */
public class GetRequestTest extends AndroidTestCase {

    HttpCallback callback;

    @Before
    public void setUp() throws Exception {
        RxVolley.CACHE_FOLDER = getContext().getCacheDir();

        callback = new HttpCallback() {
            @Override
            public void onPreStart() {
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
                Loger.debug("=====onSuccessInAsync" + new String(t));
            }

            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Loger.debug("=====onSuccess" + t);
            }

            @Override
            public void onSuccess(Map<String, String> headers, byte[] t) {
                super.onSuccess(headers, t);
                Loger.debug("=====onSuccessWithHeader" + headers.size() + new String(t));
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

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetOnSuccess() throws Exception {
        RxVolley.get("http://www.oschina.net/action/api/news_list", callback);
    }

    @Test
    public void testGetOnFailure() throws Exception {
        RxVolley.get("http://failure/url/", callback);
    }

    @Test
    public void testGetWithParams() throws Exception {
        HttpParams params = new HttpParams();
        params.put("pageIndex", 1);
        params.put("pageSize", 20);
        RxVolley.get("http://www.oschina.net/action/api/news_list", params, callback);
    }
}