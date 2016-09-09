package com.kymjs.rxvolley.demo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.kymjs.core.bitmap.client.BitmapCore;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.toolbox.Loger;

import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Created by kymjs on 2/28/16.
 */
public class RxImageRequestTest extends AppCompatActivity {
    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;
    ImageView imageView5;

    HttpCallback callback;

    String[] datas_link = new String[]{
            "http://kymjs.com/image/logo.jpg",
            "http://kymjs.com/images/10.jpg",
            "http://kymjs.com/images/11.jpg",
            "http://kymjs.com/images/12.jpg",
            "http://kymjs.com/images/13.jpg",
            "http://kymjs.com/images/14.jpg",
            "http://kymjs.com/images/15.jpg",
            "http://kymjs.com/images/16.jpg",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rxloadimage_test);
        setUp();

        testBitmapLoader();

        tearDown();
    }

    private void setUp() {
        imageView1 = (ImageView) findViewById(R.id.image1);
        imageView2 = (ImageView) findViewById(R.id.image2);
        imageView3 = (ImageView) findViewById(R.id.image3);
        imageView4 = (ImageView) findViewById(R.id.image4);
        imageView5 = (ImageView) findViewById(R.id.image5);
        callback = new HttpCallback() {
            @Override
            public void onPreStart() {
                Loger.debug("=====onPreStart");
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            //访问网络之前会回调
            @Override
            public void onPreHttp() {
                super.onPreHttp();
                Loger.debug("=====onPreHttp");
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            //仅在Bitmap回调有效
            @Override
            public void onSuccess(Map<String, String> headers, Bitmap bitmap) {
                super.onSuccess(headers, bitmap);
                Loger.debug("=====onSuccessBitmap" + headers.size());
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            @Override
            public void onFailure(int errorNo, String strMsg) {
                super.onFailure(errorNo, strMsg);
                Loger.debug("=====onFailure" + strMsg);
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Loger.debug("=====onFinish");
                Loger.debug("=====当前线程是主线程" + (Thread.currentThread() == Looper.getMainLooper
                        ().getThread()));
            }
        };
    }

    private void testBitmapLoader() {
        new BitmapCore.Builder()
                .view(imageView1)
                .url(datas_link[1])
                .callback(callback)
                .getResult()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        Log.i("kymjs", "======::网络请求完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("kymjs", "======::网络请求失败" + e.getMessage());
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        Log.i("kymjs", "======::网络请求");
                        imageView1.setImageBitmap(bitmap);
                    }
                });
        new BitmapCore.Builder()
                .callback(callback)
                .url(datas_link[2])
                .getResult()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<Bitmap, Boolean>() {
                    @Override
                    public Boolean call(Bitmap bitmap) {
                        return bitmap != null;
                    }
                })
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        Log.i("kymjs", "======::网络请求完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("kymjs", "======::网络请求失败" + e.getMessage());
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        Log.i("kymjs", "======::网络请求");
                        imageView2.setImageBitmap(bitmap);
                    }
                });
        new BitmapCore.Builder()
                .url(datas_link[3])
                .getResult()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<Bitmap, Boolean>() {
                    @Override
                    public Boolean call(Bitmap bitmap) {
                        return bitmap != null;
                    }
                })
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        Log.i("kymjs", "======::网络请求完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("kymjs", "======::网络请求失败" + e.getMessage());
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        Log.i("kymjs", "======::网络请求");
                        imageView3.setImageBitmap(bitmap);
                    }
                });
        new BitmapCore.Builder()
                .url(datas_link[4])
                .callback(callback)
                .getResult()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<Bitmap, Boolean>() {
                    @Override
                    public Boolean call(Bitmap bitmap) {
                        return bitmap != null;
                    }
                })
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        Log.i("kymjs", "======::网络请求完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("kymjs", "======::网络请求失败" + e.getMessage());
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        Log.i("kymjs", "======::网络请求");
                        imageView4.setImageBitmap(bitmap);
                    }
                });
        new BitmapCore.Builder()
                .view(imageView5)
                .url(datas_link[0])
                .callback(callback)
                .getResult()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<Bitmap, Boolean>() {
                    @Override
                    public Boolean call(Bitmap bitmap) {
                        return bitmap != null;
                    }
                })
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        Log.i("kymjs", "======::网络请求完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i("kymjs", "======::网络请求失败" + e.getMessage());
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        Log.i("kymjs", "======::网络请求");
                        imageView5.setImageBitmap(bitmap);
                    }
                });

    }

    private void tearDown() {
        //这里写rxjava解除订阅的代码
    }
}
