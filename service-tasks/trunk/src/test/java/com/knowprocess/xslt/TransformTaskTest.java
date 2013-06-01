package com.knowprocess.xslt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TransformTaskTest {

    private static final String ACTIVITI_SUPPORT_BPMN = "/process/activiti-support.bpmn";
    private TransformTask svc;

    @Before
    public void setUp() throws Exception {
        svc = new TransformTask();
    }

    @Test
    public void testTransformStringToString() {
        InputStream stream = null;
        Reader reader = null;
        StringBuffer bpmn = new StringBuffer();
        try {
            char[] buf = new char[1024];
            stream = getClass().getResourceAsStream(ACTIVITI_SUPPORT_BPMN);
            reader = new InputStreamReader(stream, "UTF-8");
            assertNotNull("Test BPMN file not found: " + ACTIVITI_SUPPORT_BPMN,
                    stream);
            while (reader.read(buf) != -1) {
                bpmn.append(buf);
                // need to reset to avoid carried over chars last time thru
                buf = new char[1024];
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail(e.getMessage());
        }
        svc.setXsltResource("/xslt/ActivitiSupportRules.xsl");
        System.out.println("BPMN: " + bpmn.toString());
        assertTrue(bpmn.toString().trim().endsWith(">"));
        String result = svc.transform(bpmn.toString().trim());
        System.out.println("result: " + result);
        assertNotNull(result);
        // assertTrue(!result.contains("ERROR"));
        String[] messages = result.split("\\n", 0);
        System.out.println("messages found: " + messages.length);
        List<String> ignored = new ArrayList<String>();
        List<String> passed = new ArrayList<String>();
        List<String> errors = new ArrayList<String>();
        for (String string : messages) {
            String msg = string.trim();
            if (msg.length() > 0 && msg.toUpperCase().startsWith("ERROR")) {
                errors.add(msg);
            } else if (msg.length() > 0 && msg.toUpperCase().startsWith("PASS")) {
                passed.add(msg);
            } else if (msg.length() > 0
                    && msg.toUpperCase().startsWith("IGNORED")) {
                ignored.add(msg);
            }
        }
        System.err.println("ERRORS:" + errors.size());
        System.err.println("IGNORED:" + ignored.size());
        System.err.println("PASSED:" + passed.size());
    }

}
