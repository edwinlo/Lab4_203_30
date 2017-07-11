/*******************************************************
 * AccelerometerEventListener.java          Feb 10th 2017
 *
 * Group # 30
 *
 * Lab Members: Edwin Lo, Madeleine Wang, Yizhou Yang
 *
 *******************************************************/

package lab4_203_30.uwaterloo.ca.lab4_203_30;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.text.LoginFilter;
import android.util.Log;
import android.widget.TextView;

public class AccelerometerEventListener implements SensorEventListener {

    private final float FILTER_CONSTANT = 12.0f;

    enum myState{WAIT, RISE_A, FALL_A, FALL_B, RISE_B, DETERMINED};
    myState state = myState.WAIT;
    myState state2 = myState.WAIT;

    enum mySig{SIG_A, SIG_B, SIG_C, SIG_D, SIG_X};
    mySig signature = mySig.SIG_X;

    //FSM: setup threshold constants here
    final float[] THRES_A = {0.5f, 2.0f, -0.4f};
    final float[] THRES_B = {-0.5f, -2.0f, 0.4f};

    //FSM: Setup FSM sample counter here
    final int SAMPLEDEFAULT = 30;
    int sampleCounterx = SAMPLEDEFAULT;
    int sampleCountery = SAMPLEDEFAULT;


    private GameLoopTask myGL;
    private TextView instanceOutput;
    //private LineGraphView graph;

    private float[][] historyReading = new float[100][3];


    //FIFO 100-element roration method
    private void insertHistoryReading(float[] values){
        for(int i = 1; i < 100; i++){
            historyReading[i - 1][0] = historyReading[i][0];
            historyReading[i - 1][1] = historyReading[i][1];
            historyReading[i - 1][2] = historyReading[i][2];
        }

        historyReading[99][0] += (values[0] - historyReading[99][0])/ FILTER_CONSTANT;
        historyReading[99][1] += (values[1] - historyReading[99][1])/ FILTER_CONSTANT;
        historyReading[99][2] += (values[2] - historyReading[99][2])/ FILTER_CONSTANT;

        callFSMx();
        callFSMy();
        //Make sure that by the 30th sample, the FSM result is generated.  If not, it is a bad signature.
        if(sampleCounterx <= 0 || sampleCountery <=0){

            if(state == myState.DETERMINED) {
                if (signature == mySig.SIG_B){
                    instanceOutput.setText("LEFT");
                    myGL.setDirection(GameLoopTask.gameDirection.LEFT);
                } else if (signature == mySig.SIG_A){
                    instanceOutput.setText("RIGHT");
                    myGL.setDirection(GameLoopTask.gameDirection.RIGHT);
                } else if (signature == mySig.SIG_C && state2 == myState.DETERMINED) {
                    instanceOutput.setText("UP");
                    myGL.setDirection(GameLoopTask.gameDirection.UP);
                } else if (signature == mySig.SIG_D && state2 == myState.DETERMINED) {
                    instanceOutput.setText("DOWN");
                    myGL.setDirection(GameLoopTask.gameDirection.DOWN);
                }
                else {
                    instanceOutput.setText("IDLE");
                    myGL.setDirection(GameLoopTask.gameDirection.NO_MOVEMENT);
                }
            }
            else if(state2 == myState.DETERMINED){
                if (signature == mySig.SIG_C){
                    instanceOutput.setText("UP");
                    myGL.setDirection(GameLoopTask.gameDirection.UP);
                } else if(signature == mySig.SIG_D) {
                    instanceOutput.setText("DOWN");
                    myGL.setDirection(GameLoopTask.gameDirection.DOWN);
                } else if (signature == mySig.SIG_A && state == myState.DETERMINED) {
                    instanceOutput.setText("LEFT");
                    myGL.setDirection(GameLoopTask.gameDirection.LEFT);
                }
                else if (signature == mySig.SIG_B && state == myState.DETERMINED) {
                    instanceOutput.setText("RIGHT");
                    myGL.setDirection(GameLoopTask.gameDirection.RIGHT);
                }
                else {
                    instanceOutput.setText("IDLE");
                    myGL.setDirection(GameLoopTask.gameDirection.NO_MOVEMENT);
                }
            }
            else{
                state = myState.WAIT;
                state2 = myState.WAIT;
                instanceOutput.setText("IDLE");
            }

            sampleCounterx = SAMPLEDEFAULT;
            sampleCountery = SAMPLEDEFAULT;
            state = myState.WAIT;
            state2 = myState.WAIT;

        }
    }

    //function for the FSM in the Y-direction
    public void callFSMy(){

        float deltaA = historyReading[99][1] - historyReading[98][1];

        switch(state2){

            case WAIT:
                sampleCountery = SAMPLEDEFAULT;
                signature = mySig.SIG_X;

                if(deltaA > THRES_A[0]){
                    state2 = myState.RISE_A;
                }
                else if(deltaA < THRES_B[0]){
                    state2 = myState.FALL_B;
                }

                break;

            case RISE_A:

                if(deltaA <= 0){
                    if(historyReading[99][1] >= THRES_A[1]){
                        state2 = myState.FALL_A;
                    }
                    else{
                        state2 = myState.DETERMINED;
                    }
                }

                break;

            case FALL_A:

                if(deltaA >= 0){
                    if (historyReading[99][1] <= THRES_A[2]) {
                        signature = mySig.SIG_C;
                    }
                    state2 = myState.DETERMINED;
                }

                break;

            case FALL_B:

                if(deltaA >= 0){
                    if(historyReading[99][1] <= THRES_B[1]){
                        state2 = myState.RISE_B;
                    }
                    else{
                        state2 = myState.DETERMINED;
                    }
                }

                break;

            case RISE_B:

                if(deltaA <= 0){
                    if (historyReading[99][1] >= THRES_B[2]) {
                        signature = mySig.SIG_D;
                    }
                    state2 = myState.DETERMINED;
                }

                break;

            case DETERMINED:

                //Log.d("FSM: ", "State DETERMINED " + signature.toString());

                break;

            default:
                state2 = myState.WAIT;
                break;

        }

        sampleCountery--;

    }

    //function for the FSM in the X-direction
    public void callFSMx(){

        float deltaA = historyReading[99][0] - historyReading[98][0];

        switch(state){
            case WAIT:

                sampleCounterx = SAMPLEDEFAULT;
                signature = mySig.SIG_X;

                if(deltaA > THRES_A[0]){
                    state = myState.RISE_A;
                }
                else if(deltaA < THRES_B[0]){
                    state = myState.FALL_B;
                }

                break;

            case RISE_A:

                if(deltaA <= 0){
                    if(historyReading[99][0] >= THRES_A[1]){
                        state = myState.FALL_A;
                    }
                    else{
                        state = myState.DETERMINED;
                    }
                }

                break;

            case FALL_A:

                if(deltaA >= 0){
                    if (historyReading[99][0] <= THRES_A[2]) {
                        signature = mySig.SIG_A;
                    }
                    state = myState.DETERMINED;
                }

                break;

            case FALL_B:

                if(deltaA >= 0){
                    if(historyReading[99][0] <= THRES_B[1]){
                        state = myState.RISE_B;
                    }
                    else{
                        state = myState.DETERMINED;
                    }
                }

                break;

            case RISE_B:

                if(deltaA <= 0){
                    if (historyReading[99][0] >= THRES_B[2]) {
                        signature = mySig.SIG_B;
                    }
                    state = myState.DETERMINED;
                }

                break;

            case DETERMINED:

                //Log.d("FSM: ", "State DETERMINED " + signature.toString());

                break;

            default:
                state = myState.WAIT;
                break;

        }

        sampleCounterx--;

    }

    public AccelerometerEventListener(TextView outputView, GameLoopTask myGL) {
        instanceOutput = outputView;
        this.myGL = myGL;
    }

    public float[][] getHistoryReading(){
        return historyReading;
    }

    public void onAccuracyChanged(Sensor s, int i) { }

    public void onSensorChanged(SensorEvent se) {

        if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            insertHistoryReading(se.values);

            /*instanceOutput.setText("The Accelerometer Reading is: \n"
                    + String.format("(%.2f, %.2f, %.2f)", se.values[0], se.values[1], se.values[2]) + "\n");*/

            //graph.addPoint(se.values);

        }
    }

}


