package com.gome.ads.zookeeper.serviceInvoker;


import com.gome.ads.zookeeper.parallelTask.ParallelTaskExecutor;

import java.util.Map;

public interface ServiceInvoker {
    void init(ParallelTaskExecutor parallelTaskExecutor);
    void httpGet(String serverName, String pathWithParam, ResponseChecker responseChecker, int retryTimes);
    void httpPostJson(String serverName, String pathWithParam, String content, ResponseChecker responseChecker, int retryTimes);
    void httpPostForm(String serverName, String pathWithParam, Map<String, String> formData, ResponseChecker responseChecker, int retryTimes);
}
