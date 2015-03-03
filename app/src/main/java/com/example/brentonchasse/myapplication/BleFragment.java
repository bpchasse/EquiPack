package com.example.brentonchasse.myapplication;

import android.app.Activity;
import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BleFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class BleFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    public TextView mTextView;
    public boolean mInputDoneMeansAdd;
    private Bundle savedState = null;

    private Button mConnectBtn;
    private Button mNotifyBtn;
    private Button mWriteBtn;
    private Button mPollBtn;
    private boolean[] buttonStates = new boolean[6];


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BleFragment.
     */
    public static BleFragment newInstance(String param1, String param2) {
        BleFragment fragment = new BleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public BleFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionbar = getActivity().getActionBar();
        if(actionbar != null) actionbar.setTitle("BLE");
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Arrays.fill(buttonStates, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ble, container, false);
        mTextView = (TextView)v.findViewById(R.id.ble_fragment_log_id);

        if(savedInstanceState != null && savedState == null)
            savedState = savedInstanceState.getBundle(Integer.toString(R.string.bleBundleSetup));
        if(savedState != null) {
            mTextView.setText(savedState.getCharSequence(Integer.toString(R.string.bleLogCharsSetup)));
            buttonStates = savedState.getBooleanArray(Integer.toString(R.string.bleButtonStates));
        }
        savedState = null;

      return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mConnectBtn = (Button) getView().findViewById(R.id.ble_fragment_connect_btn);
        mConnectBtn.setBackgroundResource(android.R.drawable.btn_default);
        mConnectBtn.setEnabled(true);
        mConnectBtn.setClickable(true);

        mNotifyBtn = (Button) getView().findViewById(R.id.ble_fragment_notify_btn);
        mNotifyBtn.setBackgroundResource(android.R.drawable.btn_default);

        mWriteBtn = (Button) getView().findViewById(R.id.ble_fragment_writeA1_btn);
        mWriteBtn.setBackgroundResource(android.R.drawable.btn_default);

        mPollBtn = (Button) getView().findViewById(R.id.ble_fragment_loopA2_btn);
        mPollBtn.setBackgroundResource(android.R.drawable.btn_default);
        mPollBtn.getBackground().setColorFilter(0xFF00ff00, PorterDuff.Mode.MULTIPLY);

        enableNotify(buttonStates[0]);
        enableWrite(buttonStates[2]);
        enablePoll(buttonStates[4]);
    }

    public void onBleBtnClick(View v) {
      changeText("");
    }

    public void changeText(String mText){
      if(mTextView != null) mTextView.setText(mText);
    }

    public void appendText(String mText){
      String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
      if(mTextView != null) mTextView.append(currentDateTimeString + "--" + mText);
    }

    public void replaceText(String mText) {
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        if(mTextView != null) mTextView.setText(currentDateTimeString + "--" + mText);
    }

    public View  getBLELog() {
      return mTextView;
    }

    public Button getBLEConnectBtn() {
      return mConnectBtn;
    }

    public Button getBLEWriteBtn() {
        return mWriteBtn;
    }

    public Button getBLELoopBtn() { return mPollBtn; }

    public Button getBLENotifyBtn() { return mNotifyBtn; }

    public void enableNotify(final boolean condition) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNotifyBtn.setClickable(condition);
                mNotifyBtn.setEnabled(condition);
            }
        });
    }

    public void enableWrite(final boolean condition) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWriteBtn.setClickable(condition);
                mWriteBtn.setEnabled(condition);
            }
        });
    }

    public void enablePoll(final boolean condition) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPollBtn.setClickable(condition);
                mPollBtn.setEnabled(condition);
            }
        });
    }

    @Override
    public void onAttach (Activity activity){
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach () {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        savedState = saveState(); /* vstup defined here for sure */
        mTextView = null;
    }

    private Bundle saveState() { /* called either from onDestroyView() or onSaveInstanceState() */
        Bundle state = new Bundle();

        state.putCharSequence(Integer.toString(R.string.bleLogCharsSetup), mTextView.getText());


        ((MainActivity) getActivity()).polling = false;
        boolean[] buttonStates = new boolean[6];
        buttonStates[0] = mNotifyBtn.isClickable();
        buttonStates[1] = mNotifyBtn.isEnabled();
        buttonStates[2] = mWriteBtn.isClickable();
        buttonStates[3] = mWriteBtn.isEnabled();
        buttonStates[4] = mPollBtn.isClickable();
        buttonStates[5] = mPollBtn.isEnabled();
        state.putBooleanArray(Integer.toString(R.string.bleButtonStates),buttonStates);

        return state;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(Integer.toString(R.string.bleBundleSetup), savedState != null ? savedState : saveState());
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
