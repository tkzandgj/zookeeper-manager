package com.gome.ads.zookeeper.parallelTask;


import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParallelTaskExecutor {
    private ExecutorService executor = null;

    public ParallelTaskExecutor() {
        int threadCnt = Runtime.getRuntime().availableProcessors() * 2;
        executor = Executors.newFixedThreadPool(threadCnt, new ParallelThreadFactory());
    }

    public ParallelTaskExecutor(int threadCnt) {
        executor = Executors.newFixedThreadPool(threadCnt, new ParallelThreadFactory());
    }

    public ParallelTaskExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public <T> DefaultFutureListener<T> getFutureListener() {
        return new DefaultFutureListener<>(this.executor);
    }

    public <T> ListenableFuture<T> submit(Callable<T> task) {
        ListenableFuture<T> listenableFuture = new ListenableFuture<>(task);
        executor.execute(listenableFuture);
        return listenableFuture;
    }

    public <T> ParallelTaskFuture<T> bathSubmit(List<Callable<T>> tasks) {
        if (tasks == null || tasks.size() == 0) {
            return null;
        }
        ParallelTaskFuture<T> parallelTaskFuture = new ParallelTaskFuture<>(tasks.size(), this);
        for(Callable<T> task : tasks) {
            parallelTaskFuture.addFuture(this.submit(task));
        }
        return parallelTaskFuture;
    }

    public void shutdown() {
        this.executor.shutdown();
    }

}
