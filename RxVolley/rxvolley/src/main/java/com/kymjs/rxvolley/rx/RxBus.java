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


import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;


/**
 * 用于替换EventBus的RxBus实现,同时用做Http响应数据的分发
 * Note:实现思路来自http://www.jianshu.com/p/ca090f6e2fe2
 *
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class RxBus {

    private RxBus() {
    }

    private static volatile RxBus mInstance;

    public static RxBus getDefault() {
        if (mInstance == null) {
            synchronized (RxBus.class) {
                if (mInstance == null) {
                    mInstance = new RxBus();
                }
            }
        }
        return mInstance;
    }

    private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());

    public void post(Object event) {
        if (event instanceof Result) {
            if (((Result) event).isSuccess()) {
                bus.onNext(event);
            } else {
                bus.onError(((Result) event).error);
            }
        }
    }

    public <T> Observable<T> take(final Class<T> eventType) {
        return bus.filter(new Func1<Object, Boolean>() {
            @Override
            public Boolean call(Object o) {
                return eventType.isInstance(o);
            }
        }).cast(eventType);
    }
}
