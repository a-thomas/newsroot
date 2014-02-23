package com.codexperiments.newsroot.data.tweet;

import java.io.IOException;

import com.codexperiments.newsroot.common.data.Database;
import com.codexperiments.newsroot.common.data.Table;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class TweetDatabase extends Database {
    private static final String DATABASE_NAME = "twitter_database";
    private static final int DATABASE_VERSION = 1;

    public enum DB_TWEET implements Table {
        TWT_TWEET() {
            public Enum<?>[] columns() {
                return COL_VIEW_TIMELINE.values();
            }
        },
        TML_TIMELINE() {
            public Enum<?>[] columns() {
                return COL_VIEW_TIMELINE.values();
            }
        },
        TMG_TIMEGAP() {
            public Enum<?>[] columns() {
                return COL_VIEW_TIMELINE.values();
            }
        },
        VIEW_TIMELINE() {
            public Enum<?>[] columns() {
                return COL_VIEW_TIMELINE.values();
            }
        };
    }

    public static enum COL_TWT_TWEET {
        TWT_ID, TWT_CREATED_AT, TWT_TEXT, TWT_NAME, TWT_SCREEN_NAME
    }

    public static enum COL_TML_TIMELINE {
        TML_ID
    }

    public static enum COL_TMG_TIMEGAP {
        TMG_ID, TMG_TWT_EARLIEST_ID, TMG_TWT_OLDEST_ID
    }

    public static enum COL_VIEW_TIMELINE {
        VIEW_KIND,
        VIEW_TIMELINE_ID,
        TWT_ID,
        TWT_CREATED_AT,
        TWT_TEXT,
        TWT_NAME,
        TWT_SCREEN_NAME,
        TMG_ID,
        TMG_TWT_EARLIEST_ID,
        TMG_TWT_OLDEST_ID
    }

    public TweetDatabase(Context pContext) {
        super(pContext, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase pDatabase) {
        super.onCreate(pDatabase);
        try {
            executeScriptFromAssets("sql/twitter_create.sql");
            // executeAssetScript("sql/ctx_timeline_01.sql");
            // executeAssetScript("sql/ctx_timeline_02.sql");
            // executeAssetScript("sql/ctx_timeline_03.sql");
            // executeAssetScript("sql/ctx_timeline_04.sql");
            // executeAssetScript("sql/ctx_timeline_05.sql");
            // executeAssetScript("sql/ctx_timeline_combine_all.sql");
        } catch (IOException eIOException) {
            // TODO Exception handling
            throw new RuntimeException(eIOException);
        }
    }

    @Override
    public void onDestroy(SQLiteDatabase pDatabase) {
        super.onDestroy(pDatabase);
        try {
            executeScriptFromAssets("sql/twitter_drop.sql");
        } catch (IOException eIOException) {
            // TODO Exception handling
            throw new RuntimeException(eIOException);
        }
    }
}
