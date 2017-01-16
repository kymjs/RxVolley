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

package com.kymjs.rxvolley.client;


import com.kymjs.common.Log;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.http.HttpHeaderParser;
import com.kymjs.rxvolley.http.NetworkResponse;
import com.kymjs.rxvolley.http.Request;
import com.kymjs.rxvolley.http.Response;
import com.kymjs.rxvolley.rx.Result;
import com.kymjs.rxvolley.toolbox.HttpParamsEntry;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

/**
 * 用来发起application/json格式的请求的，我们平时所使用的是form表单提交的参数，而使用JsonRequest提交的是json参数。
 */
public class JsonRequest extends Request<byte[]> {

    private final String mRequestBody;
    private final HttpParams mParams;

    public JsonRequest(RequestConfig config, HttpParams params, HttpCallback callback) {
        super(config, callback);
        mRequestBody = params.getJsonParams();
        mParams = params;
    }

    @Override
    public ArrayList<HttpParamsEntry> getHeaders() {
        return mParams.getHeaders();
    }

    @Override
    protected void deliverResponse(Map<String, String> headers, byte[] response) {
        if (mCallback != null) {
            mCallback.onSuccess(headers, response);
        }
        getConfig().mSubject.onNext(new Result(getUrl(), response, headers));
    }

    @Override
    public Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, response.headers,
                HttpHeaderParser.parseCacheHeaders(getUseServerControl(), getCacheTime(),
                        response));
    }

    @Override
    public String getBodyContentType() {
        return String.format("application/json; charset=%s", getConfig().mEncoding);
    }

    @Override
    public String getCacheKey() {
        if (getMethod() == RxVolley.Method.POST) {
            return getUrl() + mParams.getJsonParams();
        } else {
            return getUrl();
        }
    }

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(getConfig().mEncoding);
        } catch (UnsupportedEncodingException uee) {
            Log.d("RxVolley", String.format("Unsupported Encoding while trying to get the bytes of %s" +
                    " using %s", mRequestBody, getConfig().mEncoding));
            return null;
        }
    }

    @Override
    public Priority getPriority() {
        return Priority.IMMEDIATE;
    }
}
