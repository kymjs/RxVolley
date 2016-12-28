/*
 * Copyright (C) 2011 The Android Open Source Project, 张涛
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

package com.kymjs.rxvolley.http;

import android.annotation.TargetApi;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Process;

import com.kymjs.common.Log;
import com.kymjs.rxvolley.interf.ICache;
import com.kymjs.rxvolley.interf.IDelivery;
import com.kymjs.rxvolley.interf.INetwork;

import java.util.concurrent.BlockingQueue;

/**
 * 网络请求任务的调度器，负责不停的从RequestQueue中取Request并交给NetWork执行，
 * 执行完成后分发执行结果到UI线程的回调并缓存结果到缓存器
 *
 * @author kymjs (http://www.kymjs.com/) .
 */
public class NetworkDispatcher extends Thread {
    private final BlockingQueue<Request<?>> mQueue; // 正在发生请求的队列
    private final INetwork mNetwork; // 网络请求执行器
    private final ICache mCache; // 缓存器
    private final IDelivery mDelivery;
    private volatile boolean mQuit = false; // 标记是否退出本线程

    public NetworkDispatcher(BlockingQueue<Request<?>> queue, INetwork network, ICache cache,
                             IDelivery delivery) {
        mQueue = queue;
        mNetwork = network;
        mCache = cache;
        mDelivery = delivery;
    }

    /**
     * 强制退出本线程
     */
    public void quit() {
        mQuit = true;
        interrupt();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void addTrafficStatsTag(Request<?> request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TrafficStats.setThreadStatsTag(request.getTrafficStatsTag());
        }
    }

    /**
     * 阻塞态工作，不停的从队列中获取任务，直到退出。并把取出的request使用Network执行请求，然后NetWork返回一个NetWork响应
     */
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        int stateCode = -1;
        while (true) {
            Request<?> request;
            try {
                request = mQueue.take();
            } catch (InterruptedException e) {
                if (mQuit) {
                    return;
                } else {
                    continue;
                }
            }
            try {
                if (request.isCanceled()) {
                    request.finish("任务已经取消");
                    continue;
                }
                mDelivery.postStartHttp(request);
                addTrafficStatsTag(request);
                NetworkResponse networkResponse = mNetwork.performRequest(request);
                stateCode = networkResponse.statusCode;
                // 如果这个响应已经被分发，则不会再次分发
                if (networkResponse.notModified && request.hasHadResponseDelivered()) {
                    request.finish("已经分发过本响应");
                    continue;
                }
                Response<?> response = request.parseNetworkResponse(networkResponse);

                if (request.shouldCache() && response.cacheEntry != null) {
                    mCache.put(request.getCacheKey(), response.cacheEntry);
                }
                request.markDelivered();
                //执行异步响应
                if (networkResponse.data != null) {
                    if (request.getCallback() != null) {
                        request.getCallback().onSuccessInAsync(networkResponse.data);
                    }
                }
                mDelivery.postResponse(request, response);
            } catch (VolleyError volleyError) {
                parseAndDeliverNetworkError(request, volleyError, stateCode);
            } catch (Exception e) {
                Log.d("RxVolley", String.format("Unhandled exception %s", e.getMessage()));
                parseAndDeliverNetworkError(request, new VolleyError(e), stateCode);
            }
        }
    }

    private void parseAndDeliverNetworkError(Request<?> request, VolleyError error, int stateCode) {
        error = request.parseNetworkError(error);
        mDelivery.postError(request, error);
    }
}
