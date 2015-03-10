package com.example.brentonchasse.myapplication;

import android.opengl.Visibility;
import android.view.View;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;

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
    public  double[] seperator(int Packet[], double array[][]) {
        double[] LRArray;
        LRArray = new double[10];
        int Buffer_Size = Cycles*Cycle_Size*Sampling_Freq;
        for (int j = 0; j < Packet_Size; j++) {
            for (int i = Buffer_Size; i >1; i--) {
                array[i][j] = array[i-1][j];
                array[0][j] = Packet[j];
            }
            for(int n = 0; n<9; n++)
                LRArray = Linear_Regression_Slope(n,0, array );
        }
        return LRArray;
    }

    public double[][] Linearizer(double array[][]){
        double LRArray[][];
        LRArray = new double[10][2];
        for(int n = 0; n<9; n++)
            LRArray[n] = Linear_Regression_Slope(n,0, array );
        return LRArray;
    }

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

    public double Weight_Analytics_LR( double[][] LRArray){
        double [][] LArray = LRArray.clone();
        double LUStrap = LArray[LUS][0];
        double LDStrap = LArray[LDS][0];
        double RUStrap =LArray[RUS][0];
        double RDStrap = LArray[RDS][0];
        //average upper and lower
        double L = (LUStrap + LDStrap)/2;
        double R = (RUStrap + RDStrap)/2;
        if((Math.abs(LArray[LUS][1]+LArray[LUS][1])>0.05)||(Math.abs(LArray[LUS][1]+LArray[LUS][1])>0.05)){
            return -1*Double.MAX_VALUE;
        }

        if(Math.abs(R-L) < Threshold){
            return 0;
        }
        else{
            return (R-L);
        }
    }

    public double getWeight(double LRArray[][]){
        return (LRArray[LOAD][0]-Zero_Weight)/(2*0.000019*1665);
    }

    public double Weight_Analytics_UD(double[][] LArray, double high, double Backlast, double Straplast){
        double [][] LRArray = LArray.clone();
        if (high == -1) {

            double UStrap = (LRArray[LUS][0] + LRArray[RUS][0]) / 2;
            double DStrap = (LRArray[LDS][0] + LRArray[RDS][0]) / 2;
            high = 0;
            if (UStrap > DStrap) {
                high = 1;
            }

        }
        double pullStraps = 0;
        double Back = (LRArray[LDB][0] + LRArray[RDB][0] + LRArray[RUB][0] + LRArray[LUB][0]) / 4;
        double Strap = (LRArray[LUS][0] + LRArray[RUS][0] / 2 + LRArray[LDS][0] + LRArray[RDS][0]) / 4;
        if ((Straplast >= Straplast) && (Back < Backlast) || (Backlast < 0)) {
            if (high == 1) {
                pullStraps = -1;
            } else {
                pullStraps = 1;
            }
            if(((LRArray[LDS][1] + LRArray[RDS][1] + LRArray[RUS][1] + LRArray[LUS][1]) / 4) < -.05){
                pullStraps = 2;
            }
        }

        double[] out;
        out = new double[4];
        out[0] = pullStraps;
        out[1] = Strap;
        out[2] = Back;
        out[3] = high;
        return out[0];
    }

    public double Weight_Analytics_COM(double[][] LRArray){
        return 0.5*0.5*0.5/(2*(LRArray[LOAD][0]-Zero_Weight)/(2*0.000019*1665)*0.453592*9.81*((LRArray[LDB][0] + LRArray[RDB][0] + LRArray[RUB][0] + LRArray[LUB][0]) / 4) + a);
    }

    public double Weight_Analytics(double[][] LRArray2) {
        double out = 0;
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
                out = Weight_Analytics_UD(LRArray2, high, Backl, Strapl);
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

    /**
     * colin's tester function
     */
    public void testWeightAnalytics(){
        int out = 0;
        double last = 0;
        for(int i = 0; i < 9; i++){
            final double[][] TEST_INPUT_DYNAMIC = TestInputDynamic[i];
            Callable<Double> analyze = new Callable<Double>() {
                @Override
                public Double call() throws Exception {
                    return Weight_Analytics(TEST_INPUT_DYNAMIC);
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
                    DashboardFrag.setArrowLayoutVisibility(View.GONE);
                    DashboardFrag.setGraphVisibility(View.VISIBLE);
                }
                if (result > 0) {
                    DashboardFrag.setMessageText("Tighten RIGHT strap.");
                } else if (result < 0) {
                    DashboardFrag.setMessageText("Tighten LEFT strap.");
                } else if (result == 0) {
                    DashboardFrag.setMessageText("Straps are symmetric!");
                    DashboardFrag.addDataPoint(result);
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if(result != 0) DashboardFrag.addDataPoint(result);
            }
            //If now in the "Height" stage
            if (result != -1 * Double.MAX_VALUE && stage == 1) {
                DashboardFrag.setGraphVisibility(View.GONE);
                DashboardFrag.setArrowLayoutVisibility(View.VISIBLE);
                if(result > 0 && result != last) {
//show an up arrow
                    DashboardFrag.setMessageText("Raise bag by shortening both straps.");
                    DashboardFrag.setLeftUpArrowVisibility(View.VISIBLE);
                    DashboardFrag.setRightUpArrowVisibility(View.VISIBLE);
                    DashboardFrag.setLeftDownArrowVisibility(View.GONE);
                    DashboardFrag.setRightDownArrowVisibility(View.GONE);
                    last = result;
                } else if (result < 0 && result != last) {
//show a down arrow
                    DashboardFrag.setMessageText("Lower bag by lengthening both straps.");
                    DashboardFrag.setLeftDownArrowVisibility(View.VISIBLE);
                    DashboardFrag.setRightDownArrowVisibility(View.VISIBLE);
                    DashboardFrag.setLeftUpArrowVisibility(View.GONE);
                    DashboardFrag.setRightUpArrowVisibility(View.GONE);
                    last = result;
                }
            }
            //If now in the "Height" stage
            if (result != -1 * Double.MAX_VALUE && stage == 2) {
                DashboardFrag.setArrowLayoutVisibility(View.GONE);
                DashboardFrag.setGraphVisibility(View.VISIBLE);
                if(result > 0) {
//idk
                } else if (result < 0) {
//idk
                }
            }
        }
        //Bag has been positioned sucessfully
        DashboardFrag.populateDataPoints();  //set all points to 0
        DashboardFrag.updateDataSeries();   //update the graphed series
        DashboardFrag.setMessageText("");
        DashboardFrag.setGraphVisibility(View.VISIBLE);  //Put a "new" graph into view of the user
        DashboardFrag.setArrowLayoutVisibility(View.GONE);
    }

    public void resetAnalyticsVariables() {
        Backl = -1;
        Strapl = -1;
        high = 0;
        stage = 0;
    }
}
