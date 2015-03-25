package com.example.brentonchasse.myapplication;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

public class BleService extends Service {

    private final IBinder mBinder = new LocalBinder();

    private static final String TAG = BleService.class.getSimpleName();
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private static int mConnectionState = STATE_DISCONNECTED;
    private BluetoothGattService mService;

    private static UUID mPrefUUIDService;

    /**
     * Commands
     */
    static final int MSG_DISCONNECTED = 0;
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_VALUE = 3;
    static final int MSG_CONNECTED = 4;
    static final int MSG_SERVICES_DISCOVERED = 5;
    static final int MSG_SET_SERVICE_UUID = 6;
    static final int MSG_CHARACTERISTIC_READ = 7;
    static final int MSG_DESCRIPTOR_READ = 8;
    static final int MSG_CHARACTERISTIC_CHANGED = 9;

    private static ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    public final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;
                for (int i=mClients.size()-1; i>=0; i--) {
                    try {
                        mClients.get(i).send(Message.obtain(null, MSG_CONNECTED, gatt));
                    } catch (RemoteException e) {
                        // The client is dead.  Remove it from the list;
                        // we are going through the list from back to front
                        // so this is safe to do inside the loop.
                        mClients.remove(i);
                    }
                }
                Log.i(TAG, "Connected to GATT server.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                for (int i=mClients.size()-1; i>=0; i--) {
                    try {
                        mClients.get(i).send(Message.obtain(null, MSG_DISCONNECTED, gatt));
                    } catch (RemoteException e) {
                        // The client is dead.  Remove it from the list;
                        // we are going through the list from back to front
                        // so this is safe to do inside the loop.
                        mClients.remove(i);
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mService = gatt.getService(mPrefUUIDService);
                Log.i(TAG, "Status onServiceDiscovered: " + status);
                if (mService != null) {
                    //TODO: send message to mainActivity saying that we have successfully connected and discovered services.
                    for (int i=mClients.size()-1; i>=0; i--) {
                        try {
                            final Message msg = Message.obtain(null, MSG_SERVICES_DISCOVERED, mService);
                            mClients.get(i).send(msg);
                        } catch (RemoteException e) {
                            // The client is dead.  Remove it from the list;
                            // we are going through the list from back to front
                            // so this is safe to do inside the loop.
                            mClients.remove(i);
                        }
                    }
                } else {
                }
                Log.i(TAG, "Status onServiceDiscovered: " + status);

            } else {
                Log.i(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (int i=mClients.size()-1; i>=0; i--) {
                    try {
                        final Message msg = Message.obtain(null, MSG_CHARACTERISTIC_READ, characteristic);
                        mClients.get(i).send(msg);
                    } catch (RemoteException e) {
                        // The client is dead.  Remove it from the list;
                        // we are going through the list from back to front
                        // so this is safe to do inside the loop.
                        mClients.remove(i);
                    }
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
            for (int i=mClients.size()-1; i>=0; i--) {
                try {
                    final Message msg = Message.obtain(null, MSG_DESCRIPTOR_READ, descriptor);
                    mClients.get(i).send(msg);
                } catch (RemoteException e) {
                    // The client is dead.  Remove it from the list;
                    // we are going through the list from back to front
                    // so this is safe to do inside the loop.
                    mClients.remove(i);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            for (int i=mClients.size()-1; i>=0; i--) {
                try {
                    final Message msg = Message.obtain(null, MSG_CHARACTERISTIC_CHANGED, characteristic);
                    mClients.get(i).send(Message.obtain(null, MSG_CHARACTERISTIC_CHANGED, characteristic));
                } catch (RemoteException e) {
                    // The client is dead.  Remove it from the list;
                    // we are going through the list from back to front
                    // so this is safe to do inside the loop.
                    mClients.remove(i);
                }
            }
        }
    };

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        BleService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mBinder;
    }

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_SET_VALUE:
                    for (int i=mClients.size()-1; i>=0; i--) {
                        try {
                            final Message mesg = Message.obtain(null, MSG_SET_VALUE, msg.arg1, 0);
                            mClients.get(i).send(mesg);
                        } catch (RemoteException e) {
                            // The client is dead.  Remove it from the list;
                            // we are going through the list from back to front
                            // so this is safe to do inside the loop.
                            mClients.remove(i);
                        }
                    }
                    break;
                case MSG_SET_SERVICE_UUID:
                    mPrefUUIDService = (UUID) msg.obj;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
}
