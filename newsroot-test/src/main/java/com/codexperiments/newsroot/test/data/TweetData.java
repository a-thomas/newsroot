package com.codexperiments.newsroot.test.data;

import com.codexperiments.newsroot.data.tweet.TweetDTO;

public class TweetData {
    public static TweetDTO createTweet(long pId) {
        TweetDTO lTweetDTO = new TweetDTO();
        lTweetDTO.setCreatedAt(pId * 10);
        lTweetDTO.setId(pId);
        lTweetDTO.setName("Tweet" + pId);
        lTweetDTO.setScreenName("ScreenName" + pId);
        lTweetDTO.setText("Text" + pId);
        return lTweetDTO;
    }

    // public static void checkTweet(TweetDTO pCheckedTweetDTO, TweetDTO pRefTweetDTO) {
    // assertThat(pCheckedTweetDTO.getCreatedAt(), equalTo(pRefTweetDTO.getCreatedAt()));
    // assertThat(pCheckedTweetDTO.getId(), equalTo(pRefTweetDTO.getId()));
    // assertThat(pCheckedTweetDTO.getName(), equalTo(pRefTweetDTO.getName()));
    // assertThat(pCheckedTweetDTO.getScreenName(), equalTo(pRefTweetDTO.getScreenName()));
    // assertThat(pCheckedTweetDTO.getText(), equalTo(pRefTweetDTO.getText()));
    // }
    //
    // public static TweetDTO createTweet_01() {
    // return createTweet("Tweet01", "ScreenName01", "Text01");
    // TweetDTO lTweetDTO = new TweetDTO();
    // lTweetDTO.setCreatedAt(1000l);
    // lTweetDTO.setId(1234l);
    // lTweetDTO.setName("Tweet01");
    // lTweetDTO.setScreenName("ScreenName01");
    // lTweetDTO.setText("Text01");
    // return lTweetDTO;
    // }
    //
    // public static void checkTweet_01(TweetDTO pTweetDTO) {
    // assertThat(pTweetDTO.getCreatedAt(), equalTo(1000l));
    // assertThat(pTweetDTO.getId(), equalTo(1234l));
    // assertThat(pTweetDTO.getName(), equalTo("Tweet01"));
    // assertThat(pTweetDTO.getScreenName(), equalTo("ScreenName01"));
    // assertThat(pTweetDTO.getText(), equalTo("Text01"));
    // }
}
