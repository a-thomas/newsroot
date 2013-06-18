package com.codexperiments.newsroot.manager;

import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;

public class TwitterManager
{
    private static final String PREF_NAME = "com_codexperiments_newsroot_twittermanager";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_PSEUDO = "user_pseudo";
    private static final String PREF_USER_TOKEN = "user_token";
    private static final String PREF_USER_SECRET = "user_secret";
    private static final String PREF_USER_AUTHORIZED = "user_authorized";

    private Config mConfig;
    private SharedPreferences mPreferences;
    private Twitter mTwitter;

    private String mId;
    private String mPseudo;
    private RequestToken mRequestToken;
    private boolean mAuthorized;

    public TwitterManager(Application pApplication, Config pConfig)
    {
        super();
        mConfig = pConfig;
        mPreferences = pApplication.getSharedPreferences(PREF_NAME, 0);

        mId = mPreferences.getString(PREF_USER_ID, null);
        mPseudo = mPreferences.getString(PREF_USER_PSEUDO, null);

        mTwitter = TwitterFactory.getSingleton();
        mTwitter.setOAuthConsumer(mConfig.getConsumerKey(), mConfig.getConsumerSecret());
        mAuthorized = mPreferences.getBoolean(PREF_USER_AUTHORIZED, false);
        if (mAuthorized) {
            String lToken = mPreferences.getString(PREF_USER_TOKEN, null);
            String lSecret = mPreferences.getString(PREF_USER_SECRET, null);
            mTwitter.setOAuthAccessToken(new AccessToken(lToken, lSecret));
        }
    }

    public TwitterAuthorizationCallback requestAuthorization() throws TwitterAuthorizationFailedException
    {
        deauthorize();
        try {
            mRequestToken = mTwitter.getOAuthRequestToken(mConfig.getCallbackURL());
        } catch (TwitterException eTwitterException) {
            throw TwitterAuthorizationFailedException.authorizationFailed(eTwitterException);
        }
        return new TwitterAuthorizationCallback(mRequestToken.getAuthorizationURL(), mConfig.getCallbackURL());
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
            AccessToken lAccessToken = mTwitter.getOAuthAccessToken(mRequestToken, lVerifier);
            mPreferences.edit()
                        .putString(PREF_USER_ID, Long.toString(lAccessToken.getUserId()))
                        .putString(PREF_USER_PSEUDO, lAccessToken.getScreenName())
                        .putString(PREF_USER_TOKEN, lAccessToken.getToken())
                        .putString(PREF_USER_SECRET, lAccessToken.getTokenSecret())
                        .putBoolean(PREF_USER_AUTHORIZED, true)
                        .commit();
            mAuthorized = true;
        } catch (TwitterException eTwitterException) {
            throw TwitterAuthorizationFailedException.authorizationFailed(eTwitterException);
        }
    }

    public void deauthorize()
    {
        mPreferences.edit()
                    .putString(PREF_USER_ID, null)
                    .putString(PREF_USER_PSEUDO, null)
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

    public List<Status> getTweets() throws TwitterException
    {
        return mTwitter.getHomeTimeline();
    }


    public interface Config
    {
        String getConsumerKey();

        String getConsumerSecret();

        String getCallbackURL();
    }
}
