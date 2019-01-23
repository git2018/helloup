package cn.wswin.util.upload;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by albert on 2019/1/7.
 */

class UploadTask implements Runnable {

    private UploadInfo mInfo;
    private Socket mSocket;
    private RandomAccessFile mFile;
    private String apiUrl;
    private Boolean suspended = false;

     UploadTask(UploadInfo info) {
        mInfo = info;
    }

    UploadTask(UploadInfo info,String apiUrl) {
        mInfo = info;
        this.apiUrl = apiUrl;
    }

    @Override
    public void run() {
        ApiUtil.getCurrentLengthOnline(apiUrl+"/api/progress/"+mInfo.getMD5(), new ApiUtil.OnApiListener() {
            @Override
            public void onResult(String result) {
                try {
                    long currentLength = 0;
                    JSONObject jsonObject = new JSONObject(result);
                    String data = jsonObject.getString("data");
                    if (!data.equalsIgnoreCase("null"))
                        currentLength = Long.parseLong(jsonObject.getJSONObject("data").getString("offset"));
                    mInfo.setCurrentLength(currentLength);
                    core();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void core(){
        try {
            mSocket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(mInfo.getAddress(), mInfo.getPortal());
            mSocket.connect(socketAddress, 5000);  //连接请求超时
            mSocket.setSoTimeout(5000);  //读操作超时
        } catch (IOException e) {
            saveInfo(UploadInfo.STATE_FAILED);
            e.printStackTrace();
        }

        File file = new File(mInfo.getDir(), mInfo.getName());
        if (!file.exists() || !file.canRead()) {
            saveInfo(UploadInfo.STATE_FAILED);
        }

        if (mInfo.getMD5() == null) {
            String md5 = MD5.getMd5ByFile(file);
            mInfo.setMD5(md5);
        }

        try {
            mFile = new RandomAccessFile(file, "r");
            mFile.seek(mInfo.getCurrentLength());
            int length = 0;
            byte[] buffer = new byte[1024 * 10];
            long markTime = System.currentTimeMillis();

            while (((length = mFile.read(buffer)) != -1) ) {

                synchronized (this){
                while (suspended) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


                if (length < 0) {
                    length = 0;
                }

                byte[] filePackets = new byte[length];
                System.arraycopy(buffer, 0, filePackets, 0, length);

                byte[] bytesTotal = getSocketPacket(filePackets);

                String response = sendSocketPacket(bytesTotal);
                if (response == null) {
                    saveInfo(UploadInfo.STATE_FAILED);
                    break;
                }
                JSONObject jsonResponse = new JSONObject(response);
                int code = jsonResponse.getInt("code");
                long accept = jsonResponse.getLong("accept");

                //包重传
                int retryCount = 5;
                while (code == 500 || code == 501 ) {//解析失败  检验失败

                    if (retryCount <= 0) {
                        saveInfo(UploadInfo.STATE_FAILED);
                        return;
                    }

                    response = sendSocketPacket(bytesTotal);
                    if (response == null) {
                        saveInfo(UploadInfo.STATE_FAILED);
                        return;
                    }
                    jsonResponse = new JSONObject(response);
                    code = jsonResponse.getInt("code");
                    accept = jsonResponse.getLong("accept");
                    retryCount--;
                }

                if (code == 200) {
                    mInfo.setCurrentLength(accept);
                    if (System.currentTimeMillis() - markTime > 100) {
                        markTime = System.currentTimeMillis();
                        saveInfo(UploadInfo.STATE_UPLOADING);
                    }
//                    saveInfo(UploadInfo.STATE_UPLOADING);
                    continue;
                }

                if (code == 304) {
                    mInfo.setCurrentLength(accept);
                    saveInfo(UploadInfo.STATE_FINISHED);
                    return;
                } else if (code == 503) {//取消
                    return;
                } else if (code == 504) {//暂停
//                    saveInfo(UploadInfo.STATE_PAUSED);
                } else {
                    saveInfo(UploadInfo.STATE_FAILED);
                    return;
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                mSocket.close();
                mFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] getSocketPacket(byte[] filePackets) {
        String crc = Crc32.getCRC32(filePackets);

        JSONObject jsonObject = new JSONObject();
        JSONObject client = new JSONObject();

        try {
            client.put("name", "android")
                    .put("version", "1.0");

            jsonObject.put("id", mInfo.getId())
                    .put("md5", mInfo.getMD5())
                    .put("crc", crc)
                    .put("cmd", mInfo.getCmd())
                    .put("fileName", mInfo.getName())
                    .put("offset", mInfo.getCurrentLength())
                    .put("fileLength", mInfo.getFileLength())
                    .put("client", client)
                    .put("complete", 1);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String json = jsonObject.toString();

        byte[] bytesJsonLength = IntBtyeUtil.intToByte(json.getBytes().length);
        byte[] bytesJson = json.getBytes();
        byte[] bytesPacketLength = IntBtyeUtil.intToByte(filePackets.length);
        int totalLength = 4 + 4 + json.getBytes().length + 4 + filePackets.length;
        byte[] bytesTotalLength = IntBtyeUtil.intToByte(totalLength);

        byte[] bytesTotal = new byte[totalLength];
        System.arraycopy(bytesTotalLength, 0, bytesTotal, 0, bytesTotalLength.length);
        System.arraycopy(bytesJsonLength, 0, bytesTotal, bytesTotalLength.length, bytesJsonLength.length);
        System.arraycopy(bytesJson, 0, bytesTotal,
                bytesTotalLength.length + bytesJsonLength.length, bytesJson.length);
        System.arraycopy(bytesPacketLength, 0, bytesTotal,
                bytesTotalLength.length + bytesJsonLength.length + bytesJson.length, bytesPacketLength.length);
        System.arraycopy(filePackets, 0, bytesTotal,
                bytesTotalLength.length + bytesJsonLength.length + bytesJson.length + bytesPacketLength.length,
                filePackets.length);

        return bytesTotal;
    }

    private String sendSocketPacket(byte[] bytesTotal) {
        try {
            OutputStream outputStream = mSocket.getOutputStream();
            outputStream.write(bytesTotal);

            InputStream inputStream = mSocket.getInputStream();
            int len;
            byte[] acceptedBuffer = new byte[1024];
            ByteArrayOutputStream bytesBuffer = new ByteArrayOutputStream(4);
            byte[] bytesAcceptedTotal = new byte[4];
            while ((len = inputStream.read(acceptedBuffer)) != -1) {
                System.arraycopy(acceptedBuffer, 0, bytesAcceptedTotal, 0, 4);
                int total = IntBtyeUtil.bytesToInt(bytesAcceptedTotal);
                bytesBuffer.write(acceptedBuffer, 4, total);
                if (total == len - 4) {
                    return bytesBuffer.toString("utf-8");
                }
            }

            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

      public void pause() {
         suspended = true;
        mInfo.setCmd(UploadInfo.CMD_PAUSE);
        saveInfo(UploadInfo.STATE_PAUSED);
    }

     public void stop() {
         suspended = true;
        mInfo.setCmd(UploadInfo.CMD_CANCEL);
        saveInfo(UploadInfo.STATE_CANCELED);
    }

    synchronized public void resume() {
        suspended = false;
        notify();
        mInfo.setCmd(UploadInfo.CMD_NORMAL);
        saveInfo(UploadInfo.STATE_UPLOADING);
    }

    private void saveInfo(int state){
        mInfo.setState(state);
        UploadDBUtil.getInstance().saveUploadInfo(mInfo);
    }

}
