package cn.wswin.util.upload;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class UploadDBHelper extends SQLiteOpenHelper {

    private static UploadDBHelper uploadDbHelper;

    public static UploadDBHelper getInstance() {
        if (uploadDbHelper == null) {
            synchronized (UploadDBHelper.class) {
                if (uploadDbHelper == null) {
                    uploadDbHelper = new UploadDBHelper();
                }
            }
        }
        return uploadDbHelper;
    }

    private UploadDBHelper() {
        super(UploadUtil.getInstance().getContext(), UploadUtil.getInstance().getDbName(), null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUploadTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public synchronized void close() {
        super.close();
        uploadDbHelper = null;
    }

    private synchronized void createUploadTable(SQLiteDatabase db) {
        String sql = "create table if not exists upload_list ("
                + "id VARCHAR PRIMARY KEY,"
                + "address VARCHAR,"
                + "portal INTEGER,"
                + "dir VARCHAR,"
                + "file_name VARCHAR,"
                + "state INTEGER,"
                + "create_time INTEGER,"
                + "current_length INTEGER,"
                + "file_length INTEGER,"
                + "md5 VARCHAR"
                + ")";
        db.execSQL(sql);
    }

}
