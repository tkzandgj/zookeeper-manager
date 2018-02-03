package com.gome.ads.zookeeper.parallelTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ListenableFuture<T> implements RunnableFuture<T> {

    private volatile List<FutureListener<T>> listeners = new ArrayList<>();


    private volatile T result = null;

    private volatile Exception error;

    private volatile boolean isError = false;

    private volatile boolean cancelled = false;

    private volatile boolean started = false;

    private volatile boolean finished = false;

    private volatile short waiterCnt =0;

    private Callable<T> task;

    public ListenableFuture(Callable<T> task) {
        this.task = task;
    }

    public void addListener(FutureListener<T> listener) {
        if (finished) {
            listener.accept(this);
        } else {
            synchronized (this) {
                if (finished) {
                    listener.accept(this);
                } else {
                    listeners.add(listener);
                }
            }
        }
    }

    public Callable<T> getTask() {
        return task;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (cancelled) {
                return;
            } else {
                started = true;
            }
        }
        try {
            result = task.call();
        } catch (Exception e) {
            isError = true;
            error = e;
        }
        finished = true;
        synchronized (this) {
            if (waiterCnt > 0) {
                this.notifyAll();
            }
        }
        callListeners();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            if (started) {
                return false;
            } else {
                cancelled = true;
                finished = true;
                return true;
            }
        }
    }

    private void callListeners() {
        synchronized (this) {
            if (this.listeners.size() > 0) {
                for (FutureListener<T> futureListener : this.listeners) {
                    futureListener.accept(this);
                }
            }
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isDone() {
        return finished;
    }

    public boolean isError() {
        return isError;
    }

    public Exception getError() {
        return error;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return result;
    }

    public T getNow() {
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (finished) {
            return result;
        }
        long timeoutNanos = calcTimeoutNanos(timeout, unit);
        long entryTime = System.nanoTime();
        while(true) {
            synchronized (this) {
                try {
                    long[] timeoutMSArray = calcWaitTimeout(timeoutNanos);
                    waiterCnt++;
                    wait(timeoutMSArray[0], (int) timeoutMSArray[1]);
                } catch (InterruptedException e) {
                    // interrupted
                }
                waiterCnt--;
            }
            if (finished) {
                return result;
            } else {
                long currentTime = System.nanoTime();
                timeoutNanos = timeoutNanos - (currentTime - entryTime);
                if (timeoutNanos <= 0) {
                    throw new TimeoutException();
                }
            }
        }
    }

    private long calcTimeoutNanos(long timeout, TimeUnit unit) {
        if (unit == TimeUnit.DAYS) {
            return timeout * 24 * 60 * 60 * 1000 * 1000 * 1000;
        } else if (unit == TimeUnit.HOURS) {
            return timeout *60 * 60 * 1000 * 1000 * 1000 ;
        } else if (unit == TimeUnit.MINUTES) {
            return timeout * 60 * 1000 * 1000 * 1000;
        } else if (unit == TimeUnit.SECONDS) {
            return timeout * 1000 * 1000 * 1000;
        } else if (unit == TimeUnit.MILLISECONDS) {
            return timeout * 1000 * 1000;
        } else if (unit == TimeUnit.MICROSECONDS) {
            return timeout * 1000;
        } else if (unit == TimeUnit.NANOSECONDS) {
            return timeout;
        } else {
            return 0;
        }
    }

    private long[] calcWaitTimeout(long timeoutNanos) {
        return new long[]{timeoutNanos / 1000000, timeoutNanos % 1000000};
    }
}
