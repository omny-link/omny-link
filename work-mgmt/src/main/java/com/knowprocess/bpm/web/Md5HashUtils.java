/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
