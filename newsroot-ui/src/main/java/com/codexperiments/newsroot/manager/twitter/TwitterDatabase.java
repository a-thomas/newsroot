package com.codexperiments.newsroot.manager.twitter;

import java.io.IOException;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

public class TwitterDatabase extends Database {
    private static final String DATABASE_NAME = "twitter_database";
    private static final int DATABASE_VERSION = 1;

    public enum DB_TWITTER implements Table {
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

    public TwitterDatabase(Application pApplication) {
        super(pApplication, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase pDatabase) {
        super.onCreate(pDatabase);
        try {
            executeAssetScript("sql/twitter_create.sql");
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
            executeAssetScript("sql/twitter_drop.sql");
        } catch (IOException eIOException) {
            // TODO Exception handling
            throw new RuntimeException(eIOException);
        }
    }
}
