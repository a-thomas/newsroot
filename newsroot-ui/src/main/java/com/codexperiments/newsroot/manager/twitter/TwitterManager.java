package com.codexperiments.newsroot.manager.twitter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.repository.twitter.TwitterQuery;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public class TwitterManager {
    private static final String PREF_NAME = "com_codexperiments_newsroot_twittermanager";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_SCREEN_NAME = "user_screen_name";
    private static final String PREF_USER_TOKEN = "user_token";
    private static final String PREF_USER_SECRET = "user_secret";
    private static final String PREF_USER_AUTHORIZED = "user_authorized";

    private Config mConfig;
    private SharedPreferences mPreferences;
    private EventBus mEventBus;
    private JsonFactory mJSONFactory;
    private OAuthConsumer mConsumer;
    private OAuthProvider mProvider;
    private Set<TweetListener> mListeners;

    public interface TweetListener {
        void onNewsLoaded(List<News> pItems);
    }

    public void register(TweetListener pTweetListener) {
        mListeners.add(pTweetListener);
    }

    public void unregister(TweetListener pTweetListener) {
        mListeners.remove(pTweetListener);
    }

    // private String mId;
    // private String mScreenName;
    private boolean mAuthorized;

    public TwitterManager(Application pApplication, EventBus pEventBus, Config pConfig) {
        super();
        mConfig = pConfig;
        mPreferences = pApplication.getSharedPreferences(PREF_NAME, 0);
        mEventBus = pEventBus;
        mEventBus.registerListener(this);
        mJSONFactory = new JsonFactory();
        mListeners = new HashSet<TwitterManager.TweetListener>();

        // mId = mPreferences.getString(PREF_USER_ID, null);
        // mScreenName = mPreferences.getString(PREF_USER_SCREEN_NAME, null);
        checkAuthorization();
    }

    public void checkAuthorization() {
        mConsumer = new DefaultOAuthConsumer(mConfig.getConsumerKey(), mConfig.getConsumerSecret());
        mProvider = new DefaultOAuthProvider(mConfig.getHost() + "oauth/request_token",
                                             mConfig.getHost() + "oauth/access_token",
                                             mConfig.getHost() + "oauth/authorize");
        mAuthorized = mPreferences.getBoolean(PREF_USER_AUTHORIZED, false);
        if (mAuthorized) {
            String lToken = mPreferences.getString(PREF_USER_TOKEN, null);
            String lSecret = mPreferences.getString(PREF_USER_SECRET, null);
            mConsumer.setTokenWithSecret(lToken, lSecret);
        }
    }

    public void deauthorize() {
        mPreferences.edit()
                    .putString(PREF_USER_ID, null)
                    .putString(PREF_USER_SCREEN_NAME, null)
                    .putString(PREF_USER_TOKEN, null)
                    .putString(PREF_USER_SECRET, null)
                    .putBoolean(PREF_USER_AUTHORIZED, false)
                    .commit();
        checkAuthorization();
    }

    public TwitterAuthorizationCallback requestAuthorization() throws TwitterAuthorizationFailedException {
        deauthorize();
        try {
            String lAuthorizationUrl = mProvider.retrieveRequestToken(mConsumer, mConfig.getCallbackURL());
            return new TwitterAuthorizationCallback(lAuthorizationUrl, mConfig.getCallbackURL());
        } catch (Exception eTwitterException) {
            throw TwitterAuthorizationFailedException.from(eTwitterException);
        }
    }

    public void confirmAuthorization(Uri pUri) throws TwitterAuthorizationFailedException {
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
            checkAuthorization();
        } catch (Exception eTwitterException) {
            throw TwitterAuthorizationFailedException.from(eTwitterException);
        }
    }

    public boolean isAuthorized() {
        return mAuthorized;
    }

    public <TResult> TResult query(TwitterQuery pQuery, QueryHandler<TResult> pQueryHandler) throws TwitterAccessException {
        JsonParser lParser = null;
        HttpURLConnection lRequest = null;
        InputStream lInputStream = null;
        try {
            URL lUrl = new URL(pQuery.toString());
            Log.e(TwitterManager.class.getSimpleName(), lUrl.toString());
            lRequest = (HttpURLConnection) lUrl.openConnection();
            lRequest.setDoInput(true);
            lRequest.setDoOutput(false);

            mConsumer.sign(lRequest);
            lRequest.connect();
            int lStatusCode = lRequest.getResponseCode();
            if (lStatusCode != 200) throw new IOException();
            // TODO 429, etc.

            lInputStream = new BufferedInputStream(lRequest.getInputStream());
            lParser = mJSONFactory.createParser(lInputStream);
            return pQueryHandler.parse(pQuery, lParser);
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

    public interface QueryHandler<TResult> {
        TResult parse(TwitterQuery pQuery, JsonParser pParser) throws Exception;
    }

    public interface Config {
        String getHost();

        String getConsumerKey();

        String getConsumerSecret();

        String getCallbackURL();
    }
}
