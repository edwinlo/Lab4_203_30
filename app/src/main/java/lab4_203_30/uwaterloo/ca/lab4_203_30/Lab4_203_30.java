/*******************************************************
 * Lab3_203_30.java             Feb 18th 2017
 *
 * Group # 30
 *
 * Lab Members: Edwin Lo, Madeleine Wang, Yizhou Yang
 *
 * This lab modifies the previous lab so that a GameBlock object can be
 * seen sliding across the gameboard in the direction that the phone
 * has been moved. This is done by implementing a Timer Task with a
 * specified period that runs the move() method every 30ms in such a
 * way so that it seems like there is an animation of the GameBlock
 * sliding.
 *
 * -Also implements two FSMs in order to determine the direction of the
 * gesture
 *******************************************************/

package lab4_203_30.uwaterloo.ca.lab4_203_30;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Timer;

public class Lab4_203_30 extends AppCompatActivity {

    //LG G4 resolution is 1440x2560, so we will use 1440 because we want a square,
    //but my phone is 720x1280, so change this value later to fit 3/4 of the width of your screen
    final int GAMEBOARD_DIMENSION = 600;

    public static Context context;

    public static Timer myGameLoop;
    public static GameLoopTask myGameLoopTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab4_203_30);

        context = getApplicationContext();

        //Creating the linear layout
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layout);


        //instantiating a GameLoopTask
        myGameLoopTask = new GameLoopTask(this, layout, context);


        //instantiating a timer with a period of 30ms
        myGameLoop = new Timer();
        myGameLoop.schedule(myGameLoopTask, 20, 20);


        //setting the dimensions of the gameBoard to fit the screen
        layout.getLayoutParams().width = GAMEBOARD_DIMENSION;
        layout.getLayoutParams().height = GAMEBOARD_DIMENSION;
        layout.setBackgroundResource(R.drawable.gameboard);


        //initializing the TextView
        TextView tv1 = new TextView(getApplicationContext());
        tv1.setTextColor(Color.GREEN);
        tv1.setTextSize(42);
        //tv1.setGravity(Gravity.CENTER);


        //instantiating the sensorManager service
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);


        //Accelerometer Objects / Event Listeners Instantiation
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        final AccelerometerEventListener accelEventListener = new AccelerometerEventListener(tv1, myGameLoopTask);
        sensorManager.registerListener(accelEventListener, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        layout.addView(tv1);

    }
}
