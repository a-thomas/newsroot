package com.codexperiments.newsroot.data.tweet;

import android.database.Cursor;

import com.codexperiments.newsroot.data.tweet.TweetDatabase.COL_TMG_TIMEGAP;

public class TimeGapHandler {
    private int mIdIndex;
    private int mEarliestBoundIndex;
    private int mOldestBoundIndex;

    public TimeGapHandler(Cursor pCursor) {
        super();
        mIdIndex = pCursor.getColumnIndex(COL_TMG_TIMEGAP.TMG_ID.name());
        mEarliestBoundIndex = pCursor.getColumnIndex(COL_TMG_TIMEGAP.TMG_TWT_EARLIEST_ID.name());
        mOldestBoundIndex = pCursor.getColumnIndex(COL_TMG_TIMEGAP.TMG_TWT_OLDEST_ID.name());
    }

    public TimeGapDTO parse(Cursor pCursor) {
        TimeGapDTO lTimeGapDTO = new TimeGapDTO();
        lTimeGapDTO.setId(pCursor.getLong(mIdIndex));
        lTimeGapDTO.setEarliestBound(pCursor.getLong(mEarliestBoundIndex));
        lTimeGapDTO.setOldestBound(pCursor.getLong(mOldestBoundIndex));
        return lTimeGapDTO;
    }

    public long getId(Cursor pCursor) {
        return pCursor.getLong(mIdIndex);
    }

    public long getEarliestBound(Cursor pCursor) {
        return pCursor.getLong(mEarliestBoundIndex);
    }

    public long getOldestBound(Cursor pCursor) {
        return pCursor.getLong(mEarliestBoundIndex);
    }
}
