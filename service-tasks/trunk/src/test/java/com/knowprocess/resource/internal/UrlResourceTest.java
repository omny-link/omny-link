package com.knowprocess.resource.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class UrlResourceTest {

    private static final String SMS_BODY = "All in the game, yo";
    private static final String SMS_FROM = "+15005550006";
    private static final String SMS_TO = "+14108675309";
    private static final String IMAGE_URL = "http://farm4.staticflickr.com/3140/3094868910_41c19ce2a3_b_d.jpg";
    private String destinationFile = "tux.jpg";

    private static final String USR = "AC4b0f9bd131e7896a8de0ec87b30174cb";
    private static final String PWD = "2993fc0d08c4cc60fe141c794994a91b";
    private static final String TWILIO_SMS_URL = "https://api.twilio.com/2010-04-01/Accounts/AC4b0f9bd131e7896a8de0ec87b30174cb/SMS/Messages.json";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFetch() {
        UrlResource resource = new UrlResource();
        InputStream is = null;
        try {
            is = resource.getResource(IMAGE_URL);
            assertNotNull(is);
            byte[] b = new byte[2048];
            int length;
            OutputStream os = new FileOutputStream(destinationFile);
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            try {
                os.close();
            } catch (Exception e) {
                ;
            }
        } catch (UnknownHostException e) {
            Assume.assumeNoException(
                    String.format(
                            "Test failed to fetch resource from '%1$s'. Assume because we are running test whilst offline",
                            e.getMessage()), e);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    @Test
    public void testPostForm() {
        UrlResource resource = new UrlResource(USR, PWD);
        InputStream is = null;
        try {
            Map<String, String> data = new HashMap<String, String>();
            data.put("To", URLEncoder.encode(SMS_TO, "UTF-8"));
            data.put("From", URLEncoder.encode(SMS_FROM, "UTF-8"));
            data.put("Body", URLEncoder.encode(SMS_BODY, "UTF-8"));
            is = resource.getResource(TWILIO_SMS_URL, "POST",
                    "application/x-www-form-urlencoded", "*/*", data);
            assertNotNull(is);
            byte[] b = new byte[2048];
            String response = new Scanner(is).useDelimiter("\\A").next();
            System.out.println("Response: " + response);

            assertTrue(response.contains("\"account_sid\": \"" + USR + "\""));
            assertTrue(response.contains("\"to\": \"" + SMS_TO + "\""));
            assertTrue(response.contains("\"from\": \"" + SMS_FROM + "\""));
            assertTrue(response.contains("\"body\": \"" + SMS_BODY + "\""));
        } catch (UnknownHostException e) {
            Assume.assumeNoException(
                    String.format(
                            "Test failed to fetch resource from '%1$s'. Assume because we are running test whilst offline",
                            e.getMessage()), e);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
    }
}
