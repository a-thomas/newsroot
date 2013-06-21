package com.codexperiments.newsroot.manager.twitter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.codexperiments.newsroot.domain.twitter.Page;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public class TwitterManager
{
    private static final String REQUEST_URL = "https://api.twitter.com/oauth/request_token";
    private static final String ACCESS_URL = "https://api.twitter.com/oauth/access_token";
    private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
    private static final String API_HOME_TIMELINE = "https://api.twitter.com/1.1/statuses/user_timeline.json";

    private static final String PREF_NAME = "com_codexperiments_newsroot_twittermanager";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_SCREEN_NAME = "user_screen_name";
    private static final String PREF_USER_TOKEN = "user_token";
    private static final String PREF_USER_SECRET = "user_secret";
    private static final String PREF_USER_AUTHORIZED = "user_authorized";

    private Config mConfig;
    private SharedPreferences mPreferences;
    private OAuthConsumer mConsumer;
    private OAuthProvider mProvider;

    private JsonFactory mJSONFactory;

    private String mId;
    private String mScreenName;
    private boolean mAuthorized;

    public TwitterManager(Application pApplication, Config pConfig)
    {
        super();
        mConfig = pConfig;
        mPreferences = pApplication.getSharedPreferences(PREF_NAME, 0);

        mId = mPreferences.getString(PREF_USER_ID, null);
        mScreenName = mPreferences.getString(PREF_USER_SCREEN_NAME, null);

        mAuthorized = mPreferences.getBoolean(PREF_USER_AUTHORIZED, false);
        mJSONFactory = new JsonFactory();

        mConsumer = new DefaultOAuthConsumer(mConfig.getConsumerKey(), mConfig.getConsumerSecret());
        mProvider = new DefaultOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);
        if (mAuthorized) {
            String lToken = mPreferences.getString(PREF_USER_TOKEN, null);
            String lSecret = mPreferences.getString(PREF_USER_SECRET, null);
            mConsumer.setTokenWithSecret(lToken, lSecret);
        }
    }

    public TwitterAuthorizationCallback requestAuthorization() throws TwitterAuthorizationFailedException
    {
        deauthorize();
        try {
            String lAuthorizationUrl = mProvider.retrieveRequestToken(mConsumer, mConfig.getCallbackURL());
            return new TwitterAuthorizationCallback(lAuthorizationUrl, mConfig.getCallbackURL());
        } catch (Exception eTwitterException) {
            throw TwitterAuthorizationFailedException.from(eTwitterException);
        }
    }

    public void confirmAuthorization(Uri pUri) throws TwitterAuthorizationFailedException
    {
        if ((pUri == null) || !pUri.isHierarchical() || (!pUri.toString().startsWith(mConfig.getCallbackURL()))) {
            throw TwitterAuthorizationFailedException.illegalCallbackUrl(pUri);
        } else if (pUri.getQueryParameter("denied") != null) {
            throw TwitterAuthorizationDeniedException.authorizationDenied();
        }

        try {
            String lVerifier = pUri.getQueryParameter("oauth_verifier");
            mProvider.retrieveAccessToken(mConsumer, lVerifier);

            mPreferences.edit()
                        .putString(PREF_USER_ID, "test")
                        .putString(PREF_USER_SCREEN_NAME, "screenname")
                        .putString(PREF_USER_TOKEN, mConsumer.getToken())
                        .putString(PREF_USER_SECRET, mConsumer.getTokenSecret())
                        .putBoolean(PREF_USER_AUTHORIZED, true)
                        .commit();
            mAuthorized = true;
        } catch (Exception eTwitterException) {
            throw TwitterAuthorizationFailedException.from(eTwitterException);
        }
    }

    public void deauthorize()
    {
        mPreferences.edit()
                    .putString(PREF_USER_ID, null)
                    .putString(PREF_USER_SCREEN_NAME, null)
                    .putString(PREF_USER_TOKEN, null)
                    .putString(PREF_USER_SECRET, null)
                    .putBoolean(PREF_USER_AUTHORIZED, false)
                    .commit();

        mConsumer = new DefaultOAuthConsumer(mConfig.getConsumerKey(), mConfig.getConsumerSecret());
        mProvider = new DefaultOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);
    }

    public boolean isAuthorized()
    {
        return mAuthorized;
    }

    public List<Tweet> getOlderTweets(final Page pPage) throws TwitterAccessException
    {
        // TODO Use URLEncodedUtils, trim_user, exclude_replies
        StringBuilder lUrl = new StringBuilder(API_HOME_TIMELINE).append("?count=").append(pPage.getSliceCount());
        if (pPage.hasTweets()) {
            lUrl.append("&max_id=").append(pPage.getOldestId() - 1);
        }
        // TODO if (pPage.hasSinceId()) {
        // lUrl.append("&since_id=").append(pPage.getSinceId());
        // }

        return parseJSON(lUrl, new ParseHandler<List<Tweet>>() {
            public List<Tweet> parse(JsonParser pParser) throws Exception
            {
                List<Tweet> lTweets = TwitterParser.parseTweetList(pParser);
                pPage.refreshFrom(lTweets);
                return lTweets;
            }
        });
    }

    private <TResult> TResult parseJSON(StringBuilder pUrlBuilder, ParseHandler<TResult> pParseHandler)
                    throws TwitterAccessException
    {
        JsonParser lParser = null;
        HttpURLConnection lRequest = null;
        InputStream lInputStream = null;
        try {
            URL lUrl = new URL(pUrlBuilder.toString());
            Log.e(TwitterManager.class.getSimpleName(), lUrl.toString());
            lRequest = (HttpURLConnection) lUrl.openConnection();
            lRequest.setDoInput(true);
            lRequest.setDoOutput(false);

            mConsumer.sign(lRequest);
            lRequest.connect();
            int lStatusCode = lRequest.getResponseCode();
            if (lStatusCode != 200) throw new IOException();
            // TODO 429

            lInputStream = new BufferedInputStream(lRequest.getInputStream());
            lParser = mJSONFactory.createParser(lInputStream);
            return pParseHandler.parse(lParser);
        } catch (MalformedURLException eMalformedURLException) {
            throw TwitterAccessException.from(eMalformedURLException);
        } catch (IOException eIOException) {
            throw TwitterAccessException.from(eIOException);
        } catch (OAuthMessageSignerException eOAuthMessageSignerException) {
            throw TwitterAccessException.from(eOAuthMessageSignerException);
        } catch (OAuthExpectationFailedException eOAuthExpectationFailedException) {
            throw TwitterAccessException.from(eOAuthExpectationFailedException);
        } catch (OAuthCommunicationException eOAuthCommunicationException) {
            throw TwitterAccessException.from(eOAuthCommunicationException);
        } catch (Exception eException) {
            throw TwitterAccessException.from(eException);
        } finally {
            try {
                if (lParser != null) lParser.close();
            } catch (IOException eIOException) {
                eIOException.printStackTrace();
            }
            try {
                if (lInputStream != null) lInputStream.close();
            } catch (IOException eIOException) {
                eIOException.printStackTrace();
            }
            if (lRequest != null) lRequest.disconnect();
        }
    }


    private interface ParseHandler<TResult>
    {
        TResult parse(JsonParser pParser) throws Exception;
    }


    public interface Config
    {
        String getConsumerKey();

        String getConsumerSecret();

        String getCallbackURL();
    }
}
