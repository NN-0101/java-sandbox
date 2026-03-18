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
 * AES 加密解密工具类
 *
 * <p>该工具类提供了两种 AES 加密解密模式：
 * <ul>
 *   <li><b>MySQL 兼容模式：</b>用于数据库字段的加密解密，与 MySQL 的 AES_ENCRYPT/AES_DECRYPT 函数兼容，
 *       使用 128 位密钥，输出为 Hex 编码</li>
 *   <li><b>标准 AES-CBC 模式：</b>使用 256 位密钥和 IV 向量，输出为 Base64 编码，适用于通用数据加密</li>
 * </ul>
 *
 * <p><b>核心功能：</b>
 * <ul>
 *   <li><b>数据库字段加密：</b>{@link #aesEncrypt(String, String)} 和 {@link #aesDecrypt(String, String)}</li>
 *   <li><b>标准 AES-CBC 加解密：</b>{@link #encrypt(String, String, String)} 和 {@link #decrypt(String, String, String)}</li>
 *   <li><b>AES 密钥生成：</b>{@link #initAESKey()} 生成 32 位随机密钥</li>
 *   <li><b>密钥转换：</b>将任意长度的字符串转换为固定长度的 AES 密钥（128位或256位）</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li><b>数据库敏感字段加密：</b>如身份证号、手机号、银行卡号等，使用 MySQL 兼容模式</li>
 *   <li><b>配置文件中敏感信息加密：</b>如数据库密码、API 密钥等</li>
 *   <li><b>网络传输数据加密：</b>对敏感数据进行加密后传输</li>
 * </ul>
 *
 * <p><b>设计要点：</b>
 * <ul>
 *   <li><b>异常处理：</b>加解密失败时返回 null 并记录错误日志，避免抛出异常影响主流程</li>
 *   <li><b>空值处理：</b>输入为 null 时直接返回 null，避免空指针异常</li>
 *   <li><b>密钥派生：</b>通过异或运算将任意长度的密钥字符串转换为固定长度的密钥字节数组</li>
 *   <li><b>算法选择：</b>标准模式使用 AES/CBC/PKCS5Padding，提供更好的安全性</li>
 * </ul>
 *
 * <p><b>注意事项：</b>
 * <ul>
 *   <li>MySQL 兼容模式使用 ECB 模式（无 IV），安全性低于 CBC 模式，仅建议用于与已有 MySQL 加密数据兼容的场景</li>
 *   <li>标准 CBC 模式需要 16 字节的 IV 向量，且每次加密应使用不同的 IV（示例中使用固定 IV，实际生产环境应随机生成）</li>
 *   <li>密钥长度：MySQL 模式使用 128 位（16字节），标准模式使用 256 位（32字节）</li>
 *   <li>生成的密钥不是真正的随机密钥，而是基于输入字符串的派生密钥，安全性取决于输入密钥的复杂度</li>
 * </ul>
 *
 * @author 0101
 * @since 2026-03-12
 */
@Slf4j
public class AesUtil {

    /**
     * AES 算法/模式/填充方式（用于标准模式）
     */
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 用于生成随机 AES 密钥的字符集
     */
    private static final String AES_STRING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLOP";

    /**
     * 私有构造方法，防止实例化
     */
    private AesUtil() {
        throw new IllegalStateException("Utility class");
    }

    // ==================== MySQL 兼容模式（数据库字段加密）====================

    /**
     * MySQL 兼容的 AES 加密
     *
     * <p>使用 128 位密钥，ECB 模式，输出为 Hex 编码字符串。
     * 与 MySQL 的 AES_ENCRYPT() 函数兼容，适用于数据库字段加密。
     *
     * @param value  待加密的明文字符串
     * @param aesKey 加密密钥
     * @return Hex 编码的密文字符串，加密失败返回 null
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
     * MySQL 兼容的 AES 解密
     *
     * <p>使用 128 位密钥，ECB 模式，输入为 Hex 编码的密文字符串。
     * 与 MySQL 的 AES_DECRYPT() 函数兼容。
     *
     * @param ciphertext Hex 编码的密文字符串
     * @param aesKey     解密密钥
     * @return 解密后的明文字符串，解密失败返回 null
     */
    public static String aesDecrypt(String ciphertext, String aesKey) {
        try {
            if (null == ciphertext) {
                return null;
            } else {
                SecretKey key = generateMysqlAesKey(aesKey);
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] cleartext = Hex.decodeHex(ciphertext.toCharArray());
                byte[] ciphertextBytes = cipher.doFinal(cleartext);
                return new String(ciphertextBytes, StandardCharsets.UTF_8);
            }
        } catch (Throwable var5) {
            return null;
        }
    }

    /**
     * 生成 MySQL 兼容的 AES 密钥（128位）
     *
     * <p>将输入密钥字符串通过异或运算转换为 16 字节的密钥数组。
     * 与 MySQL 的密钥生成算法兼容。
     *
     * @param key 输入密钥字符串
     * @return 16 字节的 AES 密钥
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

    // ==================== 标准 AES-CBC 模式（通用加密）====================

    /**
     * AES-CBC 模式加密
     *
     * <p>使用 256 位密钥，CBC 模式，PKCS5Padding 填充，输出为 Base64 编码。
     * 适用于通用数据加密场景。
     *
     * @param data      待加密的明文字符串
     * @param secretKey 加密密钥（用于派生 32 字节密钥）
     * @param ivStr     IV 向量字符串（必须是 16 字节）
     * @return Base64 编码的密文字符串，加密失败返回 null
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
     * AES-CBC 模式解密
     *
     * <p>使用 256 位密钥，CBC 模式，PKCS5Padding 填充，输入为 Base64 编码的密文。
     *
     * @param encryptedData Base64 编码的密文字符串
     * @param secretKey     解密密钥（用于派生 32 字节密钥）
     * @param ivStr         IV 向量字符串（必须是 16 字节）
     * @return 解密后的明文字符串，解密失败返回 null
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
     * 生成 256 位 AES 密钥
     *
     * <p>将输入密钥字符串通过异或运算转换为 32 字节的密钥数组。
     *
     * @param key 输入密钥字符串
     * @return 32 字节的 AES 密钥
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

    // ==================== 辅助方法 ====================

    /**
     * 生成随机 32 字符的 AES 密钥
     *
     * <p>从预定义的字符集中随机选择 32 个字符组成密钥字符串。
     * 可用于初始化 AES 密钥。
     *
     * @return 32 字符的随机密钥字符串
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
     * 生成随机数
     *
     * @param count 随机数范围上限
     * @return 0-count 之间的随机整数
     */
    private static int getRandom(int count) {
        return (int) Math.round(Math.random() * (count));
    }

    /**
     * 获取最终密钥字节数组的字符串表示（调试用）
     *
     * @param key 输入密钥字符串
     * @return 密钥字节数组的字符串表示
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

    /**
     * 测试方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 测试 AES 密钥生成
        // System.out.println("AES密钥:" + initAESKey());

        // 测试标准 AES-CBC 加解密
        String str = encrypt("895632",
                "00010001C5144B9E61C057D439CC04826F217598BA05661A292ACF2081FAF99920F36D08",
                "DboFssEOkcKDygyK");
        System.out.println("Encrypted data: " + str);

        String str2 = decrypt(str,
                "00010001C5144B9E61C057D439CC04826F217598BA05661A292ACF2081FAF99920F36D08",
                "DboFssEOkcKDygyK");
        System.out.println("Decrypted data: " + str2);
    }
}