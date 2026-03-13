package com.sandbox.services.db.mysql.encrypt;

import com.google.common.base.Preconditions;
import org.apache.commons.codec.binary.Hex;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * @description: mysql自定义的AES加密器
 * @author: 0101
 * @create: 2026/3/13
 */
public class CustomAesEncryptor implements EncryptAlgorithm {

    private Properties props;

    @Override
    public String encrypt(Object plaintext) {
        try {
            if (null == plaintext) {
                return null;
            } else {
                Cipher cipher = this.getCipher(1);
                byte[] cleartext = plaintext.toString().getBytes(StandardCharsets.UTF_8);
                byte[] ciphertextBytes = cipher.doFinal(cleartext);
                return new String(Hex.encodeHex(ciphertextBytes));
            }
        } catch (Throwable var5) {
            throw new RuntimeException(var5);
        }
    }

    @Override
    public Object decrypt(String ciphertext) {
        try {
            if (null == ciphertext) {
                return null;
            } else {
                Cipher cipher = this.getCipher(2);
                byte[] cleartext = Hex.decodeHex(ciphertext.toCharArray());
                byte[] ciphertextBytes = cipher.doFinal(cleartext);
                return new String(ciphertextBytes, StandardCharsets.UTF_8);
            }
        } catch (Throwable var5) {
            throw new RuntimeException(var5);
        }
    }

    @Override
    public String getType() {
        return "mysql";
    }

    private Cipher getCipher(int mode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Preconditions.checkArgument(this.props.containsKey("aes.key.value"), "No available secret key for `%s`.", new Object[]{CustomAesEncryptor.class.getName()});
        SecretKey key = this.generateMysqlAesKey(this.props.get("aes.key.value").toString());
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, key);
        return cipher;
    }

    private SecretKeySpec generateMysqlAesKey(String key) {
        byte[] finalKey = new byte[16];
        int i = 0;
        byte[] var4 = key.getBytes(StandardCharsets.US_ASCII);

        for (byte b : var4) {
            int var10001 = i++;
            finalKey[var10001 % 16] ^= b;
        }

        return new SecretKeySpec(finalKey, "AES");
    }

    @Override
    public void init() {

    }

    @Override
    public Properties getProps() {
        return props;
    }

    @Override
    public void setProps(Properties props) {
        this.props = props;
    }
}
