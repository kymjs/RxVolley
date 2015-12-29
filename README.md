##RxVolley = RxJava + Volley + OkHttp

###Retrofit? No, I like Volley.
RxVolley is modified Volley. Removed the HttpClient, and support RxJava.  

###How to used？
Builder pattern to create objects.  

#####Callback method do Get request and contenttype is form  

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
    .contentType(RxVolley.ContentType.FROM)//default FROM or JSON
    .params(params)
    .shouldCache(true) //default: get true, post false
    .callback(callback)
    .encoding("UTF-8") //default
    .doTask();
```

#####Callback method do Post request and contenttype is json  

```java

String paramJson = "{\n" +
                "    \"name\": \"kymjs\", " +
                "    \"age\": \"18\" " +
                "}";

//request parameters, json format
HttpParams params = new HttpParams();
params.putJsonParams(paramJson);

new RxVolley.Builder()
	.url("http://www.kymjs.com/rss.xml")
    .httpMethod(RxVolley.Method.POST) //default GET or POST/PUT/DELETE/HEAD/OPTIONS/TRACE/PATCH
    .cacheTime(6) //default: get 5min, post 0min
    .params(params)
    .contentType(RxVolley.ContentType.JSON)
    .shouldCache(true) //default: get true, post false
    .callback(callback)
    .encoding("UTF-8") //default
    .doTask();
```

#####return Observable\<Result\> type

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