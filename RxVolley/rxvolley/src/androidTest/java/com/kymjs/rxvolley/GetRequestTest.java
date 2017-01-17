package com.kymjs.rxvolley;

import android.os.Looper;
import android.test.AndroidTestCase;

import com.kymjs.common.Log;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.rx.Result;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


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
                Log.d("=====onPreStart");
                // 测试类是运行在异步的,所以此处断言会异常
                // assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onPreHttp() {
                Log.d("=====onPreHttp");
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onSuccessInAsync(byte[] t) {
                assertNotNull(t);
                Log.d("=====onSuccessInAsync" + new String(t));
                //onSuccessInAsync 一定是运行在异步
                assertFalse(Thread.currentThread() == Looper.getMainLooper().getThread());
            }

            @Override
            public void onSuccess(String t) {
                Log.d("=====onSuccess" + t);
                assertNotNull(t);
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
                .map(new Function<Result, Map<String, String>>() {
                    @Override
                    public Map<String, String> apply(Result result) throws Exception {
                        return result.headers;
                    }
                })
                .subscribe(new Consumer<Map<String, String>>() {
                    @Override
                    public void accept(Map<String, String> headers) throws Exception {
                        Log.d(headers.get("Cache-Control"));
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
                .map(new Function<Result, String>() {
                    @Override
                    public String apply(Result result) throws Exception {
                        return new String(result.data);
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Log.d("=====onSuccess" + s);
                        assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
                    }
                });
    }

}