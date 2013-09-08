package com.codexperiments.newsroot.test.repository.twitter;

import static com.codexperiments.newsroot.test.common.TestRx.subscribeAndWait;
import static com.codexperiments.newsroot.test.data.TweetPageData.OLDEST_TweetPage_02_1;
import static com.codexperiments.newsroot.test.data.TweetPageData.OLDEST_TweetPage_02_2;
import static com.codexperiments.newsroot.test.data.TweetPageData.SIZE_TweetPage;
import static com.codexperiments.newsroot.test.server.MockBackendMatchers.hasQueryParam;
import static com.codexperiments.newsroot.test.server.MockBackendMatchers.hasUrl;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.simpleframework.http.Request;

import rx.Observer;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.repository.twitter.TwitterAPI;
import com.codexperiments.newsroot.repository.twitter.TwitterQuery;
import com.codexperiments.newsroot.test.common.BackendTestCase;
import com.codexperiments.newsroot.test.data.TweetPageData;

public class TwitterAPITest extends BackendTestCase {
    private TwitterManager mTwitterManager;
    private TwitterAPI mTwitterAPI;
    private Observer<TweetPage> mTweetPageObserver;

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        mTweetPageObserver = mock(Observer.class, withSettings().verboseLogging());
        mTwitterManager = new TwitterManager(getApplication(), null, new TwitterManager.Config() {
            public String getHost() {
                return "http://localhost:8378/";
            }

            public String getConsumerKey() {
                return "3Ng9QGTB7EpZCHDOIT2jg";
            }

            public String getConsumerSecret() {
                return "OolXzfWdSF6uMdgt2mvLNpDl4HOA1JNlN487LvDUA4";
            }

            public String getCallbackURL() {
                return "oauth://newsroot-callback";
            }
        });
        mTwitterAPI = new TwitterAPI(mTwitterManager, "http://localhost:8378/");
    }

    public void testFindHomeTweets_noPage() throws Exception {
        // Setup
        final int lPageCount = 5;
        final TimeGap lTimeGap = new TimeGap();

        when(getServer().getResponseAsset(argThat(any(Request.class)))).thenReturn("twitter/ctx_tweet_empty.json");

        // Run
        subscribeAndWait(mTwitterAPI.findHomeTweets(lTimeGap, lPageCount), mTweetPageObserver);

        // Verify
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", SIZE_TweetPage),
                                                                   not(hasQueryParam("max_id")))));

        ArgumentCaptor<TweetPage> lTweetPageArgs = ArgumentCaptor.forClass(TweetPage.class);
        Mockito.verify(mTweetPageObserver).onNext(lTweetPageArgs.capture());
        Mockito.verify(mTweetPageObserver).onCompleted();

        TweetPage lTweetPage = lTweetPageArgs.getAllValues().get(0);
        TweetPageData.checkTweetPage_empty(lTweetPage, lTimeGap);

        Mockito.verifyNoMoreInteractions(getServer(), mTweetPageObserver);
    }

    public void testFindHomeTweets_onePage_moreAvailable() throws Exception {
        // Setup
        final int lPageCount = 1;
        final TimeGap lTimeGap = new TimeGap();

        when(getServer().getResponseAsset(argThat(any(Request.class)))).thenReturn("twitter/ctx_tweet_02-1.json");

        // Run
        subscribeAndWait(mTwitterAPI.findHomeTweets(lTimeGap, lPageCount), mTweetPageObserver);

        // Verify
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", SIZE_TweetPage),
                                                                   not(hasQueryParam("max_id")))));

        ArgumentCaptor<TweetPage> lTweetPageArgs = ArgumentCaptor.forClass(TweetPage.class);
        Mockito.verify(mTweetPageObserver).onNext(lTweetPageArgs.capture());
        Mockito.verify(mTweetPageObserver).onCompleted();

        TweetPage lTweetPage = lTweetPageArgs.getAllValues().get(0);
        TweetPageData.checkTweetPage_02_1(lTweetPage, lTimeGap);

        Mockito.verifyNoMoreInteractions(getServer(), mTweetPageObserver);
    }

    public void testFindHomeTweets_severalPages_noMoreAvailable() throws Exception {
        // Setup
        final int lPageCount = 5;
        final TimeGap lTimeGap = new TimeGap();

        when(getServer().getResponseAsset(argThat(any(Request.class)))).thenReturn("twitter/ctx_tweet_02-1.json")
                                                                       .thenReturn("twitter/ctx_tweet_02-2.json")
                                                                       .thenReturn("twitter/ctx_tweet_02-3.json");

        // Run
        subscribeAndWait(mTwitterAPI.findHomeTweets(lTimeGap, lPageCount), mTweetPageObserver);

        // Verify
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", SIZE_TweetPage),
                                                                   not(hasQueryParam("max_id")))));
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", SIZE_TweetPage),
                                                                   hasQueryParam("max_id", OLDEST_TweetPage_02_1 - 1))));
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", SIZE_TweetPage),
                                                                   hasQueryParam("max_id", OLDEST_TweetPage_02_2 - 1))));

        ArgumentCaptor<TweetPage> lTweetPageArgs = ArgumentCaptor.forClass(TweetPage.class);
        Mockito.verify(mTweetPageObserver, times(3)).onNext(lTweetPageArgs.capture());
        Mockito.verify(mTweetPageObserver).onCompleted();

        List<TweetPage> lTweetPages = lTweetPageArgs.getAllValues();
        TweetPageData.checkTweetPage_02_1(lTweetPages.get(0), lTimeGap);
        TweetPageData.checkTweetPage_02_2(lTweetPages.get(1), lTweetPages.get(0).remainingGap());
        TweetPageData.checkTweetPage_02_3(lTweetPages.get(2), lTweetPages.get(1).remainingGap());

        Mockito.verifyNoMoreInteractions(getServer(), mTweetPageObserver);
    }
}
