package com.kymjs.rxvolley.gson;

import com.google.gson.Gson;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.gson.interf.IConvertAdapter;
import com.kymjs.rxvolley.gson.interf.IConvertType;

import java.util.Map;

/**
 * Created by kymjs on 2/27/16.
 */
public abstract class ConvertCallback<T> extends HttpCallback
        implements IConvertAdapter<T>, IConvertType<T> {

    private T tempData;

    @Override
    public T convertTo(byte[] t) {
        return new Gson().fromJson(new String(t), toType());
    }

    @Override
    public void onSuccessInAsync(byte[] t) {
        super.onSuccessInAsync(t);
        tempData = convertTo(t);
    }

    @Override
    public void onSuccess(Map<String, String> headers, byte[] t) {
        super.onSuccess(headers, t);
        onSuccess(headers, tempData);
    }

    public void onSuccess(Map<String, String> headers, T data) {
    }
}
