package link.omny.custmgmt.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        assertEquals("8a241744f3225ab547dd03cf67440ae212205b05", sha1(file));
    }

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String sha1(File file)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] b = null;
        try (RandomAccessFile f = new RandomAccessFile(file, "r");) {
            b = new byte[(int) f.length()];
            f.readFully(b);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(b);
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
}
