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
 * RSA 非对称加密工具类
 *
 * <p>该工具类提供了 RSA 密钥对生成、公钥加密、私钥解密等功能。
 * RSA 是一种非对称加密算法，使用公钥加密、私钥解密，适用于数据传输加密、
 * 数字签名等场景。
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>密钥对生成：</b>{@link #initKey(int)} 生成指定长度的 RSA 密钥对</li>
 *   <li><b>密钥转换：</b>支持密钥对象与 Base64 字符串之间的相互转换</li>
 *   <li><b>公钥加密：</b>{@link #encrypt1(String, String)} 和 {@link #encrypt2(String, String)} 提供单次和分段加密</li>
 *   <li><b>私钥解密：</b>{@link #decrypt1(String, String)} 和 {@link #decrypt2(String, String)} 提供单次和分段解密</li>
 *   <li><b>分段处理：</b>针对 RSA 算法对加密数据长度的限制，提供分段加密解密功能</li>
 * </ul>
 *
 * <p><b>RSA 加密限制：</b>
 * <ul>
 *   <li>1024 位密钥：最大加密明文长度为 117 字节，最大解密密文长度为 128 字节</li>
 *   <li>2048 位密钥：最大加密明文长度为 245 字节，最大解密密文长度为 256 字节</li>
 *   <li>当数据超过长度限制时，需要使用分段加密解密</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>敏感数据传输：</b>如密码、支付信息等</li>
 *   <li><b>数字签名：</b>验证数据的完整性和真实性</li>
 *   <li><b>密钥交换：</b>安全地传输对称加密的密钥</li>
 *   <li><b>身份认证：</b>使用私钥签名，公钥验证身份</li>
 * </ul>
 *
 * <p><b>使用示例：</b>
 * <pre>
 * // 1. 生成密钥对
 * Map&lt;String, Object&gt; keyMap = RSAUtil.initKey(1024);
 * String publicKey = RSAUtil.getPublicKeyStr(keyMap);
 * String privateKey = RSAUtil.getPrivateKeyStr(keyMap);
 *
 * // 2. 公钥加密
 * String plainText = "sensitive data";
 * String encrypted = RSAUtil.encrypt1(plainText, publicKey);
 *
 * // 3. 私钥解密
 * String decrypted = RSAUtil.decrypt1(encrypted, privateKey);
 * </pre>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>RSA 加密速度较慢，不适合加密大量数据，通常只用于加密小数据（如对称密钥）</li>
 *   <li>密钥长度越大越安全，但加解密速度越慢，1024 位已不安全，建议使用 2048 位</li>
 *   <li>加密数据长度受密钥长度限制，超过限制需要使用分段加密</li>
 *   <li>私钥必须妥善保管，泄露会导致加密失效</li>
 *   <li>公钥可以公开，但需要确保公钥的完整性和真实性</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-12
 */
@Slf4j
public class RSAUtil {

    /**
     * 密钥算法名称
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * 公钥在 Map 中的键名
     */
    private static final String PUBLIC_KEY = "RSAPublicKey";

    /**
     * 私钥在 Map 中的键名
     */
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 1024 bits RSA 密钥对的最大加密明文大小（字节）
     * RSA 加密数据长度受密钥长度限制，公式为：密钥长度 / 8 - 11
     * 1024 / 8 - 11 = 117
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /**
     * 1024 bits RSA 密钥对的最大解密密文大小（字节）
     * 1024 / 8 = 128
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    /**
     * 私有构造方法，防止实例化
     */
    private RSAUtil() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== 密钥对生成 ====================

    /**
     * 初始化 RSA 密钥对
     *
     * @param keySize 密钥长度（位），推荐 1024 或 2048
     * @return 包含公钥和私钥的 Map
     * @throws RuntimeException 密钥对生成失败时抛出
     */
    public static Map<String, Object> initKey(int keySize) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            // 设置密钥对的 bit 数，越大越安全，但加解密速度越慢
            keyPairGen.initialize(keySize);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            // 获取公钥和私钥
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            Map<String, Object> keyMap = new HashMap<>(2);
            keyMap.put(PUBLIC_KEY, publicKey);
            keyMap.put(PRIVATE_KEY, privateKey);
            return keyMap;
        } catch (Exception e) {
            throw new RuntimeException("生成密钥对失败", e);
        }
    }

    /**
     * 从密钥对 Map 中获取公钥的 Base64 字符串
     *
     * @param keyMap 包含公钥和私钥的 Map
     * @return Base64 编码的公钥字符串
     */
    public static String getPublicKeyStr(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        return encryptBASE64(key.getEncoded());
    }

    /**
     * 从密钥对 Map 中获取私钥的 Base64 字符串
     *
     * @param keyMap 包含公钥和私钥的 Map
     * @return Base64 编码的私钥字符串
     */
    public static String getPrivateKeyStr(Map<String, Object> keyMap) {
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        return encryptBASE64(key.getEncoded());
    }

    // ==================== 密钥转换 ====================

    /**
     * 从 Base64 字符串获取公钥对象
     *
     * @param publicKeyString Base64 编码的公钥字符串
     * @return 公钥对象
     * @throws NoSuchAlgorithmException 算法不存在
     * @throws InvalidKeySpecException  密钥规范无效
     */
    public static PublicKey getPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyByte = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyByte);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 从 Base64 字符串获取私钥对象
     *
     * @param privateKeyString Base64 编码的私钥字符串
     * @return 私钥对象
     * @throws Exception 获取私钥失败时抛出
     */
    public static PrivateKey getPrivateKey(String privateKeyString) throws Exception {
        byte[] privateKeyByte = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyByte);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * BASE64 编码
     *
     * @param key 需要编码的字节数组
     * @return 编码后的字符串
     */
    public static String encryptBASE64(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * BASE64 解码
     *
     * @param key 待解码的字符串
     * @return 解码后的字节数组
     */
    public static byte[] decryptBASE64(String key) {
        return Base64.getDecoder().decode(key);
    }

    // ==================== 单次加密解密（适用于短数据）====================

    /**
     * 公钥加密（单次加密，适用于数据长度 ≤ 117 字节）
     *
     * @param text         待加密的明文字符串
     * @param publicKeyStr Base64 编码的公钥
     * @return Base64 编码的密文字符串
     * @throws RuntimeException 加密失败时抛出
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
     * 私钥解密（单次解密，适用于密文长度 ≤ 128 字节）
     *
     * @param secretText    Base64 编码的密文字符串
     * @param privateKeyStr Base64 编码的私钥
     * @return 解密后的明文字符串
     * @throws RuntimeException 解密失败时抛出
     */
    public static String decrypt1(String secretText, String privateKeyStr) {
        try {
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

    // ==================== 分段加密解密（适用于长数据）====================

    /**
     * 公钥分段加密（适用于数据长度 > 117 字节）
     *
     * @param plainText     待加密的明文字符串
     * @param publicKeyStr  Base64 编码的公钥
     * @return Base64 编码的密文字符串
     * @throws Exception 加密失败时抛出
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

        // 分段加密
        processDataSegment(plainTextArray, cipher, inputLen, out, offSet, i, MAX_ENCRYPT_BLOCK);

        byte[] encryptText = out.toByteArray();
        out.close();
        return Base64.getEncoder().encodeToString(encryptText);
    }

    /**
     * 私钥分段解密（适用于密文长度 > 128 字节）
     *
     * @param encryptTextHex Base64 编码的密文字符串
     * @param privateKeyStr  Base64 编码的私钥
     * @return 解密后的明文字符串
     * @throws Exception 解密失败时抛出
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

        // 分段解密
        processDataSegment(encryptText, cipher, inputLen, out, offSet, i, MAX_DECRYPT_BLOCK);

        out.close();
        return out.toString();
    }

    /**
     * 分段处理数据的公共方法
     *
     * @param data           待处理的数据字节数组
     * @param cipher         加密/解密 Cipher 对象
     * @param inputLen       数据总长度
     * @param out            输出流
     * @param offSet         当前偏移量
     * @param i              当前段索引
     * @param maxBlockSize   每段最大处理字节数
     * @throws IllegalBlockSizeException 块大小非法
     * @throws BadPaddingException       填充错误
     */
    private static void processDataSegment(byte[] data, Cipher cipher, int inputLen,
                                           ByteArrayOutputStream out, int offSet, int i,
                                           int maxBlockSize) throws IllegalBlockSizeException, BadPaddingException {
        byte[] cache;
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > maxBlockSize) {
                cache = cipher.doFinal(data, offSet, maxBlockSize);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * maxBlockSize;
        }
    }

    /**
     * 测试方法
     *
     * @param args 命令行参数
     * @throws Exception 异常
     */
    public static void main(String[] args) throws Exception {
        // 原始明文
        String content = "mytHbFdzFzkncsGcGIFnHjFDhoCtFdin";

        // 测试公钥加密（使用给定的公钥）
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCuH/2rSugnzOMFxvYIUUl/RDnUpin7UL7Ko9ZGHTH1gE7ArYQibQhV0pCUOxdn71chSKtIc0hqZ9u8WvGtA2Rb4Ck5CNFqN4GcWhF1KxR5d6xg0DU8ENkHMwr5/E2IKRQ49H5TVAteCFGsi2SzCQZIlUg/m/jtoJB8wL+vF8skfwIDAQAB";
        String encrypted = encrypt1(content, publicKey);
        log.info("加密后的密文:[{}]，长度:[{}]", encrypted, encrypted.length());
    }
}