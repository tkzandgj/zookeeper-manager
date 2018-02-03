package com.gome.ads.zookeeper.serviceInvoker;

import com.gome.ads.zookeeper.parallelTask.ListenableFuture;
import com.gome.ads.zookeeper.parallelTask.ParallelTaskExecutor;
import com.gome.ads.zookeeper.parallelTask.ParallelTaskFuture;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Component
@Lazy(true)
public class DefaultServerInvoker implements ServiceInvoker {
    private static final Logger logger = LoggerFactory.getLogger(DefaultServerInvoker.class);

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    @Autowired
    private ServerManager serverManager;


    private ParallelTaskExecutor parallelTaskExecutor;

    @Override
    public void init(ParallelTaskExecutor parallelTaskExecutor) {
        this.serverManager.init();
        this.parallelTaskExecutor = parallelTaskExecutor;
    }

    @PreDestroy
    public void destroy() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("shutdown http client error", e);
        }
    }

    @Override
    public void httpGet(String serverName, String pathWithParam, ResponseChecker responseChecker, int retryTimes) {
        List<String> serverHosts = serverManager.getServerHosts(serverName);
        if (serverHosts == null || serverHosts.size() == 0) {
            logger.error("server  [" + serverName + "] no host found");
            return;
        }
        List<Callable<CloseableHttpResponse>> tasks = new ArrayList<>();
        for (String host : serverHosts) {
            String uri = "http://" + host + pathWithParam;
            HttpGet httpGet = new HttpGet(uri);
            ServiceInvokeTask serviceInvokeTask = new ServiceInvokeTask(httpClient, httpGet, retryTimes);
            tasks.add(serviceInvokeTask);
        }
        invoke(serverName, tasks, responseChecker);
    }

    @Override
    public void httpPostJson(String serverName, String pathWithParam, String content, ResponseChecker responseChecker, int retryTimes) {
        List<String> serverHosts = serverManager.getServerHosts(serverName);
        if (serverHosts == null || serverHosts.size() == 0) {
            logger.error("server  [" + serverName + "] no host found");
            return;
        }
        List<Callable<CloseableHttpResponse>> tasks = new ArrayList<>();
        for (String host : serverHosts) {
            String uri = "http://" + host + pathWithParam;
            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader("Content-Type", "application/json");
            StringEntity stringEntity = new StringEntity(content, "UTF-8");
            httpPost.setEntity(stringEntity);
            ServiceInvokeTask serviceInvokeTask = new ServiceInvokeTask(httpClient, httpPost, retryTimes);
            tasks.add(serviceInvokeTask);
        }
        invoke(serverName, tasks, responseChecker);
    }

    @Override
    public void httpPostForm(String serverName, String pathWithParam, Map<String, String> formData, ResponseChecker responseChecker, int retryTimes) {

    }

    private void invoke(String serverName, List<Callable<CloseableHttpResponse>> tasks, ResponseChecker responseChecker) {
        ParallelTaskFuture<CloseableHttpResponse> future = parallelTaskExecutor.bathSubmit(tasks);
        future.onAllComplete(new Consumer<List<ListenableFuture<CloseableHttpResponse>>>() {
            @Override
            public void accept(List<ListenableFuture<CloseableHttpResponse>> listenableFutures) {
                for(ListenableFuture<CloseableHttpResponse> f : listenableFutures) {
                    Callable<CloseableHttpResponse> task = f.getTask();
                    ServiceInvokeTask invokeTask = (ServiceInvokeTask)task;
                    if (f.isError()) {
                        logger.error("invoke [" + invokeTask.getUri() + "] failed", f.getError());
                    } else {
                        CloseableHttpResponse response = f.getNow();
                        if (response == null) {
                            logger.error("invoke [" + invokeTask.getUri() + "] failed, no response");
                        } else {
                            if (responseChecker != null) {
                                if (!responseChecker.check(response)) {
                                    logger.error("invoke [" + invokeTask.getUri() + "] failed, no response");
                                }
                            }
                            try {
                                response.close();
                            } catch (IOException e) {
                                logger.error("shutdown response failed", e);
                            }
                        }
                    }
                }
            }
        });
    }


}
