package com.gome.ads.zookeeper.parallelTask;

public interface FutureListener<T> {
    void accept(ListenableFuture<T> future);
}
