/**************************************************************************************************
 * Filename:       BluetoothLeService.java
 * <p>
 * Copyright (c) 2013 - 2014 Texas Instruments Incorporated
 * <p>
 * All rights reserved not granted herein.
 * Limited License.
 * <p>
 * Texas Instruments Incorporated grants a world-wide, royalty-free,
 * non-exclusive license under copyrights and patents it now or hereafter
 * owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
 * this software subject to the terms herein.  With respect to the foregoing patent
 * license, such license is granted  solely to the extent that any such patent is necessary
 * to Utilize the software alone.  The patent license shall not apply to any combinations which
 * include this software, other than combinations with devices manufactured by or for TI ('TI Devices').
 * No hardware patent is licensed hereunder.
 * <p>
 * Redistributions must preserve existing copyright notices and reproduce this license (including the
 * above copyright notice and the disclaimer and (if applicable) source code license limitations below)
 * in the documentation and/or other materials provided with the distribution
 * <p>
 * Redistribution and use in binary form, without modification, are permitted provided that the following
 * conditions are met:
 * <p>
 * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
 * software provided in binary form.
 * any redistribution and use are licensed by TI for use only with TI Devices.
 * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.
 * <p>
 * If software source code is provided to you, modification and redistribution of the source code are permitted
 * provided that the following conditions are met:
 * <p>
 * any redistribution and use of the source code, including any resulting derivative works, are licensed by
 * TI for use only with TI Devices.
 * any redistribution and use of any object code compiled from the source code and any resulting derivative
 * works, are licensed by TI for use only with TI Devices.
 * <p>
 * Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * <p>
 * DISCLAIMER.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY TI AND TI'S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL TI AND TI'S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **************************************************************************************************/
package com.kaist.iclab.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.kaist.iclab.devices.GattInfo;

import org.apache.commons.lang3.Validate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// import android.util.Log;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    static final String TAG = "BluetoothLeService";

    public final static String ACTION_GATT_CONNECTED = "com.kaist.iclab.services.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.kaist.iclab.services.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.kaist.iclab.services.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_READ = "com.kaist.iclab.services.ACTION_DATA_READ";
    public final static String ACTION_DATA_NOTIFY = "com.kaist.iclab.services.ACTION_DATA_NOTIFY";
    public final static String EXTRA_DATA = "com.kaist.iclab.services.EXTRA_DATA";
    public final static String EXTRA_UUID = "com.kaist.iclab.services.EXTRA_UUID";
    public final static String EXTRA_DEVICE_ADDRESS = "com.kaist.iclab.services.EXTRA_DEVICE_UUID";
    public final static String EXTRA_STATUS = "com.kaist.iclab.services.EXTRA_STATUS";
    public final static int GATT_TIMEOUT = 150;

    // BLE
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBtAdapter = null;
    private static BluetoothLeService mThis = null;

    public Timer disconnectionTimer;
    private final Lock lock = new ReentrantLock();

    private volatile boolean blocking = false;
    private volatile int lastGattStatus = 0; //Success

    private volatile bleRequest curBleRequest = null;

    public enum bleRequestOperation {
        wrBlocking,
        wr,
        rdBlocking,
        rd,
        nsBlocking,
    }

    public enum bleRequestStatus {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        no_such_request,
        failed,
    }

    @Override
    public void onCreate() {
        //Logger.initializeDB(this);
        super.onCreate();
    }

    public class bleRequest {
        public int id;
        public BluetoothGattCharacteristic characteristic;
        public bleRequestOperation operation;
        public volatile bleRequestStatus status;
        public int timeout;
        public int curTimeout;
        public boolean notifyenable;
        public String address;
    }

    // Queuing for fast application response.
    private volatile LinkedList<bleRequest> procQueue;
    private volatile LinkedList<bleRequest> nonBlockQueue;

    //


    private void unlockBlockingThread(int status) {
        this.lastGattStatus = status;
        this.blocking = false;
    }

    public boolean checkGatt(String address) {
        Validate.notNull(getBluetoothAdapter());
        getBluetoothGatt(address);
        if (this.blocking) {
            Log.d(TAG, "Cannot start operation : Blocked");
            return false;
        }
        return true;
    }

    /**
     * Manage the BLE service
     */
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular example,
        // close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder binder = new LocalBinder();

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        mThis = this;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            Validate.notNull(mBluetoothManager);
        }

        mBtAdapter = mBluetoothManager.getAdapter();
        if (getBluetoothAdapter() == null) {
            return false;
        }

        procQueue = new LinkedList<bleRequest>();
        nonBlockQueue = new LinkedList<bleRequest>();


        Thread queueThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    executeQueue();
                    try {
                        Thread.sleep(0, 100000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        queueThread.start();
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.initialize();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Map.Entry<String, BluetoothGatt> entry : bluetoothGattMap.entrySet()) {
            entry.getValue().close();
        }
        //Logger.closeDB();
    }

    public int writeCharacteristic(String address,
                                   BluetoothGattCharacteristic characteristic, byte b) {

        byte[] val = new byte[1];
        val[0] = b;
        characteristic.setValue(val);

        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wrBlocking;
        req.address = address;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            } else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public int writeCharacteristic(String address,
                                   BluetoothGattCharacteristic characteristic, byte[] b) {
        characteristic.setValue(b);
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wrBlocking;
        req.address = address;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            } else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    public int getNumServices(String address) {
        return getBluetoothGatt(address).getServices().size();
    }


    public List<BluetoothGattService> getSupportedGattServices(String address) {
        return getBluetoothGatt(address).getServices();
    }

    public int setCharacteristicNotification(String address,
                                             BluetoothGattCharacteristic characteristic, boolean enable) {
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.nsBlocking;
        req.notifyenable = enable;
        req.address = address;
        addRequestToQueue(req);
        boolean finished = false;
        while (!finished) {
            bleRequestStatus stat = pollForStatusofRequest(req);
            if (stat == bleRequestStatus.done) {
                finished = true;
                return 0;
            } else if (stat == bleRequestStatus.timeout) {
                finished = true;
                return -3;
            }
        }
        return -2;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        return mBtAdapter;
    }

    private static Map<String, BluetoothGatt> bluetoothGattMap = new HashMap<>();

    public boolean connect(final String address) {
        Validate.notEmpty(address);
        Validate.notNull(getBluetoothAdapter());

        final BluetoothDevice device = getBluetoothAdapter().getRemoteDevice(address);
        int connectionState = mBluetoothManager.getConnectionState(device,
                BluetoothProfile.GATT);
        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            if (device == null) {
                return false;
            }
            BluetoothGatt gatt = device.connectGatt(this, false, mGattCallbacks);
            addBluetoothGatt(gatt);
            //Logger._debug(Logger.DOMAIN_BLUETOOTH, address, "create new device connection");
        }
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(String address) {
        if (getBluetoothAdapter() == null) {
            // Log.w(TAG, "disconnect: BluetoothAdapter not initialized");
            return;
        }
        final BluetoothDevice device = getBluetoothAdapter().getRemoteDevice(address);
        int connectionState = mBluetoothManager.getConnectionState(device,
                BluetoothProfile.GATT);

        if (connectionState != BluetoothProfile.STATE_DISCONNECTED) {
            //Logger._debug(Logger.DOMAIN_BLUETOOTH, address, "disconnect device");
            removeGatt(address);
        } else {
            // Log.w(TAG, "Attempt to disconnect in state: " + connectionState);
        }
    }

    public static BluetoothGatt getBluetoothGatt(String address) {
        Validate.isTrue(bluetoothGattMap.containsKey(address));
        return bluetoothGattMap.get(address);
    }

    private void addBluetoothGatt(BluetoothGatt gatt) {
        String address = gatt.getDevice().getAddress();
        bluetoothGattMap.put(address, gatt);
    }

    private void removeGatt(String address) {
        Validate.isTrue(bluetoothGattMap.containsKey(address));
        BluetoothGatt gatt = bluetoothGattMap.get(address);
        gatt.disconnect();
        gatt.close();
        bluetoothGattMap.remove(address);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        for (String address : bluetoothGattMap.keySet()) {
            removeGatt(address);
        }
    }

    public int numConnectedDevices() {
        if (!bluetoothGattMap.isEmpty()) {
            List<BluetoothDevice> devList;
            devList = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
            return devList.size();
        }
        return 0;
    }

    //
    // Utility functions
    //

    public static BluetoothManager getBtManager() {
        return mThis.mBluetoothManager;
    }

    public static BluetoothLeService getInstance() {
        return mThis;
    }

    public void waitIdle(int timeout) {
        while (timeout-- > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }

    public void abortTimedDisconnect() {
        if (this.disconnectionTimer != null) {
            this.disconnectionTimer.cancel();
        }
    }

    private boolean addRequestToQueue(bleRequest req) {
        lock.lock();
        if (procQueue.peekLast() != null) {
            req.id = procQueue.peek().id++;
        } else {
            req.id = 0;
            procQueue.add(req);
        }
        lock.unlock();
        return true;
    }

    public bleRequestStatus pollForStatusofRequest(bleRequest req) {
        lock.lock();
        if (req == curBleRequest) {
            bleRequestStatus stat = curBleRequest.status;
            if (stat == bleRequestStatus.done) {
                curBleRequest = null;
            }
            if (stat == bleRequestStatus.timeout) {
                curBleRequest = null;
            }
            lock.unlock();
            return stat;
        } else {
            lock.unlock();
            return bleRequestStatus.no_such_request;
        }
    }

    private void executeQueue() {
        // Everything here is done on the queue
        lock.lock();
        if (curBleRequest != null) {
            Log.d(TAG, "executeQueue, curBleRequest running");
            try {
                curBleRequest.curTimeout++;
                if (curBleRequest.curTimeout > GATT_TIMEOUT) {
                    curBleRequest.status = bleRequestStatus.timeout;
                    curBleRequest = null;
                }
                Thread.sleep(10, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            lock.unlock();
            return;
        }
        if (procQueue == null) {
            lock.unlock();
            return;
        }
        if (procQueue.size() == 0) {
            lock.unlock();
            return;
        }
        bleRequest procReq = procQueue.removeFirst();

        switch (procReq.operation) {
            case rd:
                //Read, do non blocking read
                break;
            case rdBlocking:
                //Normal (blocking) read
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                procReq.curTimeout = 0;
                curBleRequest = procReq;
                int stat = sendBlockingReadRequest(procReq);
                if (stat == -2) {
                    Log.d(TAG, "executeQueue rdBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            case wr:
                //Write, do non blocking write (Ex: OAD)
                nonBlockQueue.add(procReq);
                sendNonBlockingWriteRequest(procReq);
                break;
            case wrBlocking:
                //Normal (blocking) write
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                curBleRequest = procReq;
                stat = sendBlockingWriteRequest(procReq);
                if (stat == -2) {
                    Log.d(TAG, "executeQueue wrBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            case nsBlocking:
                if (procReq.timeout == 0) {
                    procReq.timeout = GATT_TIMEOUT;
                }
                curBleRequest = procReq;
                stat = sendBlockingNotifySetting(procReq);
                if (stat == -2) {
                    Log.d(TAG, "executeQueue nsBlocking: error, BLE was busy or device disconnected");
                    lock.unlock();
                    return;
                }
                break;
            default:
                break;

        }
        lock.unlock();
    }

    public int sendNonBlockingWriteRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        String address = request.address;
        if (!checkGatt(address)) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        getBluetoothGatt(address).writeCharacteristic(request.characteristic);
        return 0;
    }

    public int sendBlockingReadRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        String address = request.address;
        if (!checkGatt(address)) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        getBluetoothGatt(address).readCharacteristic(request.characteristic);
        this.blocking = true; // Set read to be blocking
        while (this.blocking) {
            timeout++;
            waitIdle(1);
            if (timeout > GATT_TIMEOUT) {
                this.blocking = false;
                request.status = bleRequestStatus.timeout;
                return -1;
            }  //Read failed TODO: Fix this to follow connection interval !
        }
        request.status = bleRequestStatus.done;
        return lastGattStatus;
    }

    public int sendBlockingWriteRequest(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        String addres = request.address;
        if (!checkGatt(addres)) {
            request.status = bleRequestStatus.failed;
            return -2;
        }
        getBluetoothGatt(addres).writeCharacteristic(request.characteristic);
        this.blocking = true; // Set read to be blocking
        while (this.blocking) {
            timeout++;
            waitIdle(1);
            if (timeout > GATT_TIMEOUT) {
                this.blocking = false;
                request.status = bleRequestStatus.timeout;
                return -1;
            }  //Read failed TODO: Fix this to follow connection interval !
        }
        request.status = bleRequestStatus.done;
        return lastGattStatus;
    }

    public int sendBlockingNotifySetting(bleRequest request) {
        request.status = bleRequestStatus.processing;
        int timeout = 0;
        if (request.characteristic == null) {
            return -1;
        }
        String address = request.address;
        if (!checkGatt(address))
            return -2;

        BluetoothGatt gatt = getBluetoothGatt(address);

        if (gatt .setCharacteristicNotification(request.characteristic, request.notifyenable)) {

            BluetoothGattDescriptor clientConfig = request.characteristic
                    .getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
            if (clientConfig != null) {

                if (request.notifyenable) {
                    // Log.i(TAG, "Enable notification: " +
                    // characteristic.getUuid().toString());
                    clientConfig
                            .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    // Log.i(TAG, "Disable notification: " +
                    // characteristic.getUuid().toString());
                    clientConfig
                            .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                gatt.writeDescriptor(clientConfig);
                // Log.i(TAG, "writeDescriptor: " +
                // characteristic.getUuid().toString());
                this.blocking = true; // Set read to be blocking
                while (this.blocking) {
                    timeout++;
                    waitIdle(1);
                    if (timeout > GATT_TIMEOUT) {
                        this.blocking = false;
                        request.status = bleRequestStatus.timeout;
                        return -1;
                    }  //Read failed TODO: Fix this to follow connection interval !
                }
                request.status = bleRequestStatus.done;
                return lastGattStatus;
            }
        }
        return -3; // Set notification to android was wrong ...
    }

    /**
     * GATT client callbacks
     */
    private BluetoothGattCallback mGattCallbacks = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (gatt == null) {
                // Log.e(TAG, "mBluetoothGatt not created!");
                return;
            }

            BluetoothDevice device = gatt.getDevice();
            String address = device.getAddress();
            // Log.d(TAG, "onConnectionStateChange (" + address + ") " + newState +
            // " status: " + status);

            try {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        broadcastUpdate(ACTION_GATT_CONNECTED, status, address);
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        broadcastUpdate(ACTION_GATT_DISCONNECTED, status, address);
                        break;
                    default:
                        // Log.e(TAG, "New state not processed: " + newState);
                        break;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, status, gatt.getDevice().getAddress(), null);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_NOTIFY, BluetoothGatt.GATT_SUCCESS, gatt.getDevice().getAddress(), characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (blocking) unlockBlockingThread(status);
            if (nonBlockQueue.size() > 0) {
                lock.lock();
                for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
                    bleRequest req = nonBlockQueue.get(ii);
                    if (req.characteristic == characteristic) {
                        req.status = bleRequestStatus.done;
                        nonBlockQueue.remove(ii);
                        break;
                    }
                }
                lock.unlock();
            }
            broadcastUpdate(ACTION_DATA_READ, status, gatt.getDevice().getAddress(), characteristic);
        }

        private void broadcastUpdate(final String action, final int status, final String deviceAddress) {
            broadcastUpdate(action, status, deviceAddress, null);
        }

        private void broadcastUpdate(final String action, final int status, final String deviceAddress, final BluetoothGattCharacteristic characteristic) {
            //Log.d(TAG, String.format("broadcast action = %s, status = %s, deviceAddress = %s",action, status, deviceAddress));
            final Intent intent = new Intent(action);
            intent.putExtra(EXTRA_STATUS, status);
            intent.putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress);
            if (characteristic != null) {
                intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
                intent.putExtra(EXTRA_DATA, characteristic.getValue());
            }
            sendBroadcast(intent);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (blocking) unlockBlockingThread(status);
            if (nonBlockQueue.size() > 0) {
                lock.lock();
                for (int ii = 0; ii < nonBlockQueue.size(); ii++) {
                    bleRequest req = nonBlockQueue.get(ii);
                    if (req.characteristic == characteristic) {
                        req.status = bleRequestStatus.done;
                        nonBlockQueue.remove(ii);
                        break;
                    }
                }
                lock.unlock();
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            if (blocking) unlockBlockingThread(status);
            unlockBlockingThread(status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            if (blocking) unlockBlockingThread(status);
            // Log.i(TAG, "onDescriptorWrite: " + descriptor.getUuid().toString());
        }
    };
}
