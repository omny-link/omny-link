package com.knowprocess.bpm.web;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Md5HashUtils {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Md5HashUtils.class);

    public static final String getHash(String bpmn) {
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(
                    bpmn.getBytes());

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // JDK required to support MD5
            // http://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html
            LOGGER.error(e.getMessage(), e);
        }
        return "n/a";
    }

    public static final boolean isIdentical(String a, String b) {
        return Md5HashUtils.getHash(a).equals(Md5HashUtils.getHash(b));
    }
}