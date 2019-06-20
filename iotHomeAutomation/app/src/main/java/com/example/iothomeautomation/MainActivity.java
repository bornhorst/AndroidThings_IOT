/**
 *  MainActivity.java
 *
 *  Name: Andrew Capatina / Ryan Bornhorst
 *  Date: 5/25/2019
 *
 *  Description:
 *      Main Activity for project 3 558 Android program.
 *      This activity is specifically used for android phones.
 *      Activity will read Firebase and update Firebase data
 *      based on user selection using UI. User can modify
 *      4 PWM channels and DAC output.
 *
 */

package com.example.iothomeautomation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/** Class
 *
 *  Description:s
 *      Contains functions to update the UI for project 3
 *      of 558. Updates 5 text views, 1 progress bar,
 *      3 seek bars, and 2 buttons. Some of the objects
 *      are updated using data from Firebase.
 *
 */
public class MainActivity extends AppCompatActivity {

    private static final int DAC_MIN = 0;           // min/max user input for DAC.
    private static final int DAC_MAX = 31;
    //private TextView mTextMessage;
    private DatabaseReference mDatabase = null;

    /* Initializing TextViews for displaying peripheral data */
    private TextView    mTempTxtVw = null;  // Temperature object.
    private TextView    mADC3TxtVw = null;  // ADC objects.
    private TextView    mADC4TxtVw = null;
    private TextView    mADC5TxtVw = null;
    private TextView    mDAC1TxtVw = null;
    private ProgressBar mPWM3ProgressBar = null;    // PWM objects.
    private SeekBar     mPWM4SeekBar = null;
    private SeekBar     mPWM5SeekBar = null;
    private SeekBar     mPWM6SeekBar = null;

    private Button      mMinusButton = null;        // Addition and subtract buttons.
    private Button      mPlusButton = null;

    private FirebaseData data;              // Holds current Firebase data.

    private int mDacCount = 0;      // DAC count, set by using buttons.

    /** Method
     *
     *  Description:
     *      Listener method that updates UI based on selection of bottom bar.
     *
     */
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:      // Doesn't do anything for this project.
                    return true;
                case R.id.navigation_dashboard:
                   // mTextMessage.setText(R.string.title_dashboard);   // example usage.
                    return true;
                case R.id.navigation_notifications:
                   // mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    /**
     *  Description:
     *      Function to initialize listeners and
     *      UI objects like textview, seekbar, and button.
     *      Firebase will be updated once user makes selection
     *      using elements initialized in this function.
     *
     * @param savedInstanceState State of application.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_gadgets);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        /* Get resource IDs of all TextViews for project. */

        mTempTxtVw = findViewById(R.id.textView1);          // Temperature text views.
        mADC3TxtVw = findViewById(R.id.textView5);          // ADC text views.
        mADC4TxtVw = findViewById(R.id.textView6);
        mADC5TxtVw = findViewById(R.id.textView7);
        mDAC1TxtVw = findViewById(R.id.textView9);
        mPWM3ProgressBar = findViewById(R.id.progressBar);  // PWM progress bar.
        mPWM4SeekBar = findViewById(R.id.seekBar);
        mPWM5SeekBar = findViewById(R.id.seekBar2);
        mPWM6SeekBar = findViewById(R.id.seekBar3);

        mMinusButton = findViewById(R.id.button);           // Buttons for DAC.
        mPlusButton = findViewById(R.id.button2);

        mDatabase = FirebaseDatabase.getInstance().getReference();         // Creating fire base object

        // Creating a firebase listener and updating the data class.
        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data = dataSnapshot.getValue(FirebaseData.class);

                if(data != null) {
                    mTempTxtVw.setText(String.format(Integer.toString(data.ADA5IN)));   // Updating Analog Digital text views.
                    mADC3TxtVw.setText(String.format(Integer.toString(data.ADC3IN)));
                    mADC4TxtVw.setText(String.format(Integer.toString(data.ADC4IN)));
                    mADC5TxtVw.setText(String.format(Integer.toString(data.ADC5IN)));

                    mPWM3ProgressBar.setProgress(data.PWM3);    // Set the progress bar.

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        });


        // Listener for minus button.
        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update user selection.
                mDacCount -= 1;
                if (mDacCount < DAC_MIN) {
                    mDacCount = DAC_MIN;
                }
                // Update firebase and text view.
                updateFirebase();
                mDAC1TxtVw.setText(Integer.toString(mDacCount));
            }
        });

        // Listener for plus button.
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update user selection.
                mDacCount += 1;
                if (mDacCount > DAC_MAX) {
                    mDacCount = DAC_MAX;
                }
                data.DAC1OUT = mDacCount;
                // Update firebase and text view.
                updateFirebase();
                mDAC1TxtVw.setText(Integer.toString(mDacCount));
            }
        });

        // Seekbar listener.
        mPWM5SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /**
             *  Description:
             *      Update Firebase when the user is done making their selection.
             *
             * @param seekBar   Object holding integer for user selection.
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(data != null) {
                    data.PWM5 = seekBar.getProgress();
                }
                updateFirebase();

            }
        });

        // Seekbar listener.
        mPWM4SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /**
             *  Description:
             *      Update Firebase when the user is done making their selection.
             *
             * @param seekBar   Object holding integer for user selection.
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(data != null) {
                    data.PWM4 = seekBar.getProgress();
                }
                updateFirebase();


            }
        });

        // Seekbar listener.
        mPWM6SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /**
             *  Description:
             *      Update Firebase when the user is done making their selection.
             *
             * @param seekBar   Object holding integer for user selection.
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(data != null) {
                    data.PWM6 = seekBar.getProgress();
                }
                updateFirebase();

            }
        });

    }

    /**
     *  Description:
     *      Method that updates firebase with
     *      user selections.
     *
     */
    private void updateFirebase() {

        if(data != null) {
            // Get current time: https://stackoverflow.com/questions/36301543/get-todays-date-and-time-as-string#36301666
            data.TIMESTAMP = DateFormat.getDateTimeInstance().format(new Date());
            Map<String, Object> dbaseValues = data.toMap();  // Create map object containing most up to date user selections.
            // Update Firebase.
            mDatabase.updateChildren(dbaseValues);

        }
    }

}

