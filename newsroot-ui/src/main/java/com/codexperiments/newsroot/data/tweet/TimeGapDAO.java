package com.codexperiments.newsroot.data.tweet;

import com.codexperiments.newsroot.common.data.Insert;
import com.codexperiments.newsroot.common.data.ResultHandler;
import com.codexperiments.newsroot.common.data.Update;
import com.codexperiments.newsroot.common.data.ResultHandler.Row;
import com.codexperiments.newsroot.data.tweet.TweetDatabase.COL_TMG_TIMEGAP;
import com.codexperiments.newsroot.data.tweet.TweetDatabase.DB_TWEET;
import com.codexperiments.newsroot.domain.tweet.TimeGap;

public class TimeGapDAO {
    private TweetDatabase mDatabase;
    ThreadLocal<Insert<DB_TWEET>> mInsertHolder;
    ThreadLocal<Update<DB_TWEET>> mUpdateHolder;

    public TimeGapDAO(TweetDatabase pDatabase) {
        super();
        mDatabase = pDatabase;
        mInsertHolder = new ThreadLocal<Insert<DB_TWEET>>();
        mUpdateHolder = new ThreadLocal<Update<DB_TWEET>>();
    }

    public TimeGap get(Row pRow) {
        TimeGap lTimeGap = TimeGap.initialTimeGap();
        lTimeGap.setId(pRow.getLong(DB_TWEET.TMG_TIMEGAP, COL_TMG_TIMEGAP.TMG_ID));
        lTimeGap.setEarliestBound(pRow.getLong(DB_TWEET.TMG_TIMEGAP, COL_TMG_TIMEGAP.TMG_TWT_EARLIEST_ID));
        lTimeGap.setOldestBound(pRow.getLong(DB_TWEET.TMG_TIMEGAP, COL_TMG_TIMEGAP.TMG_TWT_OLDEST_ID));
        return lTimeGap;
    }

    public void create(TimeGap pTimeGap) {
        Insert<DB_TWEET> lInsert = mInsertHolder.get();
        if (lInsert == null) {
            lInsert = Insert.on(DB_TWEET.TMG_TIMEGAP);
            mInsertHolder.set(lInsert);
        }
        pTimeGap.setId(lInsert.value(COL_TMG_TIMEGAP.TMG_TWT_EARLIEST_ID, pTimeGap.earliestBound())
                              .value(COL_TMG_TIMEGAP.TMG_TWT_OLDEST_ID, pTimeGap.oldestBound())
                              .execute(mDatabase.getWritableDatabase()));
    }

    public void update(TimeGap pTimeGap) {
        Update<DB_TWEET> lUpdate = mUpdateHolder.get();
        if (lUpdate == null) {
            lUpdate = Update.on(DB_TWEET.TMG_TIMEGAP).whereEquals(COL_TMG_TIMEGAP.TMG_ID, pTimeGap.getId());
            mUpdateHolder.set(lUpdate);
        }
        lUpdate.set(COL_TMG_TIMEGAP.TMG_TWT_EARLIEST_ID, pTimeGap.earliestBound())
               .set(COL_TMG_TIMEGAP.TMG_TWT_OLDEST_ID, pTimeGap.oldestBound())
               .execute(mDatabase.getWritableDatabase());
    }

    public void delete(TimeGap pTimeGap) {
        mDatabase.getWritableDatabase().execSQL("delete from TMG_TIMEGAP where TMG_ID = ?",
                                                new String[] { Long.toString(pTimeGap.getId()) });
    }
}
