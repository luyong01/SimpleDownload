package com.ranze.simpledownload.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ranze on 2018/1/18.
 */

public class MD5Util {
    public static String encrypByMd5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());//update处理
            byte[] encryContext = md.digest();//调用该方法完成计算

            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < encryContext.length; offset++) {//做相应的转化（十六进制）
                i = encryContext[offset];
                if (i < 0) i += 256;
                if (i < 16) buf.append("0");
                buf.append(Integer.toHexString(i));
            }
//            System.out.println("32result: " + buf.toString());// 32位的加密
//            System.out.println("16result: " + buf.toString().substring(8, 24));// 16位的加密
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


}
