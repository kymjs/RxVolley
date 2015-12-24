package com.kymjs.rxvolley.rx;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;


/**
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class Poster {

    private ConcurrentHashMap<String, Result> pool = new ConcurrentHashMap<>();

    public synchronized Observable<Result> take(final String url) {
        return Observable.create(new Observable.OnSubscribe<Result>() {
            @Override
            public void call(Subscriber<? super Result> subscriber) {
                while (true) {
                    Result result = pool.get(url);
                    if (result != null) {
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                        break;
                    }
                }
            }
        }).subscribeOn(Schedulers.io());
    }


    /**
     * 入队
     */
    public void put(String url, Map<String, String> header, byte[] data) {
        pool.put(url, new Result(header, data));
    }
}
