package link.omny.custmgmt.internal;

import static org.junit.Assert.*;

import org.junit.Test;

public class ContactAvatarServiceTest {

    @Test
    public void test() throws Exception {
        ContactAvatarService avatar = new ContactAvatarService("target");
        assertTrue(avatar.create("TS").exists());
        assertTrue(avatar.create("VS").exists());
        assertTrue(avatar.create("JB").exists());
        assertTrue(avatar.create("JK").exists());
        assertTrue(avatar.create("PH").exists());
    }

}
