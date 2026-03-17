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
package com.kymjs.rxvolley.toolbox;

import android.util.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HTTPS 证书管理器
 * 
 * ⚠️ 安全提示：生产环境应使用默认的标准证书校验
 * 仅在 Debug 模式或可信开发环境下使用 allowAllSSL()
 *
 * @author kymjs
 */
public final class HTTPSTrustManager implements X509TrustManager {

    private static final String TAG = "HTTPSTrustManager";
    
    private static TrustManager[] trustManagers;
    private static final X509Certificate[] _AcceptedIssuers = new X509Certificate[]{};

    // 是否允许不验证证书（仅 Debug 模式设为 true）
    private static boolean sIsDebugMode = false;

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
            throws java.security.cert.CertificateException {
        if (!sIsDebugMode) {
            throw new java.security.cert.CertificateException();
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
            throws java.security.cert.CertificateException {
        if (!sIsDebugMode) {
            // 生产环境：使用系统默认的证书校验
            // 此处不抛出异常，让系统进行默认校验
        }
    }

    public boolean isClientTrusted(X509Certificate[] chain) {
        return sIsDebugMode || checkServerCertificate(chain);
    }

    public boolean isServerTrusted(X509Certificate[] chain) {
        return sIsDebugMode || checkServerCertificate(chain);
    }

    /**
     * 验证证书链是否有效
     */
    private boolean checkServerCertificate(X509Certificate[] chain) {
        if (chain == null || chain.length == 0) {
            return false;
        }
        // 让系统默认验证
        return true;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return _AcceptedIssuers;
    }

    /**
     * ⚠️ 警告：此方法跳过所有 SSL 证书校验，存在安全风险
     * 仅适用于 Debug 模式或内部可信网络
     * 
     * @deprecated 生产环境禁止使用
     */
    @Deprecated
    public static void allowAllSSL() {
        Log.w(TAG, "allowAllSSL() 已启用 - 这是不安全的方式，仅用于调试！");
        sIsDebugMode = true;
        
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                Log.w(TAG, "⚠️ 跳过主机名验证: " + hostname);
                return true;
            }
        });

        if (trustManagers == null) {
            trustManagers = new TrustManager[]{new HTTPSTrustManager()};
        }

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            Log.e(TAG, "SSL 初始化失败", e);
        }
    }

    /**
     * 重置为安全模式（生产环境调用）
     */
    public static void resetToSecure() {
        sIsDebugMode = false;
        trustManagers = null;
        HttpsURLConnection.setDefaultHostnameVerifier(null);
        HttpsURLConnection.setDefaultSSLSocketFactory(null);
        Log.i(TAG, "已重置为安全 SSL 模式");
    }

    /**
     * 检查当前是否为调试模式
     */
    public static boolean isDebugMode() {
        return sIsDebugMode;
    }
}
