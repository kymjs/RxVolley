/*
 * Copyright (C) 2012 Markus Junginger, greenrobot (http://greenrobot.de)
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

import com.kymjs.core.bitmap.client.BitmapRequestConfig;
import com.kymjs.rxvolley.client.HttpCallback;

import java.util.ArrayList;
import java.util.List;

final class PendingPost {
    //单例池,复用对象
    private final static List<PendingPost> pendingPostPool = new ArrayList<PendingPost>();

    PendingPost next; //队列下一个待发送对象

    BitmapRequestConfig config;
    HttpCallback callback;

    private PendingPost(BitmapRequestConfig config, HttpCallback callback) {
        this.config = config;
        this.callback = callback;
    }

    /**
     * 首先检查复用池中是否有可用,如果有则返回复用,否则返回一个新的
     *
     * @return 待发送对象
     */
    static PendingPost obtainPendingPost(BitmapRequestConfig config, HttpCallback callback) {
        synchronized (pendingPostPool) {
            int size = pendingPostPool.size();
            if (size > 0) {
                PendingPost pendingPost = pendingPostPool.remove(size - 1);
                pendingPost.next = null;
                return pendingPost;
            }
        }
        return new PendingPost(config, callback);
    }

    /**
     * 回收一个待发送对象,并加入复用池
     *
     * @param pendingPost 待回收的待发送对象
     */
    static void releasePendingPost(PendingPost pendingPost) {
        pendingPost.next = null;
        synchronized (pendingPostPool) {
            // 防止池无限增长
            if (pendingPostPool.size() < 1000) {
                pendingPostPool.add(pendingPost);
            }
        }
    }
}