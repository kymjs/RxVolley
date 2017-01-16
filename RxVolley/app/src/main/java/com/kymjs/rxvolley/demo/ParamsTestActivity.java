package com.kymjs.rxvolley.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kymjs.common.Log;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;

/**
 * Created by ZhangTao on 9/9/16.
 */
public class ParamsTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        <?php
//        foreach($_POST as $key => $val){
//            if(is_array($val)) {
//                foreach ($val as $v2) {
//                    echo "数组key中的数据：$v2<br>";
//                }
//            } else {
//                echo "key为$key 的数据：$val<br>";
//            }
//        }
//        ?>
//        
        HttpParams params = new HttpParams();
        params.put("name[]", "hello");
        params.put("name[]", "hello2");
        params.put("name[]", "hello3");
        params.put("name2", "hell    o3333");
        RxVolley.get("http://172.16.12.236/php/array_info.php", params, new HttpCallback() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Log.d("=======::" + t);
            }
        });
    }
}
