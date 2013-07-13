package com.knowprocess.collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.LinkedInApi;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class OAuthTest {

    private static final String TWITTER_GET = "https://api.twitter.com/1.1/statuses/home_timeline.json";
    private static final String TWITTER_API_SECRET = "n7YbClaprI2mRw2ZSmm7pWt1oFqmQWEHGucoUkymvw";
    private static final String TWITTER_API_KEY = "GkZvOoXYgEjkZ6XRBE9Ow";
    private static final String LINKED_API_KEY = "93b05p37govm";
    private static final String LINKED_API_SECRET = "CScPex2YMcB9s99q";
    private static final String LINKEDIN_GET = "http://api.linkedin.com/v1/people/~/connections:(id,last-name)";
    private static final int LINKEDIN = 0;
    private static final int TWITTER = 1;

    // private static final String

    @Before
    public void setUp() throws Exception {
    }

    @Test
    @Ignore
    // This may be used manually but is not ready for automation
    public void testOne() {
        // OAuthService service = getTwitterService();
        OAuthService service = getLinkedInService();

        Token accessToken = getStoredAccessToken(LINKEDIN);
        // Token accessToken = getNewAccessToken(service);

        OAuthRequest request = new OAuthRequest(Verb.GET,
        // TWITTER_GET
                LINKEDIN_GET);
        service.signRequest(accessToken, request); // the access token from step
                                                   // 4
        Response response = request.send();
        System.out.println(response.getBody());
    }

    private OAuthService getLinkedInService() {
        return new ServiceBuilder().provider(LinkedInApi.class)
                .apiKey(LINKED_API_KEY).apiSecret(LINKED_API_SECRET)
                // callback("http://your_callback_url") // do it interactively
                .build();
    }

    private OAuthService getTwitterService() {
        return new ServiceBuilder().provider(TwitterApi.class)
                .apiKey(TWITTER_API_KEY).apiSecret(TWITTER_API_SECRET)
                // callback("http://your_callback_url") // do it interactively
                .build();
    }

    private Token getStoredAccessToken(int api) {
        Token token = null;
        switch (api) {
        case TWITTER:
            new Token("", "");
            break;
        case LINKEDIN:
            new Token("b127ff41-abef-4057-9ab6-5a3d5cffc495",
                    "2b47822d-45a3-45a2-929d-64a41331e24f");
            break;
        default:
            new Token("", "");
        }
        return token;
    }

    private Token getNewAccessToken(OAuthService service) {
        Token accessToken;
        Token requestToken = service.getRequestToken();
        String authUrl = service.getAuthorizationUrl(requestToken);
        System.out.println("Paste this in browser: " + authUrl);

        // open up standard input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String verifier = null;
        try {
            verifier = br.readLine();
        } catch (IOException ioe) {
            System.out
                    .println("IO error trying to read your verification code!");
            System.exit(1);
        }

        System.out.println("Thanks for the verifier, " + verifier);

        Verifier v = new Verifier(verifier);
        accessToken = service.getAccessToken(requestToken, v); // the
                                                               // requestToken
                                                               // you had
                                                               // from
                                                               // step 2
        System.out.println("Access token to store in the app: ");
        System.out.println("  " + accessToken.getToken());
        System.out.println("  " + accessToken.getSecret());
        return accessToken;
    }

}
