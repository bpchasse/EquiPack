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
import com.jjoe64.graphview.GridLabelRenderer;
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
    private static final int NUMBER_OF_DATA_POINTS = 101;
    private static final int MAX_Y_VALUE = 5;
    private static final int MIN_Y_VALUE = -5;

    private GraphView mGraph;
    private Button mAddDataBtn;
    //private EditText mXInput;
    private EditText mYInput;

    private LineGraphSeries<DataPoint> mSeries;
    private DataPoint[] mData;

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
        mData = new DataPoint[NUMBER_OF_DATA_POINTS];
        mGraph = (GraphView) getView().findViewById(R.id.dashBoardGraph);
        mAddDataBtn = (Button) getView().findViewById(R.id.addDataBtn);
        mYInput = (EditText) getView().findViewById(R.id.yInput);

        setGraphColors();
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
        mGraph.removeAllSeries();
        mGraph.addSeries(mSeries);
    }

    public void addDataPoint(int y) {
        mSeries.shiftSeriesXValues(0, NUMBER_OF_DATA_POINTS, -1);
        mSeries.appendData(new DataPoint(NUMBER_OF_DATA_POINTS-1, y), false, NUMBER_OF_DATA_POINTS);
        updateDataSeries();
    }

    public void populateDataPoints() {
        for(int i = 0; i < NUMBER_OF_DATA_POINTS; i++) {
            mData[i] = new DataPoint(i, 0);
        }
        mSeries = new LineGraphSeries<DataPoint>(mData);
    }

    public void formatGraphToDefault() {
        Viewport viewport = mGraph.getViewport();
        viewport.setMaxX(NUMBER_OF_DATA_POINTS-1);
        viewport.setMinX(0);
        viewport.setMaxY(MAX_Y_VALUE);
        viewport.setMinY(MIN_Y_VALUE);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
    }

    public void setGraphColors() {
        int blue = getResources().getColor(android.R.color.holo_blue_light);
        GridLabelRenderer gridRenderer = mGraph.getGridLabelRenderer();
        gridRenderer.setGridColor(blue);
        gridRenderer.setPadding(15);
        gridRenderer.setTextSize(12);
        gridRenderer.setLabelsSpace(8);
        gridRenderer.setHorizontalLabelsColor(blue);
        gridRenderer.setVerticalLabelsColor(blue);
        gridRenderer.setHorizontalAxisTitleColor(blue);
        gridRenderer.setVerticalAxisTitleColor(blue);
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
