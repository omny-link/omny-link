/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.resource.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Date;

import com.knowprocess.resource.spi.Repository;

public class MemRepository implements Repository {

	private String name;
    private Object obj;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
    public void write(String resourceName, String mimeType, Date created,
            InputStream is) throws IOException {
		//System.out.println(String.format("write(%1$s, %2$s, %3$s, %4$s)",
		//		resourceName, mimeType, created, is));
		setName(resourceName);
        if (mimeType.startsWith("text") || mimeType.equals("application/json")) {
            Reader reader = null;
			StringWriter sb = new StringWriter();
            try {
                char[] buf = new char[1024];
                reader = new InputStreamReader(is, "UTF-8");
                int charsRead = 0 ; 
                while ((charsRead = reader.read(buf)) != -1) {
					sb.write(buf, 0, charsRead);
                    // need to reset to avoid carried over chars last time thru
                    buf = new char[1024];
                }
			} catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.obj = sb.toString();
		} else if (mimeType.equals("application/pdf")
				|| mimeType.equals("application/octet-stream")) {
			
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}
			this.obj = baos.toByteArray();

		} else {
            throw new IllegalArgumentException(
					"Only text/* mime types, application/json, application/octet-stream and application/pdf supported at this stage.");
        }
    }

    /**
     * @return The last object written to this repo cast to a String.
     */
    public String getString() {
        return (String) obj;
    }

	public byte[] getBytes() {
		if (!(obj instanceof byte[])) {
			throw new IllegalStateException("Object is of type: "+ obj.getClass().getCanonicalName()); 
		}
		return (byte[]) obj;
	}
}
