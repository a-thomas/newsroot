package com.codexperiments.newsroot.manager.twitter;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.ResultHandler.Row;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.COL_VIEW_TIMELINE;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.DB_TWITTER;

public class ViewTimelineDAO {
    public enum Kind {
        TWEET, TIMEGAP
    }

    private TwitterDatabase mDatabase;
    private Kind[] mKinds;

    public ViewTimelineDAO(TwitterDatabase pDatabase) {
        super();
        mDatabase = pDatabase;
        mKinds = Kind.values();
    }

    public Kind getKind(Row pRow) {
        return mKinds[pRow.getInt(DB_TWITTER.VIEW_TIMELINE, COL_VIEW_TIMELINE.VIEW_KIND)];
    }

    public Tweet getTweet(Row pRow) {
        Tweet lTweet = new Tweet();
        lTweet.setId(pRow.getLong(DB_TWITTER.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_ID));
        lTweet.setName(pRow.getString(DB_TWITTER.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_NAME));
        lTweet.setScreenName(pRow.getString(DB_TWITTER.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_SCREEN_NAME));
        lTweet.setText(pRow.getString(DB_TWITTER.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_TEXT));
        lTweet.setCreatedAt(pRow.getLong(DB_TWITTER.VIEW_TIMELINE, COL_VIEW_TIMELINE.TWT_CREATED_AT));
        return lTweet;
    }

    public TimeGap getTimeGap(Row pRow) {
        TimeGap lTimeGap = TimeGap.initialTimeGap();
        lTimeGap.setId(pRow.getLong(DB_TWITTER.VIEW_TIMELINE, COL_VIEW_TIMELINE.TMG_ID));
        lTimeGap.setEarliestBound(pRow.getLong(DB_TWITTER.VIEW_TIMELINE, COL_VIEW_TIMELINE.TMG_TWT_EARLIEST_ID));
        lTimeGap.setOldestBound(pRow.getLong(DB_TWITTER.VIEW_TIMELINE, COL_VIEW_TIMELINE.TMG_TWT_OLDEST_ID));
        return lTimeGap;
    }
}
