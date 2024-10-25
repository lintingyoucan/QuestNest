package com.lt.questnest.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtil {

    /**
     * 使用SHA-256算法对密码进行加密
     * @param password 明文密码
     * @return 加密后的密码（十六进制字符串表示）
     */
    public static String encryptPassword(String password) {
        try {
            // 创建SHA-256消息摘要实例
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 将密码的字节数组传入进行摘要计算，生成哈希值
            byte[] hash = md.digest(password.getBytes());

            // 用于将字节数组转换为十六进制格式
            StringBuilder hexString = new StringBuilder();

            // 遍历字节数组，将每个字节转换为两位的十六进制字符串
            for (byte b : hash) {
                // 0xff & b 取低8位，避免负数
                String hex = Integer.toHexString(0xff & b);

                // 如果转换结果是一位，需要在前面补0，例如 "a" -> "0a"
                if (hex.length() == 1) {
                    hexString.append('0');
                }

                // 添加到字符串构建器
                hexString.append(hex);
            }

            // 返回加密后的十六进制字符串
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // 抛出运行时异常，处理加密过程中可能出现的异常
            throw new RuntimeException("SHA-256加密算法未找到", e);
        }
    }

}
