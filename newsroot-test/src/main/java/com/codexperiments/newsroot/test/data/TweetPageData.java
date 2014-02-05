package com.codexperiments.newsroot.test.data;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.TimeRange;
import com.codexperiments.newsroot.repository.tweet.TweetPageResponse;

public class TweetPageData {
    public static final int PAGE_SIZE = 20;

    public static final long EARLIEST_02_1 = 349497246842241000L;
    public static final long OLDEST_02_1 = 349473806655565800L;

    public static final long EARLIEST_02_2 = 349471049735344100L;
    public static final long OLDEST_02_2 = 349457499134504960L;

    public static final long EARLIEST_02_3 = 349452667745087500L;
    public static final long OLDEST_02_3 = 349443896905965600L;

    public static void checkTweetPageResponse(TweetPageResponse pTweetPageResponse, TweetPageResponse pTweetPageResponseRef) {
        TweetDTO[] lTweets = pTweetPageResponse.tweetPage().tweets();
        TweetDTO[] lTweetRefs = pTweetPageResponseRef.tweetPage().tweets();
        // assertThat(pTweetPageResponse.remainingGap(), equalTo(pTweetPageResponseRef.remainingGap()));
        // assertThat(pTweetPageResponse.initialGap(), equalTo(pTweetPageResponseRef.initialGap()));
        assertThat(lTweets, equalTo(lTweetRefs));
    }

    public static void checkTweetPage_empty(TweetPageResponse pTweetPageResponse, TimeGap pTimeGap) {
        TweetDTO[] lTweets = pTweetPageResponse.tweetPage().tweets();
        assertThat(lTweets.length, equalTo(0));
        // assertThat(pTweetPageResponse.initialGap(), equalTo(pTimeGap));
        // assertThat(pTweetPageResponse.remainingGap(), nullValue());
    }

    public static void checkTweetPage_02_1(TweetPageResponse pTweetPageResponse, TimeGap pTimeGap) {
        TweetDTO[] lTweets = pTweetPageResponse.tweetPage().tweets();
        assertThat(lTweets.length, equalTo(20));
        // assertThat(pTweetPageResponse.initialGap(), equalTo(pTimeGap));
        // assertThat(pTweetPageResponse.remainingGap(), equalTo(remainingGap(lTweets, pTimeGap)));

        TweetDTO lFirstTweet = pTweetPageResponse.tweetPage().tweets()[0];
        assertThat(lFirstTweet.getId(), equalTo(EARLIEST_02_1));

        TweetDTO lLastTweet = pTweetPageResponse.tweetPage().tweets()[19];
        assertThat(lLastTweet.getId(), equalTo(OLDEST_02_1));
    }

    public static void checkTweetPage_02_2(TweetPageResponse pTweetPageResponse, TimeGap pTimeGap) {
        TweetDTO[] lTweets = pTweetPageResponse.tweetPage().tweets();
        assertThat(lTweets.length, equalTo(20));
        // assertThat(pTweetPageResponse.initialGap(), equalTo(pTimeGap));
        // assertThat(pTweetPageResponse.remainingGap(), equalTo(remainingGap(lTweets, pTimeGap)));

        TweetDTO lFirstTweet = pTweetPageResponse.tweetPage().tweets()[0];
        assertThat(lFirstTweet.getId(), equalTo(EARLIEST_02_2));

        TweetDTO lLastTweet = pTweetPageResponse.tweetPage().tweets()[19];
        assertThat(lLastTweet.getId(), equalTo(OLDEST_02_2));
    }

    public static void checkTweetPage_02_3(TweetPageResponse pTweetPageResponse, TimeGap pTimeGap) {
        TweetDTO[] lTweets = pTweetPageResponse.tweetPage().tweets();
        assertThat(pTweetPageResponse.tweetPage().tweets().length, equalTo(19));
        // assertThat(pTweetPageResponse.initialGap(), equalTo(pTimeGap));
        // assertThat(pTweetPageResponse.remainingGap(), nullValue());

        TweetDTO lFirstTweet = lTweets[0];
        assertThat(lFirstTweet.getId(), equalTo(EARLIEST_02_3));
        TweetDTO lLastTweet = lTweets[18];
        assertThat(lLastTweet.getId(), equalTo(OLDEST_02_3));
    }

    private static final TimeGap remainingGap(TweetDTO[] pTweets, TimeGap pTimeGap) {
        return pTweets.length >= PAGE_SIZE ? pTimeGap.remainingGap(TimeRange.from(pTweets)) : null;
    }
}
