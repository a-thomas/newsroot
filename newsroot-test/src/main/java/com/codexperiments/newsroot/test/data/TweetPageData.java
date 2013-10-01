package com.codexperiments.newsroot.test.data;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.TimeRange;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;

public class TweetPageData {
    public static final int PAGE_SIZE = 20;

    public static final long EARLIEST_02_1 = 349497246842241000L;
    public static final long OLDEST_02_1 = 349473806655565800L;

    public static final long EARLIEST_02_2 = 349471049735344100L;
    public static final long OLDEST_02_2 = 349457499134504960L;

    public static final long EARLIEST_02_3 = 349452667745087500L;
    public static final long OLDEST_02_3 = 349443896905965600L;

    public static void checkTweetPage_empty(TweetPageResponse pTweetPageResponse, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPageResponse.tweetPage().tweets();
        assertThat(lTweets, hasSize(0));
        assertThat(pTweetPageResponse.initialGap(), equalTo(pTimeGap));
        assertThat(pTweetPageResponse.remainingGap(), nullValue());
    }

    public static void checkTweetPage_02_1(TweetPageResponse pTweetPageResponse, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPageResponse.tweetPage().tweets();
        assertThat(lTweets, hasSize(20));
        assertThat(pTweetPageResponse.initialGap(), equalTo(pTimeGap));
        assertThat(pTweetPageResponse.remainingGap(), equalTo(remainingGap(lTweets, pTimeGap)));

        Tweet lFirstTweet = pTweetPageResponse.tweetPage().tweets().get(0);
        assertThat(lFirstTweet.getId(), equalTo(EARLIEST_02_1));

        Tweet lLastTweet = pTweetPageResponse.tweetPage().tweets().get(19);
        assertThat(lLastTweet.getId(), equalTo(OLDEST_02_1));
    }

    public static void checkTweetPage_02_2(TweetPageResponse pTweetPageResponse, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPageResponse.tweetPage().tweets();
        assertThat(lTweets, hasSize(20));
        assertThat(pTweetPageResponse.initialGap(), equalTo(pTimeGap));
        assertThat(pTweetPageResponse.remainingGap(), equalTo(remainingGap(lTweets, pTimeGap)));

        Tweet lFirstTweet = pTweetPageResponse.tweetPage().tweets().get(0);
        assertThat(lFirstTweet.getId(), equalTo(EARLIEST_02_2));

        Tweet lLastTweet = pTweetPageResponse.tweetPage().tweets().get(19);
        assertThat(lLastTweet.getId(), equalTo(OLDEST_02_2));
    }

    public static void checkTweetPage_02_3(TweetPageResponse pTweetPageResponse, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPageResponse.tweetPage().tweets();
        assertThat(pTweetPageResponse.tweetPage().tweets(), hasSize(19));
        assertThat(pTweetPageResponse.initialGap(), equalTo(pTimeGap));
        assertThat(pTweetPageResponse.remainingGap(), nullValue());

        Tweet lFirstTweet = lTweets.get(0);
        assertThat(lFirstTweet.getId(), equalTo(EARLIEST_02_3));
        Tweet lLastTweet = lTweets.get(18);
        assertThat(lLastTweet.getId(), equalTo(OLDEST_02_3));
    }

    private static final TimeGap remainingGap(List<Tweet> pTweets, TimeGap pTimeGap) {
        return pTweets.size() >= PAGE_SIZE ? pTimeGap.remainingGap(TimeRange.from(pTweets)) : null;
    }
}
