package cn.wswin.util.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5 {

    public static String getMd5ByFile(File file) {
        InputStream inputStream = null;
        StringBuilder result = new StringBuilder();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, read);
            }
            byte[] md5 = messageDigest.digest();
            for (byte b : md5) {
                String s = Integer.toHexString(b & 0xff);
                if (s.length() == 1) {
                    s = "0" + s;
                }
                result.append(s);
            }
//            BigInteger bigInteger = new BigInteger(1, md5);
//            output = bigInteger.toString(16);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result.toString();
    }

    public static String getMD5(String s) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(s.getBytes("UTF-8"));

            // 转换并返回结果，也是字节数组，包含16个元素
            byte[] m = md5.digest();

            // 字符数组转换成字符串返回
            return byteArrayToHex(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String byteArrayToHex(byte[] byteArray) {

        // 首先初始化一个字符数组，用来存放每个16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
        char[] resultCharArray = new char[byteArray.length * 2];

        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray) {

            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];

            resultCharArray[index++] = hexDigits[b & 0xf];

        }

        // 字符数组组合成字符串返回
        return new String(resultCharArray);
    }

}
