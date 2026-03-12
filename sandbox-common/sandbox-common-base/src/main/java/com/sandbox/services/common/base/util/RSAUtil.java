package com.sandbox.services.common.base.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: RSA工具类
 * @Author: 0101
 * @create: 2026/3/12
 */
@Slf4j
public class RSAUtil {

    public static final String KEY_ALGORITHM = "RSA";

    private static final String PUBLIC_KEY = "RSAPublicKey";

    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 1024 bits 的 RSA 密钥对，最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * 1024 bits 的 RSA 密钥对，最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    // 生成密钥对
    public static Map<String, Object> initKey(int keySize) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            // 设置密钥对的 bit 数，越大越安全
            keyPairGen.initialize(keySize);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            // 获取公钥
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            // 获取私钥
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            Map<String, Object> keyMap = new HashMap<>(2);
            keyMap.put(PUBLIC_KEY, publicKey);
            keyMap.put(PRIVATE_KEY, privateKey);
            return keyMap;
        } catch (Exception e) {
            throw new RuntimeException("生成密钥对失败{}时遇到异常", e);
        }
    }

    /**
     * 获取公钥字符串
     */
    public static String getPublicKeyStr(Map<String, Object> keyMap) {
        // 获得 map 中的公钥对象，转为 key 对象
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        // 编码返回字符串
        return encryptBASE64(key.getEncoded());
    }

    /**
     * 获取私钥字符串
     */
    public static String getPrivateKeyStr(Map<String, Object> keyMap) {
        // 获得 map 中的私钥对象，转为 key 对象
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        // 编码返回字符串
        return encryptBASE64(key.getEncoded());
    }

    /**
     * 获取公钥
     */
    public static PublicKey getPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyByte = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyByte);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 获取私钥
     */
    public static PrivateKey getPrivateKey(String privateKeyString) throws Exception {
        byte[] privateKeyByte = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByte);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * BASE64 编码返回加密字符串
     *
     * @param key 需要编码的字节数组
     * @return 编码后的字符串
     */
    public static String encryptBASE64(byte[] key) {
        return new String(Base64.getEncoder().encode(key));
    }

    /**
     * BASE64 解码，返回字节数组
     *
     * @param key 待解码的字符串
     * @return 解码后的字节数组
     */
    public static byte[] decryptBASE64(String key) {
        return Base64.getDecoder().decode(key);
    }

    /**
     * 公钥加密
     *
     * @param text         待加密的明文字符串
     * @param publicKeyStr 公钥
     * @return 加密后的密文
     */
    public static String encrypt1(String text, String publicKeyStr) {
        try {
            log.info("明文字符串为:[{}]", text);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(publicKeyStr));
            byte[] tempBytes = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(tempBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密字符串[" + text + "]时遇到异常", e);
        }
    }

    /**
     * 私钥解密
     *
     * @param secretText    待解密的密文字符串
     * @param privateKeyStr 私钥
     * @return 解密后的明文
     */
    public static String decrypt1(String secretText, String privateKeyStr) {
        try {
            // 生成私钥
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKeyStr));
            // 密文解码
            byte[] secretTextDecoded = Base64.getDecoder().decode(secretText.getBytes(StandardCharsets.UTF_8));
            byte[] tempBytes = cipher.doFinal(secretTextDecoded);
            return new String(tempBytes);
        } catch (Exception e) {
            throw new RuntimeException("解密字符串[" + secretText + "]时遇到异常", e);
        }
    }

    /**
     * 分段加密
     */
    public static String encrypt2(String plainText, String publicKeyStr) throws Exception {
        log.info("明文:[{}]，长度:[{}]", plainText, plainText.length());
        byte[] plainTextArray = plainText.getBytes(StandardCharsets.UTF_8);
        PublicKey publicKey = getPublicKey(publicKeyStr);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        int inputLen = plainTextArray.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        int i = 0;
        dataSegment(plainTextArray, cipher, inputLen, out, offSet, i, MAX_ENCRYPT_BLOCK);
        byte[] encryptText = out.toByteArray();
        out.close();
        return Base64.getEncoder().encodeToString(encryptText);
    }

    /**
     * 分段解密
     */
    public static String decrypt2(String encryptTextHex, String privateKeyStr) throws Exception {
        byte[] encryptText = Base64.getDecoder().decode(encryptTextHex);
        PrivateKey privateKey = getPrivateKey(privateKeyStr);
        Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        int inputLen = encryptText.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        int i = 0;
        // 对数据分段解密
        dataSegment(encryptText, cipher, inputLen, out, offSet, i, MAX_DECRYPT_BLOCK);
        out.close();
        return out.toString();
    }

    /**
     * 提取数据分段公共代码
     */
    private static void dataSegment(byte[] encryptText, Cipher cipher, int inputLen, ByteArrayOutputStream out, int offSet, int i, int maxDecryptBlock) throws IllegalBlockSizeException, BadPaddingException {
        byte[] cache;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxDecryptBlock) {
                cache = cipher.doFinal(encryptText, offSet, maxDecryptBlock);
            } else {
                cache = cipher.doFinal(encryptText, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxDecryptBlock;
        }
    }

    public static void main(String[] args) throws Exception {
        Map<String, Object> keyMap;
        String cipherText;
        // 原始明文
        String content = "mytHbFdzFzkncsGcGIFnHjFDhoCtFdin";

        // 生成密钥对
//        keyMap = initKey(1024);
//        String publicKey = getPublicKeyStr(keyMap);
//        log.info("公钥:[{}]，长度:[{}]", publicKey, publicKey.length());
//        String privateKey = getPrivateKeyStr(keyMap);
//        log.info("私钥:[{}]，长度:[{}]", privateKey, privateKey.length());
//
//        // 加密
//        cipherText = encrypt1(content, publicKey);
//        log.info("加密后的密文:[{}]，长度:[{}]", cipherText, cipherText.length());
//
//        // 解密
//        String plainText = decrypt1(cipherText, privateKey);
//        log.info("解密后明文:[{}]", plainText);

        String s = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCuH/2rSugnzOMFxvYIUUl/RDnUpin7UL7Ko9ZGHTH1gE7ArYQibQhV0pCUOxdn71chSKtIc0hqZ9u8WvGtA2Rb4Ck5CNFqN4GcWhF1KxR5d6xg0DU8ENkHMwr5/E2IKRQ49H5TVAteCFGsi2SzCQZIlUg/m/jtoJB8wL+vF8skfwIDAQAB";
        String s1 = encrypt1(content, s);
        log.info("加密后的密文:[{}]，长度:[{}]", s1, s1.length());
//        String sy = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMtx1irI2/7EMs7BUszhQi01XS0PTBfImlyEO/x1KmA0nmLj8D97ma8ho26ANTrFR6xB9Z9MIQI3730dsFAc33eysQ9ofhAvR2V/rjpMVlQdzwGMh671WSPoPP2nQJA9NiiDiD1/9kIAPsv5huf4AzSXqLGWl8MHq59+f6VUyYyrAgMBAAECgYB/CtY/n3BptmE35VdsY1os2v8VbVEKJRXnohfHfe75ZsJlZIuZSPjeeTYSgfM7gQhR1FqqoCKYvQ9LZSe9z1nnyvo4qBIeapoKkNX58BQAP7+binjZ/lenKgW3b+818LE6UJmiuV8lcWQs48Pab1RDTTgpxfCnZ9YFPlExc8V1GQJBAPDKQzxXLqtqbAS//QnJd5SNjx9N6UXB2LgadZLh4ijvEng2wW6JM7+5alRFc53wyPeTHJ/iZGa1Nvqj4c4ZyCUCQQDYS6xsvV50cN5dMhFCcCKHXhA5N14DzXsH64aC/D2L0Pdk446z5ACRd7MAv3rMymYPsg2aiIXdj8hWrft6+8CPAkEAqHy/Y1+V1nhXMVOuGcURma8cMMlxq1Ai91/8WVUZzY7MG7Ykz9XNkiCJ2IHojuHHngUgiHf/zSaMXAGEpZTAyQJBAMoXQl5DPSqMfqcS7Z9myEw0bfIXskbZSVeoDwD5jsPBFjV0jFO193XzIhPT1HfdsCiAE0fMwU63VdbvLiXFbS8CQHEIeSwcI9YquNCr2zB6RYqEVfVBCRPXUZ6JIQdrYl/FuVNFaFiqwMHHlIX0LMiuyhk7ZHpl0V2eZ7MzTb5TN7Q=";
//        // 解密
//        String c = decrypt1(s1, sy);
//        log.info("解密后明文:[{}]", c);
    }
}

