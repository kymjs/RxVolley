package com.kymjs.rxvolley.toolbox;

import android.util.Log;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/17/15.
 */
public class Loger {
    private static boolean sEnable = true;

    public static void setEnable(boolean enable) {
        sEnable = enable;
    }

    public static void debug(String msg) {
        if (sEnable) {
            Log.i("RxVolley", "" + msg);
        }
    }
}
