package com.gome.ads.zookeeper.parallelTask;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelThreadFactory implements ThreadFactory {
    private static AtomicInteger threadCnt = new AtomicInteger(0);

    private static final String THREAD_NAME_PREFIX = "ParallelThread-";

    @Override
    public Thread newThread(Runnable r) {
        int cnt = threadCnt.incrementAndGet();
        Thread thread = new Thread(r, THREAD_NAME_PREFIX + cnt);
        if (thread.isDaemon())
            thread.setDaemon(false);
        return thread;
    }
}
