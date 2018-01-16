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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import com.knowprocess.resource.spi.Repository;

/**
 * Simple implementation of the <code>ServiceEndpointStub</code> that handles
 * JSON REST services.
 * 
 * @author timstephenson
 * 
 */
public class JsonRepository implements Repository {

	private static final String APPLICATION_JSON = "application/json";	

    private int invoke(String endpoint, String userName, String password,
            String json, Writer out) {
		int code = 404; // assume the worst, will set based on service response
		URL url;
		HttpURLConnection connection = null;
		try {
			System.out.println("--ADD TEST DATA--");
			System.out.println("Service User Name:" + userName);
			System.out.println("Service Password:" + password);
			System.out.println("Service URL:" + endpoint);
			System.out.println("JSON Data:" + json);
//			out.write("INPUT : \n");
//			out.write(json);
			
			
			// Create connection
			url = new URL(endpoint);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", APPLICATION_JSON);
			connection.setRequestProperty("Accept", APPLICATION_JSON);
			
			connection.setRequestProperty("Content-Length", "" + Integer.toString(json.getBytes().length));
			// connection.setRequestProperty("Content-Language", "en-US");
			connection.setRequestProperty("Authorization",
					"Basic " + Encode.encodeAccount(userName, password));
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(json.getBytes());
			wr.flush();
			wr.close();

			code = connection.getResponseCode();
			System.out.println("Response code: " + code);
			// Assert.assertEquals(expectedCode, code);
			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			//response.append("response code: " + code + "\n");
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			// System.out.println(response.toString());
//			out.write("OUTPUT : \n");
			out.write(response.toString());
//			out.write("RESPONSE CODE : " + code);
		} catch (Exception e) {	
			try{
				out.write(e.getMessage());
			}catch (Exception ex) {
				// TODO: handle exception
			}
			e.printStackTrace();
			
		} finally {
			if (connection != null) {
				connection.disconnect();
				connection = null;
			}
		}
		return code;
	}

    @Override
    public void write(String resourceName, String mimeType, Date dateSince,
            InputStream is) {
        // TODO Auto-generated method stub

    }

	
	

}
