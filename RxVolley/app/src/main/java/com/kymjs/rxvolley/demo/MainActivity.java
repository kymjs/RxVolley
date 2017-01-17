package com.kymjs.rxvolley.demo;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kymjs.common.FileUtils;
import com.kymjs.common.Log;
import com.kymjs.okhttp3.OkHttpStack;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.kymjs.rxvolley.client.ProgressListener;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.rx.Result;

import java.io.File;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxVolley.setRequestQueue(RequestQueue.newRequestQueue(RxVolley.CACHE_FOLDER,
                new OkHttpStack(new OkHttpClient())));


        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
    }

    /**
     * 上传进度回调
     */
    private void testUploadProgress() {
        HttpParams params = new HttpParams();
        params.putHeaders("cookie", "aliyungf_tc=AQAAAOEM/UExEAsAUAYscy4Da0FfTWqX;" +
                "oscid=vv%2BiiKldi6wRaKbbRig0DDvMcIURmo56ZCZD2bfC83AsmxdhUxEVnr3ORNGz7BjiFlkpGQHUKJoRTzVAwy3oVtcO7JsM4nRIjEl6ZgM%2BmZgplCH0foAIiQ%3D%3D;");

        params.put("uid", 863548);
        params.put("msg", "睡会");
        params.put("img", new File(FileUtils.getSDCardPath() + "/request.png"));

        RxVolley.post("http://192.168.1.11/action/api/software_tweet_pub", params,
                new ProgressListener() {
                    @Override
                    public void onProgress(long transferredBytes, long totalSize) {
                        Log.d(transferredBytes + "=====" + totalSize);
                        Log.d("=====当前线程" + (Thread.currentThread() == Looper.getMainLooper
                                ().getThread()));
                    }
                }, new HttpCallback() {
                    @Override
                    public void onSuccess(String t) {
                        Log.d("=====完成" + t);
                    }
                });
    }

    /**
     * RxJava
     */
    private void test() {
        HttpCallback callback = new HttpCallback() {
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

        Observable<Result> observable = new RxVolley.Builder()
//                .url("http://kymjs.com/feed.xml")
                .url("https://api.douban.com/v2/book/26692621") //服务器端声明了no-cache
                .contentType(RxVolley.ContentType.FORM)
                .shouldCache(true)
                .httpMethod(RxVolley.Method.GET)
                .callback(callback)
                .getResult();

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Result>() {
                    @Override
                    public void onComplete() {
                        Log.i("kymjs", "======网络请求结束");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.i("kymjs", "======网络请求开始");
                    }

                    @Override
                    public void onNext(Result s) {
                        Log.i("kymjs", "======网络请求" + new String(s.data));
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("kymjs", "======网络请求失败" + e.getMessage());
                    }
                });
    }

    /**
     * 下载
     */
    private void download() {
        RxVolley.download(FileUtils.getSDCardPath() + "/a.apk",
                "https://www.oschina.net/uploads/osc-android-app-2.4.apk", new ProgressListener() {
                    @Override
                    public void onProgress(long transferredBytes, long totalSize) {
                        Log.d(transferredBytes + "======" + totalSize);
                    }
                }, new HttpCallback() {
                    @Override
                    public void onSuccess(String t) {
                        Log.d("====success" + t);
                    }

                    @Override
                    public void onFailure(int errorNo, String strMsg) {
                        Log.d(errorNo + "====failure" + strMsg);
                    }
                });
    }
}
