package com.kaist.iclab.datamanager;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class DataProvider extends ContentProvider {

    private static String TAG = "DataProvider";

    public static final String PROVIDER_NAME = "com.kaist.iclab.multisensing.datamanager";
    public static final Uri CONTENT_URI_LOG = Uri.parse("content://" + PROVIDER_NAME + "/log");
    public static final Uri CONTENT_URI_RANGE = Uri.parse("content://" + PROVIDER_NAME + "/range");
    public static final Uri CONTENT_URI_REVIEW = Uri.parse("content://" + PROVIDER_NAME + "/review");

    private static final int LOG = 10;
    private static final int LOG_RECORD = 11;
    private static final int RANGE = 20;
    private static final int REVIEW = 30;

    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "log", LOG);
        uriMatcher.addURI(PROVIDER_NAME, "log/#", LOG_RECORD);
        uriMatcher.addURI(PROVIDER_NAME, "range", RANGE);
        uriMatcher.addURI(PROVIDER_NAME, "review", REVIEW);
    }


    private DAO dao;
    private SQLiteDatabase database;


    private long lastId;

    @Override
    public boolean onCreate() {
        Log.i(TAG, "onCreate");
        dao = new DAO(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            switch (uriMatcher.match(uri)) {
                case RANGE: {
                    return dao.getQueryRange();
                }
                case REVIEW: {
                    if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryReview(selection, selectionArgs);
                }
                case LOG:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryLogCount(projection, selection, selectionArgs);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
            try {
            switch (uriMatcher.match(uri)) {
                case LOG: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writeLog(DAO.NEW, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_LOG, id + "");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
/*  @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        try {
            switch (uriMatcher.match(uri)) {
                case LOG: {
                    Log.d(TAG, "CP insert"+dao);
                    long id = dao.writeLog(DAO.NEW, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_LOG, id + "");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }*/

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        Log.d("DataProvider", "Insert: " + uri);
        final int match = uriMatcher.match(uri);
        if (match == LOG) {
            return insertInBulk(getDatabase(), DAO.TABLE_LOG, values);
        }
        return 0;
    }

    private int insertInBulk(SQLiteDatabase database, String tableName, ContentValues[] values) {
        long id = 0;
        database.beginTransaction();
        for (ContentValues value : values) {
            id = database.insertOrThrow(tableName, null, value);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        Log.d(TAG, "bulk insert done, new id: " + id + " and # of inserted items: "+values.length);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        try {
            switch (uriMatcher.match(uri)) {
                case LOG: {
                    Log.d(TAG, "CP delete"+dao);
                    return dao.deleteAllLog();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        try {
            switch (uriMatcher.match(uri)) {
                case REVIEW: {
                    return (int)dao.writeReview(values);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return -1;
    }

    long timestamp = 0;
    void showToast(final String message) {
        long t = new Date().getTime();
        if (t - timestamp <= 30000) return;
        timestamp = t;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run()
            {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    public SQLiteDatabase getDatabase() {
        if (database == null) {
            database = dao.getWritableDatabase();
        }
        return database;
    }

}
