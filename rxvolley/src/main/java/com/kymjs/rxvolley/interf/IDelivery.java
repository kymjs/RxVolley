/*
 * Copyright (c) 2014, Android Open Source Project,张涛.
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
package com.kymjs.rxvolley.interf;


import com.kymjs.rxvolley.client.ProgressListener;
import com.kymjs.rxvolley.http.Request;
import com.kymjs.rxvolley.http.Response;
import com.kymjs.rxvolley.http.VolleyError;

/**
 * 分发器，将异步线程中的结果响应到UI线程中
 *
 * @author kymjs (http://www.kymjs.com/).
 */
public interface IDelivery {
    /**
     * 分发响应结果
     *
     * @param request
     * @param response
     */
    void postResponse(Request<?> request, Response<?> response);

    /**
     * 分发Failure事件
     *
     * @param request 请求
     * @param error   异常原因
     */
    void postError(Request<?> request, VolleyError error);

    void postResponse(Request<?> request, Response<?> response, Runnable runnable);

    /**
     * 分发当Http请求开始时的事件
     */
    void postStartHttp(Request<?> request);

    /**
     * 进度
     *
     * @param transferredBytes 进度
     * @param totalSize        总量
     */
    void postProgress(ProgressListener listener, long transferredBytes, long totalSize);
}
