package com.kymjs.rxvolley.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.toolbox.Loger;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new RxVolley.Builder().url("http://www.kymjs.com/rss.xml")
                .callback(new HttpCallback() {
                    @Override
                    public void onSuccess(String t) {
                        super.onSuccess(t);
                        Loger.debug("======" + t);
                    }

                    @Override
                    public void onFailure(int errorNo, String strMsg) {
                        super.onFailure(errorNo, strMsg);
                        Loger.debug("======" + strMsg);
                    }
                }).doTask();
    }
}
