
package cn.wswin.util.upload;

import android.content.Context;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by albert on 2019/1/7.
 */
public class UploadUtil {

    private String dbName = "uploadpic.db";
    private Context context;
    private static UploadUtil sInstance;

    // 处理器个数
    private final static int PROCESS_NUM = Runtime.getRuntime().availableProcessors();
    private final static int THREAD_NUM = Math.max(PROCESS_NUM, 4) * 5;
    private final static int DISPENSE_MAX_WAITTING_THREAD_NUM = Short.MAX_VALUE >> 1;// 16383

    // 线程池，拒绝策略为丢弃旧的任务
    private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            THREAD_NUM,
            THREAD_NUM,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(DISPENSE_MAX_WAITTING_THREAD_NUM),
            new NameThreadFactory("图片备份任务"),
            new ThreadPoolExecutor.DiscardOldestPolicy());
    // 存储任务容器
    private static ConcurrentHashMap<String, UploadTask> map = new ConcurrentHashMap<>();
    
    public static UploadUtil getInstance() {
        if (sInstance == null) {
            synchronized (UploadUtil.class) {
                if (sInstance == null)
                    sInstance = new UploadUtil();
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        this.context = context;
        //装载任务列表
        for (UploadInfo info:getAllUploadTasks()){
            map.put(info.getId(), new UploadTask(info));
        }
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void destroy() {
        executorService.shutdown();
        sInstance = null;
    }

    /***
     *   上传 队列
     *  */
    public void enqueue(UploadInfo info) {
        UploadTask task = new UploadTask(info);
        map.put(info.getId(), task);
        executorService.submit(task);
    }

    public void start(UploadInfo info) {
        if (map.containsKey(info.getId()))
            map.get(info.getId()).start();
    }

    public void pause(UploadInfo info) {
        if (map.containsKey(info.getId()))
            map.get(info.getId()).pause();
    }

    public void stop(UploadInfo info) {
        if (map.containsKey(info.getId())) {
            map.get(info.getId()).stop();
            map.remove(info.getId());
        }
    }

    /**
     * 提交上传任务
     */
    public void commitUploadTask(String pid,String path,String host,String port) {
        File file = new File(path);
        String md5 = MD5.getMd5ByFile(file);

        UploadInfo uploadInfo = new UploadInfo.Builder(host,
                Integer.valueOf(port),
                file.getParent(),
                file.getName(),
                pid,
                md5
        )
                .md5(md5)
                .build();
        uploadInfo.setState(UploadInfo.STATE_ENQUEUE);
        enqueue(uploadInfo);
    }

    /**
     * 获取所有上传任务
     */
    public List<UploadInfo> getAllUploadTasks() {
        return UploadDBUtil.getInstance().loadAllUploadInfo();
    }

    public interface Listener{
        void onChange(UploadInfo info);
    }

    public interface CloudListener {
        void onCreateDir(UploadInfo info);
        void onAddItem(UploadInfo info);
    }

    public void setListener(Listener listener) {
        if (listener != null) UploadDBUtil.getInstance().setListener(listener);
    }

    public void setCloudListener(CloudListener listener) {
        if (listener != null) UploadDBUtil.getInstance().setCloudListener(listener);
    }

    public Context getContext() {
        return context;
    }

}
