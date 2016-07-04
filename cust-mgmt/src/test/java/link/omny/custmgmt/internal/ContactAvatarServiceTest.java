package link.omny.custmgmt.internal;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

public class ContactAvatarServiceTest {

    private static ContactAvatarService avatarSvc;

    @BeforeClass
    public static void setUpClass() {
        avatarSvc = new ContactAvatarService("target");
    }
    
    @Test
    public void testSuccessfullyGenerateFile() throws Exception {
        ContactAvatarService avatar = new ContactAvatarService("target");
        assertTrue(avatar.create("TS").exists());
        assertTrue(avatar.create("VS").exists());
        assertTrue(avatar.create("JB").exists());
        assertTrue(avatar.create("JK").exists());
        assertTrue(avatar.create("PH").exists());
    }
    
    @Test
    public void testNullInput() throws Exception {
        File file = avatarSvc.create(null);
        System.out.println("Generated: "+file.getAbsolutePath());
        assertTrue(file.exists());
    }
}
