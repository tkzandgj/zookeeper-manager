package com.gome.ads.zookeeper.parallelTask;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class DefaultFutureListener<T> implements FutureListener<T> {

    private Executor executor;

    private volatile Consumer<ListenableFuture<T>> callback;

    public DefaultFutureListener(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void accept(ListenableFuture<T> future) {
        if (callback != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    callback.accept(future);
                }
            });
        }
    }

    public void onComplete(Consumer<ListenableFuture<T>> callback) {
        this.callback = callback;
    }
}
