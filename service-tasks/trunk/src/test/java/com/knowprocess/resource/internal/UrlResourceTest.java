package com.knowprocess.resource.internal;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class UrlResourceTest {

	private static final String IMAGE_URL = "http://farm4.staticflickr.com/3140/3094868910_41c19ce2a3_b_d.jpg";
	private String destinationFile = "tux.jpg";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFetch() {
		UrlResource resource = new UrlResource();
		InputStream is = null;
		try {
			is = resource.getResource(IMAGE_URL);
			assertNotNull(is);
			byte[] b = new byte[2048];
			int length;
			OutputStream os = new FileOutputStream(destinationFile);
			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}

			is.close();
			os.close();
		} catch (UnknownHostException e) {
			Assume.assumeNoException(
					String.format(
							"Test failed to fet7ch resource from '%1$s'. Assume because we are running test whilst offline",
							e.getMessage()), e);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
