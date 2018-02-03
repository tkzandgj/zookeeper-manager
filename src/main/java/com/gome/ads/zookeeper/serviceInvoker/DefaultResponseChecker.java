package com.gome.ads.zookeeper.serviceInvoker;

import org.apache.http.HttpResponse;

public class DefaultResponseChecker implements ResponseChecker {
    @Override
    public boolean check(HttpResponse response) {

        return false;
    }
}
