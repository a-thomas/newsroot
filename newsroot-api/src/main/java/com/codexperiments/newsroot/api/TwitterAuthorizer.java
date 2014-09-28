package com.codexperiments.newsroot.api;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import static com.codexperiments.newsroot.api.AuthorizationDeniedException.authorizationDenied;
import static com.codexperiments.newsroot.api.AuthorizationFailedException.authorizationFailed;
import static com.google.common.base.Strings.isNullOrEmpty;

public class TwitterAuthorizer {
    private final String endpoint;
    private final String applicationKey;
    private final String applicationSecret;

    private OAuthProvider provider;
    private OAuthConsumer consumer;

    private Callback callback = null;
    private String expectedCallbackUri;

    public TwitterAuthorizer(String endpoint, String applicationKey, String applicationSecret) {
        provider = new DefaultOAuthProvider(endpoint + "/oauth/request_token", endpoint + "/oauth/access_token", endpoint + "/oauth/authorize");
        consumer = new DefaultOAuthConsumer(applicationKey, applicationSecret);

        this.endpoint = endpoint;
        this.applicationKey = applicationKey;
        this.applicationSecret = applicationSecret;
    }

    public void sign(HttpURLConnection connection) throws IOException {
        try {
            consumer.sign(connection);
        } catch (OAuthException oauthException) {
            throw new IOException(oauthException);
        }
    }

    public void authorize(String userToken, String userSecret) {
        if (!isNullOrEmpty(userToken) && !isNullOrEmpty(userSecret)) {
            consumer.setTokenWithSecret(userToken, userSecret);
        }
    }

    public void deauthorize() {
        provider = new DefaultOAuthProvider(endpoint + "/oauth/request_token", endpoint + "/oauth/access_token", endpoint + "/oauth/authorize");
        consumer = new DefaultOAuthConsumer(applicationKey, applicationSecret);
// TODO        notifyCredentialChanged(userToken, userSecret);
    }

    public String requestAuthorization(Callback callback, String expectedCallbackUri) throws AuthorizationFailedException {
        if (isNullOrEmpty(expectedCallbackUri)) throw new IllegalStateException("Callback Uri must be null or empty");
        this.callback = callback; // TODO

        deauthorize();
        try {
            this.expectedCallbackUri = expectedCallbackUri;
            return provider.retrieveRequestToken(consumer, expectedCallbackUri);
        } catch (OAuthException oauthException) {
            throw authorizationFailed(oauthException);
        }
    }

    public void confirmAuthorization(String providedCallbackUri) throws AuthorizationFailedException, AuthorizationDeniedException {
        if (expectedCallbackUri == null) throw new IllegalStateException("Authorization not requested");
        if (providedCallbackUri == null || !providedCallbackUri.contains(expectedCallbackUri)) {
            throw new IllegalArgumentException(String.format("Invalid callback URI %s", providedCallbackUri));
        }

        Map<String, String> uriParams = getQueryParams(providedCallbackUri);
        String verifier = uriParams.get("oauth_verifier");
        if (isNullOrEmpty(verifier)) {
            if (uriParams.containsKey("denied")) throw authorizationDenied();
            else throw authorizationFailed(String.format("Invalid oauth_verifier parameter in URI %s", providedCallbackUri));
        }

        try {
            provider.retrieveAccessToken(consumer, verifier);
            String userToken = consumer.getToken();
            String userSecret = consumer.getTokenSecret();
            notifyCredentialChanged(userToken, userSecret);
        } catch (OAuthException oauthException) {
            throw authorizationFailed(oauthException);
        }
    }

    private void notifyCredentialChanged(String consumerKey, String consumerSecret) {
        if (callback != null) callback.onCredentialChanged(consumerKey, consumerSecret);
    }

    // TOOD Optimize
    private static Map<String, String> getQueryParams(String url) {
        try {
            Map<String, String> params = new HashMap<>();
            String[] urlParts = url.split("\\?");
            if (urlParts.length > 1) {
                String query = urlParts[1];
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    String key = URLDecoder.decode(pair[0], "UTF-8");
                    String value = "";
                    if (pair.length > 1) {
                        value = URLDecoder.decode(pair[1], "UTF-8");
                    }
                    params.put(key, value);
                }
            }
            return params;
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    public interface Callback {
        void onCredentialChanged(String consumerKey, String consumerSecret);
    }
}
