[![OSL](https://kymjs.com/qiniu/image/logo3.png)](https://kymjs.com/works/)
=================

> RxVolley = Volley + RxAndroid3 + OkHttp3

[中文帮助](https://github.com/kymjs/RxVolley/blob/master/Readme_zh.md)   

## Retrofit? No, I Love Volley.
RxVolley is modified Volley. Removed the HttpClient, and support RxJava.   

If you are building with Gradle, simply add the following line to the ```dependencies``` section of your ```build.gradle``` file:   

latest version numbers: [![](https://jitpack.io/v/kymjs/RxVolley.svg)](https://jitpack.io/#kymjs/RxVolley)

>implementation 'com.github.kymjs.rxvolley:rxvolley:3.0.6'  
>
>// If use okhttp function    
>implementation 'com.github.kymjs.rxvolley:okhttp3:3.0.6'  
>//or okhttp2   
>implementation 'com.github.kymjs.rxvolley:okhttp:3.0.6'
>
>// If use image-loader function  
>implementation 'com.github.kymjs.rxvolley:image:3.0.6'


## Getting Started
Builder pattern to create objects.    

#### Callback method do Get request and contenttype is form  

```
HttpParams params = new HttpParams();

//http header, optional parameters
params.putHeaders("cookie", "your cookie");
params.putHeaders("User-Agent", "rxvolley"); 

//request parameters
params.put("name", "kymjs");
params.put("age", "18");

HttpCallback callBack = new HttpCallback(){
	@Override
    public void onSuccess(String t) {
    }
    @Override
    public void onFailure(int errorNo, String strMsg) {
    }
}

new RxVolley.Builder()
	.url("https://www.kymjs.com/rss.xml")
    .httpMethod(RxVolley.Method.GET) //default GET or POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH
    .cacheTime(6) //default: get 5min, post 0min
    .contentType(RxVolley.ContentType.FORM)//default FORM or JSON
    .params(params)
    .shouldCache(true) //default: get true, post false
    .callback(callBack)
    .encoding("UTF-8") //default
    .doTask();
```

#### Callback method do Post request and contenttype is json  

```

String paramJson = "{\n" +
                "    \"name\": \"kymjs\", " +
                "    \"age\": \"18\" " +
                "}";

//request parameters, json format
HttpParams params = new HttpParams();
params.putJsonParams(paramJson);

// upload progress
ProgressListener listener = new ProgressListener(){
    @Override
    public void onProgress(long transferredBytes, long totalSize){
    }
}

new RxVolley.Builder()
	.url("https://www.kymjs.com/rss.xml")
    .httpMethod(RxVolley.Method.POST) //default GET or POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH
    .cacheTime(6) //default: get 5min, post 0min
    .params(params)
    .contentType(RxVolley.ContentType.JSON)
    .shouldCache(true) //default: get true, post false
    .progressListener(listener) //upload progress
    .callback(callback)
    .encoding("UTF-8") //default
    .doTask();
```

#### return Observable\<Result\> type

```
Observable<Result> observable = new RxVolley.Builder()
	.url("https://www.kymjs.com/rss.xml")
    .httpMethod(RxVolley.Method.POST) //default GET or POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH
    .cacheTime(6) //default: get 5min, post 0min
    .params(params)
    .contentType(RxVolley.ContentType.JSON)
    .getResult(); 
    
//do something
observable.subscribe(subscriber);
``` 

## Requirements

RxVolley can be included in any Android application.  

RxVolley supports Android 3.1, API12 (HONEYCOMB_MR1) and later.  

## License

Licensed under the Apache License Version 2.0.  [The "License"](http://www.apache.org/licenses/LICENSE-2.0)  
