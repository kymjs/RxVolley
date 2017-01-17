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

package com.kymjs.rxvolley.client;

import android.text.TextUtils;

import com.kymjs.common.FileUtils;
import com.kymjs.common.Log;
import com.kymjs.rxvolley.toolbox.HttpParamsEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Http请求的参数集合
 *
 * @author kymjs (http://www.kymjs.com/) .
 */
public class HttpParams {

    private final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private String mBoundary = null;
    private static final String NEW_LINE_STR = "\r\n";
    private static final String CONTENT_TYPE = "Content-Type: ";
    private static final String CONTENT_DISPOSITION = "Content-Disposition: ";

    public static final String CHARSET = "UTF-8";

    //文本参数和字符集
    private static final String TYPE_TEXT_CHARSET = String.format("text/plain; charset=%s",
            CHARSET);

    //字节流参数
    private static final String TYPE_OCTET_STREAM = "application/octet-stream";

    //二进制参数
    private static final byte[] BINARY_ENCODING = "Content-Transfer-Encoding: binary\r\n\r\n"
            .getBytes();
    // 文本参数
    private static final byte[] BIT_ENCODING = "Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes();

    private final ArrayList<HttpParamsEntry> urlParams = new ArrayList<>(8);
    private final ArrayList<HttpParamsEntry> mHeaders = new ArrayList<>(4);

    private final ByteArrayOutputStream mOutputStream = new ByteArrayOutputStream();
    private boolean hasFile;
    private String contentType = null;

    private String jsonParams;

    public HttpParams() {
        this.mBoundary = generateBoundary();
    }

    /**
     * 生成分隔符
     */
    private String generateBoundary() {
        final StringBuilder buf = new StringBuilder();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buf.toString();
    }

    public void putHeaders(final String key, final int value) {
        this.putHeaders(key, value + "");
    }

    public void putHeaders(final String key, final String value) {
        mHeaders.add(new HttpParamsEntry(key, value));
    }

    public void put(final String key, final int value) {
        this.put(key, value + "");
    }

    public void putJsonParams(String json) {
        this.jsonParams = json;
    }

    /**
     * 添加文本参数
     */
    public void put(final String key, final String value) {
        urlParams.add(new HttpParamsEntry(key, value));
        writeToOutputStream(key, value.getBytes(), TYPE_TEXT_CHARSET,
                BIT_ENCODING, "");
    }

    /**
     * 添加二进制参数, 例如Bitmap的字节流参数
     */
    public void put(String paramName, final byte[] rawData) {
        hasFile = true;
        writeToOutputStream(paramName, rawData, TYPE_OCTET_STREAM,
                BINARY_ENCODING, "RxVolleyFile");
    }

    /**
     * 添加文件参数,可以实现文件上传功能
     */
    public void put(final String key, final File file) {
        try {
            hasFile = true;
            writeToOutputStream(key, FileUtils.input2byte(new FileInputStream(file)),
                    TYPE_OCTET_STREAM, BINARY_ENCODING, file.getName());
        } catch (FileNotFoundException e) {
            Log.d("RxVolley", "HttpParams.put()-> file not found");
        }
    }

    /**
     * 添加二进制文件参数
     *
     * @param key      参数key
     * @param rawData  二进制参数body
     * @param type     参数的contentType
     * @param fileName 二进制文件名,可以为空
     */
    public void put(final String key, final byte[] rawData, String type, String fileName) {
        hasFile = true;
        if (TextUtils.isEmpty(fileName)) {
            fileName = "RxVolleyFile";
        }
        writeToOutputStream(key, rawData, type, BINARY_ENCODING, fileName);
    }

    /**
     * 将数据写入到输出流中
     */
    private void writeToOutputStream(String paramName, byte[] rawData,
                                     String type, byte[] encodingBytes, String fileName) {
        try {
            writeFirstBoundary();
            mOutputStream
                    .write((CONTENT_TYPE + type + NEW_LINE_STR).getBytes());
            mOutputStream
                    .write(getContentDispositionBytes(paramName, fileName));
            mOutputStream.write(encodingBytes);
            mOutputStream.write(rawData);
            mOutputStream.write(NEW_LINE_STR.getBytes());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 参数开头的分隔符
     *
     * @throws IOException
     */
    private void writeFirstBoundary() throws IOException {
        mOutputStream.write(("--" + mBoundary + "\r\n").getBytes());
    }

    private byte[] getContentDispositionBytes(String paramName, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("--").append(mBoundary).append("\r\n").append(CONTENT_DISPOSITION)
                .append("form-data; name=\"").append(paramName).append("\"");
        if (!TextUtils.isEmpty(fileName)) {
            stringBuilder.append("; filename=\"").append(fileName).append("\"");
        }
        return stringBuilder.append(NEW_LINE_STR).toString().getBytes();
    }

    public long getContentLength() {
        return mOutputStream.toByteArray().length;
    }

    public String getContentType() {
        //如果contentType没有被自定义，且参数集包含文件，则使用有文件的contentType
        if (hasFile && contentType == null) {
            contentType = "multipart/form-data; boundary=" + mBoundary;
        }
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isChunked() {
        return false;
    }

    public boolean isRepeatable() {
        return false;
    }

    public boolean isStreaming() {
        return false;
    }

    public void writeTo(final OutputStream outstream) throws IOException {
        if (hasFile) {
            // 参数最末尾的结束符
            final String endString = "--" + mBoundary + "--\r\n";
            // 写入结束符
            mOutputStream.write(endString.getBytes());
            //
            outstream.write(mOutputStream.toByteArray());
        } else if (!TextUtils.isEmpty(getUrlParams())) {
            outstream.write(getUrlParams().substring(1).getBytes());
        }
    }

    public void consumeContent() throws IOException,
            UnsupportedOperationException {
        if (isStreaming()) {
            throw new UnsupportedOperationException(
                    "Streaming entity does not implement #consumeContent()");
        }
    }

    public InputStream getContent() {
        return new ByteArrayInputStream(mOutputStream.toByteArray());
    }

    public StringBuilder getUrlParams() {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        Collections.sort(urlParams);

        for (HttpParamsEntry entry : urlParams) {
            if (!isFirst) {
                result.append("&");
            } else {
                result.append("?");
                isFirst = false;
            }
            try {
                result.append(URLEncoder.encode(entry.k, CHARSET)).append("=").
                        append(URLEncoder.encode(entry.v, CHARSET));
            } catch (UnsupportedEncodingException e) {
                result.append(entry.k).append("=").append(entry.v);
            }
        }
        return result;
    }

    public String getJsonParams() {
        return jsonParams;
    }

    public ArrayList<HttpParamsEntry> getHeaders() {
        mHeaders.add(new HttpParamsEntry("Accept-Encoding", "identity"));
        return mHeaders;
    }
}
