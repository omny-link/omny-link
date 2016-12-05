package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MemoTest {

    @Test
    public void testSetPlainContent() {
        Memo memo = new Memo();
        memo.setRichContent("<h1>The War of the Worlds</h1><br/><h2>By H.G. Wells</h2>");
        assertEquals("The War of the Worlds By H.G. Wells",
                memo.getPlainContent());
    }

}

