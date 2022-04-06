/*
 * Copyright (c) 2015, 张涛.
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
package com.kymjs.rxvolley.http;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.InputStream;
import java.util.HashMap;

/**
 * 兼容6.0，用于替换掉org.apache.http.HttpResponse
 * NOTE:再次感谢Q群中的 @黑猫白猫抓到老鼠 提供的这个URLHttpResponse的思路以及代码实现
 *
 * @author 李晨光(https://github.com/lichenguang8706)
 * @author kymjs (http://www.kymjs.com/) .
 */
public class URLHttpResponse implements Parcelable {

    private static final long serialVersionUID = 1L;

    private HashMap<String, String> headers;

    private int responseCode;

    private String responseMessage;

    private InputStream contentStream;

    private String contentEncoding;

    private String contentType;

    private long contentLength;

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public InputStream getContentStream() {
        return contentStream;
    }

    public void setContentStream(InputStream contentStream) {
        this.contentStream = contentStream;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.headers);
        dest.writeInt(this.responseCode);
        dest.writeString(this.responseMessage);
        dest.writeString(this.contentEncoding);
        dest.writeString(this.contentType);
        dest.writeLong(this.contentLength);
    }

    public URLHttpResponse() {
    }

    protected URLHttpResponse(Parcel in) {
        this.headers = (HashMap<String, String>) in.readSerializable();
        this.responseCode = in.readInt();
        this.responseMessage = in.readString();
        this.contentEncoding = in.readString();
        this.contentType = in.readString();
        this.contentLength = in.readLong();
    }

    public static final Parcelable.Creator<URLHttpResponse> CREATOR = new Parcelable
            .Creator<URLHttpResponse>() {
        public URLHttpResponse createFromParcel(Parcel source) {
            return new URLHttpResponse(source);
        }

        public URLHttpResponse[] newArray(int size) {
            return new URLHttpResponse[size];
        }
    };
}
