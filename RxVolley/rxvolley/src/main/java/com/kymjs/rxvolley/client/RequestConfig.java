package com.kymjs.rxvolley.client;

import com.kymjs.rxvolley.http.DefaultRetryPolicy;
import com.kymjs.rxvolley.http.RetryPolicy;
import com.kymjs.rxvolley.interf.IRespondAdapter;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/17/15.
 */
public final class RequestConfig {

    public int mTimeout = 5000; // 请求超时时间

    public int mDelayTime = 0; // 为了更真实的模拟网络,缓存延迟响应

    public int mCacheTime = 5; //缓存时间(分钟)

    public boolean mUseServerControl; //服务器控制缓存时间,为true时mCacheTime无效

    public int mMethod; // 请求方式

    public boolean mShouldCache = true; // 是否缓存本次请求

    public String mUrl; //请求接口地址

    public RetryPolicy mRetryPolicy = new DefaultRetryPolicy(); //重试策略

    public String mEncoding = "UTF-8"; //编码

    public Object mTag; // 本次请求的tag，方便在取消时找到它
    
    public IRespondAdapter<?> adapter; //网络请求响应结果类型转换适配器
}
