package com.codexperiments.newsroot.manager.tweet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.codexperiments.newsroot.repository.tweet.TweetQuery;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

public class TweetManager {
    private static final String PREF_NAME = "com_codexperiments_newsroot_twittermanager";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_SCREEN_NAME = "user_screen_name";
    private static final String PREF_USER_TOKEN = "user_token";
    private static final String PREF_USER_SECRET = "user_secret";
    private static final String PREF_USER_AUTHORIZED = "user_authorized";

    private Config mConfig;
    private SharedPreferences mPreferences;
    private JsonFactory mJSONFactory;
    private OAuthConsumer mConsumer;
    private OAuthProvider mProvider;

    private String mId;
    private String mScreenName;
    private boolean mAuthorized;

    public TweetManager(Context pContext, Config pConfig) {
        super();
        mConfig = pConfig;
        mPreferences = pContext.getSharedPreferences(PREF_NAME, 0);
        mJSONFactory = new JsonFactory();

        checkAuthorization();
    }

    public void checkAuthorization() {
        mConsumer = new DefaultOAuthConsumer(mConfig.getConsumerKey(), mConfig.getConsumerSecret());
        mProvider = new DefaultOAuthProvider(mConfig.getHost() + "oauth/request_token",
                                             mConfig.getHost() + "oauth/access_token",
                                             mConfig.getHost() + "oauth/authorize");
        mAuthorized = mPreferences.getBoolean(PREF_USER_AUTHORIZED, false);
        if (!mAuthorized) {
            mPreferences.edit()
                        .putString(PREF_USER_TOKEN, "1272413971-6yoFhYjcCg7EJbYXIQNWv9wRh8vmPTRRwt29NXD")
                        .putString(PREF_USER_SECRET, "341L0ANySNsvlhSVIj823SYN6j7oILuhRDvW8h7gDs")
                        .putBoolean(PREF_USER_AUTHORIZED, true)
                        .apply();
        }
        mAuthorized = mPreferences.getBoolean(PREF_USER_AUTHORIZED, false);
        if (mAuthorized) {
            mId = mPreferences.getString(PREF_USER_ID, null);
            mScreenName = mPreferences.getString(PREF_USER_SCREEN_NAME, null);
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

    public TweetAuthorizationCallback requestAuthorization() throws TweetAuthorizationFailedException {
        deauthorize();
        try {
            String lAuthorizationUrl = mProvider.retrieveRequestToken(mConsumer, mConfig.getCallbackURL());
            return new TweetAuthorizationCallback(lAuthorizationUrl, mConfig.getCallbackURL());
        } catch (Exception eTweetException) {
            throw TweetAuthorizationFailedException.from(eTweetException);
        }
    }

    public void confirmAuthorization(Uri pUri) throws TweetAuthorizationFailedException {
        if ((pUri == null) || !pUri.isHierarchical() || (!pUri.toString().startsWith(mConfig.getCallbackURL()))) {
            throw TweetAuthorizationFailedException.illegalCallbackUrl(pUri);
        } else if (pUri.getQueryParameter("denied") != null) {
            throw TweetAuthorizationDeniedException.authorizationDenied();
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
        } catch (Exception eTweetException) {
            throw TweetAuthorizationFailedException.from(eTweetException);
        }
    }

    public String getId() {
        return mId;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public boolean isAuthorized() {
        return mAuthorized;
    }

    public <TResult> TResult query(TweetQuery pQuery, QueryHandler<TResult> pQueryHandler) throws TweetAccessException {
        JsonParser lParser = null;
        HttpURLConnection lRequest = null;
        InputStream lInputStream = null;
        try {
            URL lUrl = new URL(pQuery.toString());
            Log.e(TweetManager.class.getSimpleName(), lUrl.toString());
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
            throw TweetAccessException.from(eMalformedURLException);
        } catch (IOException eIOException) {
            throw TweetAccessException.from(eIOException);
        } catch (OAuthMessageSignerException eOAuthMessageSignerException) {
            throw TweetAccessException.from(eOAuthMessageSignerException);
        } catch (OAuthExpectationFailedException eOAuthExpectationFailedException) {
            throw TweetAccessException.from(eOAuthExpectationFailedException);
        } catch (OAuthCommunicationException eOAuthCommunicationException) {
            throw TweetAccessException.from(eOAuthCommunicationException);
        } catch (Exception eException) {
            throw TweetAccessException.from(eException);
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

    public Observable<HttpURLConnection> connect(final Observable<String> pUrls) {
        return Observable.create(new OnSubscribeFunc<HttpURLConnection>() {
            public Subscription onSubscribe(final Observer<? super HttpURLConnection> pConnectionObserver) {
                final Subscription lParentSubscription = pUrls.subscribe(new Observer<String>() {
                    public void onNext(String pUrl) {
                        JsonParser lParser = null;
                        HttpURLConnection lRequest = null;
                        InputStream lInputStream = null;
                        try {
                            URL lUrl = new URL(pUrl);
                            Log.e(TweetManager.class.getSimpleName(), lUrl.toString());
                            lRequest = (HttpURLConnection) lUrl.openConnection();
                            lRequest.setDoInput(true);
                            lRequest.setDoOutput(false);

                            mConsumer.sign(lRequest);
                            lRequest.connect();
                            int lStatusCode = lRequest.getResponseCode();
                            if (lStatusCode != 200) throw new IOException();
                            // TODO 429, etc.

                            pConnectionObserver.onNext(lRequest);
                        } catch (Exception eException) {
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
                            pConnectionObserver.onError(eException);
                        }
                    }

                    public void onCompleted() {
                        pConnectionObserver.onCompleted();
                    }

                    public void onError(Throwable pThrowable) {
                        pConnectionObserver.onError(pThrowable);
                    }
                });
                // return Subscriptions.create(new Action0() {
                // public void call() {
                // lParentSubscription.unsubscribe();
                // }
                // });
                return lParentSubscription;
            }
        });
    }

    public interface QueryHandler<TResult> {
        TResult parse(TweetQuery pQuery, JsonParser pParser) throws Exception;
    }

    public interface Config {
        String getHost();

        String getConsumerKey();

        String getConsumerSecret();

        String getCallbackURL();
    }
}
