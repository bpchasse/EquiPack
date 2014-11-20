package com.example.brentonchasse.myapplication;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.widget.BaseAdapter;
import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.Semaphore;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                   BleFragment.OnFragmentInteractionListener,
                   WeightFragment.OnFragmentInteractionListener,
                   FeedbackFragment.OnFragmentInteractionListener,
                   DashboardFragment.OnFragmentInteractionListener,
                   SettingsFragment.OnFragmentInteractionListener {

    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;

    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt   mBluetoothGatt;
    private Handler mHandler;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothLeService mBluetoothLeService;
    private FragmentManager mFragmentManager;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private boolean mScanning;
    //private final UUID[] EQUIPACK_UUID = {UUID.fromString("5A382F74-3182-412A-BDC6-6068BDFB0A48")};

    private int mConnectionState = 0;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final boolean AUTO_CONNECT_BOOL = true;

    BleFragment BleFrag = new BleFragment();
    WeightFragment WeightFrag = new WeightFragment();
    FeedbackFragment FeedbackFrag = new FeedbackFragment();
    DashboardFragment DashboardFrag = new DashboardFragment();
    SettingsFragment SettingsFrag = new SettingsFragment();
    private Fragment currentFrag;
    private String currentFragTag;

    public String mDeviceName;

    private final Semaphore available = new Semaphore(1);


  @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_options);
      mHandler = new Handler();
      mLeDeviceListAdapter = new LeDeviceListAdapter();
      mBluetoothLeService = new BluetoothLeService();
      mNavigationDrawerFragment = (NavigationDrawerFragment)
              getFragmentManager().findFragmentById(R.id.navigation_drawer);
      mFragmentManager = getFragmentManager();
      mTitle = getTitle();

      // Set up the Navigation drawer.
      mNavigationDrawerFragment.setUp(R.id.navigation_drawer,(DrawerLayout) findViewById(R.id.drawer_layout));

      //Restore preferences
      SharedPreferences myPreferences = getPreferences(MODE_PRIVATE);
      mDeviceName = myPreferences.getString(getString(R.string.settings_device_name_key),
                                                   getString(R.string.app_name));

      /**
       * Must comment out to debug in emulator
       */
        //Initialize Bluetooth adapter
        final BluetoothManager bluetoothManager
                = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //Enable Bluetooth
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
          Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()){
          scanLeDevice(mBluetoothAdapter.isEnabled());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
      if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      } else if(requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK){
        scanLeDevice(mBluetoothAdapter.isEnabled());
      }
    }

    private void scanLeDevice(final boolean enable){
        if(enable) {
            mHandler.postDelayed(new Runnable() {
                                     @Override
                                     public void run() {
                                         mScanning = false;
                                         mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                     }
                                 }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                      String deviceName = device.getName();
                      if(deviceName != null && deviceName.equals(mDeviceName)){

                        try {
                          available.acquire();
                        } catch (InterruptedException e) {
                          e.printStackTrace();
                        }

                        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                            mBluetoothGatt = device.connectGatt(mBluetoothLeService, false, mBluetoothLeService.mGattCallback);
                            mScanning = false;
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                          }
                        });
                        available.release();
                    }
                }
     };

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        switch(position) {
            case (0): //EquiPack Dashboard
              currentFrag = DashboardFrag;
              currentFragTag = getString(R.string.app_name);
              pushFragmentOntoStack(mFragmentManager, DashboardFrag, getString(R.string.app_name));
              break;
            case 1: //ContentWeightFragment
              currentFrag = WeightFrag;
              currentFragTag = getString(R.string.weight_title);
              pushFragmentOntoStack(mFragmentManager, WeightFrag, getString(R.string.weight_title));
              break;
            case 2: //StrapOptimizationFragment
              currentFrag = FeedbackFrag;
              currentFragTag = getString(R.string.feedback_title);
              pushFragmentOntoStack(mFragmentManager, FeedbackFrag, getString(R.string.feedback_title));
               break;
            case 3: //BLEFragment
              currentFrag = BleFrag;
              currentFragTag = getString(R.string.ble_title);
              pushFragmentOntoStack(mFragmentManager, BleFrag, getString(R.string.ble_title));
               break;
            case 4: //SettingsFragment
              currentFrag = SettingsFrag;
              currentFragTag = getString(R.string.settings_title);
              pushFragmentOntoStack(mFragmentManager, SettingsFrag, getString(R.string.settings_title));
               break;
            default:
              pushFragmentOntoStack(mFragmentManager, DashboardFrag, getString(R.string.app_name));
              break;
        }
    }

    public void pushFragmentOntoStack(FragmentManager fragmentManager,
                                      Fragment newFragment,
                                      String fragmentName) {
        if(fragmentManager == null)
          fragmentManager = getFragmentManager();

        fragmentManager.beginTransaction()
                .replace(R.id.container, newFragment)
                .addToBackStack(fragmentName)
                .commit();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onBackPressed() {
      if (mFragmentManager.getBackStackEntryCount() > 1) {
        Log.i("MainActivity", "popping backstack");
        mFragmentManager.popBackStackImmediate();
      } else {
        Log.i("MainActivity", "nothing on backstack, calling super");
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
            // Only show items in the action bar relevant to this screen if the drawer is not showing.
            // Otherwise, let the drawer decide what to show in the action bar.
            restoreActionBar();
            return true;
        }

      return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks
        // on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    public void onFragmentClickEvent(View v) {
        BleFrag.onBleBtnClick(v);
    }

    public void onFragmentInteraction(Uri uri) {  }

    public void setDeviceName(String deviceName){ mDeviceName = deviceName; }

    @Override
    protected void onStop(){
      super.onStop();

      //Store the preferences in case they may have changes
      SharedPreferences myPreferences = getPreferences(MODE_PRIVATE);
      SharedPreferences.Editor editor = myPreferences.edit();
      editor.putString(getString(R.string.settings_device_name_key), mDeviceName);
      editor.commit();
    }


    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        //String unknownServiceString = getResources().getString(R.string.unknown_service);
        //String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            //currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            //currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                //currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                //currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
        }
        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }
        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }
        public void clear() {
            mLeDevices.clear();
        }
        @Override
        public int getCount() {
            return mLeDevices.size();
        }
        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            /*ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());*/
            return view;
        }
    }

    private class BluetoothLeService extends Service {
        private final String TAG = BluetoothLeService.class.getSimpleName();
        private final IBinder mBinder = new LocalBinder();
        private static final int STATE_DISCONNECTED = 0;
        private static final int STATE_CONNECTING = 1;
        private static final int STATE_CONNECTED = 2;
        private static final String ACTION_GATT_CONNECTED =
                "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
        private static final String ACTION_GATT_DISCONNECTED =
                "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
        private static final String ACTION_GATT_SERVICES_DISCOVERED =
                "com.example.bluetooth.le.ACTION_GATT_SERICED_DISCOVERED";
        private static final String ACTION_DATA_AVAILABLE =
                "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
        private static final String EXTRA_DATA =
                "com.example.bluetooth.le.EXTRA_DATA";

        public class LocalBinder extends Binder {
            BluetoothLeService getService() {
                return BluetoothLeService.this;
            }
        }

        public final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    mBluetoothGatt.discoverServices();
//                    broadcastUpdate(intentAction, null);
                    Log.i(TAG, "Connected to GATT server.");
                    //Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
//                    broadcastUpdate(intentAction, null);
                }
            }
            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
              if (status == BluetoothGatt.GATT_SUCCESS) {
                  //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null);
                  BluetoothDevice connectedDevice = gatt.getDevice();
                  String deviceName = connectedDevice.getName();
                  UUID  serviceUUID = UUID.fromString("00001111-0000-1000-8000-00805f9b34fb");
                  BluetoothGattService btServices = gatt.getService(serviceUUID);

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
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            }

            private void broadcastUpdate(final String action) {
              final Intent intent = new Intent(action);
              sendBroadcast(intent);
            }

            private void broadcastUpdate(final String action,
                                         final BluetoothGattCharacteristic characteristic) {
              final Intent intent = new Intent(action);

              // This is special handling for the Heart Rate Measurement profile. Data
              // parsing is carried out as per profile specifications.
              //if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
                int flag = characteristic.getProperties();
                int format = -1;
                if ((flag & 0x01) != 0) {
                  format = BluetoothGattCharacteristic.FORMAT_UINT16;
                  Log.d(TAG, "Heart rate format UINT16.");
                } else {
                  format = BluetoothGattCharacteristic.FORMAT_UINT8;
                  Log.d(TAG, "Heart rate format UINT8.");
                }
                final int heartRate = characteristic.getIntValue(format, 1);
                Log.d(TAG, String.format("Received heart rate: %d", heartRate));
                intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
              /*} else {
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                  final StringBuilder stringBuilder = new StringBuilder(data.length);
                  for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                  intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                          stringBuilder.toString());
                }
              }*/
              sendBroadcast(intent);
            }
        };

        // Handles various events fired by the Service.
        // ACTION_GATT_CONNECTED: connected to a GATT server.
        // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
        // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
        // ACTION_DATA_AVAILABLE: received data from the device. This can be a
        // result of read or notification operations.
        private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnectionState = STATE_CONNECTED;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnectionState = STATE_DISCONNECTED;
                    invalidateOptionsMenu();
                     //clearUI();
                } else if (BluetoothLeService.
                     ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                  // Show all the supported services and characteristics on the
                  // user interface.
                  displayGattServices(mBluetoothGatt.getServices());
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }
            }
        };
        @Override
        public IBinder onBind(Intent intent) {
            return mBinder;
        }

        @Override
        public boolean onUnbind(Intent intent) {
          // After using a given device, you should make sure that BluetoothGatt.close() is called
          // such that resources are cleaned up properly.  In this particular example, close() is
          // invoked when the UI is disconnected from the Service.
          mBluetoothGatt.close();
          return super.onUnbind(intent);
        }

    }
}
