package com.codexperiments.newsroot.manager.twitter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

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

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.j256.ormlite.dao.Dao;

public class TwitterManager
{
    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final String REQUEST_URL = "oauth/request_token";
    private static final String ACCESS_URL = "/oauth/access_token";
    private static final String AUTHORIZE_URL = "/oauth/authorize";
    private static final String API_HOME_TIMELINE = "/1.1/statuses/home_timeline.json";

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
    private Dao<Tweet, Integer> mTweetDao;
    private Dao<Timeline, Integer> mTimelineDao;
    private Dao<TimeGap, Integer> mTimeGapDao;

    private String mId;
    private String mScreenName;
    private boolean mAuthorized;

    private Timeline mTimeline;

    public TwitterManager(Application pApplication, TwitterDatabase pDatabase, Config pConfig)
    {
        super();
        mConfig = pConfig;
        mPreferences = pApplication.getSharedPreferences(PREF_NAME, 0);

        mJSONFactory = new JsonFactory();
        mTweetDao = pDatabase.getTweetDao();
        mTimelineDao = pDatabase.getTimelineDao();

        mId = mPreferences.getString(PREF_USER_ID, null);
        mScreenName = mPreferences.getString(PREF_USER_SCREEN_NAME, null);
        checkAuthorization();

        mTimeline = new Timeline();
    }

    public void checkAuthorization()
    {
        mConsumer = new DefaultOAuthConsumer(mConfig.getConsumerKey(), mConfig.getConsumerSecret());
        mProvider = new DefaultOAuthProvider(mConfig.getHost() + REQUEST_URL, //
                                             mConfig.getHost() + ACCESS_URL, //
                                             mConfig.getHost() + AUTHORIZE_URL);
        mAuthorized = mPreferences.getBoolean(PREF_USER_AUTHORIZED, false);
        if (mAuthorized) {
            String lToken = mPreferences.getString(PREF_USER_TOKEN, null);
            String lSecret = mPreferences.getString(PREF_USER_SECRET, null);
            mConsumer.setTokenWithSecret(lToken, lSecret);
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
        checkAuthorization();
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
            checkAuthorization();
        } catch (Exception eTwitterException) {
            throw TwitterAuthorizationFailedException.from(eTwitterException);
        }
    }

    public boolean isAuthorized()
    {
        return mAuthorized;
    }

    public List<Tweet> findNewTweets() throws TwitterAccessException
    {
        // StringBuilder lUrl = new StringBuilder(API_HOME_TIMELINE).append("?count=").append(DEFAULT_PAGE_SIZE);
        // if (mTimeline.hasTweets()) {
        // lUrl.append("&since_id=").append(mTimeline.getEarliestId() - 1);
        // }
        //
        // return parseJSON(lUrl, new ParseHandler<List<Tweet>>() {
        // public List<Tweet> parse(JsonParser pParser) throws Exception
        // {
        // final List<Tweet> lTweets = TwitterParser.parseTweetList(pParser);
        // TimeGap lTimeGap = mTimeline.refresh(lTweets); // TODO Concurrent access problem here!
        // // Maybe we have more tweets to load. May happen if application was disconnected for a long time.
        //
        // mTweetDao.callBatchTasks(new Callable<Void>() {
        // public Void call() throws Exception
        // {
        // for (Tweet lTweet : lTweets) {
        // mTweetDao.createIfNotExists(lTweet);
        // }
        // mTimelineDao.createOrUpdate(mTimeline);
        // return null;
        // }
        // });
        //
        // return lTweets;
        // }
        // });
        return null;
    }

    public List<Tweet> findOldTweets() throws TwitterAccessException
    {
        // // TODO Use URLEncodedUtils, trim_user, exclude_replies
        // StringBuilder lUrl = new StringBuilder(API_HOME_TIMELINE).append("?count=").append(DEFAULT_PAGE_SIZE);
        // if (mTimeline.hasTweets()) {
        // lUrl.append("&max_id=").append(mTimeline.getOldestId() - 1);
        // }
        //
        // return parseJSON(lUrl, new ParseHandler<List<Tweet>>() {
        // public List<Tweet> parse(JsonParser pParser) throws Exception
        // {
        // final List<Tweet> lTweets = TwitterParser.parseTweetList(pParser);
        // mTimeline.refresh(lTweets); // TODO Concurrent access problem here!
        //
        // mTweetDao.callBatchTasks(new Callable<Void>() {
        // public Void call() throws Exception
        // {
        // for (Tweet lTweet : lTweets) {
        // mTweetDao.createIfNotExists(lTweet);
        // }
        // mTimelineDao.createOrUpdate(mTimeline);
        // return null;
        // }
        // });
        //
        // return lTweets;
        // }
        // });
        return null;
    }

    public List<Tweet> findTweets(final TimeGap pTimeGap) throws TwitterAccessException
    {
        // TODO Use URLEncodedUtils, trim_user, exclude_replies
        StringBuilder lUrl = new StringBuilder(mConfig.getHost()).append(API_HOME_TIMELINE)
                                                                 .append("?count=")
                                                                 .append(DEFAULT_PAGE_SIZE);
        if (pTimeGap.hasEarliestBound()) {
            lUrl.append("&max_id=").append(pTimeGap.getEarliestId() - 1);
        }
        if (pTimeGap.hasOldestBound()) {
            lUrl.append("&since_id=").append(pTimeGap.getOldestId());
        }

        return parseJSON(lUrl, new ParseHandler<List<Tweet>>() {
            public List<Tweet> parse(JsonParser pParser) throws Exception
            {
                final List<Tweet> lTweets = TwitterParser.parseTweetList(pParser);
                final TimeGap lRemainingTimeGap = pTimeGap.substract(lTweets, DEFAULT_PAGE_SIZE);

                mTweetDao.callBatchTasks(new Callable<Void>() {
                    public Void call() throws Exception
                    {
                        for (Tweet lTweet : lTweets) {
                            mTweetDao.createIfNotExists(lTweet);
                        }

                        mTimeGapDao.delete(pTimeGap);
                        if (lRemainingTimeGap == null) {
                            mTimeGapDao.create(lRemainingTimeGap);
                        }
                        return null;
                    }
                });
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
        String getHost();

        String getConsumerKey();

        String getConsumerSecret();

        String getCallbackURL();
    }
}
