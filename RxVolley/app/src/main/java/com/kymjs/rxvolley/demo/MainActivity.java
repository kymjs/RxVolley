package com.kymjs.rxvolley.demo;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.kymjs.okhttp.OkHttpStack;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.kymjs.rxvolley.client.ProgressListener;
import com.kymjs.rxvolley.http.RequestQueue;
import com.kymjs.rxvolley.rx.Result;
import com.kymjs.rxvolley.toolbox.FileUtils;
import com.kymjs.rxvolley.toolbox.Loger;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private Subscription subscription;

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
                        Loger.debug(transferredBytes + "=====" + totalSize);
                        Loger.debug("=====当前线程" + (Thread.currentThread() == Looper.getMainLooper
                                ().getThread()));
                    }
                }, new HttpCallback() {
                    @Override
                    public void onSuccess(String t) {
                        Loger.debug("=====完成" + t);
                    }
                });
    }

    /**
     * RxJava
     */
    private void test() {
        Observable<Result> observable = new RxVolley.Builder()
                .url("http://kymjs.com/feed.xmlsss")
//                .url("https://api.douban.com/v2/book/26692621") //服务器端声明了no-cache
                .contentType(RxVolley.ContentType.FORM)
                .shouldCache(true)
                .httpMethod(RxVolley.Method.GET)
                .getResult();

        subscription = observable
                .filter(new Func1<Result, Boolean>() {
                    @Override
                    public Boolean call(Result result) {
                        return result.data != null;
                    }
                })
                .map(new Func1<Result, String>() {
                    @Override
                    public String call(Result result) {
                        return new String(result.data);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.i("kymjs", "======网络请求结束");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("kymjs", "======网络请求失败" + e.getMessage());
                    }

                    @Override
                    public void onNext(String s) {
                        Log.i("kymjs", "======网络请求" + s);
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
                        Loger.debug(transferredBytes + "======" + totalSize);
                    }
                }, new HttpCallback() {
                    @Override
                    public void onSuccess(String t) {
                        Loger.debug("====success" + t);
                    }

                    @Override
                    public void onFailure(int errorNo, String strMsg) {
                        Loger.debug(errorNo + "====failure" + strMsg);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscription != null && subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
