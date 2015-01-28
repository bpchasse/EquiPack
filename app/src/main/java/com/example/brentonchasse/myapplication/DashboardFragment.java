package com.example.brentonchasse.myapplication;

import android.app.Activity;
import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DashboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class DashboardFragment extends Fragment {
    private static final int NUMBER_OF_DATA_POINTS = 10;

    private GraphView mGraph;
    private Button mAddDataBtn;
    //private EditText mXInput;
    private EditText mYInput;

    private LineGraphSeries<DataPoint> mSeries;
    private ArrayList<DataPoint> mData = new ArrayList<DataPoint>();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DashboardFragment.
     */
    public static DashboardFragment newInstance(String param1, String param2) {
        DashboardFragment fragment = new DashboardFragment();
        return fragment;
    }
    public DashboardFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionbar = getActivity().getActionBar();
        if(actionbar != null) actionbar.setTitle(getString(R.string.app_name));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGraph = (GraphView) getView().findViewById(R.id.dashBoardGraph);
        mAddDataBtn = (Button) getView().findViewById(R.id.addDataBtn);
        mAddDataBtn.setBackgroundResource(android.R.drawable.btn_default);
        //mXInput = (EditText) getView().findViewById(R.id.xInput);
        mYInput = (EditText) getView().findViewById(R.id.yInput);

        formatGraphToDefault();
        populateDataPoints();
        updateDataSeries();
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public Button getAddDataBtn() {
        return mAddDataBtn;
    }

    /*public int getXFromInput() {
        return Integer.parseInt(mXInput.getText().toString());
    }*/

    public int getYFromInput() {
        return Integer.parseInt(mYInput.getText().toString());
    }

    public void updateDataSeries() {
        DataPoint[] data = new DataPoint[NUMBER_OF_DATA_POINTS];
        data = mData.toArray(data);
        mSeries = new LineGraphSeries<DataPoint>(data);
        mGraph.removeAllSeries();
        mGraph.addSeries(mSeries);
    }

    public void addDataPoint(int y) {
        if(mData.size() == NUMBER_OF_DATA_POINTS) {
            mData.remove(0);
            for(int i = 0;i < mData.size(); i++) {
                mData.set(i, new DataPoint(mData.get(i).getX() - 1, mData.get(i).getY()));
            }
            mData.add(new DataPoint (NUMBER_OF_DATA_POINTS, y));
        }
        updateDataSeries();
    }

    public void populateDataPoints() {
        for(int i = 0; i < NUMBER_OF_DATA_POINTS; i++) {
            mData.add(new DataPoint(i, 0));
        }
    }

    public void formatGraphToDefault() {
        Viewport viewport = mGraph.getViewport();
        viewport.setMaxX(NUMBER_OF_DATA_POINTS);
        viewport.setMinX(0);
        viewport.setMaxY(NUMBER_OF_DATA_POINTS/2);
        viewport.setMinY(NUMBER_OF_DATA_POINTS/(-2));
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
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
