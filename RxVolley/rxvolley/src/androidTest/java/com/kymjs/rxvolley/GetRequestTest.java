package com.kymjs.rxvolley;

import android.os.Looper;
import android.test.AndroidTestCase;

import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.rx.Result;
import com.kymjs.rxvolley.toolbox.Loger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author kymjs (http://www.kymjs.com/) on 1/5/16.
 */
public class GetRequestTest extends AndroidTestCase {

    HttpCallback callback;

    @Before
    public void setUp() throws Exception {
        RxVolley.setRequestQueue(RequestQueue.newRequestQueue(getContext().getCacheDir()));

        callback = new HttpCallback() {
            @Override
            public void onPreStart() {
                Loger.debug("=====onPreStart");
                // 测试类是运行在异步的,所以此处断言会异常
                // assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onPreHttp() {
                Loger.debug("=====onPreHttp");
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onSuccessInAsync(byte[] t) {
                assertNotNull(t);
                Loger.debug("=====onSuccessInAsync" + new String(t));
                //onSuccessInAsync 一定是运行在异步
                assertFalse(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onSuccess(String t) {
                Loger.debug("=====onSuccess" + t);
                assertNotNull(t);
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

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetOnSuccessWithNoCacheHeader() throws Exception {
        //header中声明了cache-control:must-revalidate, no-cache, private
        RxVolley.get("https://api.douban.com/v2/book/26692621", callback);
    }

    @Test
    public void testGetOnSuccessWithNoCacheHeader2() throws Exception {
        //header中声明了cache-control:must-revalidate, no-cache, private
        new RxVolley.Builder()
                .url("https://api.douban.com/v2/book/26692621")
                .getResult()
                .map(new Func1<Result, Map<String, String>>() {
                    @Override
                    public Map<String, String> call(Result result) {
                        return result.headers;
                    }
                })
                .subscribe(new Action1<Map<String, String>>() {
                    @Override
                    public void call(Map<String, String> headers) {
                        Loger.debug(headers.get("Cache-Control"));
                    }
                });
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

    /**
     * 也是跟download一样的问题,在测试中不返回
     */
    @Test
    public void testGetRxJava() throws Exception {
        new RxVolley.Builder().callback(callback)
                .url("http://www.oschina.net/action/api/news_list").getResult()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Result, String>() {
                    @Override
                    public String call(Result result) {
                        return new String(result.data);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Loger.debug("=====onSuccess" + s);
                        assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
                    }
                });
    }

}