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
    private Boolean suspended = false;

     UploadTask(UploadInfo info) {
        mInfo = info;
    }

    @Override
    public void run() {
        ApiUtil.getCurrentLengthOnline(UploadUtil.getInstance().getApiUrl()+"/api/progress/"+mInfo.getMD5(), new ApiUtil.OnApiListener() {
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

        try {
            mFile = new RandomAccessFile(new File(mInfo.getDir(), mInfo.getName()), "r");
            int length = 0;
            byte[] buffer = new byte[1024 * 10];
            long markTime = System.currentTimeMillis();
            int retryCount = 5;

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

                byte[] filePackets = new byte[length];
                System.arraycopy(buffer, 0, filePackets, 0, length);
                byte[] bytesTotal = getSocketPacket(filePackets);

                String response = sendSocketPacket(bytesTotal);

                JSONObject jsonResponse = new JSONObject(response);
                int code = jsonResponse.getInt("code");
                long accept = jsonResponse.getLong("accept");

//                long bd = mFile.getFilePointer();
//                long yd = accept;
//                boolean check = bd == yd;
//                Log.d("getFilePointer","长度"+mInfo.getFileLength()+ " 本地"+bd + " 云端"+yd + " 当前" + length +" "+check + " "+code);
                mFile.seek(accept);

                if (code == 200) {
                    mInfo.setCurrentLength(accept);
                    saveInfo(UploadInfo.STATE_UPLOADING);
                    continue;
                }

                if (code == 304) {
                    mInfo.setCurrentLength(accept);
                    saveInfo(UploadInfo.STATE_FINISHED);
                    return;
                }

                if (code == 502) {
                    if (accept == mInfo.getFileLength() && length<buffer.length){
                        mInfo.setCurrentLength(accept);
                        saveInfo(UploadInfo.STATE_FINISHED);
                    }else {
                        saveInfo(UploadInfo.STATE_FAILED);
                    }
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

    private synchronized String sendSocketPacket(byte[] bytesTotal) {
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

    interface Listener{
         void onResult(String response);
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
