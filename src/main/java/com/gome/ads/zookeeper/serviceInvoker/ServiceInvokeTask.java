package com.gome.ads.zookeeper.serviceInvoker;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class ServiceInvokeTask implements Callable<CloseableHttpResponse> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceInvokeTask.class);

    private CloseableHttpClient httpClient;

    private HttpUriRequest httpUriRequest;

    private int retryTimes = 0;

    public ServiceInvokeTask(CloseableHttpClient httpClient, HttpUriRequest httpUriRequest, int retryTimes) {
        this.httpClient = httpClient;
        this.httpUriRequest = httpUriRequest;
        this.retryTimes = retryTimes;
    }

    @Override
    public CloseableHttpResponse call() throws Exception {
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpUriRequest);
        } catch (Exception e) {
            if (retryTimes > 0) {
                logger.error("invoke  [" + httpUriRequest.getURI().toString() + "] failed, retry", e);
                retryTimes--;
                call();
            } else {
                throw e;
            }
        }
        return response;
    }

    public String getUri() {
        return this.httpUriRequest.getURI().toString();
    }
}
