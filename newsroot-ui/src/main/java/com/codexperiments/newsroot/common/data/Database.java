package com.codexperiments.newsroot.common.data;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.collect.Lists;

public abstract class Database extends SQLiteOpenHelper {
    private Context mContext;
    private SQLiteDatabase mConnection;

    public Database(Context pContext, String pName, int pVersion) {
        super(pContext, pName, null, pVersion);
        mContext = pContext;
        mConnection = null;
        super.getWritableDatabase();
    }

    public SQLiteDatabase getConnection() {
        return mConnection;
    }

    @Override
    public void onCreate(SQLiteDatabase pDatabase) {
        mConnection = pDatabase;
    }

    @Override
    public void onUpgrade(SQLiteDatabase pDatabase, int pOldVersion, int pNewVersion) {
        mConnection = pDatabase;
        recreate();
    }

    public void onDestroy(SQLiteDatabase pDatabase) {
        mConnection = pDatabase;
    }

    public void recreate() {
        onDestroy(getWritableDatabase());
        onCreate(getWritableDatabase());
    }

    public Database executeScriptFromAssets(String pAssetPath) throws IOException {
        executeScriptFromAssets(pAssetPath, mContext);
        return this;
    }

    public Database executeScriptFromAssets(String pAssetPath, Context pContext) throws IOException {
        InputStream lAssetStream = null;
        mConnection.beginTransaction();
        try {
            lAssetStream = pContext.getAssets().open(pAssetPath);
            byte[] lScript = new byte[lAssetStream.available()];
            lAssetStream.read(lScript);

            int lPreviousIndex = 0;
            byte lPreviousChar = '\0';
            byte lCurrentChar;
            boolean lIgnore = false;
            int lScriptSize = lScript.length;
            for (int i = 0; i < lScriptSize; ++i) {
                lCurrentChar = lScript[i];
                if ((lCurrentChar == '\n' || lCurrentChar == '\r') && (lPreviousChar == ';')) {
                    String lStatement = new String(lScript, lPreviousIndex, (i - lPreviousIndex) + 1, "UTF-8");
                    if (!lIgnore && !TextUtils.isEmpty(lStatement)) {
                        mConnection.execSQL(lStatement);
                    }

                    lPreviousIndex = i + 1;
                    lIgnore = false;
                } else if ((lCurrentChar == '-') && (lPreviousChar == '-')) {
                    lIgnore = true;
                }

                lPreviousChar = lCurrentChar;
            }
            mConnection.setTransactionSuccessful();
        } finally {
            mConnection.endTransaction();
            try {
                if (lAssetStream != null) lAssetStream.close();
            } catch (IOException eIOException) {
                Log.e(Database.class.getSimpleName(), "Error while reading assets", eIOException);
            }
        }
        return this;
    }

    public <TEntity> void parse(Query pQuery, RowHandler pRowHandler) {
        Cursor lCursor = mConnection.rawQuery(pQuery.toQuery(), pQuery.toParams());
        try {
            int lResultSize = lCursor.getCount();
            for (int i = lResultSize; i < lResultSize; ++i) {
                lCursor.moveToNext();
                pRowHandler.handleRow(lCursor);
            }
        } finally {
            lCursor.close();
        }
    }

    public Cursor runQuery(Query pQuery) {
        return mConnection.rawQuery(pQuery.toQuery(), pQuery.toParams());
    }

    @SuppressWarnings("unchecked")
    public <TEntity> TEntity[] parseArray(Query pQuery, ObjectHandler<TEntity> pObjectHandler) {
        Cursor lCursor = mConnection.rawQuery(pQuery.toQuery(), pQuery.toParams());
        try {
            int lResultSize = lCursor.getCount();
            pObjectHandler.initialize(lCursor);
            TEntity[] lEntity = (TEntity[]) Array.newInstance(pObjectHandler.ofType(), lResultSize);
            for (int i = 0; i < lResultSize; ++i) {
                lCursor.moveToNext();
                lEntity[i] = pObjectHandler.parse(lCursor);
            }
            return lEntity;
        } finally {
            lCursor.close();
        }
    }

    public <TEntity> List<TEntity> parseList(Query pQuery, ObjectHandler<TEntity> pObjectHandler) {
        Cursor lCursor = mConnection.rawQuery(pQuery.toQuery(), pQuery.toParams());
        try {
            int lResultSize = lCursor.getCount();
            List<TEntity> lEntity = Lists.newArrayListWithCapacity(lResultSize);
            for (int i = lResultSize; i < lResultSize; ++i) {
                lCursor.moveToNext();
                lEntity.set(i, pObjectHandler.parse(lCursor));
            }
            return lEntity;
        } finally {
            lCursor.close();
        }
    }
}
