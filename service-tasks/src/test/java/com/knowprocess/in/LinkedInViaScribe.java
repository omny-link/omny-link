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
package com.knowprocess.in;

import java.util.Scanner;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class LinkedInViaScribe {
	private static final String PROTECTED_RESOURCE_URL = "http://api.linkedin.com/v1/people/~/mailbox";
	private static final Verb VERB = Verb.POST;
	private static final String PAYLOAD = "{ \"recipients\": "
			+ "{ \"values\": [ {\"person\": {\"_path\":\"/people/3701322\",}} ] },"
			+ "\"subject\": \"Hello.\","
			+ "\"body\": \"Let me know ifi you get this!\"}";

//	private static final String PROTECTED_RESOURCE_URL = "https://api.linkedin.com/v1/people/~";

	// "http://api.linkedin.com/v1/people/~/connections:(id,last-name)";

	public static void main(String[] args) {
		String apiKey = System.getProperty("key");
		String apiSecret = System.getProperty("secret");
		if (apiSecret == null || apiKey == null) {
			System.out
					.println("You must supply both 'key' and 'secret' as command line parameters (-Dkey=...)");
		}
		OAuthService service = new ServiceBuilder().provider(LinkedInApi.class)
				.apiKey(apiKey).apiSecret(apiSecret).build();
		Scanner in = new Scanner(System.in);

		System.out.println("=== LinkedIn's OAuth Workflow ===");
		System.out.println();

		// Obtain the Request Token
		System.out.println("Fetching the Request Token...");
		Token requestToken = service.getRequestToken();
		System.out.println("Got the Request Token!");
		System.out.println();

		System.out.println("Now go and authorize Scribe here:");
		System.out.println(service.getAuthorizationUrl(requestToken));
		System.out.println("And paste the verifier here");
		System.out.print(">>");
		Verifier verifier = new Verifier(in.nextLine());
		System.out.println();

		// Trade the Request Token and Verfier for the Access Token
		System.out.println("Trading the Request Token for an Access Token...");
		Token accessToken = service.getAccessToken(requestToken, verifier);
		System.out.println("Got the Access Token!");
		System.out.println("(if your curious it looks like this: "
				+ accessToken + " )");
		System.out.println();

		// Now let's go and ask for a protected resource!
		System.out.println("Now we're going to access a protected resource...");
		OAuthRequest request = new OAuthRequest(VERB,
				PROTECTED_RESOURCE_URL);
		if (VERB == Verb.POST || VERB == Verb.PUT) {
			request.addPayload(PAYLOAD);
		}
		service.signRequest(accessToken, request);
		Response response = request.send();
		System.out.println("Got it! Lets see what we found...");
		System.out.println();
		System.out.println(response.getBody());

		System.out.println();
		System.out
				.println("Thats it man! Go and build something awesome with Scribe! :)");
	}

}
