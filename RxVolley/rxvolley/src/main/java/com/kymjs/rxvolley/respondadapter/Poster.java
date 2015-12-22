package com.kymjs.rxvolley.respondadapter;


import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;


/**
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class Poster {
    protected final PendingPostQueue queue = new PendingPostQueue();

    public void enqueue(String requestUrl, Map<String, String> header, byte[] result) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(requestUrl, header, result);
        queue.enqueue(pendingPost);
    }

    public Observable<Result> take(final String url) {
        return Observable.create(new Observable.OnSubscribe<Result>() {
            @Override
            public void call(Subscriber<? super Result> subscriber) {
                while (true) {
                    PendingPost pendingPost = queue.poll();
                    if (pendingPost != null && url.equals(pendingPost.requestUrl)) {
                        subscriber.onNext(new Result(pendingPost.header, pendingPost.data));
                        subscriber.onCompleted();
                        PendingPost.releasePendingPost(pendingPost);
                        break;
                    }
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}
