package com.codexperiments.newsroot.test.data;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.domain.twitter.TweetPage;

public class TweetPageData {
    public static final int SIZE_TweetPage = 20;

    public static final long EARLIEST_TweetPage_02_1 = 349497246842241000L;
    public static final long OLDEST_TweetPage_02_1 = 349473806655565800L;

    public static final long EARLIEST_TweetPage_02_2 = 349471049735344100L;
    public static final long OLDEST_TweetPage_02_2 = 349457499134504960L;

    public static final long EARLIEST_TweetPage_02_3 = 349452667745087500L;
    public static final long OLDEST_TweetPage_02_3 = 349443896905965600L;

    public static void checkTweetPage_empty(TweetPage pTweetPage, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPage.tweets();
        assertThat(lTweets, hasSize(0));
        assertThat(pTweetPage.timeGap(), equalTo(pTimeGap));
        assertThat(pTweetPage.remainingGap(), nullValue());
    }

    public static void checkTweetPage_02_1(TweetPage pTweetPage, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPage.tweets();
        assertThat(lTweets, hasSize(20));
        assertThat(pTweetPage.timeGap(), equalTo(pTimeGap));
        assertThat(pTweetPage.remainingGap(), equalTo(pTimeGap.substract(lTweets, 20)));

        Tweet lFirstTweet = pTweetPage.tweets().get(0);
        assertThat(lFirstTweet.getId(), equalTo(EARLIEST_TweetPage_02_1));

        Tweet lLastTweet = pTweetPage.tweets().get(19);
        assertThat(lLastTweet.getId(), equalTo(OLDEST_TweetPage_02_1));
    }

    public static void checkTweetPage_02_2(TweetPage pTweetPage, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPage.tweets();
        assertThat(lTweets, hasSize(20));
        assertThat(pTweetPage.timeGap(), equalTo(pTimeGap));
        assertThat(pTweetPage.remainingGap(), equalTo(pTimeGap.substract(lTweets, 20)));

        Tweet lFirstTweet = pTweetPage.tweets().get(0);
        assertThat(lFirstTweet.getId(), equalTo(EARLIEST_TweetPage_02_2));

        Tweet lLastTweet = pTweetPage.tweets().get(19);
        assertThat(lLastTweet.getId(), equalTo(OLDEST_TweetPage_02_2));
    }

    public static void checkTweetPage_02_3(TweetPage pTweetPage, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPage.tweets();
        assertThat(pTweetPage.tweets(), hasSize(19));
        assertThat(pTweetPage.timeGap(), equalTo(pTimeGap));
        assertThat(pTweetPage.remainingGap(), nullValue());

        Tweet lFirstTweet = lTweets.get(0);
        assertThat(lFirstTweet.getId(), equalTo(EARLIEST_TweetPage_02_3));
        Tweet lLastTweet = lTweets.get(18);
        assertThat(lLastTweet.getId(), equalTo(OLDEST_TweetPage_02_3));
    }
}
