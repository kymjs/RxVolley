/*
 * Copyright (c) 2014, 张涛.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kymjs.rxvolley.toolbox;

import android.util.Log;

/**
 * Log打印管理
 *
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
