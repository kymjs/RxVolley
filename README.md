##RxVolley = Volley + RxJava + OkHttp

####Retrofit? No, I like Volley.
RxVolley is modified Volley. Removed the HttpClient, and support RxJava.   

If you are building with Gradle, simply add the following line to the ```dependencies``` section of your ```build.gradle``` file:   

>compile 'com.kymjs.rxvolley:rxvolley:1.1.3'  
>
>// If use okhttp function    
>compile 'com.kymjs.rxvolley:okhttp:1.1.3'  
>
>// If use image-loader function  
>compile 'com.kymjs.rxvolley:bitmapcore:1.1.3'


## Getting Started
Builder pattern to create objects.    
or  [中文帮助](http://rxvolley.mydoc.io/) (正在不断完善中)

####Callback method do Get request and contenttype is form  

```java
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
	.url("http://www.kymjs.com/rss.xml")
    .httpMethod(RxVolley.Method.GET) //default GET or POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH
    .cacheTime(6) //default: get 5min, post 0min
    .contentType(RxVolley.ContentType.FORM)//default FORM or JSON
    .params(params)
    .shouldCache(true) //default: get true, post false
    .callback(callBack)
    .encoding("UTF-8") //default
    .doTask();
```

####Callback method do Post request and contenttype is json  

```java

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
	.url("http://www.kymjs.com/rss.xml")
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

####return Observable\<Result\> type

```java
Observable<Result> observable = new RxVolley.Builder()
	.url("http://www.kymjs.com/rss.xml")
    .httpMethod(RxVolley.Method.POST) //default GET or POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH
    .cacheTime(6) //default: get 5min, post 0min
    .params(params)
    .contentType(RxVolley.ContentType.JSON)
    .getResult(); 
    
//do something
observable.subscribe(subscriber);
``` 

##Requirements

RxVolley can be included in any Android application.  

RxVolley supports Android 3.1, API12 (HONEYCOMB_MR1) and later.  

##More

####Which project uses it ？

* CodeCafe [https://github.com/kymjs/CodeCafe](https://github.com/kymjs/CodeCafe)    

	> My blog project.  
	

* OSChina Git App [http://git.oschina.net/oschina/git-osc-android](http://git.oschina.net/oschina/git-osc-android)  
	
	> OpenSourceChina company Git client.  
	
####License

Licensed under the Apache License Version 2.0.  [The "License"](http://www.apache.org/licenses/LICENSE-2.0)  
