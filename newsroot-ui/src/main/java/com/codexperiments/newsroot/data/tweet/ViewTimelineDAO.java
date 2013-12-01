package com.codexperiments.newsroot.data.tweet;

import com.codexperiments.newsroot.common.data.ResultHandler;
import com.codexperiments.newsroot.common.data.ResultHandler.Row;
import com.codexperiments.newsroot.data.tweet.TweetDatabase.COL_VIEW_TIMELINE;
import com.codexperiments.newsroot.data.tweet.TweetDatabase.DB_TWEET;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Tweet;

public class ViewTimelineDAO {
    public enum Kind {
        TWEET, TIMEGAP
    }

    private TweetDatabase mDatabase;
    private Kind[] mKinds;

    public ViewTimelineDAO(TweetDatabase pDatabase) {
        super();
        mDatabase = pDatabase;
        mKinds = Kind.values();
    }

    public Kind getKind(Row pRow) {
        return mKinds[pRow.getInt(DB_TWEET.VIEW_TIMELINE, COL_VIEW_TIMELINE.VIEW_KIND)];
    }

    public Tweet getTweet(Row pRow) {
        Tweet lTweet = new Tweet();
        lTweet.setId(pRow.getLong(DB_TWEET.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_ID));
        lTweet.setName(pRow.getString(DB_TWEET.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_NAME));
        lTweet.setScreenName(pRow.getString(DB_TWEET.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_SCREEN_NAME));
        lTweet.setText(pRow.getString(DB_TWEET.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_TEXT));
        lTweet.setCreatedAt(pRow.getLong(DB_TWEET.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_CREATED_AT));
        return lTweet;
    }

    public TimeGap getTimeGap(Row pRow) {
        TimeGap lTimeGap = TimeGap.initialTimeGap();
        lTimeGap.setId(pRow.getLong(DB_TWEET.VIEW_TIMELINE, COL_VIEW_TIMELINE.TMG_ID));
        lTimeGap.setEarliestBound(pRow.getLong(DB_TWEET.VIEW_TIMELINE, COL_VIEW_TIMELINE.TMG_TWT_EARLIEST_ID));
        lTimeGap.setOldestBound(pRow.getLong(DB_TWEET.VIEW_TIMELINE, COL_VIEW_TIMELINE.TMG_TWT_OLDEST_ID));
        return lTimeGap;
    }
}
