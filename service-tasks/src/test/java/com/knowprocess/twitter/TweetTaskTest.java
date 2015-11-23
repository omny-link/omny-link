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
