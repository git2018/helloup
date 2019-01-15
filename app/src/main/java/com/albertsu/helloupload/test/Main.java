package com.albertsu.helloupload.test;

/**
 * <p>
 * Create Time: 2018年5月25日
 * </p>
 * @version 1.0
 */
import java.lang.Thread.State;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * Create Time: 2018年5月25日
 * </p>
 *
 * @version 1.0
 */
public class Main {

    // 处理器个数
    public final static int PROCESS_NUM = Runtime.getRuntime().availableProcessors();

    // 根据处理器个数定义线程个数
    public final static int THREAD_NUM = Math.max(PROCESS_NUM, 4) * 5;

    // 任务队列大小
    public final static int DISPENSE_MAX_WAITTING_THREAD_NUM = Short.MAX_VALUE >> 1;// 16383

    // 最大任务执行时间，时间上限
    public final static int MAXIMUM_TASK_EXECUTION_TIME = 5;

    // 线程池，拒绝策略为丢弃旧的任务
    private static ThreadPoolExecutor es = new ThreadPoolExecutor(
            THREAD_NUM,
            THREAD_NUM,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(DISPENSE_MAX_WAITTING_THREAD_NUM),
            new NameThreadFactory("测试任务"),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    // 定时任务，定时删除任务执行时间大于5秒钟的任务
    private static ScheduledExecutorService threadCostTimeEs = new ScheduledThreadPoolExecutor(1,
            new NameThreadFactory("定时任务"), new ThreadPoolExecutor.AbortPolicy());

    // 存储任务容器
    public static ConcurrentHashMap<String, Task> map = new ConcurrentHashMap<>();

    // mian方法
    public static void main(String[] args) {

        // 模拟提交100个任务
        for (int i = 0; i < 100; i++) {
            Task task = new Task("Task-" + ((i + 1) < 10 ? "0" + (i + 1) : (i + 1)));
            map.put(task.getTaskName(), task);
            es.submit(task);
        }
        // 定时任务定时处理逻辑
        threadCostTimeEs.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // 监控线程池
                System.out.println("获取HTML线程池任务总数：" + es.getQueue().size() + "，" + "活动线程数量：" + es.getActiveCount() + "，" + "总任务数量：" + es.getTaskCount());
                Set<Entry<String, Task>> set = map.entrySet();
                Iterator<Entry<String, Task>> itr = set.iterator();
                while (itr.hasNext()) {
                    try {
                        Entry<String, Task> entry = itr.next();
                        Task t = entry.getValue();
                        long start = t.getStart();
                        if (start != 0) {
                            long currentTime = System.currentTimeMillis();
                            long sub = currentTime - start;
                            sub = sub / 1000;
                            // 队列中包含该任务，并且任务执行时间大于最大任务执行时间（目前5秒）
                            if (sub > MAXIMUM_TASK_EXECUTION_TIME && !es.getQueue().contains(t)) {
                                System.out.println(t.getTaskName() + "====>开始中断     耗时====" + sub + "  start==" + start
                                        + "  currentTime=" + currentTime);
                                State state = t.getThreadStatus();
                                // 任务为Runnable状态的，设置中断状态
                                if (state == State.RUNNABLE) {
                                    t.setInterrupte(t.getTaskInThread());
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

}
