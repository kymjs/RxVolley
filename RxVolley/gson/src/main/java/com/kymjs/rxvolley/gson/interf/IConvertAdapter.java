package com.kymjs.rxvolley.gson.interf;

/**
 * Created by kymjs on 2/26/16.
 */
public interface IConvertAdapter<T> {
    T convertTo(byte[] t);
}
