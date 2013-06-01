package com.knowprocess.resource.internal;

import org.apache.commons.codec.binary.Base64;

public class Encode {
	public static String encodeAccount(String userName, String password) {
		if (userName == null || password == null)
			throw new NullPointerException();
		return new String(Base64.encodeBase64((userName + ":" + password)
				.getBytes()));
	}
}
