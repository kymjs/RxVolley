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

import android.support.annotation.NonNull;

/**
 * Http请求中的参数(包括请求头参数)封装
 *
 * @author kymjs (http://www.kymjs.com/) on 12/17/15.
 */
public class HttpParamsEntry implements Comparable<HttpParamsEntry> {
    public String k;
    public String v;

    @Override
    public boolean equals(Object o) {
        if (o instanceof HttpParamsEntry) {
            return k.equals(((HttpParamsEntry) o).k);
        } else {
            return super.equals(o);
        }
    }

    @Override
    public int hashCode() {
        return k.hashCode();
    }

    public HttpParamsEntry(String key, String value) {
        k = key;
        v = value;
    }

    @Override
    public int compareTo(@NonNull HttpParamsEntry another) {
        if (k == null) {
            return -1;
        } else {
            return k.compareTo(another.k);
        }
    }
}
