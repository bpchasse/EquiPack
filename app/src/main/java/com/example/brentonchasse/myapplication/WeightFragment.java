package com.example.brentonchasse.myapplication;

import android.app.Activity;
import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jjoe64.graphview.series.Series;

import java.text.DecimalFormat;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeightFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WeightFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class WeightFragment extends Fragment {
    public boolean mWeightInKg;
    private TextView mWeightInKgSwitch;
    private TextView mWeightTextView;
    private Button mCalibrateBtn;
    private Button mGetWeightBtn;
    private Bundle savedState;
    private String mDisplayedWeight;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WeightFragment.
     */
    public static WeightFragment newInstance() {
        WeightFragment fragment = new WeightFragment();
        return fragment;
    }
    public WeightFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionbar = getActivity().getActionBar();
        if(actionbar != null) actionbar.setTitle(getString(R.string.weight_title));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWeightInKgSwitch = (TextView) getView().findViewById(R.id.weightInKgSwitch);
        mCalibrateBtn = (Button) getView().findViewById(R.id.calibrateBtn);
        mGetWeightBtn = (Button) getView().findViewById(R.id.getWeightBtn);
        mWeightTextView = (TextView) getView().findViewById(R.id.weightTextView);
        setWeightInKg(mWeightInKg);
        if(mWeightInKg)
            mWeightInKgSwitch.setText(R.string.weight_in_lbs_switch_text);
        else
            mWeightInKgSwitch.setText(R.string.weight_in_kg_switch_text);
        /*if (mDisplayedWeight != null)
            mWeightTextView.setText((CharSequence) mDisplayedWeight);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weight, container, false);
        if(savedInstanceState != null && savedState == null)
            savedState = savedInstanceState.getBundle(Integer.toString(R.string.weightInKgStateAll));
        if(savedState != null) {
            mWeightInKg = savedState.getBoolean(Integer.toString(R.string.weightInKgStateBooleanKey));
            mDisplayedWeight = savedState.getString(Integer.toString(R.string.weightDisplayedKey));
        }
        savedState = null;
        return v;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDisplayedWeight != null)
            mWeightTextView.setText((CharSequence) mDisplayedWeight);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setWeightInKg(boolean inKg) {
        mWeightInKg = inKg;
        if(mWeightTextView != null) {
            String[] displayedContent = mWeightTextView.getText().toString().split(" "); //[0] - weight, [1] - units 'lbs'/'kg'
            setDisplayedWeight(Double.parseDouble(displayedContent[0]));
        }
        savedState = saveState();
    }
    public boolean getWeightInKg() {
        return mWeightInKg;
    }
    public TextView getWeightInKgSwitch() {
        return mWeightInKgSwitch;
    }

    public void setDisplayedWeight(double weightValue) {
        String weightUnits = mWeightInKg ? "kg" : "lbs";
        //TODO: convert weightvalue to kg from lbs when necessary
        DecimalFormat formatter = new DecimalFormat("#00.00");
        String textValue = formatter.format(weightValue) + " " + weightUnits;
        mWeightTextView.setText((CharSequence) textValue);
    }

    public Button getCalibrateBtn() { return mCalibrateBtn; }
    public Button getGetWeightBtn() { return mGetWeightBtn; }
    public void setGetWeightBtnTxt(final String txt) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGetWeightBtn.setText(txt);
            }
        });
    }
    public void enableGetWeightBtn(final boolean b) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGetWeightBtn.setEnabled(b);
            }
        });
    }
    public TextView getWeightTextView() { return mWeightTextView; }

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        savedState = saveState(); /* vstup defined here for sure */
    }

    private Bundle saveState() { /* called either from onDestroyView() or onSaveInstanceState() */
        Bundle state = new Bundle();
        state.putBoolean(Integer.toString(R.string.weightInKgStateBooleanKey), mWeightInKg);
        if(mWeightTextView != null)
            state.putString(Integer.toString(R.string.weightDisplayedKey), mWeightTextView.getText().toString());
        return state;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(Integer.toString(R.string.weightInKgStateAll), savedState != null ? savedState : saveState());
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
