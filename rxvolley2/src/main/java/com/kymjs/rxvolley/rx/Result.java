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
package com.kymjs.rxvolley.rx;

import com.kymjs.rxvolley.http.Response;
import com.kymjs.rxvolley.http.VolleyError;

import java.util.Map;

/**
 * 仅用于RxJava返回的封装数据(结构体封装)
 *
 * @author kymjs (http://www.kymjs.com/) on 12/22/15.
 */
public class Result {

    public String url;
    public byte[] data;
    public VolleyError error;
    public Map<String, String> headers;
    public int errorCode;

    public boolean isSuccess() {
        return error == null;
    }

    public Result(String url, Response<byte[]> response) {
        this.url = url;
        this.data = response.result;
        this.error = response.error;
        this.headers = response.headers;
    }

    public Result(String url, byte[] result, Map<String, String> headers) {
        this.url = url;
        this.data = result;
        this.error = null;
        this.headers = headers;
    }

    public Result(String url, byte[] result) {
        this.url = url;
        this.data = result;
        this.error = null;
        this.headers = null;
    }

    public Result(String url, VolleyError error, int errorCode) {
        this.url = url;
        this.error = error;
        this.errorCode = errorCode;
    }
}
