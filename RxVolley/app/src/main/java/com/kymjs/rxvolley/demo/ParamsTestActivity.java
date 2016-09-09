package com.kymjs.rxvolley.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.HttpParams;
import com.kymjs.rxvolley.toolbox.Loger;

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
        params.put("name2", "hello3333");
        RxVolley.post("http://192.168.31.155/php/array_info.php", params, new HttpCallback() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                Loger.debug("=======::" + t);
            }
        });
    }
}
