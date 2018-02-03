package com.gome.ads.zookeeper.serviceInvoker;

import org.apache.http.HttpResponse;

public interface ResponseChecker {
    boolean check(HttpResponse response);
}
