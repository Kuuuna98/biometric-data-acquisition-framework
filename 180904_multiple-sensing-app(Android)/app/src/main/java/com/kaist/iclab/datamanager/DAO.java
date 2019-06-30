package com.kaist.iclab.datamanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

public class DAO extends SQLiteOpenHelper {

    final private static String TAG = "DAO";

    //private final String databasePath;
    private static final String DATABASE_NAME = "sensors_data.db";
    private static final int DATABASE_VERSION = 1;

    /** Constructor */
    public DAO(Context context) {
        super(context, DATABASE_NAME,null, DATABASE_VERSION);

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.i(TAG, "onOpen:" + db.getVersion() + ", " + db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate:" + db);
        setupTable(db);
    }

    //버전이 다를경우 업그레이드 함수가 실행된다. 이경우 마이그레시션 코드를 구현할 필요가 있음
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: " + oldVersion + " -> " + newVersion + " " + db);
        setupTable(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onDowngrade: " + oldVersion + " -> " + newVersion + " " + db);
        setupTable(db);
    }

    private void setupTable(SQLiteDatabase db) {

        //("Heart Rate = %d beats per minute, Quality = %s
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                TABLE_LOG +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                    LOG_FIELD_JSON + " TEXT NOT NULL, " +
                    LOG_FIELD_REG + " LONG NOT NULL" +
                ");");

        //db.execSQL("DROP TABLE " + TABLE_REVIEW);
        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                TABLE_REVIEW +
                "(_id TEXT NOT NULL, " +
                    "review TEXT NOT NULL, " +
                    "PRIMARY KEY(_id)" +
                ");");
    }

    public static int NEW = 0;
    public static String TABLE_LOG = "log";
    public static String LOG_FIELD_TYPE = "type";
    public static String LOG_FIELD_JSON = "json";
    public static String LOG_FIELD_REG = "reg";

    public static String TABLE_REVIEW = "review";


    public long writeLog(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writeLog:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW) {
            id = database.insert(TABLE_LOG, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(TABLE_LOG, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }

    public int deleteAllLog() throws Exception{
        Log.d(TAG, "delete Logs Table:");
        SQLiteDatabase database = getWritableDatabase();
        return database.delete(TABLE_LOG,null, null);
    }

    public Cursor getQueryRange() {
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_LOG + " WHERE " + LOG_FIELD_TYPE + " = ? ORDER BY reg ASC", new String[] { "MainActivity" });
    }

    public long writeReview(ContentValues contentValues) throws Exception {
        SQLiteDatabase database = getWritableDatabase();
        return database.replace(TABLE_REVIEW, null, contentValues);
    }

    public Cursor queryReview(String selection, String[] selectionArgs) {
        //return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_REVIEW + " WHERE _id = ?", new String[] { id });
        return getReadableDatabase().query(TABLE_REVIEW, null, selection, selectionArgs, null, null, null);
    }

    public Cursor queryLogCount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(TABLE_LOG, projection, selection, selectionArgs, null, null, null);
    }
}
