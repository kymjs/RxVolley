package com.kymjs.rxvolley.interf;

/**
 * 类型转换适配器
 *
 * @author kymjs (http://www.kymjs.com/) on 12/18/15.
 */
public interface IRespondAdapter<T> {
    T convert(byte[] bytes);
}
