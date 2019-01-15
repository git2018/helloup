package cn.wswin.util.upload;

import java.io.File;

/**
 * Created by albert on 2019/1/7.
 */

public class UploadInfo {

    static final int STATE_DELETED = -1;
    static final int STATE_INITIAL = 0;
    public static final int STATE_ENQUEUE = 1;
    public static final int STATE_UPLOADING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_FINISHED = 4;
    public static final int STATE_FAILED = 5;
    public static final int STATE_CANCELED = 6;
    public static final String statusTxt[] = {"空闲中","队列中","同步中","已暂停","已完成","传输失败","已取消"};

    public static int CMD_NORMAL = 1, CMD_PAUSE = 2, CMD_CANCEL = 3;

    private String mAddress;
    private int mPortal;
    private final String mId;
    private final String mDir;
    private final String mName;
    private final long mFileLength;
    private String mMD5;
    private int mUploadState;
    private long mCurrentLength;
    private long mCreateTime;
    private int mCmd = CMD_NORMAL;

    private String pid;
    private String mFileId  ;
    UploadInfo(Builder builder) {
        mAddress = builder.mAddress;
        mPortal = builder.mPortal;
        mDir = builder.mDir;
        mName = builder.mName;
        mId = builder.mId;
        mFileLength = builder.mFileLength;
        mMD5 = builder.mMD5;
        mUploadState = STATE_INITIAL;
        mCreateTime = System.currentTimeMillis();
        pid  =builder.mPid;
        mFileId = builder.mFileId;
    }

    /**
     *  新增 一个PID
     * */
    public String getmFileId() {
        return mFileId;
    }

    public String getPid() {
        return pid;
    }

    public String getId() {
        return mId;
    }

    public String getAddress() {
        return mAddress;
    }

    public int getPortal() {
        return mPortal;
    }

    public String getDir() {
        return mDir;
    }

    public String getName() {
        return mName;
    }

    void updateAddress(String address) {
        mAddress = address;
    }

    void updatePortal(int portal) {
        mPortal = portal;
    }

    public long getFileLength() {
        return mFileLength;
    }

    public synchronized void setMD5(String md5) {
        mMD5 = md5;
    }

    public String getMD5() {
        return mMD5;
    }

    public synchronized void setState(int uploadState) {
        mUploadState = uploadState;
    }

    public int getState() {
        return mUploadState;
    }

    public long getCreateTime() {
        return mCreateTime;
    }

    public void setCreateTime(long createTime) {
        mCreateTime = createTime;
    }

    public synchronized long getCurrentLength() {
        return mCurrentLength;
    }

    public synchronized void setCurrentLength(long currentLength) {
        mCurrentLength = currentLength;
    }

    public int getCmd() {
        return mCmd;
    }

    public void setCmd(int cmd) {
        this.mCmd = cmd;
    }

    public int getProgress() {
        return Math.round(getCurrentLength() / (getFileLength() * 1.0f) * 100);
    }

    public static final class Builder {
        private String mAddress;
        private int mPortal;
        private String mDir;
        private String mName;
        private String mId;
        private long mFileLength;
        private String mMD5;
      //  public String mPid;

        /**
         *  加入PID
         * */
       private String mPid;
       private String mFileId;
        public Builder(String address,
                       int portal,
                       String dir, String name,
                       String pid, String Id) {
            mAddress = address;
            mPortal = portal;
            mDir = dir;
            mName = name;
            mPid =pid;
            mFileId = Id;
        }

        public Builder id(String id) {
            mId = id;
            return this;
        }

        public Builder md5(String md5) {
            mMD5 = md5;
            return this;
        }

        public UploadInfo build() {
            if (mId == null) {
                mId = mMD5;
            }
            mFileLength = new File(mDir, mName).length();
            return new UploadInfo(this);
        }
    }

}
