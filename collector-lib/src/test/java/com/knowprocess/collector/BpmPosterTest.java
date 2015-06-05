package com.knowprocess.collector;

import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.knowprocess.collector.api.Status;
import com.knowprocess.collector.internal.BpmPoster;

public class BpmPosterTest extends TestCase {

    private static final String PASS = System.getProperty("bpm.password");
    private static final String USER = "tim@knowprocess.com";
    private static final String SPACE = " ";
    private BpmPoster svc;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        assertNotNull("Remember to set the -Dbpm.password", PASS);

        String url = "http://www.meetup.com/Android-Australia-User-Group-Melbourne/events/48679412/";
        String description = "Android DevCamp meetup";
        String tags = ""; // no concept of tags or labels as yet
        String project = "";
        String assignment = "+" + USER; // aka assign to self

        svc = new BpmPoster(USER, PASS);
        try {
            String markup = description + SPACE + SPACE + tags + SPACE
                    + project + SPACE + assignment;
            Status status = svc.post(svc.createStatus(url, markup));

            assertEquals(url, status.getUrl());
            assertEquals(201, status.getCode());
            assertEquals(assignment + ',', status.getProject());

        } catch (UnknownHostException e) {
            System.out.println("Warning: unable to connect, are you offline?");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
