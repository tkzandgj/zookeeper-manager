package com.gome.ads.zookeeper.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tukangzheng
 * httpclient工具
 */
public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
    private static final String CHARSET_UTF_8 = "UTF-8";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static PoolingHttpClientConnectionManager connectionManager;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 1000 * 3;

    static {
        // 设置连接池
        connectionManager = new PoolingHttpClientConnectionManager();
        // 设置连接池大小
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(connectionManager.getMaxTotal());
        /*校验链接*/
        connectionManager.setValidateAfterInactivity(1000);
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // 设置连接超时
        configBuilder.setConnectTimeout(MAX_TIMEOUT);
        // 设置读取超时
        configBuilder.setSocketTimeout(MAX_TIMEOUT);
        // 设置从连接池获取连接实例的超时
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
        requestConfig = configBuilder.build();
    }

    /**
     * 发送get请求
     *
     * @param url url
     * @return
     */
    public static String doGet(String url) {
        return doGet(url, null);
    }

    /**
     * 发送 GET 请求
     *
     * @param url
     * @param params
     * @return
     */
    public static String doGet(String url, Map<String, Object> params) {
        return doGet(url, params, false);
    }

    /**
     * 发送http请求
     * @param url
     * @param params
     * @param isSSl
     * @return
     */
    public static String doGet(String url, Map<String, Object> params, boolean isSSL) {
        HttpGet httpGet = httpGetHandler(url, params);
        return HttpClient.domain(httpClient -> {
            return execute(httpClient, httpGet);
        }, isSSL);
    }

    private static HttpGet httpGetHandler(String url, Map<String, Object> params) {
        HttpGet httpGet = new HttpGet();
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setPath(url);
        if (params != null) {
            List<NameValuePair> naps = new ArrayList<>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                naps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            uriBuilder.setParameters(naps);
        }
        try {
            URI uri = uriBuilder.build();
            httpGet.setURI(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return httpGet;
    }

    /**
     * 发送post请求
     *
     * @param url post url
     * @return
     */
    public static String doPost(String url) {
        return doPost(url, new HashMap<>());
    }

    /**
     * 发送post请求
     *
     * @param url    post url
     * @param params post参数
     * @return
     */
    public static String doPost(String url, Map<String, Object> params) {
        return doPost(url, params, false);
    }

    /**
     * 发送post请求
     *
     * @param url    post url
     * @param params post参数
     * @return
     */
    public static String doPost(String url, Map<String, Object> params, boolean isSSL) {
        return HttpClient.domain(httpClient -> {
            HttpPost httpPost = httpPostHandler(url, params);
            return execute(httpClient, httpPost);
        }, isSSL);
    }

    /**
     * post请求发送json数据
     * @param url
     * @param jsonString
     * @return
     */
    public static String doPostJson(String url, String jsonString) {
        return doPostJson(url, jsonString, false);
    }

    /**
     * post请求发送json数据
     * @param url 请求url
     * @param jsonString json字符串
     * @param isSSL 是否为https请求
     * @return
     */
    public static String doPostJson(String url, String jsonString, boolean isSSL) {
        return HttpClient.domain(httpClient -> {
            HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity;
            try {
                stringEntity = new StringEntity(jsonString);
            } catch (UnsupportedEncodingException e) {
                return null;
            }
            httpPost.setHeader("Content-Type", CONTENT_TYPE_JSON);
            httpPost.setEntity(stringEntity);
            return execute(httpClient, httpPost);
        }, isSSL);
    }

    private static HttpPost httpPostHandler(String url, Map<String, Object> params) {
        HttpPost httpPost = new HttpPost(url);
        if (params != null) {
            List<NameValuePair> naps = new ArrayList<>();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                naps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(naps, CHARSET_UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return httpPost;
    }

    private static String execute(CloseableHttpClient httpClient, HttpRequestBase httpRequest) {
        String content = null;
        CloseableHttpResponse response = null;
        try {
            httpRequest.setConfig(requestConfig);
            response = httpClient.execute(httpRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                logger.error("Response error, request method:{}, request url:{}, response status code:{}",
                        httpRequest.getMethod(), httpRequest.getURI(), response.getStatusLine().getStatusCode());
                return null;
            }
            HttpEntity entity = response.getEntity();
            if(entity != null) {
                content = EntityUtils.toString(entity, CHARSET_UTF_8);
            } else {
                logger.info("Response content is null, request method:{}, request url:{}",
                        httpRequest.getMethod(), httpRequest.getURI());
            }
        } catch (IOException e) {
            logger.error("Execute error", e);
        } finally {
            doResponseClose(response);
        }
        return content;
    }

    private static void doHttpClientClose(CloseableHttpClient httpClient) {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void doResponseClose(CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface HttpClientInterface<T> {
        T domain(CloseableHttpClient httpClient);
    }

    static class HttpClient {
        /**
         * @param interfaces 具体的操作接口
         * @param bl         是否是https请求,true代表https请求
         * @param <T>        返回泛型
         * @return
         */
        public static <T extends Object> T domain(HttpClientInterface<T> interfaces, boolean isSSL) {
            // 返回值
            T object;
            CloseableHttpClient httpClient;
            if (!isSSL) {
                httpClient = HttpClients.createDefault();
            } else {
                httpClient = HttpClients.custom().setSSLSocketFactory(createSSLConnSocketFactory()).
                        setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).
                        setConnectionManagerShared(true).build();
            }
            try {
                // 业务操作
                object = interfaces.domain(httpClient);
            } finally {
                doHttpClientClose(httpClient);
            }
            return object;
        }
    }

    /**
     * 创建SSL安全连接
     *
     * @return
     */
    @SuppressWarnings("deprecation")
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, (arg0, agr1) -> true);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }

}
