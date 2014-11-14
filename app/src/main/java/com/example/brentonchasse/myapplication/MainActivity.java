package com.example.brentonchasse.myapplication;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
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
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
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
import java.util.UUID;
//import java.util.logging.Handler;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                   BleFragment.OnFragmentInteractionListener,
                   WeightFragment.OnFragmentInteractionListener,
                   FeedbackFragment.OnFragmentInteractionListener,
                   DashboardFragment.OnFragmentInteractionListener {

    // Fragment managing the behaviors, interactions and presentation of the navigation drawer.
    private NavigationDrawerFragment mNavigationDrawerFragment;

    // Used to store the last screen title. For use in {@link #restoreActionBar()}.
    private CharSequence mTitle;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt   mBluetoothGatt;
    private Handler mHandler;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothLeService mBluetoothLeService;
    private boolean mScanning;
    //private final UUID[] EQUIPACK_UUID = {UUID.fromString("5A382F74-3182-412A-BDC6-6068BDFB0A48")};
    private int mConnectionState = 0;
//15711333-6A4D-417A-A324-87F328798BEC
    //54220ca215c056733cb34e9e34c40596d34253a0
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private static final boolean AUTO_CONNECT_BOOL = true;

    BleFragment BleFrag = new BleFragment();
    WeightFragment WeightFrag = new WeightFragment();
    FeedbackFragment FeedbackFrag = new FeedbackFragment();
    DashboardFragment DashboardFrag = new DashboardFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_options);
      mHandler = new Handler();
      mLeDeviceListAdapter = new LeDeviceListAdapter();
      mBluetoothLeService = new BluetoothLeService();
      mNavigationDrawerFragment = (NavigationDrawerFragment)
              getFragmentManager().findFragmentById(R.id.navigation_drawer);
      mTitle = getTitle();
      // Set up the drawer.
      mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
      //View myView = findViewById(R.id.option_drawer);
      //MenuItem dashboardItem = (MenuItem) findViewById(R.id.option_drawer);
      //dashboardItem.setVisible(false);
     // this.invalidateOptionsMenu();

      //while(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
        //Initialize Bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //Enable Bluetooth
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
          Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
      //}
      /**
       * Must comment out to debug in emulator
       */
      //scanLeDevice(mBluetoothAdapter.isEnabled());
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                            mBluetoothGatt = device.connectGatt(mBluetoothLeService, AUTO_CONNECT_BOOL, mBluetoothLeService.mGattCallback);
                        }
                    });
                }
     };

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        switch(position) {
            case (0): //EquiPack Dashboard
                fragment = new DashboardFragment();
                mTitle = getString(R.string.app_name);
                break;
            case 1: //ContentWeightFragment
                fragment = new WeightFragment();
                mTitle = getString(R.string.weight_title);
                break;
            case 2: //StrapOptimizationFragment
                fragment = new FeedbackFragment();
                mTitle = getString(R.string.feedback_title);
                break;
            case 3: //BLEFragment
                fragment = new BleFragment();
                mTitle = getString(R.string.ble_title);
                break;
            default:
                fragment = new DashboardFragment();
                mTitle = getString(R.string.app_name);
                break;
        }
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        if(position < 3)
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
        else
            fragmentManager.beginTransaction().replace(R.id.container, fragment)
                    .addToBackStack("backFragment").commit();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen if the drawer is not showing.
            // Otherwise, let the drawerdecide what to show in the action bar.
            restoreActionBar();
            return true;
        }

      return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will automatically handle clicks
        // on the Home/Up button, so long as you specify a parent activity in AndroidManifest.xml.
        this.onNavigationDrawerItemSelected(0);
        return super.onOptionsItemSelected(item);
    }

    // A placeholder fragment containing a simple view.
    public static class OptionFragment extends Fragment {
        // The fragment argument representing the section number for this fragment.
        private static final String ARG_SECTION_NUMBER = "section_number";

        // Returns a new instance of this fragment for the given section number.
        public static OptionFragment newInstance(int sectionNumber) {
            OptionFragment fragment = new OptionFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_options, container, false);
            return rootView;
        }
    }

    public void onFragmentClickEvent(View v) {
        BleFrag.onBleBtnClick(v);
    }

    public void onFragmentInteraction(Uri uri) {    }

    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    /*private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }*/

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
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
                "com.example.bluetooth.le.ACTION_GATT_SERICED_DISCOVEREDsd";
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
                    broadcastUpdate(intentAction, null);
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                    broadcastUpdate(intentAction, null);
                }
            }
            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null);
                } else {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }
            @Override
            // Result of a characteristic read operation
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            }

            private void broadcastUpdate(final String action, BluetoothGattCharacteristic characteristic) {
                final Intent intent = new Intent(action);
                sendBroadcast(intent);
            }
        };
        @Override
        public IBinder onBind(Intent intent) {
            return mBinder;
        }
    }
}
