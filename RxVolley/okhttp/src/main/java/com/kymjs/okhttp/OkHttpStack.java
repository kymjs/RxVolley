/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2015 Circle Internet Financial
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.kymjs.okhttp;

import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.http.Request;
import com.kymjs.rxvolley.http.URLHttpResponse;
import com.kymjs.rxvolley.interf.IHttpStack;
import com.kymjs.rxvolley.toolbox.HttpParamsEntry;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp的执行器,可用于替换原框架自带的HttpUrlConnection执行器
 * 修改自: https://gist.github.com/bryanstern/4e8f1cb5a8e14c202750
 */
@Deprecated
public class OkHttpStack implements IHttpStack {

    private final OkHttpClient mClient;

    public OkHttpStack(OkHttpClient client) {
        this.mClient = client;
    }

    @Override
    public URLHttpResponse performRequest(Request<?> request, ArrayList<HttpParamsEntry>
            additionalHeaders) throws IOException {
        OkHttpClient client = mClient.clone();
        int timeoutMs = request.getTimeoutMs();
        client.setConnectTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        client.setReadTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        client.setWriteTimeout(timeoutMs, TimeUnit.MILLISECONDS);

        com.squareup.okhttp.Request.Builder okHttpRequestBuilder = new com.squareup.okhttp
                .Request.Builder();
        okHttpRequestBuilder.url(request.getUrl());


        for (final HttpParamsEntry entry : request.getHeaders()) {
            okHttpRequestBuilder.addHeader(entry.k, entry.v);
        }
        for (final HttpParamsEntry entry : additionalHeaders) {
            okHttpRequestBuilder.addHeader(entry.k, entry.v);
        }

        setConnectionParametersForRequest(okHttpRequestBuilder, request);
        com.squareup.okhttp.Request okHttpRequest = okHttpRequestBuilder.build();
        Call okHttpCall = client.newCall(okHttpRequest);
        Response okHttpResponse = okHttpCall.execute();


        return responseFromConnection(okHttpResponse);
    }

    private URLHttpResponse responseFromConnection(Response okHttpResponse)
            throws IOException {
        URLHttpResponse response = new URLHttpResponse();
        //contentStream
        int responseCode = okHttpResponse.code();
        if (responseCode == -1) {
            throw new IOException(
                    "Could not retrieve response code from HttpUrlConnection.");
        }
        response.setResponseCode(responseCode);
        response.setResponseMessage(okHttpResponse.message());

        response.setContentStream(okHttpResponse.body().byteStream());

        response.setContentLength(okHttpResponse.body().contentLength());
        response.setContentEncoding(okHttpResponse.header("Content-Encoding"));
        if (okHttpResponse.body().contentType() != null) {
            response.setContentType(okHttpResponse.body().contentType().type());
        }


        //header
        HashMap<String, String> headerMap = new HashMap<>();
        Headers responseHeaders = okHttpResponse.headers();
        for (int i = 0, len = responseHeaders.size(); i < len; i++) {
            final String name = responseHeaders.name(i), value = responseHeaders.value(i);
            if (name != null) {
                headerMap.put(name, value);
            }
        }
        response.setHeaders(headerMap);
        return response;
    }

    private static void setConnectionParametersForRequest(com.squareup.okhttp.Request.Builder
                                                                  builder, Request<?> request)
            throws IOException {
        switch (request.getMethod()) {
            case RxVolley.Method.GET:
                builder.get();
                break;
            case RxVolley.Method.DELETE:
                builder.delete();
                break;
            case RxVolley.Method.POST:
                builder.post(createRequestBody(request));
                break;
            case RxVolley.Method.PUT:
                builder.put(createRequestBody(request));
                break;
            case RxVolley.Method.HEAD:
                builder.head();
                break;
            case RxVolley.Method.OPTIONS:
                builder.method("OPTIONS", null);
                break;
            case RxVolley.Method.TRACE:
                builder.method("TRACE", null);
                break;
            case RxVolley.Method.PATCH:
                builder.patch(createRequestBody(request));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static RequestBody createRequestBody(Request r) {
        final byte[] body = r.getBody();
        if (body == null) return null;

        return RequestBody.create(MediaType.parse(r.getBodyContentType()), body);
    }
}