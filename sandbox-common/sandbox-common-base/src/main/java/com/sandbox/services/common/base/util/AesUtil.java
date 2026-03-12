package com.sandbox.services.common.base.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @description: AES加解密
 * @author: 0101
 * @create: 2026/3/12
 */
@Slf4j
public class AesUtil {

    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 数据库字段aes加密
     *
     * @param value  值
     * @param aesKey 秘钥
     * @return 加密后内容
     */
    public static String aesEncrypt(String value, String aesKey) {
        if (Objects.isNull(value)) {
            return null;
        }
        try {
            SecretKeySpec key = generateMysqlAesKey(aesKey);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cleartext = value.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = cipher.doFinal(cleartext);
            return new String(Hex.encodeHex(ciphertextBytes));
        } catch (Exception e) {
            log.error(String.format("aes_encrypt error, content= %s, errorMsg= %s", value, e.getMessage()), e);
        }
        return null;
    }

    /**
     * 数据库字段aes解密
     *
     * @param ciphertext 密文
     * @param aesKey     秘钥
     * @return 解密后内容
     */
    public static String aesDecrypt(String ciphertext, String aesKey) {
        try {
            if (null == ciphertext) {
                return null;
            } else {
                SecretKey key = generateMysqlAesKey(aesKey);
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(2, key);
                byte[] cleartext = Hex.decodeHex(ciphertext.toCharArray());
                byte[] ciphertextBytes = cipher.doFinal(cleartext);
                return new String(ciphertextBytes, StandardCharsets.UTF_8);
            }
        } catch (Throwable var5) {
            return null;
        }
    }

    /**
     * 生成 aes 秘钥 16字节 AES-128
     *
     * @param key key
     * @return 秘钥
     */
    private static SecretKeySpec generateMysqlAesKey(String key) {
        byte[] finalKey = new byte[16];
        int i = 0;
        byte[] var4 = key.getBytes(StandardCharsets.US_ASCII);
        int var5 = var4.length;

        for (byte b : var4) {
            int var10001 = i++;
            finalKey[var10001 % 16] ^= b;
        }

        return new SecretKeySpec(finalKey, "AES");
    }

    /**
     * aes加密
     *
     * @param data      数据
     * @param secretKey 秘钥
     * @param ivStr     IV向量
     * @return 加密后数据
     */
    public static String encrypt(String data, String secretKey, String ivStr) {
        if (Objects.isNull(data)) {
            return null;
        }
        try {
            SecretKeySpec key = generateAesKey(secretKey);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes(UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            log.error(String.format("aes_encrypt error, content= %s, errorMsg= %s", data, e.getMessage()), e);
        }
        return null;
    }

    /**
     * aes解密
     *
     * @param encryptedData 加密数据
     * @param secretKey     秘钥
     * @param ivStr         IV向量
     * @return 解密后数据
     */
    public static String decrypt(String encryptedData, String secretKey, String ivStr) {
        if (Objects.isNull(encryptedData)) {
            return null;
        }
        try {
            SecretKeySpec key = generateAesKey(secretKey);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes(UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(String.format("aes_decrypt error, content= %s, errorMsg= %s", encryptedData, e.getMessage()), e);
        }
        return null;
    }

    /**
     * 生成 aes 秘钥 32字节 AES-256
     *
     * @param key key
     * @return 秘钥
     */
    private static SecretKeySpec generateAesKey(String key) {
        byte[] finalKey = new byte[32];
        int i = 0;
        byte[] var4 = key.getBytes(StandardCharsets.US_ASCII);
        int var5 = var4.length;

        for (byte b : var4) {
            int var10001 = i++;
            finalKey[var10001 % 32] ^= b;
        }

        return new SecretKeySpec(finalKey, "AES");
    }

    /**
     * 生成AES密钥随机字符串
     */
    private static final String AES_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLOP";


    private static int getRandom(int count) {
        return (int) Math.round(Math.random() * (count));
    }

    /**
     * 生成AES key
     *
     * @return AES key
     */
    public static String initAESKey() {
        StringBuilder sb = new StringBuilder();
        int len = AES_STRING.length();
        for (int i = 0; i < 32; i++) {
            sb.append(AES_STRING.charAt(getRandom(len - 1)));
        }
        return sb.toString();
    }

    /**
     * 生成 finalKey
     *
     * @param key key
     * @return 秘钥
     */
    private static String getFinalKey(String key) {
        byte[] finalKey = new byte[32];
        int i = 0;
        byte[] var4 = key.getBytes(StandardCharsets.US_ASCII);
        int var5 = var4.length;

        for (byte b : var4) {
            int var10001 = i++;
            finalKey[var10001 % 32] ^= b;
        }
        return Arrays.toString(finalKey);
    }

    public static void main(String[] args) throws Exception {

//        System.out.println("AES密钥:" + initAESKey());

        String str = encrypt("895632", "00010001C5144B9E61C057D439CC04826F217598BA05661A292ACF2081FAF99920F36D08", "DboFssEOkcKDygyK");
        System.out.println("Encrypted data: " + str);

        String str2 = decrypt(str, "00010001C5144B9E61C057D439CC04826F217598BA05661A292ACF2081FAF99920F36D08", "DboFssEOkcKDygyK");
        System.out.println("Decrypted data: " + str2);

    }
}
