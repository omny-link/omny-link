package com.knowprocess.collector;

import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.knowprocess.collector.api.Status;
import com.knowprocess.collector.internal.AbstractPoster;
import com.knowprocess.collector.internal.DeliciousPoster;

public class DeliciousPosterTest extends TestCase {

    private static final String PASS = System.getProperty("del.password");
    private static final String USER = "TimStephenson";
    private static final String SPACE = " ";
    private AbstractPoster svc;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() {
        String url = "http://www.meetup.com/Android-Australia-User-Group-Melbourne/events/48679412/";
        String description = "Android DevCamp meetup";
        String tagsIn = "#android, #meetup, #melbourne";
        String tagsOut = "android,meetup,melbourne,";
        String context = "";
        String project = "";

        try {
            svc = new DeliciousPoster(USER, PASS);
        } catch (IllegalArgumentException e) {
            System.out
                    .println("Did you specify -Ddel.password=YOUR_PASSWORD ?");
            fail(e.getMessage());
        }

        try {
            String markup = description + SPACE + tagsIn + SPACE
                    + context + SPACE + project;
            System.out.println("markup: " + markup);
            Status status = svc.post(svc.createStatus(url, markup));
            
            // TODO does delicious care about project or context?
            assertEquals(200, status.getCode());
            assertEquals(url, status.getUrl());
            assertEquals(tagsOut, status.getTags());
            System.out.println("post-parsed description: " + status.getText());
            assertEquals(description, status.getText());
        } catch (UnknownHostException e) {
            System.out.println("Warning: unable to connect, are you offline?");
        } catch (Throwable e) {
            fail(e.getMessage());
        }
    }

}
