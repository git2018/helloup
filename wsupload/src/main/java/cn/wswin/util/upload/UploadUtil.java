
package cn.wswin.util.upload;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private static UploadUtil sInstance;
//    private Handler handler;

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

    public void init(Context context,String dbName) {
        this.context = context;
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
        UploadDBUtil.getInstance().saveUploadInfo(uploadInfo);

        UploadTask task = new UploadTask(uploadInfo);
        map.put(uploadInfo.getId(), task);
        executorService.execute(task);
    }

    public void start(UploadInfo info) {
        if (info.getState() == UploadInfo.STATE_PAUSED &&map.containsKey(info.getId()))
            map.get(info.getId()).resume();
    }

    public void pause(UploadInfo info) {
        if (info.getState() == UploadInfo.STATE_UPLOADING && map.containsKey(info.getId()))
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
        if (map.size() == 0){
            List<UploadInfo> list1 = UploadDBUtil.getInstance().loadAllUploadInfo();
            return sort(list1);
        }else {
            List<UploadInfo> infos = new ArrayList<>();
            for (UploadTask task : map.values()) {
                infos.add(task.getInfo());
            }
            return sort(infos);
        }
    }

    public interface Listener{
        void onChange(UploadInfo info);
    }

    public interface OnGoingListener{
        void onResult(int number);
    }

    public interface CloudListener {
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

    private List<UploadInfo> sort(List<UploadInfo> infos){
        Comparator<UploadInfo> comparator = new Comparator<UploadInfo>() {
            @SuppressLint("NewApi")
            public int compare(UploadInfo p1, UploadInfo p2) {
                return Integer.compare(p1.getState() , p2.getState());
            }
        };
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(infos,comparator);
        return infos;
    }
}
