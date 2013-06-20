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
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;

import com.codexperiments.newsroot.domain.Tweet;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public class TwitterManager
{
    private static final String REQUEST_URL = "https://api.twitter.com/oauth/request_token";
    private static final String ACCESS_URL = "https://api.twitter.com/oauth/access_token";
    private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
    private static final URL API_HOME_TIMELINE;

    private static final String PREF_NAME = "com_codexperiments_newsroot_twittermanager";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_SCREEN_NAME = "user_screen_name";
    private static final String PREF_USER_TOKEN = "user_token";
    private static final String PREF_USER_SECRET = "user_secret";
    private static final String PREF_USER_AUTHORIZED = "user_authorized";

    static {
        try {
            API_HOME_TIMELINE = new URL("https://api.twitter.com/1.1/statuses/home_timeline.json");
        } catch (MalformedURLException eMalformedURLException) {
            throw new IllegalStateException();
        }
    }

    private Config mConfig;
    private SharedPreferences mPreferences;
    private Twitter mTwitter;
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

        mTwitter = TwitterFactory.getSingleton();
        mTwitter.setOAuthConsumer(mConfig.getConsumerKey(), mConfig.getConsumerSecret());
        mAuthorized = mPreferences.getBoolean(PREF_USER_AUTHORIZED, false);
        mJSONFactory = new JsonFactory();

        mConsumer = new DefaultOAuthConsumer(mConfig.getConsumerKey(), mConfig.getConsumerSecret());
        mProvider = new DefaultOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);
        if (mAuthorized) {
            String lToken = mPreferences.getString(PREF_USER_TOKEN, null);
            String lSecret = mPreferences.getString(PREF_USER_SECRET, null);
            mTwitter.setOAuthAccessToken(new AccessToken(lToken, lSecret));
            mConsumer.setTokenWithSecret(lToken, lSecret);
        }
    }

    public TwitterAuthorizationCallback requestAuthorization() throws TwitterAuthorizationFailedException
    {
        deauthorize();
        try {
            // mRequestToken = null; // mTwitter.getOAuthRequestToken(mConfig.getCallbackURL());
            final String lAuthorizationUrl = mProvider.retrieveRequestToken(mConsumer, mConfig.getCallbackURL());
            // Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
            // | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_FROM_BACKGROUND);
            // mContext.startActivity(intent);
            return new TwitterAuthorizationCallback(/* mRequestToken.getAuthorizationURL() */lAuthorizationUrl,
                                                    mConfig.getCallbackURL());
        } catch (/* Twitter */Exception eTwitterException) {
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
            // AccessToken lAccessToken = mTwitter.getOAuthAccessToken(mRequestToken, lVerifier);
            // mPreferences.edit()
            // .putString(PREF_USER_ID, Long.toString(lAccessToken.getUserId()))
            // .putString(PREF_USER_SCREEN_NAME, lAccessToken.getScreenName())
            // .putString(PREF_USER_TOKEN, lAccessToken.getToken())
            // .putString(PREF_USER_SECRET, lAccessToken.getTokenSecret())
            // .putBoolean(PREF_USER_AUTHORIZED, true)
            // .commit();
            mProvider.retrieveAccessToken(mConsumer, lVerifier);

            mPreferences.edit()
                        .putString(PREF_USER_ID, "test")
                        .putString(PREF_USER_SCREEN_NAME, "screenname")
                        .putString(PREF_USER_TOKEN, mConsumer.getToken())
                        .putString(PREF_USER_SECRET, mConsumer.getTokenSecret())
                        .putBoolean(PREF_USER_AUTHORIZED, true)
                        .commit();
            mAuthorized = true;
            // } catch (TwitterException eTwitterException) {
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

        mTwitter.setOAuthAccessToken(null);
        mTwitter.shutdown();
        mTwitter = TwitterFactory.getSingleton();
    }

    public boolean isAuthorized()
    {
        return mAuthorized;
    }

    public List<Tweet> getTweets(Paging pPaging) throws TwitterAccessException
    {
        return parseJSON(new ParseHandler<List<Tweet>>() {
            @Override
            public List<Tweet> parse(JsonParser pParser) throws Exception
            {
                return TwitterParser.parseTweetList(pParser);
            }
        });
    }

    private <TResult> TResult parseJSON(ParseHandler<TResult> pParseHandler) throws TwitterAccessException
    {
        JsonParser lParser = null;
        HttpURLConnection lRequest = null;
        InputStream lInputStream = null;
        try {
            lRequest = (HttpURLConnection) API_HOME_TIMELINE.openConnection();
            lRequest.setDoInput(true);
            lRequest.setDoOutput(false);

            mConsumer.sign(lRequest);
            lRequest.connect();
            int lStatusCode = lRequest.getResponseCode();
            if (lStatusCode != 200) throw new IOException();

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
