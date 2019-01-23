package cn.wswin.util.upload;

import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by albert on 2019/1/7.
 */
class UploadDBUtil {

    private static UploadDBUtil sInstance;
    private UploadUtil.Listener listener ;
    private UploadUtil.OnGoingListener onGoingListener ;
    private UploadUtil.CloudListener cloudListener;

    public void setListener(UploadUtil.Listener listener) {
        this.listener = listener;
    }

    public void setCloudListener(UploadUtil.CloudListener listener) {
        this.cloudListener = listener;
    }

    public void setOnGoingListener(UploadUtil.OnGoingListener listener) {
        this.onGoingListener = listener;
    }

    private UploadDBUtil() {
    }

    public static UploadDBUtil getInstance() {
        if (sInstance == null) {
            synchronized (UploadDBUtil.class) {
                if (sInstance == null) {
                    sInstance = new UploadDBUtil();
                }
            }
        }
        return sInstance;
    }

    public void saveUploadInfo(final UploadInfo info) {
            UploadDBHelper uploadDbHelper = UploadDBHelper.getInstance();
            int onGoingNum = 0;
            if (uploadDbHelper != null) {
                if (info.getState() != UploadInfo.STATE_CANCELED) {
                    String sql = "insert or replace into upload_list ("
                            + "id, address, portal, dir, file_name, state, create_time, current_length, file_length, md5"
                            + ")"
                            + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    Object[] objects = new Object[]{
                            info.getId(), info.getAddress(), info.getPortal(), info.getDir(),
                            info.getName(), info.getState(), info.getCreateTime(),
                            info.getCurrentLength(), info.getFileLength(), info.getMD5()};
                    uploadDbHelper.getWritableDatabase().execSQL(sql, objects);
                } else {
                    String sql = "DELETE FROM upload_list WHERE id = ?";
                    uploadDbHelper.getWritableDatabase().execSQL(sql, new Object[]{info.getId()});
                }

                String sql = "SELECT * FROM upload_list WHERE state != "+UploadInfo.STATE_FINISHED;
                Cursor cursor = uploadDbHelper.getWritableDatabase().rawQuery(sql, null);
                onGoingNum = cursor.getCount();
                cursor.close();
            }
            //切回主线程
        final int finalOnGoingNum = onGoingNum;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null)
                            listener.onChange(info);
                        if (cloudListener!=null){
                            switch (info.getState()){
                                case UploadInfo.STATE_ENQUEUE: cloudListener.onCreateDir(info); break;
                                case UploadInfo.STATE_FINISHED:cloudListener.onAddItem(info); break;
                                default:
                            }
                        }
                        if (onGoingListener != null)
                            onGoingListener.onResult(finalOnGoingNum);
                    }
                });
    }

    public synchronized List<UploadInfo> loadAllUploadInfo() {
        List<UploadInfo> infoList = new ArrayList<>();

        UploadDBHelper uploadDbHelper = UploadDBHelper.getInstance();
        if (uploadDbHelper != null) {
            String sql = "SELECT * FROM upload_list ORDER BY file_name ASC";
            Cursor cursor = uploadDbHelper.getWritableDatabase().rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex("id"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                int portal = cursor.getInt(cursor.getColumnIndex("portal"));
                String dir = cursor.getString(cursor.getColumnIndex("dir"));
                String fileName = cursor.getString(cursor.getColumnIndex("file_name"));
                int state = cursor.getInt(cursor.getColumnIndex("state"));
                long createTime = cursor.getInt(cursor.getColumnIndex("create_time"));
                int currentLength = cursor.getInt(cursor.getColumnIndex("current_length"));
                String md5 = cursor.getString(cursor.getColumnIndex("md5"));

                UploadInfo info = new UploadInfo.Builder(address, portal,
                        dir, fileName
                ,"","")
                        .id(id)
                        .md5(md5)
                        .build();

                info.setState(state);
                info.setCreateTime(createTime);
                info.setCurrentLength(currentLength);

                infoList.add(info);
            }
            cursor.close();
        }
        return infoList;
    }

    synchronized boolean isUploadInfoExists(String id) {
        UploadDBHelper uploadDbHelper = UploadDBHelper.getInstance();
        boolean isExists = false;
        if (uploadDbHelper != null) {
            Cursor cursor = uploadDbHelper.getWritableDatabase()
                    .query("upload_list", null, "id=?", new String[]{id},
                            null, null, null);
            if (cursor.moveToNext()) {
                isExists = true;
            }
            cursor.close();
        }
        return isExists;
    }

    public synchronized UploadInfo getUploadInfo(String id) {
        UploadDBHelper uploadDbHelper = UploadDBHelper.getInstance();
        if (uploadDbHelper != null) {
            Cursor cursor = uploadDbHelper.getWritableDatabase()
                    .query("upload_list", null, "id=?", new String[]{id},
                            null, null, null);
            if (cursor.moveToNext()) {
                String address = cursor.getString(cursor.getColumnIndex("address"));
                int portal = cursor.getInt(cursor.getColumnIndex("portal"));
                String dir = cursor.getString(cursor.getColumnIndex("dir"));
                String fileName = cursor.getString(cursor.getColumnIndex("file_name"));
                int state = cursor.getInt(cursor.getColumnIndex("state"));
                long createTime = cursor.getInt(cursor.getColumnIndex("create_time"));
                int currentLength = cursor.getInt(cursor.getColumnIndex("current_length"));
                String md5 = cursor.getString(cursor.getColumnIndex("md5"));

                UploadInfo info = new UploadInfo.Builder(address,
                        portal, dir, fileName,"","")
                        .id(id)
                        .md5(md5)
                        .build();

                info.setState(state);
                info.setCreateTime(createTime);
                info.setCurrentLength(currentLength);

                return info;
            }
            cursor.close();
        }
        return null;
    }

    synchronized void deleteUploadInfo(UploadInfo info) {
        UploadDBHelper uploadDbHelper = UploadDBHelper.getInstance();
        if (uploadDbHelper != null) {
            String sql = "DELETE FROM upload_list WHERE id = ?";
            uploadDbHelper.getWritableDatabase().execSQL(sql, new Object[]{info.getId()});
        }
    }

    synchronized void deleteAllUploadInfo() {
        UploadDBHelper uploadDbHelper = UploadDBHelper.getInstance();
        if (uploadDbHelper != null) {
            String sql = "delete from upload_list";
            uploadDbHelper.getWritableDatabase().execSQL(sql, new Object[]{});

        }
    }

}
