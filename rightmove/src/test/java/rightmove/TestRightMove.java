package rightmove;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;

import com.knowprocess.resource.spi.RestPost;

public class TestRightMove {

    private static final String SEND_RESOURCE = "https://adfapi.adftest.rightmove.com/v1/property/sendpropertydetails";
    private static final String LIST_RESOURCE = "https://adfapi.adftest.rightmove.com/v1/property/getbranchpropertylist";
    private static final String REMOVE_RESOURCE = "https://adfapi.adftest.rightmove.com/v1/property/removeproperty";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // System.
    }

    @Test
    public void testPublishUnpublish() {

        System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
        System.setProperty("javax.net.ssl.keyStore", "omnylink.p12");
        // System.setProperty("javax.net.debug", "ssl");// # very verbose debug
        System.setProperty("javax.net.ssl.keyStorePassword", "hN9MY2FovD");

        // Publish
        RestPost post = new RestPost();
        String usr = null;// "rightmove";
        String pwd = null;// "rightpass";
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Accept", MediaType.APPLICATION_JSON_VALUE);
        requestHeaders.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        String[] responseHeadersSought = {};
        String payload = getJsonPayload("/rtdf/json/send-property-request.json");
        assertNotNull("JSON test 'send' payload not found", payload);
        try {
            Map<String, Object> response = post.post(usr, pwd, SEND_RESOURCE,
                    requestHeaders, responseHeadersSought, payload);
            System.out.println(String.format("response: %1$s", response));
        } catch (Exception e) {
            fail(String.format("Cannot post payload to %1$s", SEND_RESOURCE));
        }

        // LIST
        String listPayload = getJsonPayload("/rtdf/json/list-branch-90511-property.json");
        assertNotNull("JSON test 'list' payload not found", payload);
        try {
            Map<String, Object> response = post.post(usr, pwd, LIST_RESOURCE,
                    requestHeaders, responseHeadersSought, listPayload);
            System.out.println(String.format("response: %1$s", response));
        } catch (Exception e) {
            fail(String.format("Cannot post 'list' payload to %1$s",
                    LIST_RESOURCE));
        }

        // Unpublish
        String removePayload = getJsonPayload("/rtdf/json/remove-property-request.json");
        assertNotNull("JSON test 'remove' payload not found", payload);
        try {
            Map<String, Object> response = post.post(usr, pwd, REMOVE_RESOURCE,
                    requestHeaders, responseHeadersSought, removePayload);
            System.out.println(String.format("response: %1$s", response));
        } catch (Exception e) {
            fail(String.format("Cannot post 'remove' payload to %1$s",
                    REMOVE_RESOURCE));
        }

    }

    private String getJsonPayload(String resourceName) {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(resourceName);
            assertNotNull(String.format("Test payload file %1$s not found",
                    resourceName), is);
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            e.printStackTrace();
            fail(String.format("Cannot read test payload from %1$s",
                    resourceName));
            return null;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

}
