package com.knowprocess.deployment;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.activiti.bpmn.model.Process;
import org.junit.Before;
import org.junit.Test;

import com.knowprocess.resource.spi.Fetcher;

public class ProcessDefinerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParseWeekend() {
        Fetcher fetcher = new Fetcher();
        try {
            String markup = fetcher.fetchToString("classpath:///weekend.txt");
            ProcessDefiner definer = new ProcessDefiner();
            Process process = definer.parse(markup);
            assertNotNull(process);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
