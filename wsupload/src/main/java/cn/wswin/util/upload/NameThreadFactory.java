package cn.wswin.util.upload;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NameThreadFactory implements ThreadFactory {
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String threadName;

    public NameThreadFactory(String threadName) {
        this.threadName = threadName;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, threadName + threadNumber.getAndIncrement());
        return t;
    }
}
