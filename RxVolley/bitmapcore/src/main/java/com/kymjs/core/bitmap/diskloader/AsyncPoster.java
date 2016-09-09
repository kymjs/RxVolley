/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de),张涛
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
package com.kymjs.core.bitmap.diskloader;

import android.graphics.Bitmap;

import com.kymjs.core.bitmap.DiskImageDisplayer;
import com.kymjs.core.bitmap.client.BitmapCore;
import com.kymjs.core.bitmap.client.BitmapRequestConfig;
import com.kymjs.core.bitmap.toolbox.CreateBitmap;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.http.Response;
import com.kymjs.rxvolley.http.VolleyError;
import com.kymjs.rxvolley.rx.Result;
import com.kymjs.rxvolley.rx.RxBus;
import com.kymjs.rxvolley.toolbox.FileUtils;

import java.io.FileInputStream;
import java.util.Collections;

/**
 * 从本地加载一个bitmap的任务,并行异步加载的实现,思路取自EventBus
 *
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class AsyncPoster implements Runnable {
    protected final PendingPostQueue queue;
    protected final DiskImageDisplayer displayer;

    public AsyncPoster(DiskImageDisplayer displayer) {
        queue = new PendingPostQueue();
        this.displayer = displayer;
    }

    public void enqueue(BitmapRequestConfig config, HttpCallback callback) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(config, callback);
        queue.enqueue(pendingPost);
        BitmapCore.getExecutorService().execute(this);
    }

    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        if (pendingPost == null) {
            throw new IllegalStateException("No pending post available");
        }
        byte[] bytes = loadFromFile(pendingPost.config.mUrl, pendingPost.config.maxWidth,
                pendingPost.config.maxHeight, pendingPost.callback);
        RxBus.getDefault().post(new Result(pendingPost.config.mUrl, bytes));
        PendingPost.releasePendingPost(pendingPost);
    }

    /**
     * 从本地载入一张图片
     *
     * @param path 图片的地址
     */
    protected byte[] loadFromFile(String path, int maxWidth, int maxHeight, HttpCallback callback) {
        byte[] data = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            data = FileUtils.input2byte(fis);
            handleBitmap(path, data, maxWidth, maxHeight, callback);
        } catch (Exception e) {
            displayer.post(path, callback, Response.<Bitmap>error(new VolleyError(e)));
        } finally {
            FileUtils.closeIO(fis);
        }
        return data;
    }

    private Bitmap handleBitmap(String path, byte[] data, int maxWidth, int maxHeight,
                                HttpCallback callback) {
        Bitmap bitmap = CreateBitmap.create(data, maxWidth, maxHeight);
        if (bitmap == null) {
            displayer.post(path, callback,
                    Response.<Bitmap>error(new VolleyError("bitmap create error")));
        } else {
            displayer.post(path, callback,
                    Response.success(bitmap, Collections.<String, String>emptyMap(), null));
        }
        return bitmap;
    }
}
