package com.knowprocess.twitter;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.knowprocess.core.internal.BaseUserAwareTask;

/**
 * Simple service task to send a tweet.
 *
 *
 * @author Tim Stephenson
 */
public class TweetTask extends BaseUserAwareTask implements JavaDelegate {

    protected Expression consumerKey;
    protected Expression consumerSecret;
    protected Expression accessToken;
    protected Expression accessSecret;

    public void tweet(String key, String secret, String accessToken,
            String accessSecret, String msg) {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(key)
                .setOAuthConsumerSecret(secret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        if (!twitter.getAuthorization().isEnabled()) {
            System.err.println("OAuth consumer key/secret is not set.");
        }

        try {
            Status status = twitter.updateStatus(msg);
            System.out.println("Successfully updated the status to ["
                    + status.getText() + "].");
        } catch (TwitterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ActivitiException("Unable to send tweet", e);
        }
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String usr = getUsername(execution);
        tweet((String) lookup(execution, usr, consumerKey),
                (String) lookup(execution, usr, consumerSecret),
                (String) lookup(execution, usr, accessToken),
                (String) lookup(execution, usr, accessSecret),
                (String) execution.getVariable("tweet"));

    }

}
