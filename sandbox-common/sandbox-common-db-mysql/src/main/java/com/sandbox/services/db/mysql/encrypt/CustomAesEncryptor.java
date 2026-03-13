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

    @Override
    public String encrypt(Object o) {
        return "";
    }

    @Override
    public Object decrypt(String s) {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public String getType() {
        return "";
    }
}
