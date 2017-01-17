/*
 * SSLHelper      2016-01-20
 * Copyright (c) 2016 hujiang Co.Ltd. All right reserved(http://www.hujiang.com).
 * 
 */

package com.kymjs.okhttp3;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.internal.tls.OkHostnameVerifier;

/**
 * process host name verify and ssl socket factory.
 *
 * @author simon
 * @version 1.0.0
 * @since 2016-01-20
 */
public class CertificateUtils {
    /**
     * 加载并使用内置证书
     *
     * @param certificateInputStream 证书流
     * @return
     */
    public static SSLSocketFactory getSSLSocketFactory(InputStream certificateInputStream) {
        if (certificateInputStream == null) {
            throw new NullPointerException("certificateInputStream should not be Null");
        }
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            Certificate certificate;
            certificate = certificateFactory.generateCertificate(certificateInputStream);

            String keyStoreType = KeyStore.getDefaultType();
            Log.i("rxvolley", "keyStoreType:" + keyStoreType);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            return sslContext.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                certificateInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 加载并使用内置证书
     *
     * @param keyStoreInputStream keyStore
     * @param password            password
     * @return
     */
    public static SSLSocketFactory getSSLSocketFactory(InputStream keyStoreInputStream, String password) {
        if (keyStoreInputStream == null) {
            throw new NullPointerException("keyStoreInputStream should not be Null");
        }
        try {
            String keyStoreType = KeyStore.getDefaultType();
            Log.i("rxvolley", "keyStoreType:" + keyStoreType);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(keyStoreInputStream, TextUtils.isEmpty(password) ? null : password.toCharArray());

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                keyStoreInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static SSLSocketFactory getDefaultSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 信任所有请求，不做证书验证。
     */
    public static final HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * 信任Android内置的CA证书，自签名证书会验证失败。It is similar to {@link HttpsURLConnection#getDefaultHostnameVerifier()}}
     */
    public static final HostnameVerifier DEFAULT_HOSTNAME_VERIFIER = OkHostnameVerifier.INSTANCE;
}