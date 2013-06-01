package com.knowprocess.opml;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.knowprocess.opml.api.OpmlFeed;
import com.knowprocess.opml.internal.OpmlInputImpl;


public class OpmlReaderTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRead() {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream("/subscriptions.opml");
            OpmlFeed feed = new OpmlInputImpl().build(is);
            System.out.println("subscriptions count: "
                    + feed.getChildren().size());
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
