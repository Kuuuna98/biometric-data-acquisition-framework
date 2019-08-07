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

    public static final Uri CONTENT_URI_E4_ACC = Uri.parse("content://" + PROVIDER_NAME + "/E4_ACC");
    public static final Uri CONTENT_URI_E4_BVP = Uri.parse("content://" + PROVIDER_NAME + "/E4_BVP");
    public static final Uri CONTENT_URI_E4_IBI = Uri.parse("content://" + PROVIDER_NAME + "/E4_IBI");
    public static final Uri CONTENT_URI_E4_TEMPERATURE = Uri.parse("content://" + PROVIDER_NAME + "/E4_TEMPERATURE");
    public static final Uri CONTENT_URI_E4_GSR = Uri.parse("content://" + PROVIDER_NAME + "/E4_GSR");
    public static final Uri CONTENT_URI_LocationService = Uri.parse("content://" + PROVIDER_NAME + "/LocationService");

    public static final Uri CONTENT_URI_Phone_ACC = Uri.parse("content://" + PROVIDER_NAME + "/Phone_ACC");
    public static final Uri CONTENT_URI_Phone_ACC_SEL = Uri.parse("content://" + PROVIDER_NAME + "/Phone_ACC_SEL");

    public static final Uri CONTENT_URI_Phone_GYRO = Uri.parse("content://" + PROVIDER_NAME + "/Phone_GYRO");
    public static final Uri CONTENT_URI_MainActivity = Uri.parse("content://" + PROVIDER_NAME + "/MainActivity");

    public static final Uri CONTENT_URI_RANGE = Uri.parse("content://" + PROVIDER_NAME + "/range");
    public static final Uri CONTENT_URI_REVIEW = Uri.parse("content://" + PROVIDER_NAME + "/review");

    private static final int LOG = 10;
    private static final int LOG_RECORD = 11;
    private static final int RANGE = 20;
    private static final int REVIEW = 30;

    private static final int E4_ACC = 40;
    private static final int E4_BVP = 41;
    private static final int E4_GSR = 42;
    private static final int E4_IBI = 43;
    private static final int E4_TEMPERATURE = 44;
    private static final int LocationService = 50;
    private static final int Phone_ACC = 60;
    private static final int Phone_ACC_SEL = 61;

    private static final int Phone_GYRO = 70;
    private static final int MainActivity = 80;



    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "log", LOG);

        uriMatcher.addURI(PROVIDER_NAME, "log/#", LOG_RECORD);
        uriMatcher.addURI(PROVIDER_NAME, "range", RANGE);
        uriMatcher.addURI(PROVIDER_NAME, "review", REVIEW);

        uriMatcher.addURI(PROVIDER_NAME, "E4_ACC", E4_ACC);
        uriMatcher.addURI(PROVIDER_NAME, "E4_BVP", E4_BVP);
        uriMatcher.addURI(PROVIDER_NAME, "E4_GSR", E4_GSR);
        uriMatcher.addURI(PROVIDER_NAME, "E4_IBI", E4_IBI);
        uriMatcher.addURI(PROVIDER_NAME, "E4_TEMPERATURE", E4_TEMPERATURE);
        uriMatcher.addURI(PROVIDER_NAME, "LocationService", LocationService);
        uriMatcher.addURI(PROVIDER_NAME, "Phone_ACC", Phone_ACC);
        uriMatcher.addURI(PROVIDER_NAME, "Phone_ACC_SEL", Phone_ACC_SEL);


        uriMatcher.addURI(PROVIDER_NAME, "Phone_GYRO", Phone_GYRO);
        uriMatcher.addURI(PROVIDER_NAME, "MainActivity", MainActivity);

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
//                case RANGE: {
//                    return dao.getQueryRange();
//                }
//                case REVIEW: {
//                    if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
//                    return dao.queryReview(selection, selectionArgs);
//                }
//                case LOG:{
//                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
//                    return dao.queryLogCount(projection, selection, selectionArgs);
//                }
                case E4_ACC:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryE4ACCCount(projection, selection, selectionArgs);
                }
                case E4_BVP:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryE4BVPCount(projection, selection, selectionArgs);
                }
                case E4_GSR:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryE4GSRCount(projection, selection, selectionArgs);
                }
                case E4_IBI:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryE4IBICount(projection, selection, selectionArgs);
                }
                case E4_TEMPERATURE:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryE4TEMCount(projection, selection, selectionArgs);
                }
                case Phone_ACC_SEL:{
                    return dao.selectPhoneACC();
                }
                case Phone_ACC:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryPHACCCount(projection, selection, selectionArgs);
                }
                case Phone_GYRO:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryPHGYROCount(projection, selection, selectionArgs);
                }
                case LocationService:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryLocationCount(projection, selection, selectionArgs);
                }
                case MainActivity:{
                    //if (selectionArgs != null && selectionArgs.length == 1 && selection == null) selection = "_id = ?";
                    return dao.queryMainCount(projection, selection, selectionArgs);
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
//                case LOG: {
//                    Log.d(TAG, "CP insert: "+uri);
//                    long id = dao.writeLog(DAO.NEW, values);
//                    Log.d(TAG, "new id: " + id);
//                    //showToast("DP: "+ id);
//                    lastId = id;
//                    return Uri.withAppendedPath(CONTENT_URI_LOG, id + "");
//                }
                case E4_ACC: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writeE4ACC(DAO.NEW_E4ACC, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_E4_ACC, id + "");
                }
                case E4_BVP: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writeE4BVP(DAO.NEW_E4BVP, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_E4_BVP, id + "");
                }
                case E4_GSR: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writeE4GSR(DAO.NEW_E4GSR, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_E4_GSR, id + "");
                }
                case E4_TEMPERATURE: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writeE4TEMPERATURE(DAO.NEW_E4TEM, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_E4_TEMPERATURE, id + "");
                }
                case E4_IBI: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writeE4IBI(DAO.NEW_E4IBI, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_E4_IBI, id + "");
                }
                case LocationService: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writeLocation(DAO.NEW_LOC, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_LocationService, id + "");
                }
                case Phone_ACC: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writePHACC(DAO.NEW_PHACC, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_Phone_ACC, id + "");
                }
                case Phone_GYRO: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writePHGYRO(DAO.NEW_PHGYRO, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_Phone_GYRO, id + "");
                }
                case MainActivity: {
                    Log.d(TAG, "CP insert: "+uri);
                    long id = dao.writeMAIN(DAO.NEW_MAIN, values);
                    Log.d(TAG, "new id: " + id);
                    //showToast("DP: "+ id);
                    lastId = id;
                    return Uri.withAppendedPath(CONTENT_URI_MainActivity, id + "");
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
//        if (match == LOG) {
//            return insertInBulk(getDatabase(), DAO.TABLE_LOG, values);
//        }else
        if (match == E4_ACC) {
            return insertInBulk(getDatabase(), DAO.E4_ACC, values);
        }else if (match == E4_BVP) {
            return insertInBulk(getDatabase(), DAO.E4_BVP, values);
        }else if (match == E4_GSR) {
            return insertInBulk(getDatabase(), DAO.E4_GSR, values);
        }else if (match == E4_IBI) {
            return insertInBulk(getDatabase(), DAO.E4_IBI, values);
        }else if (match == E4_TEMPERATURE) {
            return insertInBulk(getDatabase(), DAO.E4_TEMPERATURE, values);
        }else if (match == LocationService) {
            return insertInBulk(getDatabase(), DAO.LocationService, values);
        }else if (match == Phone_ACC) {
            return insertInBulk(getDatabase(), DAO.Phone_ACC, values);
        }else if (match == Phone_GYRO) {
            return insertInBulk(getDatabase(), DAO.Phone_GYRO, values);
        }else if (match == MainActivity) {
            return insertInBulk(getDatabase(), DAO.MainActivity, values);
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
                case MainActivity: {
                    Log.d(TAG, "CP delete"+dao);
                    return dao.deleteAll();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
/*        try {
            switch (uriMatcher.match(uri)) {
                case REVIEW: {
                    return (int)dao.writeReview(values);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }*/
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
s