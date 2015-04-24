package com.example.brentonchasse.myapplication;

import android.opengl.Visibility;
import android.view.View;
import android.widget.Toast;


import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by bp on 3/9/15.
 */
public class EquipackAnalytics {
    private DashboardFragment DashboardFrag;

    //Public and global statics
    public static int Cycle_Size = 5;	//in Seconds
    public static int Cycles = 10;
    public static int Packet_Size = 10;
    public static int Sampling_Freq = 60000; //in Hz
    public static int LDS = 8;
    public static int LUS = 1;
    public static int RDS = 2;
    public static int RUS = 3;
    public static int LDB = 4;
    public static int LUB = 5;
    public static int RDB = 6;
    public static int RUB = 7;
    public static int LOAD = 0;
    public static double a = 50;
    public static double Zero_Weight = 0.000140;
    public static double Threshold = 0.1;


    public double Backl = -1;
    public double Strapl = -1;
    public double high = 0;
    public int stage = 0;
    public double[] Init;
    public boolean Initialized = false;

    public void Calibrate_Initial(double packet[]){
        Init = new double[8];
        for(int i=0;i<8;i++){
            Init[i] = packet[i];
        }
    }


    public EquipackAnalytics(DashboardFragment frag) {
        DashboardFrag = frag;
    }

    public double[][][] TestInputStatic = new double[][][]{
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
            {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
    };
    public double[][][] TestInputDynamic = new double[][][]
            {{{2, 0},{0.5,0}, {1.5,0},{0.25,0},{0.25,0},{0.5,0},{0.5,0},{0.5,0},{1.5,0}},
                    {{2, 0},{0.5,0}, {1.5,0},{0.25,0},{0.25,0},{0.5,0},{0.5,0},{0.5,0},{1.5,0}},
                    {{2, 0},{0.5,0}, {1.5,0},{0.25,0},{0.25,0},{0.5,0},{0.5,0},{0.5,0},{1.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{3.5,0}, {0.5,0},{3.5,0},{2.5,0},{2.5,0},{2.5,0},{2.5,0},{0.5,0}},
                    {{2, 0},{3.5,0}, {0.5,0},{3.5,0},{2.5,0},{2.5,0},{2.5,0},{2.5,0},{0.5,0}},
                    {{2, 0},{3.4,0}, {0.4,0},{3.4,0},{0.6,0},{0.6,0},{0.6,0},{0.6,0},{0.4,0}},
                    {{2, 0},{3,0}, {0.2,0},{3,0},{1,0},{1,0},{1,0},{1,0},{0.2,0}},
                    {{2, 0},{3.2,0}, {.4,0},{3.2,0},{0.8,0},{0.8,0},{0.8,0},{0.8,0},{0.8,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}},
                    {{2, 0},{0.5,0}, {0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0},{0.5,0}}};


    /**
     * Weight Analytics
     */


    public double[] Linear_Regression_Slope(int Sensor, int Cycle, double array[][])	{
        double sumy = 0;
        double avgx = 0;
        double avgy = 0;
        double Slope = 0;
        double Value = 0;
        double xx = 0;
        double xy = 0;
        double yy = 0;
        for ( int i=Cycle_Size*Sampling_Freq*Cycle; i<Cycle_Size*Sampling_Freq; i++) {

            sumy = array[Sensor][i] + sumy;
            avgy = sumy / (Cycle_Size * Sampling_Freq);
            avgx = (Cycle_Size * Sampling_Freq + 1) / 2;
        }
        for( int i=Cycle_Size*Sampling_Freq*Cycle; i<Cycle_Size*Sampling_Freq; i++){
            xx += (i - avgx) * (i - avgx);
            yy += (array[Sensor][i] - avgy) * (array[Sensor][i] - avgy);
            xy += (i - avgx) * (array[Sensor][i] - avgx);
        }
        Slope=xy/xx;


        Value= avgy - Slope*avgx;
        double[] out;
        out = new double[2];
        out[0] = Value;
        out[1] = Slope;
        return out;
    }

    public double Weight_Analytics_LR( double[] LRArray){
        //Return 0 : even sensors, go to next Stage
        //Return 1 : Loosen Right
        //Return -1: Loosen Left
        //Return 2: Error, Retest
        double [] LArray = LRArray.clone();
        double LUStrap = LArray[LUS]-Init[LUS];
        double LDStrap = LArray[LDS]-Init[LDS];
        double RUStrap = LArray[RUS]-Init[RUS];
        double RDStrap = LArray[RDS]-Init[RDS];
        //average upper and lower
        double L = (LUStrap + LDStrap)/2;
        double R = (RUStrap + RDStrap)/2;
        //if((Math.abs(LArray[LUS][1]+LArray[LUS][1])>0.05)||(Math.abs(LArray[LUS][1]+LArray[LUS][1])>0.05)){
        //    return -1*Double.MAX_VALUE;
        //}
        //Error testing, Obsolete

        if(Math.abs(R-L) < Threshold){
            return 0;
        }
        else{
            return (Math.abs(R-L)/(R-L));
        }
    }

    public double getWeight(double LRArray[]){
        return (LRArray[LOAD]-Zero_Weight)/(2*0.000019*1665);
    }

    public double Weight_Analytics_UD(double[] LArray){
        double [] LRArray = LArray.clone();
        if (high == -1) {

            double UStrap = (LRArray[LUS] + LRArray[RUS]) / 2;
            double DStrap = (LRArray[LDS] + LRArray[RDS]) / 2;
            high = 0;
            if (UStrap > DStrap) {
                high = 1;
            }

        }
        double pullStraps = 0;
        double Back = (LRArray[LDB] + LRArray[RDB] + LRArray[RUB] + LRArray[LUB]) / 4;
        double Strap = (LRArray[LUS] + LRArray[RUS] / 2 + LRArray[LDS] + LRArray[RDS]) / 4;
        if ((Strapl >= Strapl - 0/*Threshold*/) && (Back <= Backl + 0/*Threshold*/) || (Backl < 0)) {
            if (high == 1) {
                pullStraps = -1;
            } else {
                pullStraps = 1;
            }
            if(((LRArray[LDS] + LRArray[RDS] + LRArray[RUS] + LRArray[LUS]) / 4) < -.05){
                pullStraps = 2;
                //Why did I do this?
            }
        }

        Strapl = Strap;
        Backl = Back;
        return pullStraps;
    }

    public double Weight_Analytics_COM(double[] LRArray){
        return 0.5*0.5*0.5/(2*(LRArray[LOAD]-Zero_Weight)/(2*0.000019*1665)*0.453592*9.81*((LRArray[LDB] + LRArray[RDB] + LRArray[RUB] + LRArray[LUB]) / 4) + a);
    }

    public int[] Weight_Analytics(double[] LRArray2) {
        int[] out = new int[2];
        int result = 0;
        if(!Initialized)
            Calibrate_Initial(LRArray2);
        switch (stage) {
            case 0:
                result = (int) (Weight_Analytics_LR(LRArray2));
                switch (result) {
                    case 0:
                        out[0] = -5;
                        out[1] = -5;
                        stage = 1;
                        break;
                    case -1:
                        out[0] = 1;
                        out[1] = 0;
                        break;
                    case 1:
                        out[0] = 0;
                        out[1] = 1;
                        break;
                    case 2:
                        out[0] = -2;
                        out[1] = -2;
                        break;
                    case 3:
                        out[0] = -3;
                        out[1] = -3;
                        resetAnalyticsVariables();
                        break;
                    //0: even symmetry, send message
                    //1:Loosen Right, Tighten Left
                    //-1:Tighten Right, Loosen Left
                    //2: Non-fatal error, ignore data
                    //3: Fatal error exit stage
                }

                break;
            case 1:
                //TODO: hide graph, show arrows
                result = (int) Weight_Analytics_UD(LRArray2);
                switch (result) {
                    case 0:
                        out[0] = -6;
                        out[1] = -6;
                        stage = 1;
                        break;
                    case -1:
                        out[0] = 1;
                        out[1] = 1;
                        break;
                    case 1:
                        out[0] = 0;
                        out[1] = 0;
                        break;
                    case 2:
                        out[0] = -2;
                        out[1] = -2;
                        break;
                    case 3:
                        out[0] = -3;
                        out[1] = -3;
                        resetAnalyticsVariables();
                        break;
                    //0: even symmetry, send message
                    //1:Loosen Right, Tighten Left
                    //-1:Tighten Right, Loosen Left
                    //2: Non-fatal error, ignore data
                    //3: Fatal error exit stage
                }
                break;
            //case 2:
            //    out = Weight_Analytics_COM(LRArray2);
            //    if (out < .10) {
            //send message to move center of mass forward
            //TODO:Do we want other things to happen here?
            //    }
            //    break;
        }
        return out;
    }

    public boolean Verify_Model(double[]Packet, boolean back) {
        if (Math.abs(Packet[RUS] - Packet[LUS] - (Packet[RDS] - Packet[LDS])) > 3) {
            return true;
        }
        else
            return false;
    }

    /**
     * colin's tester function
     */
    public void testWeightAnalytics(){
        int out = 0;
        double last = 0;
        for(int i = 0; i < 9; i++){
            final double[] TEST_INPUT_DYNAMIC = TestInputDynamic[i][0];
            final int[] tmp = new int[TEST_INPUT_DYNAMIC.length];
            for(int t = 0; t < TEST_INPUT_DYNAMIC.length; t++){
                tmp[t] = (int) TEST_INPUT_DYNAMIC[t];
            }
            Callable<Double> analyze = new Callable<Double>() {
                @Override
                public Double call() throws Exception {
                    return Weight_Analytics_test( TEST_INPUT_DYNAMIC);
                }
            };

            //Schedule the above callable to be run after a delay.
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            ScheduledFuture<Double> f = scheduler.schedule(analyze, 1, TimeUnit.SECONDS);
            boolean listen = true;
            double result = -1 * Double.MAX_VALUE;
            while (listen) {
                //Listen until the callable has finished
                if (f.isDone()) {
                    try {
                        listen = false;
                        result = f.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Result: " + result);
            System.out.println("Stage: " + stage);
            //If not incorrect and is in "Symmetry" stage, or returned from last call of "Symmetry" stage
            if (result != -1 * Double.MAX_VALUE && (stage == 0 || result == 0 && stage == 1)) {
                /**All UI changes made here must be specifically run in the UI thread, this is a forked thread **/
                if(i == 0) {
                    DashboardFrag.setArrowLayoutVisibility(View.VISIBLE);
                    DashboardFrag.setGraphVisibility(View.GONE);
                    //DashboardFrag.setArrowLayoutVisibility(View.GONE);
                    //DashboardFrag.setGraphVisibility(View.VISIBLE);
                }
                if (result > 0) {
                    DashboardFrag.setMessageText("Tighten RIGHT strap.");
                    DashboardFrag.setRightDownArrowVisibility(View.VISIBLE);
                    DashboardFrag.setLeftDownArrowVisibility(View.INVISIBLE);
                    DashboardFrag.setRightUpArrowVisibility(View.INVISIBLE);
                    DashboardFrag.setLeftUpArrowVisibility(View.INVISIBLE);
                } else if (result < 0) {
                    DashboardFrag.setMessageText("Tighten LEFT strap.");
                    DashboardFrag.setRightDownArrowVisibility(View.INVISIBLE);
                    DashboardFrag.setLeftDownArrowVisibility(View.VISIBLE);
                    DashboardFrag.setRightUpArrowVisibility(View.INVISIBLE);
                    DashboardFrag.setLeftUpArrowVisibility(View.INVISIBLE);
                } else if (result == 0) {
                    DashboardFrag.setMessageText("Straps are symmetric!");
                    /*
                        Hide all of the arrows once symmetric
                     */
                    DashboardFrag.setLeftDownArrowVisibility(View.INVISIBLE);
                    DashboardFrag.setRightDownArrowVisibility(View.INVISIBLE);
                    DashboardFrag.setLeftUpArrowVisibility(View.INVISIBLE);
                    DashboardFrag.setRightUpArrowVisibility(View.INVISIBLE);
                    //DashboardFrag.addDataPoint(result);
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //if(result != 0) DashboardFrag.addDataPoint(result);
            }
            //If now in the "Height" stage
            if (result != -1 * Double.MAX_VALUE && stage == 1) {
                DashboardFrag.setGraphVisibility(View.GONE);
                DashboardFrag.setArrowLayoutVisibility(View.VISIBLE);
                if(result > 0 && result != last) {
//show an up arrow
                    DashboardFrag.setMessageText("Raise bag by tightening both straps.");
                    DashboardFrag.setLeftDownArrowVisibility(View.VISIBLE);
                    DashboardFrag.setRightDownArrowVisibility(View.VISIBLE);
                    DashboardFrag.setLeftUpArrowVisibility(View.INVISIBLE);
                    DashboardFrag.setRightUpArrowVisibility(View.INVISIBLE);
                    last = result;
                } else if (result < 0 && result != last) {
//show a down arrow
                    DashboardFrag.setMessageText("Lower bag by loosening both straps.");
                    DashboardFrag.setLeftUpArrowVisibility(View.VISIBLE);
                    DashboardFrag.setRightUpArrowVisibility(View.VISIBLE);
                    DashboardFrag.setLeftDownArrowVisibility(View.INVISIBLE);
                    DashboardFrag.setRightDownArrowVisibility(View.INVISIBLE);
                    last = result;
                }
            }
            //If now in the "Height" stage
            if (result != -1 * Double.MAX_VALUE && stage == 2) {
                DashboardFrag.setArrowLayoutVisibility(View.VISIBLE);
                //DashboardFrag.setArrowLayoutVisibility(View.INVISIBLE);
                //DashboardFrag.setGraphVisibility(View.VISIBLE);
                DashboardFrag.setGraphVisibility(View.GONE);
                if(result > 0) {
//idk
                } else if (result < 0) {
//idk
                }
            }
        }
        //Bag has been positioned sucessfully

        //DashboardFrag.populateDataPoints();  //set all points to 0
        //DashboardFrag.updateDataSeries();   //update the graphed series

        DashboardFrag.setMessageText("Optimized!");
        DashboardFrag.setAddDataBtnEnabled(true, "Optimize!");
        //DashboardFrag.setGraphVisibility(View.VISIBLE);  //Put a "new" graph into view of the user
        //DashboardFrag.setArrowLayoutVisibility(View.INVISIBLE);
        DashboardFrag.setGraphVisibility(View.GONE);
        /*
            Hide all of the arrows once optimized
         */
        DashboardFrag.setLeftDownArrowVisibility(View.INVISIBLE);
        DashboardFrag.setRightDownArrowVisibility(View.INVISIBLE);
        DashboardFrag.setLeftUpArrowVisibility(View.INVISIBLE);
        DashboardFrag.setRightUpArrowVisibility(View.INVISIBLE);
        //DashboardFrag.setArrowLayoutVisibility(View.VISIBLE);
    }

    public double Weight_Analytics_test(double[] LRArray2) {
        double out = 0;
        if(!Initialized)
            Calibrate_Initial(LRArray2);
        switch (stage) {
            case 0:
                out = Weight_Analytics_LR(LRArray2);
                if (out == 0) {
                    //even symmetry, send message
                    out = 0;
                    stage = 1;
                }

                break;
            case 1:
                //TODO: hide graph, show arrows
                out = Weight_Analytics_UD(LRArray2);
                if ((out == 0) || (out == 2)) {
                    //correct height
                    //send message to stop adjusting
                    stage = 2;
                } /*else {
                   TODO: show an arrow that tells user to raise or lower the pack
                }*/
                break;
            case 2:
                out = Weight_Analytics_COM(LRArray2);
                if (out < .10) {
                    //send message to move center of mass forward
                    //TODO:Do we want other things to happen here?
                }
                break;
        }
        return out;
    }

    public void resetAnalyticsVariables() {
        Backl = -1;
        Strapl = -1;
        high = 0;
        stage = 0;
    }
}