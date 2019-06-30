package com.kaist.iclab.services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.Service;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.goebl.david.Webb;
import com.kaist.iclab.R;
import com.kaist.iclab.activity.MainActivity;
import com.kaist.iclab.datamanager.DAO;
import com.kaist.iclab.datamanager.DataProvider;
import com.kaist.iclab.datamanager.DatabaseHandler;
import com.kaist.iclab.util.Constants;

import org.json.JSONObject;

import java.util.Date;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 100;
    private static final float LOCATION_DISTANCE = 1f; //minimum distance between location updates, in milliseconds

    private int sensingCount = 0;
    private NotificationManager nManager = null;
    private NotificationCompat.Builder ncomp = null;
    private static int NOTIFYID_SMARTPHONE = 20180317;

    private String deviceSetName ="";
    private String smartphoneMode = "";


    private int accDelta=0;
    private int gyroDelta=0;
    private int locDelta=0;
    private String CHANNEL_ID = "LOCATION_SERVICE";

    private PowerManager mPowermanager;
    private PowerManager.WakeLock mWakeLock;

    private String mNotiMessage = "";

    private ContentValues[] multipleSensorData;
    private int BULK_COUNT=0;

    String serverURL = "http://168.188.127.108:5555/AppRating/FileReceiver.jsp";

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.d(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + location);
            onLocationEvent(location, mLastLocation);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private SensorManager sensorManager;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        deviceSetName = intent.getStringExtra("device_set_name");
        smartphoneMode = intent.getStringExtra("smartphone_mode");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "gps provider does not exist " + ex.getMessage());
        }
        try {
            sensorManager = (SensorManager)getApplicationContext().getSystemService(SENSOR_SERVICE);
            sensorManager.registerListener(sensorEventListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(sensorEventListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_NORMAL);
        } catch (Exception e) {}


        Enable();
        initAckDelta();
    }

    public void initAckDelta(){
        accDelta = 0;
        gyroDelta = 0;
        locDelta = 0;
    }

    SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                getSensor(event);
            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                getSensor(event);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, sensor.toString());
        }
    };

    private void getSensor(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];
        long actualTime = event.timestamp;

        accDelta++;
        gyroDelta++;

        try {
            ContentValues cv = new ContentValues();
            String type = event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ? "Phone.ACC" : "Phone.GYRO";
            cv.put(DAO.LOG_FIELD_TYPE, type);
            cv.put(DAO.LOG_FIELD_REG, new Date().getTime());
            JSONObject json = new JSONObject();
            json.put("x", x);
            json.put("y", y);
            json.put("z", z);
            json.put("time", actualTime);
            cv.put(DAO.LOG_FIELD_JSON, json.toString());

            // bulk insert to improve async_query_handler
            if (BULK_COUNT < Constants.DB_BULK_RATE -1){
                multipleSensorData[BULK_COUNT] = cv;
                BULK_COUNT++;
            }else{
                multipleSensorData[BULK_COUNT] = cv;
                BULK_COUNT = 0;
                final DatabaseHandler handler = new DatabaseHandler(getContentResolver());
                handler.startBulkInsert(1, null, DataProvider.CONTENT_URI_LOG, multipleSensorData);
            }
            //AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {};
            //handler.startInsert(-1, null, DataProvider.CONTENT_URI_LOG, cv);
            mNotiMessage = "X,Y,Z: " + x + ", " + y + ", " + z;
            Log.d(TAG, type + ": X,Y,Z: " + x + ", " + y + ", " + z);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }



    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.e(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
        try {
            sensorManager.unregisterListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            sensorManager.unregisterListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
            sensorManager.unregisterListener(sensorEventListener);
            sensorManager = null;
        } catch (Exception e) {}

        Disable(); // notification
    }

    private void Enable(){
        InitNotification();
        mNotificationUpdateHandler.postDelayed(UpdateNotification, Constants.UPDATE_INTERVAL);

        // WakeLock code section
        mPowermanager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowermanager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WakeAlways");
        mWakeLock.acquire();        ncomp = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        // Array of Content Values Init.
        multipleSensorData = new ContentValues[Constants.DB_BULK_RATE];

    }
    private void Disable(){
        nManager.cancel(NOTIFYID_SMARTPHONE);
        mNotificationUpdateHandler.removeCallbacks(UpdateNotification);

        if (mWakeLock != null){
            mWakeLock.release();
            mWakeLock = null;
        }
    }


    // Notification Init.
    private void InitNotification()
    {
        nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        ncomp = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        ncomp.setContentTitle(getResources().getString(R.string.app_title));
        ncomp.setContentText(getResources().getString(R.string.notify_text));
        ncomp.setTicker("Phone Sensing");
        ncomp.setSmallIcon(R.mipmap.sensing_icon);
        ncomp.setAutoCancel(false);
        ncomp.setOngoing(true);

        nManager.notify(NOTIFYID_SMARTPHONE, ncomp.build());
    }

    // Show service status
    private Handler mNotificationUpdateHandler = new Handler();
    private Runnable UpdateNotification = new Runnable()
    {
        @Override
        public void run() {
            ncomp.setContentTitle("Phone sensing: "+ splitToComponentTimes(sensingCount += Constants.ACK_RATE));
            ncomp.setContentText("accD:"+ accDelta+" gyroD:"+gyroDelta+" locD:"+locDelta);
            ncomp.setSmallIcon(R.mipmap.sensing_icon);
            ncomp.setAutoCancel(false);
            ncomp.setOngoing(true);
            nManager.notify(NOTIFYID_SMARTPHONE, ncomp.build());


            // Send ACK message
            new Thread() {
                public void run() {
                    Webb webb = Webb.create();
                    try {
                        JSONObject body = new JSONObject();
                        body.put("setNumber", deviceSetName);
                        body.put("deviceType", smartphoneMode);
                        body.put("accDelta", accDelta);
                        body.put("gyroDelta", gyroDelta);
                        body.put("locDelta", locDelta);

                        JSONObject result = webb.post(Constants.SERVERL_URL_ACK)
                                .useCaches(false)
                                //.param("data_type",MainActivity.deviceSetName)
                                .body(body)
                                .ensureSuccess()
                                .asJsonObject()
                                .getBody();

                        if (result == null || result.getString("result").equals("OK") == false)
                            throw new Exception("Server Error");

                    }catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                    initAckDelta(); // reset ack messages
                }
            }.start();


            //initAckDelta(); // reset ack messages
            mNotificationUpdateHandler.postDelayed(UpdateNotification, Constants.UPDATE_INTERVAL);
        }
    };

    private String splitToComponentTimes(int duration) {
        int hours = (int) duration / 3600;
        int remainder = (int) duration - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return String.format("%d:%d:%d", ints[0], ints[1], ints[2]);
    }

    private void initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void onLocationEvent(Location location, Location lastLocation) {
        Log.d(TAG, "onLocationChanged: " + location);
        try {
            locDelta++;

            ContentValues values = new ContentValues();
            values.put(DAO.LOG_FIELD_TYPE, "LocationService");
            values.put(DAO.LOG_FIELD_REG, new Date().getTime());
            JSONObject json = new JSONObject();
            json.put("Provider", location.getProvider());
            json.put("Time", location.getTime());
            json.put("Accuracy", location.getAccuracy());
            json.put("Bearing", location.getBearing());
            json.put("Latitude", location.getLatitude());
            json.put("Longitude", location.getLongitude());
            json.put("Speed", location.getSpeed());

            values.put(DAO.LOG_FIELD_JSON, json.toString());

            AsyncQueryHandler handler = new AsyncQueryHandler(getContentResolver()) {
            };
            handler.startInsert(-1, null, DataProvider.CONTENT_URI_LOG, values);

            broadcastMessage("Location", location.toString());
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private void broadcastMessage(String type, String message) {
        Intent intent = new Intent("SensorService");
        intent.putExtra("type", type);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);
    }
}
