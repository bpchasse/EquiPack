package com.example.brentonchasse.myapplication;
import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.os.Handler;
import android.widget.Toast;

import java.nio.channels.ScatteringByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                                                      BleFragment.OnFragmentInteractionListener,
                                                      WeightFragment.OnFragmentInteractionListener,
                                                      FeedbackFragment.OnFragmentInteractionListener,
                                                      DashboardFragment.OnFragmentInteractionListener,
                                                      SettingsFragment.OnFragmentInteractionListener {
    BleService mBle = null;
    boolean mBleBound = false;
    Messenger mBleMessenger = null;
    boolean mConnected = false;
    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    NavigationDrawerFragment mNavigationDrawerFragment;
    BleFragment BleFrag = new BleFragment();
    WeightFragment WeightFrag = new WeightFragment();
    FeedbackFragment FeedbackFrag = new FeedbackFragment();
    DashboardFragment DashboardFrag = new DashboardFragment();
    SettingsFragment SettingsFrag = new SettingsFragment();
    EquipackAnalytics analytics = new EquipackAnalytics(DashboardFrag);

    private Fragment currentFrag;
    private String currentFragTag;

    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattService mService;
    private Handler mHandler;
    private FragmentManager mFragmentManager;


    private BluetoothGattCharacteristic mCharacteristicRead;
    private BluetoothGattCharacteristic mCharacteristicWrite;
    private byte[] mCharacteristicValue;
    private boolean[] mCharacteristicPermissions = new boolean[3];

    private boolean mScanning;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;

    public UUID mPrefUUIDService;
    public UUID mPrefUUIDCharacteristicRead;
    public UUID mPrefUUIDCharacteristicWrite;
    public String mDeviceName;
    public String mPrefUUIDServiceString;
    public String mPrefUUIDCharacteristicReadString;
    public String mPrefUUIDCharacteristicWriteString;

    public byte[] cTurnOnPower;
    public byte[] cGetWeightData;
    public byte[][] cSensor = new byte[8][];
    public int[] mSensorData = {-1,-1,-1,-1,-1,-1,-1,-1};
    public int mCurrentSensorNumber = 0;

    public byte[] mPrefWriteValue;
    public boolean polling = false;
    public boolean waitingForPower = false;
    public int mPrefWeight;

    private final Semaphore available = new Semaphore(1);
    private final Semaphore logLock = new Semaphore(1);
    private final Semaphore getWeightLock = new Semaphore(1);
    private final Semaphore handlerLock = new Semaphore(1);
    private final Semaphore getSensorLock = new Semaphore(1);

    private final static int NUMBER_OF_SAMPLES_PER_CYCLE = 10;
    private double ZERO_WEIGHTB = 50.5;
    private boolean doCalibrate = false;
    private String SCANNING_MODE = "";
    public List<Double> weightDataCycle = new ArrayList<Double>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        mHandler = new Handler();
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mFragmentManager = getFragmentManager();
        mTitle = getTitle();
        cTurnOnPower = hexStringToByteArray("0301000000000000000000000000000000000000");
        cGetWeightData = hexStringToByteArray("0408000000000000000000000000000000000000");
        cSensor[0] = hexStringToByteArray("04000D0000000000000000000000000000000000");
        cSensor[1] = hexStringToByteArray("04010D0000000000000000000000000000000000");
        cSensor[2] = hexStringToByteArray("04020D0000000000000000000000000000000000");
        cSensor[3] = hexStringToByteArray("04030D0000000000000000000000000000000000");
        cSensor[4] = hexStringToByteArray("04040D0000000000000000000000000000000000");
        cSensor[5] = hexStringToByteArray("04050D0000000000000000000000000000000000");
        cSensor[6] = hexStringToByteArray("04060D0000000000000000000000000000000000");
        cSensor[7] = hexStringToByteArray("04070D0000000000000000000000000000000000");


        // Set up the Navigation drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        //Restore preferences
        SharedPreferences myPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        mDeviceName = myPreferences.getString(
                getString(R.string.settings_device_name_key),
                getString(R.string.app_name));
        mPrefUUIDCharacteristicReadString = myPreferences.getString(
                getString(R.string.settings_UUIDCharacteristicR_key),
                getString(R.string.settings_UUIDCharacteristicR_default));
        mPrefUUIDCharacteristicRead = UUID.fromString(mPrefUUIDCharacteristicReadString);
        mPrefUUIDCharacteristicWriteString = myPreferences.getString(
                getString(R.string.settings_UUIDCharacteristicW_key),
                getString(R.string.settings_UUIDCharacteristicW_default));
        mPrefUUIDCharacteristicWrite = UUID.fromString(mPrefUUIDCharacteristicWriteString);
        mPrefUUIDServiceString = myPreferences.getString(
                getString(R.string.settings_UUIDService_key),
                getString(R.string.settings_UUIDService_default));
        mPrefUUIDService = UUID.fromString(mPrefUUIDServiceString);
        mPrefWeight = myPreferences.getInt(
                getString(R.string.settings_weight_key),
                Integer.parseInt(getString(R.string.settings_weight_default)));
        mPrefWriteValue = hexStringToByteArray(myPreferences.getString(
                getString(R.string.settings_writeValue_key),
                getString(R.string.settings_writeValue_default)));
        WeightFrag.setWeightInKg(myPreferences.getBoolean(getString(R.string.settings_weightKg_key),
                Boolean.parseBoolean(getString(R.string.settings_weightKg_default))));

        //Initialize Bluetooth adapter
        //enableBLEThenScan();
        //sendSMS("8603028885", "Equipack says 'Hello'");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService within BleService
        doBindService();
    }

    void doBindService() {
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    void doUnbindService() {
        if (mBleBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mBleMessenger != null) {
                try {
                    Message msg = Message.obtain(null,
                            BleService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mBleMessenger.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mBleBound = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
        //Store the preferences in case they may have changes
        SharedPreferences myPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = myPreferences.edit();
        editor.putString(getString(R.string.settings_device_name_key), mDeviceName);
        editor.apply();
    }

    private Runnable makeRunner(final Callable<Void> func) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    func.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(final Message msg) {
            final Message message = msg;
            final int what = msg.what;
            final Object obj = msg.obj;
            //Handle the message on a different thread (not the UI thread)
            //  Trust that any UI changes determined by the handler will be explicitly run on the UI thread
            myThread t = new myThread(what, obj);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.execute(t);
        }
    }

    final class myThread implements Runnable{
        private int w;
        private Object o;

        public myThread(int what, Object obj) {
            this.w = what;
            this.o = obj;
        }

        @Override
        public void run() {
            try {
                handlerLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            switch (this.w) {
                case BleService.MSG_CONNECTED:
                    connectedHandler(this.o);
                    break;
                case BleService.MSG_SERVICES_DISCOVERED:
                    discoveredServiceHandler(this.o);
                    break;
                case BleService.MSG_CHARACTERISTIC_READ:
                    characteristicReadHandler(this.o);
                    break;
                case BleService.MSG_DESCRIPTOR_READ:
                    descriptorReadHandler(this.o);
                    break;
                case BleService.MSG_CHARACTERISTIC_CHANGED:
                    updateHandler(this.o);
                    break;
                case BleService.MSG_DISCONNECTED:
                    disconnectedHandler(this.o);
                    break;
            }
            handlerLock.release();
        }
    }

    private void connectedHandler(Object obj) {
        mConnected = true;
        if(mBleMessenger != null) {
            mBluetoothGatt = (BluetoothGatt) obj;
            try {
                mBleMessenger.send(Message.obtain(null, BleService.MSG_SET_SERVICE_UUID, mPrefUUIDService));
                mBluetoothGatt.discoverServices();
                //Set button back to "Discovering" text
                if(SCANNING_MODE.equals("getWeight")) {
                    WeightFrag.setGetWeightBtnTxt("Discovering...");
                }else if(SCANNING_MODE.equals("getSensors")) {
                    DashboardFrag.setMessageText("Connected");
                    DashboardFrag.setAddDataBtnEnabled(true, "STOP!");
                    DashboardFrag.setAddDataBtnColor(R.drawable.rounded_corner_red);
                }
            } catch (RemoteException e) {
                mBleMessenger = null;
            }
        }
    }
    private void discoveredServiceHandler(Object obj) {
        mService = (BluetoothGattService) obj;
        if (SCANNING_MODE.equals("testBLE")) {
            //Perform a deeper investigation of the preferred service
            logBle("onServicesDiscovered(): Getting \"Read\" and \"Write\" characteristics of service: " + mPrefUUIDServiceString + "\n"
                    + "          Read UUID: " + mPrefUUIDCharacteristicReadString + "\n" + "         Write UUID: " + mPrefUUIDCharacteristicWriteString + "\n");
            mCharacteristicRead = mService.getCharacteristic(mPrefUUIDCharacteristicRead);
            mCharacteristicWrite = mService.getCharacteristic(mPrefUUIDCharacteristicWrite);

            checkCharacteristicProperties(mCharacteristicRead, "Read");
            checkCharacteristicProperties(mCharacteristicWrite, "Write");

            //Read the preferred characteristic of the preferred service
            if (mCharacteristicRead != null && isCharacteristicReadable(mCharacteristicRead)) {
                try {
                    logBle("onServicesDiscovered(): Reading \"Read\" characteristic\n");
                    mBluetoothGatt.readCharacteristic(mCharacteristicRead);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mCharacteristicWrite != null && isCharacteristicReadable(mCharacteristicWrite)) {
                try {
                    logBle("onServicesDiscovered(): Reading \"Write\" characteristic\n");
                    mBluetoothGatt.readCharacteristic(mCharacteristicWrite);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (SCANNING_MODE.equals("getWeight") || SCANNING_MODE.equals("getSensors")) {
            mCharacteristicRead = mService.getCharacteristic(mPrefUUIDCharacteristicRead);
            mCharacteristicWrite = mService.getCharacteristic(mPrefUUIDCharacteristicWrite);
            boolean isNotifiable = isCharacterisiticNotifiable(mCharacteristicRead);
            boolean isWritable = isCharacteristicWriteable(mCharacteristicWrite);
            //Read the preferred characteristic of the preferred service
            if (mCharacteristicRead != null && mCharacteristicWrite != null) {
                try {
                    setNotifications(mCharacteristicRead, true);
                    //Set button back to "Setting up" text
                    if(SCANNING_MODE.equals("getSensors")){
                        DashboardFrag.setMessageText("Setting up...");
                    } else if(SCANNING_MODE.equals("getWeight")){
                        WeightFrag.setGetWeightBtnTxt("Setting up...");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void characteristicReadHandler(Object obj) {
        BluetoothGattCharacteristic chara = (BluetoothGattCharacteristic) obj;
        mCharacteristicValue = chara.getValue();
        logBle("onCharacteristicRead(): Successfully read Characteristic:\n                       "
                + chara.getUuid().toString() + "\n" + "                       Value:  0x"
                + bytesToHex(mCharacteristicValue) + "\n");
        if (SCANNING_MODE.equals("testBLE"))
            BleFrag.enableNotify(true);
    }
    private void descriptorReadHandler(Object obj) {
        BluetoothGattDescriptor descriptor = (BluetoothGattDescriptor) obj;
        logBle("onDescriptorWrite:\n                    Descriptor UUID: " + descriptor.getUuid() +
                "\n                    Has a new value of: "
                + bytesToHex(descriptor.getValue()) + "\n");
        if (SCANNING_MODE.equals("testBLE")) {
            BleFrag.enableWrite(true);
            BleFrag.enablePoll(true);
        } else if (SCANNING_MODE.equals("getWeight")) {
            //waitingForPower = true;
            //setCharacteristic(mCharacteristicWrite, cTurnOnPower); //turn on power
            setCharacteristic(mCharacteristicWrite, cGetWeightData);
            //Set button back to "Powering" text
            WeightFrag.setGetWeightBtnTxt("Powering up...");
        } else if (SCANNING_MODE.equals("getSensors")) {
            setCharacteristic(mCharacteristicWrite, cSensor[mCurrentSensorNumber]);
        }
    }
    private void updateHandler(Object obj){
        BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) obj;
        String stringVal = new String(characteristic.getValue());
        byte[] byteVal= characteristic.getValue();
        String hexVal = bytesToHex(byteVal);

        if(hexVal.charAt(0) == 'A') {
            double sensor1 = ( (double)( ( ( (int)byteVal[1] & 0xFF ) * 256 ) + ( (int)byteVal[2] & 0xFF ) ) * 3.3f ) / 4096f;
            double sensor2 = ( (double)( ( ( (int)byteVal[3] & 0xFF ) * 256 ) + ( (int)byteVal[4] & 0xFF ) ) * 3.3f ) / 4096f;

            if (hexVal.charAt(1) == '1') {
                logBle("onCharacteristicChanged:\n                   Characteristic UUID: " + characteristic.getUuid()
                        + "\n                   Return OpCode: 0xA1 == Test"
                        + "\n                   Return Data: "
                        + hexVal.substring(2, hexVal.length()) + "\n");
            } else if (hexVal.charAt(1) == '2') {
                logSet("onCharacteristicChanged:\n                   Characteristic UUID: " + characteristic.getUuid()
                        + "\n                   Return OpCode: 0xA2 == Read ADCs"
                        + "\n                   Return Data:"
                        + "\n                           Sensor 1: " + sensor1 + "V"
                        + "\n                           Sensor 2: " + sensor2 + "V\n");
                if(polling) {
                    byte[] data = hexStringToByteArray("0200000000000000000000000000000000000000");
                    setCharacteristic(mCharacteristicWrite, data);
                }
            } else if (hexVal.charAt(1) == '3' /*&& waitingForPower*/) {
                //waitingForPower = false;
                setCharacteristic(mCharacteristicWrite, cGetWeightData);
                WeightFrag.setGetWeightBtnTxt("EquiPack Awake!");
            } else if (hexVal.charAt(1) == '4' && hexVal.charAt(3) == '8') {
                double average = ( (double)( ( ( (int)byteVal[1] & 0xFF ) * 256 ) + ( (int)byteVal[2] & 0xFF ) ) * 3.3f ) / 4096f;
                double variance = ( (double)( ( ( (int)byteVal[3] & 0xFF ) * 256 ) + ( (int)byteVal[4] & 0xFF ) ) * 3.3f ) / 4096f;

                handleGettingWeight(average);

                if(SCANNING_MODE.equals("getWeight")) {
                    setCharacteristic(mCharacteristicWrite, cGetWeightData);
                    //Set button back to "Running!" text
                    WeightFrag.enableGetWeightBtn(true);
                    WeightFrag.getGetWeightBtn().setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                    WeightFrag.setGetWeightBtnTxt("Running!");
                } else {
                    WeightFrag.enableGetWeightBtn(true);
                    WeightFrag.getGetWeightBtn().setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                    //Set button back to default text
                    WeightFrag.setGetWeightBtnTxt(getString(R.string.get_weight_button_text));
                }
            } else if (hexVal.charAt(1) == '4') {
                if(SCANNING_MODE.equals("getSensors")) {
                    //If the current sensor number matches the sensor number that we just received data for

                        //get the sensor data from byteVal
                        int sensorData = ( ( (int)byteVal[1] << 256) & 0xFF ) + ( ( (int)byteVal[2] ) & 0xFF );

                        try {
                            getSensorLock.acquire();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //put the sensor data in the array of data received
                        final int currNum = mCurrentSensorNumber;
                        mSensorData[mCurrentSensorNumber] = sensorData;
                        getSensorLock.release();

                        if(mCurrentSensorNumber == 7){
                            //send the array of data to the analytics
                            new Thread ( new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getSensorLock.acquire();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    int curr = currNum;

                                    if(DashboardFrag.isSimulating()) {
                                        analytics.testWeightAnalytics();
                                        analytics.resetAnalyticsVariables();
                                    } else {

                                        if(DashboardFrag.isDebugging()) {
                                            boolean noData = true;
                                            for (int m = 0; m < mSensorData.length; m++) {
                                                if(mSensorData[m] != -1) {
                                                    noData = false;
                                                    break;
                                                }
                                            }
                                            if (noData) {
                                                final int[] data = {1, 2, 3, 4, 5, 6, 7, 8};
                                                DashboardFrag.updateDebugData(data);
                                            } else {
                                                final int[] unprocessedDataSet = mSensorData;
                                                DashboardFrag.updateDebugData(unprocessedDataSet);
                                            }

                                        } else {
                                            //TODO: do colin's stuff
                                            //8 unprocessed data points (one for each pressure sensor) are stored in the int[] mSensorData
                                            final int[] unprocessedDataSet = mSensorData;

                                            //There is a global instance if your analytics called "analytics"
                                            //Let's assume the new function is defined "public int colin(final int[] dataSetToProcess)"
                                            int[] feedbackReturnValue = analytics.colin(unprocessedDataSet);
                                            //feedbackReturnVal should contain at least:
                                                // at index:
                                                        //0:    binary (t/f) should left arrow point up  (-1 = no left arrow)
                                                        //1:    binary (t/f) should right arrow point up (-1 = no right arrow)
                                                        //2:    some indication of state change (if you need the total weight sensor data for example)

                                            /**
                                             * Brenton's half-pseudo for dashboard UI updates
                                             *
                                             * input: int[] with indexes:
                                             *                      0: int -> binary (1/0 = t/f): left arrow points up (1 = up, 0 = down, -1 = none)
                                             *                      1: int -> binary (1/0 = t/f): right arrow points up (1 = up, 0 = down, -1 = none)
                                             */
                                            analyticsUIUpdater(feedbackReturnValue);
                                        }
                                    }
                                    mCurrentSensorNumber = 0;
                                    getSensorLock.release();
                                }
                            }).start();
                        } else {
                            try {
                                getSensorLock.acquire();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mCurrentSensorNumber++;
                            getSensorLock.release();
                        }
                    try {
                        getSensorLock.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //always do this, mCurrentSensorNumber is the same as before(if received data was for wrong sensor)
                        //Otherwise mCurrentSensorNumber has been changed to the next sensor number that we need to get
                    setCharacteristic(mCharacteristicWrite, cSensor[mCurrentSensorNumber]);
                    getSensorLock.release();
                }
            }
        }
    }

    private void analyticsUIUpdater(int [] arg) {
        final int up = 1;
        final int down = 0;
        final int inviz = -1;
    /*
        if( //only left strap up ) {
            //lowers left side of backpack
            DashboardFrag.setArrows(up, inviz);
            DashboardFrag.setMessageText("Loosen left strap.");
        } else if (// only right strap up ) {
            //lowers right side of backpack
            DashboardFrag.setArrows(inviz, up);
            DashboardFrag.setMessageText("Loosen right strap.");
        } else if (// only left strap down ) {
            //raises left side of backpack
            DashboardFrag.setMessageText("Tighten left strap.");
            DashboardFrag.setArrows(down, inviz);
        } else if (// only right strap down ) {
            //raises right side of backpack
            DashboardFrag.setArrows(inviz, down);
            DashboardFrag.setMessageText("Tighten right strap.");
        } else if (// both straps up ) {
            //lowers the backpack's height
            DashboardFrag.setArrows(up, up);
            DashboardFrag.setMessageText("Loosen both straps to lower bag.");
        } else if (// both straps down ) {
            //raises the backpack's height
            DashboardFrag.setArrows(down, down);
            DashboardFrag.setMessageText("Tighten both straps to raise bag.");
        } else if (// left strap up, right strap down ) {
            //Shifts weight from left shoulder to right shoulder
            DashboardFrag.setArrows(up, down);
            DashboardFrag.setMessageText("Loosen left strap while tightening right strap.");
        } else if (// left strap down, right strap up ) {
            //Shifts weight from right shoulder to left shoulder
            DashboardFrag.setArrows(down, up);
            DashboardFrag.setMessageText("Tighten left strap while loosening right strap.");
        } else if (// straps are symmetric ) {
            //weight on both shoulders is correct
            DashboardFrag.setMessageText("Straps are symmetric!");
        } else if (// bag has reached correct height ) {
            //optimized!
            DashboardFrag.setMessageText("Pack's position has been optimized!");
        } else {
            // any failure cases need to be handled here
            DashboardFrag.setMessageText("Analytics have failed.");
        }
    */
    }

    private void resetSensors() {
        mCurrentSensorNumber = 0;
        //set the array back to its unfilled initial state
        for(int i = 0; i < mSensorData.length; i++)
            mSensorData[i] = -1;
    }
    private void disconnectedHandler(Object obj) {
        mConnected = false;
        if (SCANNING_MODE.equals("getWeight") || WeightFrag.getGetWeightBtn() != null){
            SCANNING_MODE = "";
            WeightFrag.enableGetWeightBtn(true);
            WeightFrag.getGetWeightBtn().setBackground(getResources().getDrawable(R.drawable.rounded_corner));
            WeightFrag.setGetWeightBtnTxt(getString(R.string.get_weight_button_text));
        } else if (SCANNING_MODE.equals("getSensors") || DashboardFrag.getAddDataBtn() != null) {
            SCANNING_MODE = "";
            DashboardFrag.setAddDataBtnEnabled(true, "Optimize!");
            DashboardFrag.setAddDataBtnColor(R.drawable.rounded_corner);
            DashboardFrag.setMessageText("Disconnected from Pack.");
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());



    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BleService.LocalBinder binder = (BleService.LocalBinder) service;
            mBle = binder.getService();
            mBleMessenger = new BleService().mMessenger;
            Message msg = Message.obtain(null,
                    BleService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            try {
                mBleMessenger.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
            mBleBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBleMessenger = null;
            mBleBound = false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            scanLeDevice(mBluetoothAdapter.isEnabled());
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case (0): //EquiPack Dashboard
                currentFrag = DashboardFrag;
                currentFragTag = getString(R.string.app_name);
                pushFragmentOntoStack(mFragmentManager, DashboardFrag, currentFragTag);
                break;
            case 1: //ContentWeightFragment
                currentFrag = WeightFrag;
                currentFragTag = getString(R.string.weight_title);
                pushFragmentOntoStack(mFragmentManager, WeightFrag, currentFragTag);
                break;
            case 2: //StrapOptimizationFragment
                currentFrag = FeedbackFrag;
                currentFragTag = getString(R.string.feedback_title);
                pushFragmentOntoStack(mFragmentManager, FeedbackFrag, currentFragTag);
                break;
            case 3: //BLEFragment
                currentFrag = BleFrag;
                currentFragTag = getString(R.string.ble_title);
                pushFragmentOntoStack(mFragmentManager, BleFrag, currentFragTag);
                break;
            case 4: //SettingsFragment
                currentFrag = SettingsFrag;
                currentFragTag = getString(R.string.settings_title);
                pushFragmentOntoStack(mFragmentManager, SettingsFrag, currentFragTag);
                break;
            default:
                currentFragTag = "DefaultFragmentCase";
                pushFragmentOntoStack(mFragmentManager, DashboardFrag, getString(R.string.app_name));
                break;
        }
    }

    public void pushFragmentOntoStack(FragmentManager fragmentManager,Fragment newFragment,String fragmentName) {
        if (fragmentManager == null)
            fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, newFragment)
                .addToBackStack(fragmentName)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (mFragmentManager.getBackStackEntryCount() > 1) {
            mFragmentManager.popBackStackImmediate();
        } else {
            mFragmentManager.popBackStackImmediate();
            super.onBackPressed();
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * SMS sending related functions
     */
    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
        // When the SMS has been sent
        BroadcastReceiver sendingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        // When the SMS has been delivered
        BroadcastReceiver receivingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        registerReceiver(sendingReceiver, new IntentFilter(SENT));
        registerReceiver(receivingReceiver, new IntentFilter(DELIVERED));
        //Send the phoneNumber a message, with sent and delivered pending intents
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }


    public void onFragmentClickEvent(View v) {
        if (v == BleFrag.getBLEConnectBtn()) {
            logBle("onFragmentClickEvent: BLE Connection event triggered\n");
            BleFrag.onBleBtnClick(v);
            BleFrag.enableNotify(false);
            BleFrag.enableWrite(false);
            BleFrag.enablePoll(false);
            SCANNING_MODE = "TestBLE";
            if(!mConnected)
                enableBLEThenScan();
        } else if (v == BleFrag.getBLEWriteBtn()) {
            logBle("onFragmentClickEvent: Writing value event triggered\n");
            setCharacteristic(mCharacteristicWrite, mPrefWriteValue);
        } else if (v == BleFrag.getBLELoopBtn()) {
            byte[] data = hexStringToByteArray("0200000000000000000000000000000000000000");
            polling = !polling;
            if(polling){
                logBle("onFragmentClickEvent: Beginning polling event triggered\n");
                BleFrag.getBLELoopBtn().getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                setCharacteristic(mCharacteristicWrite, data);
            }
            else if(!polling){
                logBle("onFragmentClickEvent: Stopping polling event triggered\n");
                BleFrag.getBLELoopBtn().getBackground().setColorFilter(0xFF00ff00, PorterDuff.Mode.MULTIPLY);
            }
        } else if (v == BleFrag.getBLENotifyBtn()) {
            logBle("onFragmentClickEvent(): Attempting to enable notifications on \"Read\" characteristic\n");
            setNotifications(mCharacteristicRead, true);
        } else if (v == DashboardFrag.getAddDataBtn()) {
            boolean debugging = DashboardFrag.isDebugging();

            if(!mConnected) {
                DashboardFrag.setMessageText("Searching for device...");
                DashboardFrag.setAddDataBtnEnabled(false, "Searching");
                DashboardFrag.setAddDataBtnColor(R.drawable.rounded_corner_pressed);
                if(!DashboardFrag.isSimulating() || DashboardFrag.isDebugging())
                    enableBLEThenScan();
            } else if (SCANNING_MODE.equals("")){
                DashboardFrag.setMessageText("Resuming connection (powering up)...");
                /*
                    Start requesting sensor data with this command
                    Only request if we aren't simulating
                 */
                if(!DashboardFrag.isSimulating())
                    setCharacteristic(mCharacteristicWrite, cSensor[mCurrentSensorNumber]);
            } else if (SCANNING_MODE.equals("getSensors")) {
                //We are trying to stop scanning
                DashboardFrag.setMessageText(getString(R.string.dashboard_graph_title));
                DashboardFrag.setAddDataBtnEnabled(true, "Optimize!");
                DashboardFrag.setAddDataBtnColor(R.drawable.rounded_corner);
            }

            if(SCANNING_MODE.equals("getSensors")) {
                SCANNING_MODE = "";
                mCurrentSensorNumber = 0;
                //DashboardFrag.setAddDataBtnColor(R.drawable.rounded_corner_red);
            }else {
                SCANNING_MODE = "getSensors";
            }


            if (!DashboardFrag.isDebugging()) {
                DashboardFrag.setAddDataBtnEnabled(false, "Processing");
                DashboardFrag.setAddDataBtnColor(R.drawable.rounded_corner_pressed);
                //TODO: this code shows that the debugger ui can be updated
                if (DashboardFrag.isSimulating()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            analytics.testWeightAnalytics();
                            analytics.resetAnalyticsVariables();
                        }
                    }).start();
                }
            }

        } else if (v == WeightFrag.getCalibrateBtn()) {
            if(!doCalibrate)
                WeightFrag.getCalibrateBtn().setBackground(getResources().getDrawable(android.R.drawable.button_onoff_indicator_on));
            else if (doCalibrate)
                WeightFrag.getCalibrateBtn().setBackground(getResources().getDrawable(android.R.drawable.button_onoff_indicator_off));
            doCalibrate = !doCalibrate;
            /**
             * User requested to calibrate their bag
             */
        } else if (v == WeightFrag.getGetWeightBtn()) {
            WeightFrag.getCalibrateBtn().setBackground(getResources().getDrawable(android.R.drawable.button_onoff_indicator_off));
            WeightFrag.enableGetWeightBtn(false);
            WeightFrag.getGetWeightBtn().setBackground(getResources().getDrawable(R.drawable.rounded_corner_pressed));
            //Set button back to "connecting text" since we will not begin to connect
            /**
             * User requested to get the weight of their bag
             */
            if(!mConnected) {
                WeightFrag.setGetWeightBtnTxt("Searching...");
                enableBLEThenScan();
            } else if (SCANNING_MODE.equals("")){
                WeightFrag.setGetWeightBtnTxt("Resuming...");
                setCharacteristic(mCharacteristicWrite, cGetWeightData);
            } else if (SCANNING_MODE.equals("getWeight")) {
                WeightFrag.enableGetWeightBtn(true);
                WeightFrag.getGetWeightBtn().setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                WeightFrag.setGetWeightBtnTxt(getString(R.string.get_weight_button_text));
            }
            if(SCANNING_MODE.equals("getWeight"))
                SCANNING_MODE = "";
            else
                SCANNING_MODE = "getWeight";
        }
    }

    public void onFragmentInteraction(Uri uri) {}

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * BLE related functions
     */

    private void logBle(final String newLog) {
        try {
            logLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            String log = newLog;

            @Override
            public void run() {
                BleFrag.appendText(log);
            }
        });
        logLock.release();
    }

    private void logSet(final String newLog) {
        try {
            logLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            String log = newLog;

            @Override
            public void run() {
                BleFrag.replaceText(log);
            }
        });
        logLock.release();
    }

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public void setPrefServiceUUID(String prefUUID) {
        mPrefUUIDService = UUID.fromString(prefUUID);
    }

    public void setPrefReadUUID(String prefUUID) {
        mPrefUUIDCharacteristicRead = UUID.fromString(prefUUID);
    }

    public void setPrefWriteUUID(String prefUUID) {
        mPrefUUIDCharacteristicWrite = UUID.fromString(prefUUID);
    }

    public void setPrefWriteValue(String writePref) {
        mPrefWriteValue = hexStringToByteArray(writePref);
    }

    public void setPrefWeight(int prefWeight) {
        mPrefWeight = prefWeight;
    }








    public void setCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data) {
        if (characteristic != null && data != null && mBluetoothGatt != null
                && isCharacteristicWriteable(characteristic)) {

            characteristic.setValue(data);
            boolean writeSent = mBluetoothGatt.writeCharacteristic(characteristic);

            if (writeSent) logBle("setCharacteristic: Asynchronous write has been successfully called.\n");
            else logBle("setCharacteristic: Asynchronous write was NOT successfully called.\n");
        }
    }

    public void setNotifications(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (characteristic != null && mBluetoothGatt != null
                && isCharacterisiticNotifiable(characteristic)) {
            if (enabled) {
                //Enable local notifications
                logBle("setNotifications: Enabling local notifications\n");
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);

                //Enabled remote notifications
                logBle("setNotifications: Enabling remote notifications for descriptor:\n               " +
                        "00002902-0000-1000-8000-00805f9b34fb\n");
                BluetoothGattDescriptor desc = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                logBle("setNotifications: Writing changes made to remote descriptor:\n                " +
                        "00002902-0000-1000-8000-00805f9b34fb\n");
                mBluetoothGatt.writeDescriptor(desc);
            } else {
                //Disable local notifications
                logBle("setNotifications: Disabling local notifications\n");
                mBluetoothGatt.setCharacteristicNotification(characteristic, false);

                //Disable remote notifications
                logBle("setNotifications: Disabling remote notifications for descriptor:\n               " +
                        "00002902-0000-1000-8000-00805f9b34fb\n");
                BluetoothGattDescriptor desc = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                desc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                logBle("setNotifications: Writing changes made to remote descriptor:\n                " +
                        "00002902-0000-1000-8000-00805f9b34fb\n");
                mBluetoothGatt.writeDescriptor(desc);
            }

        }
    }

    public void checkCharacteristicProperties(BluetoothGattCharacteristic characteristic, String name){
        if(characteristic == null) {
            logBle("checkCharacteristicProperties(): not a valid characteristic UUID for: " + name
                    + "\n                       Change in preferences.\n");
            return;
        }
        logBle("checkCharacteristicProperties(): Checking " + "\"" + name + "\"" + " characteristic Properties...\n");

        if (isCharacteristicWriteable(characteristic)) {
            logBle("checkCharacteristicProperties():       Writeable -> true\n");
            mCharacteristicPermissions[0] = true;
        } else {
            logBle("checkCharacteristicProperties():       Writeable -> false\n");
            mCharacteristicPermissions[0] = false;
        }
        if (isCharacteristicReadable(characteristic)) {
            logBle("checkCharacteristicProperties():       Readable -> true\n");
            mCharacteristicPermissions[1] = true;
        } else {
            logBle("checkCharacteristicProperties():       Readable -> false\n");
            mCharacteristicPermissions[1] = false;
        }
        if (isCharacterisiticNotifiable(characteristic)) {
            logBle("checkCharacteristicProperties():       Notifiable -> true\n");
            mCharacteristicPermissions[2] = true;
        } else {
            logBle("checkCharacteristicProperties():       Notifiable -> false\n");
            mCharacteristicPermissions[2] = false;
        }
    }

    /**
     * @return Returns true if property is writable
     */
    public boolean isCharacteristicWriteable(BluetoothGattCharacteristic pChar) {
        return (pChar.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    /**
     * @return Returns true if property is Readable
     */
    public boolean isCharacteristicReadable(BluetoothGattCharacteristic pChar) {
        return ((pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
    }

    /**
     * @return Returns true if property is supports notification
     */
    public boolean isCharacterisiticNotifiable(BluetoothGattCharacteristic pChar) {
        int properties = pChar.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }


    /**
     * BLE SERVICE STARTUP RELATED FUNCTIONS
     *
     * Enable BLE
     *
     * Scan for devices with a timeout
     */
    public void enableBLEThenScan() {
        new Thread (new Runnable() {
            @Override
            public void run() {
                final BluetoothManager bluetoothManager
                        = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                //Enable Bluetooth
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    logBle("Requesting user to turn BLE ON\n");
                } else if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                    logBle("scanLeDevice(): called\n");
                    scanLeDevice(mBluetoothAdapter.isEnabled());
                } else if (SCANNING_MODE.equals("getWeight")){
                    SCANNING_MODE = "";
                    WeightFrag.enableGetWeightBtn(true);
                    WeightFrag.getGetWeightBtn().setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                    //Set button back to default text
                    WeightFrag.setGetWeightBtnTxt(getString(R.string.get_weight_button_text));
                }
            }
        }).start();
    }
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mScanning) logBle("scanLeDevice(): No correct " +
                            "device was found -> scan stopping\n");
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);

                    //Still were scanning to getWeight but we didn't connect. Reset the getWeight btn
                    if (SCANNING_MODE.equals("getWeight") && !mConnected && mScanning) {
                        SCANNING_MODE = "";
                        WeightFrag.enableGetWeightBtn(true);
                        WeightFrag.getGetWeightBtn().setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                        //Set button back to default text
                        WeightFrag.setGetWeightBtnTxt(getString(R.string.get_weight_button_text));
                    } else if (SCANNING_MODE.equals("getSensors") && !mConnected && mScanning) {
                        SCANNING_MODE = "";
                        DashboardFrag.setMessageText("Unable to locate Pack.");
                        DashboardFrag.setAddDataBtnEnabled(true, "Optimize!");
                        DashboardFrag.setAddDataBtnColor(R.drawable.rounded_corner);
                    }
                    //Were no longer scanning
                    mScanning = false;
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            logBle("scanLeDevice(): scan started\n");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (SCANNING_MODE.equals("getWeight")){
                SCANNING_MODE = "";
                WeightFrag.enableGetWeightBtn(true);
                WeightFrag.getGetWeightBtn().setBackground(getResources().getDrawable(R.drawable.rounded_corner));
                //Set button back to default text
                WeightFrag.setGetWeightBtnTxt(getString(R.string.get_weight_button_text));
            }

        }
    }
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            String deviceName = device.getName();
            if (deviceName != null && deviceName.equals(mDeviceName) && mScanning) {
                try {
                    available.acquire();
                    mScanning = false;
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        logBle("mLeScanCallback: Preferred Device Found!\n");
                        logBle("mLeScanCallback: CONNECTING to device's GATT server\n");
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        //Set button back to "Connecting" text
                        if(SCANNING_MODE.equals("getWeight"))
                            WeightFrag.setGetWeightBtnTxt("Connecting...");
                        else if (SCANNING_MODE.equals("getSensors"))
                            DashboardFrag.setMessageText("Connecting...");
                        mBluetoothGatt = device.connectGatt(mBle, false, mBle.mGattCallback);
                        logBle("scanLeDevice(): scan stopping\n");
                    }
                });
                available.release();
            }
        }
    };

    final protected char[] hexArray = "0123456789ABCDEF".toCharArray();
    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void handleGettingWeight(double sensorData) {
        final double data = sensorData;
        Runnable handler = new Runnable() {
            @Override
            public void run() {
                if (weightDataCycle.size() < NUMBER_OF_SAMPLES_PER_CYCLE) {
                    weightDataCycle.add(data);
                } else if (weightDataCycle.size() == NUMBER_OF_SAMPLES_PER_CYCLE) {
                    double averageRawWeightOverCycle = 0;
                    for (int i = 0; i < NUMBER_OF_SAMPLES_PER_CYCLE; i++)
                        averageRawWeightOverCycle += weightDataCycle.get(i);
                    averageRawWeightOverCycle = averageRawWeightOverCycle / NUMBER_OF_SAMPLES_PER_CYCLE;
                    if (doCalibrate) {
                        ZERO_WEIGHTB = averageRawWeightOverCycle/(.00019*1665*2);
                        WeightFrag.getCalibrateBtn().setBackgroundColor(android.R.drawable.button_onoff_indicator_off);
                        doCalibrate = false;
                    } else {
                        weightDataCycle.clear();
                        WeightFrag.setDisplayedWeight((averageRawWeightOverCycle)/(0.00019*1665*2) - ZERO_WEIGHTB);
                    }
                }
            }
        };
        try {
            getWeightLock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runOnUiThread(handler);
        getWeightLock.release();

    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
