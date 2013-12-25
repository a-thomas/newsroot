package com.codexperiments.newsroot.test.data;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.codexperiments.newsroot.data.tweet.TweetDTO;

public class TweetData {
    public static TweetDTO createTweet_01() {
        TweetDTO lTweetDTO = new TweetDTO();
        lTweetDTO.setCreatedAt(1000l);
        lTweetDTO.setId(1234l);
        lTweetDTO.setName("Tweet01");
        lTweetDTO.setScreenName("ScreenName01");
        lTweetDTO.setText("Text01");
        return lTweetDTO;
    }

    public static void checkTweet_01(TweetDTO pTweetDTO) {
        assertThat(pTweetDTO.getCreatedAt(), equalTo(1000l));
        assertThat(pTweetDTO.getId(), equalTo(1234l));
        assertThat(pTweetDTO.getName(), equalTo("Tweet01"));
        assertThat(pTweetDTO.getScreenName(), equalTo("ScreenName01"));
        assertThat(pTweetDTO.getText(), equalTo("Text01"));
    }
}
