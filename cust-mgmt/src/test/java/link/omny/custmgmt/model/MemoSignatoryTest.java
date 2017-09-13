package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MemoSignatoryTest {

    @Test
    public void testSingleSignatureFormattedForDocuSign() {
        MemoSignatory signatory = new MemoSignatory("${contact.getString('fullName','')}", "${contact.getString('email','')}", 250, 340, 1);
        assertEquals("{"
                + "\"name\": \"${contact.getString('fullName','')}\","
                + "\"email\": \"${contact.getString('email','')}\","
                + "\"recipientId\": \"\","
                + "\"tabs\": { \"signHereTabs\": [{ \"xPosition\": \"250\", \"yPosition\": \"340\", \"documentId\": \"1\", \"pageNumber\": \"1\" }]}"
                + "}", signatory.formatForDocuSign());
        assertEquals("250,340,1", signatory.getTabs());
    }

}
