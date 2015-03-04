package com.example.brentonchasse.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionbar = getActivity().getActionBar();
        if(actionbar != null) actionbar.setTitle(getString(R.string.settings_title));
        addPreferencesFromResource(R.layout.fragment_settings);
        onSharedPreferenceChanged(null, "");
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
      Preference pref = findPreference(key);
      MainActivity activity = (MainActivity) getActivity();

      //Set the activity variables to the new preferred values as long as they are validly/logically set
      if(pref != null && activity != null) {
          String newVal;
          if (key.equals(getString(R.string.settings_device_name_key))) {
              newVal = sharedPreferences.getString(key, "");
              if (!newVal.equals("")) {
                  activity.setDeviceName(newVal);
              }
          } else if (key.equals(getString(R.string.settings_UUIDService_key))) {
              newVal = sharedPreferences.getString(key, getString(R.string.settings_UUIDService_default));
              if (newVal.matches(getString(R.string.UUID_regex))){
                  activity.setPrefServiceUUID(newVal);
              }
          } else if (key.equals(getString(R.string.settings_UUIDCharacteristicR_key))) {
              newVal = sharedPreferences.getString(key, getString(R.string.settings_UUIDCharacteristicR_default));
              if (newVal.matches(getString(R.string.UUID_regex))) {
                  activity.setPrefReadUUID(newVal);
              }
          } else if (key.equals(getString(R.string.settings_UUIDCharacteristicW_key))) {
              newVal = sharedPreferences.getString(key, getString(R.string.settings_UUIDCharacteristicW_default));
              if (newVal.matches(getString(R.string.UUID_regex))) {
                  activity.setPrefWriteUUID(newVal);
              }
          } else if (key.equals(getString(R.string.settings_writeValue_key))) {
              newVal = sharedPreferences.getString(key, getString(R.string.settings_writeValue_default));
              if (newVal.length() == 40 && newVal.charAt(0) == '0'
                      && (newVal.charAt(1) == '1' || newVal.charAt(1) == '0')) {
                  activity.setPrefWriteValue(newVal);
              }
          } else if (key.equals(getString(R.string.settings_weight_key))) {
              newVal = "" + sharedPreferences.getInt(key, -1) + "";
              if (Integer.valueOf(newVal) != -1     //weight should be > 0lbs && < 1000lbs
                            && Integer.valueOf(newVal) > 0
                            && Integer.valueOf(newVal) < 1000) {
                  activity.setPrefWeight(Integer.valueOf(newVal));
              }
          } else if (key.equals(getString(R.string.settings_inputDone_key))) {
              activity.DashboardFrag.setInputMeansDone(sharedPreferences.getBoolean(key,false));
          } else if (key.equals(getString(R.string.settings_weightKg_key))) {
              boolean inKg = sharedPreferences.getBoolean(key, false);
              activity.WeightFrag.setWeightInKg(inKg);
          }
          //Save the changes made to the preferences if they need to be changed.
          onSharedPreferenceChanged(null, "");
      } else {
          SharedPreferences sharedPreferences1 = getActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);

          EditTextPreference setHelper = (EditTextPreference) findPreference(getString(R.string.settings_UUIDService_key));
          setHelper.setDefaultValue(R.string.settings_UUIDService_default);
          sharedPreferences1.edit().putString(getString(R.string.settings_UUIDService_key), setHelper.getText()).apply();

          setHelper = (EditTextPreference) findPreference(getString(R.string.settings_UUIDCharacteristicR_key));
          setHelper.setDefaultValue(R.string.settings_UUIDCharacteristicR_default);
          sharedPreferences1.edit().putString(getString(R.string.settings_UUIDCharacteristicR_key), setHelper.getText()).apply();

          setHelper = (EditTextPreference) findPreference(getString(R.string.settings_UUIDCharacteristicW_key));
          setHelper.setDefaultValue(R.string.settings_UUIDCharacteristicW_default);
          sharedPreferences1.edit().putString(getString(R.string.settings_UUIDCharacteristicW_key), setHelper.getText()).apply();

          setHelper = (EditTextPreference) findPreference(getString(R.string.settings_writeValue_key));
          setHelper.setDefaultValue(R.string.settings_writeValue_default);
          sharedPreferences1.edit().putString(getString(R.string.settings_writeValue_key), setHelper.getText()).apply();

          setHelper = (EditTextPreference) findPreference(getString(R.string.settings_weight_key));
          setHelper.setDefaultValue(R.string.settings_weight_default);
          sharedPreferences1.edit().putInt(getString(R.string.settings_weight_key), Integer.valueOf(setHelper.getText())).apply();

          CheckBoxPreference setHelperB = (CheckBoxPreference) findPreference(getString(R.string.settings_inputDone_key));
          setHelperB.setDefaultValue(R.string.settings_inputDone_default);
          sharedPreferences1.edit().putBoolean(getString(R.string.settings_inputDone_key), setHelperB.isChecked()).apply();

          setHelperB = (CheckBoxPreference) findPreference(getString(R.string.settings_weightKg_key));
          setHelperB.setDefaultValue(R.string.settings_weightKg_default);
          sharedPreferences1.edit().putBoolean(getString(R.string.settings_weightKg_key), setHelperB.isChecked()).apply();
      }
    }

    @Override
    public void onResume() {
      super.onResume();
      getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
      getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
      super.onPause();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
