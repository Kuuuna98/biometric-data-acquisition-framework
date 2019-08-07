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

//    private static final String DATABASE_E4ACC = "E4_ACC.db";
//    private static final String DATABASE_E4BVP = "E4_BVP.db";
//    private static final String DATABASE_E4GSR = "E4_GSR.db";
//    private static final String DATABASE_E4IBI = "E4_IBI.db";
//    private static final String DATABASE_E4TEM = "E4_TEMPERATURE.db";
//    private static final String DATABASE_PHLOC = "LocationService.db";
//    private static final String DATABASE_PHACC = "Phone_ACC.db";
//    private static final String DATABASE_PHGYRO = "Phone_GYRO.db";
//    private static final String DATABASE_MAIN = "MainActivity.db";


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
//        db.execSQL("CREATE TABLE IF NOT EXISTS " +
//                TABLE_LOG +
//                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    LOG_FIELD_TYPE + " TEXT NOT NULL, " +
//                    LOG_FIELD_JSON + " TEXT NOT NULL, " +
//                    LOG_FIELD_REG + " LONG NOT NULL" +
//                ");");
//
//        //db.execSQL("DROP TABLE " + TABLE_REVIEW);
//        db.execSQL("CREATE TABLE IF NOT EXISTS " +
//                TABLE_REVIEW +
//                "(_id TEXT NOT NULL, " +
//                    "review TEXT NOT NULL, " +
//                    "PRIMARY KEY(_id)" +
//                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                E4_ACC +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
           //     LOG_FIELD_PhoneNumber + " TEXT NOT NULL, " +
                LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                LOG_FIELD_X + " LONG NOT NULL, " +
                LOG_FIELD_Y + " LONG NOT NULL, " +
                LOG_FIELD_Z + " LONG NOT NULL, " +
                LOG_FIELD_TIME + " LONG NOT NULL, " +
                LOG_FIELD_REG + " LONG NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                E4_BVP +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
         //       LOG_FIELD_PhoneNumber + " TEXT NOT NULL, " +
                LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                LOG_FIELD_SENSINGDATA + " LONG NOT NULL, " +
                LOG_FIELD_REG + " LONG NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                E4_GSR +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
           //     LOG_FIELD_PhoneNumber + " TEXT NOT NULL, " +
                LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                LOG_FIELD_SENSINGDATA + " LONG NOT NULL, " +
                LOG_FIELD_TIME + " LONG NOT NULL, " +
                LOG_FIELD_REG + " LONG NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                E4_IBI +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
             //   LOG_FIELD_PhoneNumber + " TEXT NOT NULL, " +
                LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                LOG_FIELD_SENSINGDATA + " LONG NOT NULL, " +
                LOG_FIELD_TIME + " LONG NOT NULL, " +
                LOG_FIELD_REG + " LONG NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                E4_TEMPERATURE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        //        LOG_FIELD_PhoneNumber + " TEXT NOT NULL, " +
                LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                LOG_FIELD_SENSINGDATA + " LONG NOT NULL, " +
                LOG_FIELD_TIME + " LONG NOT NULL, " +
                LOG_FIELD_REG + " LONG NOT NULL" +
                ");");


        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                Phone_ACC +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        //        LOG_FIELD_PhoneNumber + " TEXT NOT NULL, " +
                LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                LOG_FIELD_X + " LONG NOT NULL, " +
                LOG_FIELD_Y + " LONG NOT NULL, " +
                LOG_FIELD_Z + " LONG NOT NULL, " +
                LOG_FIELD_TIME + " LONG NOT NULL, " +
                LOG_FIELD_REG + " LONG NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                Phone_GYRO +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
       //         LOG_FIELD_PhoneNumber + " TEXT NOT NULL, " +
                LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                LOG_FIELD_X + " LONG NOT NULL, " +
                LOG_FIELD_Y + " LONG NOT NULL, " +
                LOG_FIELD_Z + " LONG NOT NULL, " +
                LOG_FIELD_TIME + " LONG NOT NULL, " +
                LOG_FIELD_REG + " LONG NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                LocationService +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
       //         LOG_FIELD_PhoneNumber + " TEXT NOT NULL, " +
                LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                LOG_FIELD_LAT + " LONG NOT NULL, " +
                LOG_FIELD_LONG + " LONG NOT NULL, " +
                LOG_FIELD_TIME + " LONG NOT NULL, " +
                LOG_FIELD_REG + " LONG NOT NULL" +
                ");");

        db.execSQL("CREATE TABLE IF NOT EXISTS " +
                MainActivity +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
       //         LOG_FIELD_PhoneNumber + " TEXT NOT NULL, " +
                LOG_FIELD_TYPE + " TEXT NOT NULL, " +
                LOG_FIELD_MARK + " TEXT NOT NULL, " +
                LOG_FIELD_TIME + " LONG NOT NULL, " +
                LOG_FIELD_REG + " LONG NOT NULL" +
                ");");
    }

    /*센서별 테이블생성*/
    public static String E4_ACC = "E4_ACC";
    public static String E4_BVP = "E4_BVP";
    public static String E4_GSR = "E4_GSR";
    public static String E4_IBI = "E4_IBI";
    public static String E4_TEMPERATURE = "E4_TEMPERATURE";
    public static String LocationService = "LocationService";
    public static String Phone_ACC = "Phone_ACC";
    public static String Phone_GYRO = "Phone_GYRO";
    public static String MainActivity = "MainActivity";

    public static int NEW = 0;

  //  public static String TABLE_LOG = "log";
    // public static String TABLE_REVIEW = "review";

    public static String LOG_FIELD_TYPE = "type";
    public static String LOG_FIELD_JSON = "json";
    public static String LOG_FIELD_REG = "reg";
    public static String LOG_FIELD_TIME = "time";
    public static String LOG_FIELD_MARK = "mark";
    public static String LOG_FIELD_X = "x";
    public static String LOG_FIELD_Y = "y";
    public static String LOG_FIELD_Z = "z";
    public static String LOG_FIELD_SENSINGDATA = "data";
    public static String LOG_FIELD_LAT = "Latitude";
    public static String LOG_FIELD_LONG = "Longtitude";
  //  public static String LOG_FIELD_PhoneNumber = "PhoneNumber";

    public static int NEW_E4ACC = 0;
    public static int NEW_E4BVP = 0;
    public static int NEW_E4TEM = 0;
    public static int NEW_E4IBI = 0;
    public static int NEW_E4GSR = 0;
    public static int NEW_PHACC = 0;
    public static int NEW_PHGYRO = 0;
    public static int NEW_LOC = 0;
    public static int NEW_MAIN = 0;



   /* public long writeLog(long id, ContentValues contentValues) throws Exception {
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
    }*/

    public long writeE4ACC(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writeE4ACC:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW_E4ACC) {
            id = database.insert(E4_ACC, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(E4_ACC, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }

    public long writeE4BVP(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writeE4BVP:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW_E4BVP) {
            id = database.insert(E4_BVP, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(E4_BVP, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }

    public long writeE4GSR(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writeE4GSR:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW_E4GSR) {
            id = database.insert(E4_GSR, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(E4_GSR, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }

    public long writeE4IBI(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writeE4IBI:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW_E4IBI) {
            id = database.insert(E4_IBI, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(E4_IBI, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }

    public long writeE4TEMPERATURE(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writeE4TEMPERATURE:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW_E4TEM) {
            id = database.insert(E4_TEMPERATURE, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(E4_TEMPERATURE, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }

    public long writeLocation(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writeLocation:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW_LOC) {
            id = database.insert(LocationService, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(LocationService, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }

    public long writePHACC(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writePHACC:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW_PHACC) {
            id = database.insert(Phone_ACC, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(Phone_ACC, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }

    public long writePHGYRO(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writePHGYRO:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW_PHGYRO) {
            id = database.insert(Phone_GYRO, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(Phone_GYRO, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }

    public long writeMAIN(long id, ContentValues contentValues) throws Exception {
        Log.d(TAG, "writeMAIN:");
        SQLiteDatabase database = getWritableDatabase();
        //database.beginTransaction();
        if (id == NEW_MAIN) {
            id = database.insert(MainActivity, null, contentValues);
            //database.setTransactionSuccessful();
            //database.endTransaction();
        } else {
            database.update(MainActivity, contentValues, "_id = ?", new String[]{ id + "" });
        }
        return id;
    }




    public int deleteAll() throws Exception{
        Log.d(TAG, "delete Logs Table:");
        SQLiteDatabase database = getWritableDatabase();

        database.delete(E4_TEMPERATURE,null, null);
        database.delete(E4_ACC,null, null);
        database.delete(E4_BVP,null, null);
        database.delete(E4_GSR,null, null);
        database.delete(E4_IBI,null, null);
        database.delete(LocationService,null, null);
        database.delete(Phone_GYRO,null, null);
        database.delete(Phone_ACC,null, null);
        return database.delete(MainActivity,null, null);

        // database.delete(TABLE_LOG,null, null);
    }

//    public Cursor getQueryRange() {
//        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_LOG + " WHERE " + LOG_FIELD_TYPE + " = ? ORDER BY reg ASC", new String[] { "MainActivity" });
//    }
//    public long writeReview(ContentValues contentValues) throws Exception {
//        SQLiteDatabase database = getWritableDatabase();
//        return database.replace(TABLE_REVIEW, null, contentValues);
//    }
//
//    public Cursor queryReview(String selection, String[] selectionArgs) {
//        //return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_REVIEW + " WHERE _id = ?", new String[] { id });
//        return getReadableDatabase().query(TABLE_REVIEW, null, selection, selectionArgs, null, null, null);
//    }
//
//    public Cursor queryLogCount(String[] projection, String selection, String[] selectionArgs) {
//        return getReadableDatabase().query(TABLE_LOG, projection, selection, selectionArgs, null, null, null);
//    }

    public Cursor queryE4TEMCount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(E4_TEMPERATURE, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor queryE4IBICount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(E4_IBI, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor queryE4GSRCount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(E4_GSR, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor queryE4BVPCount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(E4_BVP, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor queryE4ACCCount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(E4_ACC, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor queryPHGYROCount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(Phone_GYRO, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor queryPHACCCount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(Phone_ACC, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor queryLocationCount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(LocationService, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor queryMainCount(String[] projection, String selection, String[] selectionArgs) {
        return getReadableDatabase().query(MainActivity, projection, selection, selectionArgs, null, null, null);
    }

    public Cursor selectPhoneACC(){
        return getReadableDatabase().rawQuery( "SELECT * FROM Phone_ACC;", null);
      //  return getReadableDatabase().query(Phone_ACC, null, null, null, null, null, null);
    }

}
