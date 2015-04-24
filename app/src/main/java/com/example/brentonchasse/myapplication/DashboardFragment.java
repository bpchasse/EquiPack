package com.example.brentonchasse.myapplication;

import android.app.Activity;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


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
    private static final int NUMBER_OF_DATA_POINTS = 11;
    private static final double MAX_Y_VALUE = .2;
    private static final double MIN_Y_VALUE = -.2;

    private Bundle savedState;
    private GraphView mGraph;
    private Button mAddDataBtn;
    private Button mCalBtn;
    private EditText mYInput;
    private FrameLayout mArrowBox;
    private LinearLayout mDebugLayout;
    private boolean mDebugging;
    private boolean mSimulate;
    private TextView[] mDebugDataViews = new TextView[8];
    private ImageView mLUp;
    private ImageView mRUp;
    private ImageView mLDown;
    private ImageView mRDown;
    private boolean mInputDoneMeansAdd;

    private LineGraphSeries<DataPoint> mSeries;
    private List<double[]> mRestoredSeriesData = new ArrayList<double[]>();
    private DataPoint[] mData;

    private Semaphore messageLock;
    private Semaphore graphLock;

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
        mInputDoneMeansAdd = getActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE).getBoolean(getString(R.string.settings_inputDone_key), false);
        mDebugging = getActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE).getBoolean(getString(R.string.settings_debug_mode_key), false);
        mSimulate = getActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE).getBoolean(getString(R.string.settings_simulate_key), false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        if(savedInstanceState != null && savedState == null)
            savedState = savedInstanceState.getBundle(Integer.toString(R.string.dashboardGraphRestore));
        if(savedState != null) {
            int numSeries = savedState.getInt(Integer.toString(R.string.dashboardNumberOfRestorableSeries));
            for(int i = 0; i  < numSeries; i++) {
                mRestoredSeriesData.add(savedState.getDoubleArray(Integer.toString(R.string.dashboardGraphSeries) + i));
            }
        }
        savedState = null;
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGraph = (GraphView) getView().findViewById(R.id.dashBoardGraph);
        mAddDataBtn = (Button) getView().findViewById(R.id.addDataBtn);
        mCalBtn = (Button) getView().findViewById(R.id.dashCalBtn);
        mYInput = (EditText) getView().findViewById(R.id.yInput);
        mRUp = (ImageView) getView().findViewById(R.id.rightUpArrow);
        mLUp = (ImageView) getView().findViewById(R.id.leftUpArrow);
        mRDown = (ImageView) getView().findViewById(R.id.rightDownArrow);
        mLDown = (ImageView) getView().findViewById(R.id.leftDownArrow);
        mArrowBox = (FrameLayout) getView().findViewById(R.id.arrowLayout);
        mDebugLayout = (LinearLayout) getView().findViewById(R.id.debugLayout);

        int sensorsLocated = 0;
        for (int i = mDebugLayout.getChildCount()-1; i >= 0; i--) {
            View tmp = mDebugLayout.getChildAt(i);
            if(tmp instanceof LinearLayout) {
                LinearLayout tmp2 = ((LinearLayout) tmp);
                int numChildren = tmp2.getChildCount();
                for(int j = numChildren-1; j >=0; j--) {
                    tmp = tmp2.getChildAt(j);
                    if(tmp instanceof LinearLayout) {
                        LinearLayout tmp3 = ((LinearLayout) tmp);
                        int numChildren2 = tmp3.getChildCount();
                        for(int k = numChildren2-1; k >=0; k--) {
                            tmp = tmp3.getChildAt(k);
                            if(tmp instanceof TextView && sensorsLocated < 8 && k == 1) {
                                mDebugDataViews[sensorsLocated] = (TextView) tmp;
                                sensorsLocated++;
                            } else if (sensorsLocated == 7) {
                                break;
                            }
                        }
                    }
                }
            }
        }


        //if not in debugger mode{
        //mDebugLayout.setVisibility(View.INVISIBLE);
            mData = new DataPoint[NUMBER_OF_DATA_POINTS];

            mYInput.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (mInputDoneMeansAdd) {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            double y = getYFromInput();
                            if (y != -Double.MAX_VALUE) {
                                addDataPoint(y);
                                return true;
                            }
                        }
                        return false;
                    }
                    return false;
                }
            });

            mRUp.setVisibility(View.INVISIBLE);
            mLUp.setVisibility(View.INVISIBLE);
            mRDown.setVisibility(View.INVISIBLE);
            mLDown.setVisibility(View.INVISIBLE);


            formatGraph();
            formatViewPort();
            populateDataPoints();
            restorePreExistingSeries();
            updateDataSeries();

        //} else if in debugger mode {
        //    mArrowBox.setVisibility(View.INVISIBLE);
        //    mDebugLayout.setVisibility(View.VISIBLE);



        //}
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDebugging == true) {
            mDebugLayout.setVisibility(View.VISIBLE);
            mArrowBox.setVisibility(View.GONE);
        } else {
            mDebugLayout.setVisibility(View.GONE);
            mArrowBox.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        savedState = saveState(); /* vstup defined here for sure */
    }

    private Bundle saveState() { /* called either from onDestroyView() or onSaveInstanceState() */
        Bundle state = new Bundle();
        List<Series> series = mGraph.getSeries();
        int i = 0;
        for (;i < series.size(); i++) {
            state.putDoubleArray(Integer.toString(R.string.dashboardGraphSeries) + i, series.get(i).getAllYValues());
        }
        state.putInt(Integer.toString(R.string.dashboardNumberOfRestorableSeries), i);
        return state;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(Integer.toString(R.string.dashboardGraphRestore), savedState != null ? savedState : saveState());
    }

    public void setInputMeansDone(boolean meansDone) { mInputDoneMeansAdd = meansDone; }

    public void setMessageText(String messageTxt) {
        final String txt = messageTxt;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView messageView = (TextView) getView().findViewById(R.id.dashboardMessage);
                messageView.setText(txt);
                messageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
        });
    }

    public Button getAddDataBtn() {
        return mAddDataBtn;
    }

    public Button getCalBtn() {
        return mCalBtn;
    }

    public void setAddDataBtnEnabled(final boolean bool, final String txt) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAddDataBtn.setEnabled(bool);
                if (bool) {
                    mAddDataBtn.setBackgroundResource(R.drawable.rounded_corner);
                } else {
                    mAddDataBtn.setBackgroundResource(R.drawable.rounded_corner_pressed);
                }
                mAddDataBtn.setText(txt);
            }
        });
    }

    public void setAddDataBtnColor(final int background) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    mAddDataBtn.setBackgroundResource(background);
            }
        });
    }

    public void setCalBtnPressed(final boolean bool) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (bool) {
                    mCalBtn.setBackgroundResource(R.drawable.rounded_corner_pressed);
                } else {
                    mCalBtn.setBackgroundResource(R.drawable.rounded_corner);
                }
            }
        });
    }

    public boolean isDebugging() {
        return mDebugging;
    }

    public boolean isSimulating() {
        return mSimulate;
    }

    public double getYFromInput() {
        String input = mYInput.getText().toString();
        if (!input.equals("") && !input.equals(".") && !input.equals("+") && !input.equals("-") && !input.equals("-.") &&
          !input.equals("+.")) {
            return Double.parseDouble(input);
        } else {
            return -Double.MAX_VALUE;
        }
    }

    public void setGraphVisibility(int visibility) {
        final int viz = (visibility == View.GONE) ? View.GONE : (visibility == View.VISIBLE) ? View.VISIBLE : View.INVISIBLE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGraph.setVisibility(viz);
            }
        });
    }

    public void setArrowLayoutVisibility(final int visibility) {
        final int viz = (visibility == View.GONE) ? View.GONE : (visibility == View.VISIBLE) ? View.VISIBLE : View.INVISIBLE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mArrowBox.setVisibility(viz);
            }
        });
    }

    public void setArrows(final int leftUp, final int rightUp){
        if(leftUp > 0) {
            if(leftUp == 0) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLDown.setVisibility(View.VISIBLE);
                        mLUp.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLDown.setVisibility(View.INVISIBLE);
                        mLUp.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLDown.setVisibility(View.INVISIBLE);
                    mLUp.setVisibility(View.INVISIBLE);
                }
            });
        }
        if(rightUp > 0) {
            if(rightUp == 0) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLDown.setVisibility(View.VISIBLE);
                        mLUp.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLDown.setVisibility(View.INVISIBLE);
                        mLUp.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRDown.setVisibility(View.INVISIBLE);
                    mRUp.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public void setRightUpArrowVisibility(final int visibility) {
        final int viz = (visibility == View.GONE) ? View.GONE : (visibility == View.VISIBLE) ? View.VISIBLE : View.INVISIBLE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRUp.setVisibility(viz);
            }
        });
    }
    public void setRightDownArrowVisibility(final int visibility) {
        final int viz = (visibility == View.GONE) ? View.GONE : (visibility == View.VISIBLE) ? View.VISIBLE : View.INVISIBLE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRDown.setVisibility(viz);
            }
        });
    }
    public void setLeftUpArrowVisibility(final int visibility) {
        final int viz = (visibility == View.GONE) ? View.GONE : (visibility == View.VISIBLE) ? View.VISIBLE : View.INVISIBLE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLUp.setVisibility(viz);
            }
        });
    }
    public void setLeftDownArrowVisibility(final int visibility) {
        final int viz = (visibility == View.GONE) ? View.GONE : (visibility == View.VISIBLE) ? View.VISIBLE : View.INVISIBLE;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLDown.setVisibility(viz);
            }
        });
    }

    public void setDebugMode(boolean debugMode) {
        mDebugging = debugMode;
    }

    public void setSimulateMode(boolean simulateMode) {
        mSimulate = simulateMode;
    }

    public void updateDebugData(final int[] data) {

        for(int i = 0; i < mDebugDataViews.length; i++) {
            final String newSensorData = Integer.toString(data[i]);
            final int k = i;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDebugDataViews[k].setText(newSensorData);
                }
            });
        }

        /*getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < mDebugDataViews.length; i++) {
                    mDebugDataViews[i].setText(Integer.toString(data[i]));
                }
                //getActivity().reset
            }
        });*/
    }


    public void updateDataSeries() {
        mGraph.removeAllSeries();
        mGraph.addSeries(mSeries);
    }

    public void addDataPoint(double y) {
        final double ty = y;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSeries.shiftSeriesXValues(0, NUMBER_OF_DATA_POINTS, -1);
                mSeries.appendData(new DataPoint(NUMBER_OF_DATA_POINTS - 1, ty), false, NUMBER_OF_DATA_POINTS);
            }
        });
        //mSeries.shiftSeriesXValues(0, NUMBER_OF_DATA_POINTS, -1);
        //mSeries.appendData(new DataPoint(NUMBER_OF_DATA_POINTS - 1, y), false, NUMBER_OF_DATA_POINTS);
    }

    public void populateDataPoints() {
        for(int i = 0; i < NUMBER_OF_DATA_POINTS; i++) {
            mData[i] = new DataPoint(i, 0);
        }
        mSeries = new LineGraphSeries<DataPoint>(mData);
    }

    public void formatViewPort() {
        Viewport viewport = mGraph.getViewport();
        viewport.setMaxX(NUMBER_OF_DATA_POINTS - 1);
        viewport.setMinX(0);
        viewport.setMaxY(MAX_Y_VALUE);
        viewport.setMinY(MIN_Y_VALUE);
        viewport.setXAxisBoundsManual(true);
        viewport.setYAxisBoundsManual(true);
    }

    public void formatGraph() {
        int blue = getResources().getColor(android.R.color.holo_blue_light);
        int white = getResources().getColor(android.R.color.white);
        GridLabelRenderer gridRenderer = mGraph.getGridLabelRenderer();
        gridRenderer.setHighlightCenterHorizontalLines(true);
        gridRenderer.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        gridRenderer.setGridColor(blue);
        gridRenderer.setPadding(15);
        gridRenderer.setTextSize(14);
        gridRenderer.setLabelsSpace(5);
        gridRenderer.setHorizontalLabelsColor(white);
        gridRenderer.setVerticalLabelsColor(white);
        gridRenderer.setHorizontalAxisTitleColor(white);
        gridRenderer.setVerticalAxisTitleColor(white);
    }

    public void restorePreExistingSeries() {
        if (mRestoredSeriesData.size() > 0) {
            for (int s = 0; s < mRestoredSeriesData.size(); s++) {
                double[] yValues = mRestoredSeriesData.get(s);
                for (int x = 0; x < yValues.length; x++) {
                    addDataPoint(yValues[x]);
                }
            }
        }
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
