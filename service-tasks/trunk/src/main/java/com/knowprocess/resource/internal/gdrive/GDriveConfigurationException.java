package com.knowprocess.resource.internal.gdrive;

import java.io.IOException;

public class GDriveConfigurationException extends IOException {

	private static final long serialVersionUID = 7947527739173933366L;

	public GDriveConfigurationException(String msg, Exception cause) {
		super(msg, cause);
	}
}
