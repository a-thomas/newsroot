package com.codexperiments.newsroot.manager.twitter;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.manager.twitter.ResultHandler.Row;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.COL_TMG_TIMEGAP;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.DB_TWITTER;

public class TimeGapDAO {
    private TwitterDatabase mDatabase;
    ThreadLocal<Insert<DB_TWITTER>> mInsertHolder;
    ThreadLocal<Update<DB_TWITTER>> mUpdateHolder;

    public TimeGapDAO(TwitterDatabase pDatabase) {
        super();
        mDatabase = pDatabase;
        mInsertHolder = new ThreadLocal<Insert<DB_TWITTER>>();
        mUpdateHolder = new ThreadLocal<Update<DB_TWITTER>>();
    }

    public TimeGap get(Row pRow) {
        TimeGap lTimeGap = new TimeGap();
        lTimeGap.setId(pRow.getLong(DB_TWITTER.TMG_TIMEGAP, COL_TMG_TIMEGAP.TMG_ID));
        lTimeGap.setEarliestBound(pRow.getLong(DB_TWITTER.TMG_TIMEGAP, COL_TMG_TIMEGAP.TMG_TWT_EARLIEST_ID));
        lTimeGap.setOldestBound(pRow.getLong(DB_TWITTER.TMG_TIMEGAP, COL_TMG_TIMEGAP.TMG_TWT_OLDEST_ID));
        return lTimeGap;
    }

    public void create(TimeGap pTimeGap) {
        Insert<DB_TWITTER> lInsert = mInsertHolder.get();
        if (lInsert == null) {
            lInsert = Insert.on(DB_TWITTER.TMG_TIMEGAP);
            mInsertHolder.set(lInsert);
        }
        pTimeGap.setId(lInsert.value(COL_TMG_TIMEGAP.TMG_TWT_EARLIEST_ID, pTimeGap.earliestBound())
                              .value(COL_TMG_TIMEGAP.TMG_TWT_OLDEST_ID, pTimeGap.oldestBound())
                              .execute(mDatabase.getWritableDatabase()));
    }

    public void update(TimeGap pTimeGap) {
        Update<DB_TWITTER> lUpdate = mUpdateHolder.get();
        if (lUpdate == null) {
            lUpdate = Update.on(DB_TWITTER.TMG_TIMEGAP).whereEquals(COL_TMG_TIMEGAP.TMG_ID, pTimeGap.getId());
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
