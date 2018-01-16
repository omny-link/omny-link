/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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
package com.knowprocess.twitter;

import java.util.Date;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class TweetTaskTest {

    protected static String key;
    protected static String secret;
    protected static String accessToken;
    protected static String accessSecret;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        key = System.getProperty("consumerKey");
        secret = System.getProperty("consumerSecret");
        accessToken = System.getProperty("accessToken");
        accessSecret = System.getProperty("accessSecret");

        Assume.assumeFalse(
                "No OAuth credentails configured, test will be skipped.",
                (key == null || secret == null || accessToken == null || accessSecret == null));
    }

    @Test
    public void testOutsideActiviti() {
        TweetTask task = new TweetTask();
        task.tweet(key, secret, accessToken, accessSecret,
                "Hello! The time is: " + new Date());
    }

}
