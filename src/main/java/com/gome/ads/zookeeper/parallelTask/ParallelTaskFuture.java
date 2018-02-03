package com.gome.ads.zookeeper.parallelTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 并行任务的回调方式，目的是实现线程非阻塞
 * 因为用到了netty的promise，所以没有办法抽取到公共的ads-utils里
 */
public class ParallelTaskFuture<V> {
    /** 并行任务数 */
    private AtomicInteger taskNum;

    private volatile List<ListenableFuture<V>> futures;

    private volatile Consumer<List<ListenableFuture<V>>> consumer;

    private ParallelTaskExecutor executor;

    public ParallelTaskFuture(int num, ParallelTaskExecutor executor) {
        taskNum = new AtomicInteger(num);
        futures = Collections.synchronizedList(new ArrayList<ListenableFuture<V>>(num));
        this.executor = executor;
    }

    public void onAllComplete(Consumer<List<ListenableFuture<V>>> consumer) {
        this.consumer = consumer;
        if (taskNum.get() == 0) {
            consumer.accept(futures);
        }
    }

    public void addFuture(ListenableFuture<V> future) {
        DefaultFutureListener<V> listener = executor.getFutureListener();
        listener.onComplete(new Consumer<ListenableFuture<V>>() {
            @Override
            public void accept(ListenableFuture<V> listenableFuture) {
                oneTaskComplete(listenableFuture);
            }
        });
        future.addListener(listener);
    }

    private void oneTaskComplete(ListenableFuture<V> future) {
        futures.add(future);
        int remain = this.taskNum.decrementAndGet();
        if (remain == 0) {
            if (consumer != null) {
                consumer.accept(futures);
            }
        }
    }
}
