
package cn.wswin.util.upload;

import android.content.Context;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by albert on 2019/1/7.
 */
public class UploadUtil {
    private String dbName = "uploadpic.db";
    private Context context;
    private String apiUrl;
    private static UploadUtil sInstance;

    private ExecutorService executorService = Executors.newFixedThreadPool(5);
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

    public void init(Context context,String dbName,String apiBaseUrl) {
        this.context = context;
        this.apiUrl = apiBaseUrl;
        this.dbName = dbName;
        //装载任务列表
        for (UploadInfo info:getAllUploadTasks()){
            map.put(info.getId(), new UploadTask(info));
        }
    }

    /**
     * 提交上传任务
     */
    public void commitUploadTask(String path,String host,String port) {
        File file = new File(path);
        String md5 = MD5.getMd5ByFile(file);

        UploadInfo uploadInfo = new UploadInfo.Builder(host,
                Integer.valueOf(port),
                file.getParent(),
                file.getName(),
                "0",
                md5
        )
                .md5(md5)
                .build();
        uploadInfo.setState(UploadInfo.STATE_ENQUEUE);
        enqueue(uploadInfo);
    }


    /***
     *   上传 队列
     *  */
    public void enqueue(final UploadInfo info) {
        final UploadTask task = new UploadTask(info,apiUrl);
        map.put(info.getId(), task);
        UploadDBUtil.getInstance().saveUploadInfo(info);
        executorService.execute(task);
    }

    public void start(UploadInfo info) {
        if (map.containsKey(info.getId()))
            map.get(info.getId()).start();
    }

    public void pause(UploadInfo info) {
        if (info.getState() == UploadInfo.STATE_UPLOADING &&
                map.containsKey(info.getId()))
            map.get(info.getId()).pause();
    }



    public void stop(UploadInfo info) {
        if (map.containsKey(info.getId())) {
            map.get(info.getId()).stop();
            map.remove(info.getId());
        }
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

    public interface OnGoingListener{
        void onResult(int number);
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

    public void setOnGoingListener(OnGoingListener listener) {
        if (listener != null) UploadDBUtil.getInstance().setOnGoingListener(listener);
    }

    public Context getContext() {
        return context;
    }
    public String getDbName() {
        return dbName;
    }

    public void destroy() {
        executorService.shutdown();
        sInstance = null;
    }
}
