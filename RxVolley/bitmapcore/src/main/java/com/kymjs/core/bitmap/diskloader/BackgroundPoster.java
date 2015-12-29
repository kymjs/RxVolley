package com.kymjs.core.bitmap.diskloader;

import com.kymjs.core.bitmap.DiskImageDisplayer;
import com.kymjs.core.bitmap.client.BitmapCore;
import com.kymjs.core.bitmap.client.BitmapRequestConfig;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.rx.RxBus;
import com.kymjs.rxvolley.toolbox.Loger;

import java.util.Collections;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class BackgroundPoster extends AsyncPoster {

    private volatile boolean executorRunning;

    public BackgroundPoster(DiskImageDisplayer displayer) {
        super(displayer);
    }

    @Override
    public void enqueue(BitmapRequestConfig config, HttpCallback callback) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(config, callback);
        synchronized (this) {
            queue.enqueue(pendingPost);
            if (!executorRunning) {
                executorRunning = true;
                BitmapCore.getExecutorService().execute(this);
            }
        }
    }

    @Override
    public void run() {
        try {
            try {
                while (true) {
                    PendingPost pendingPost = queue.poll(1000);
                    if (pendingPost == null) {
                        synchronized (this) {
                            // Check again, this time in synchronized
                            pendingPost = queue.poll();
                            if (pendingPost == null) {
                                executorRunning = false;
                                return;
                            }
                        }
                    }
                    byte[] bytes = loadFromFile(pendingPost.config.mUrl, pendingPost.config
                            .maxWidth, pendingPost.config.maxHeight, pendingPost.callback);
                    RxBus.getDefault().put(pendingPost.config.mUrl,
                            Collections.<String, String>emptyMap(), bytes);
                    PendingPost.releasePendingPost(pendingPost);
                }
            } catch (InterruptedException e) {
                Loger.debug(Thread.currentThread().getName() + " was interruppted" + e.getMessage
                        ());
            }
        } finally {
            executorRunning = false;
        }
    }
}
