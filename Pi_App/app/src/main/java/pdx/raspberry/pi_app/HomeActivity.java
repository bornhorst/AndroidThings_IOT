/** HomeActivity
 *
 *  Description:
 *      This is the main activity used for the Pi application of project 3.
 *      Activity uses I2C to interface with a PIC16 micro controller.
 *      Pi uses the ADC, PWM, and DAC peripherals of PIC MCU. ADCs read
 *      PWM output given to RGB leds and the output of the temperature sensor.
 *      This activity uses the same XML layout for the phone application
 *      created along with this project. Only difference is all UI objects
 *      are textviews. This file will update the values given by ADCs to Firebase.
 *      Will also use database values for driving hardware.
 *
 *
 */

package pdx.raspberry.pi_app;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Skeleton of an Android Things activity.
 * <p>
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * PeripheralManagerService service = new PeripheralManagerService();
 * mLedGpio = service.openGpio("BCM6");
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
 * mLedGpio.setValue(true);
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 */
public class HomeActivity extends AppCompatActivity {

    private static final int I2C_SLAVE_ADDR = 0x08;     // Device Addresses.
    private static final int PWM3_ADDRESS = 0x00;
    private static final int PWM4_ADDRESS = 0x01;
    private static final int PWM5_ADDRESS = 0x02;
    private static final int PWM6_ADDRESS = 0x03;
    private static final int DAC1_ADDRESS = 0x04;
    private static final int ADA5_ADDRESS = 0x05;
    private static final int ADC3_ADDRESS = 0x07;
    private static final int ADC4_ADDRESS = 0x09;
    private static final int ADC5_ADDRESS = 0x0b;

    static int mLockFlag = 0;   // Initialize shared resource flag.



    private static final String TAG = "HomeActivity";

    private Handler handler = new Handler();    // Instantiate handler for thread.

    private DatabaseReference mDatabase = null; // Database reference object.
    private FirebaseData data;                  // Firebase data set in listener for database.
    FirebaseData dataCpy = null;                // Contains copy of firebase data.

    private TextView mTempTxtVw;          // Temperature text views.
    private TextView mADC3TxtVw;          // ADC text views.
    private TextView mADC4TxtVw;
    private TextView mADC5TxtVw;
    private TextView mDAC1TxtVw;
    private TextView mPWM3TxtVw;
    private TextView mPWM4TxtVw;
    private TextView mPWM5TxtVw;
    private TextView mPWM6TxtVw;

    RaspPi raspPi = new RaspPi();       // Instantiate the I2C interface class.
    /**
     *  Initializes all UI elements and thread for driving hardware.
     *
     * @param savedInstanceState State of application.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);             // Set layout on screen.

        mTempTxtVw = findViewById(R.id.textView1);          // Temperature text views.
        mADC3TxtVw = findViewById(R.id.textView5);          // ADC text views.
        mADC4TxtVw = findViewById(R.id.textView6);
        mADC5TxtVw = findViewById(R.id.textView7);
        mDAC1TxtVw = findViewById(R.id.textView9);
        mPWM3TxtVw = findViewById(R.id.textView15);         // PWM progress bar.
        mPWM4TxtVw = findViewById(R.id.textView16);
        mPWM5TxtVw = findViewById(R.id.textView17);
        mPWM6TxtVw = findViewById(R.id.textView18);

        mTempTxtVw.setText("0");    // Init for text fields.
        mADC3TxtVw.setText("0");
        mADC4TxtVw.setText("0");
        mADC5TxtVw.setText("0");
        mDAC1TxtVw.setText("0");
        mPWM3TxtVw.setText("0");
        mPWM4TxtVw.setText("0");
        mPWM5TxtVw.setText("0");
        mPWM6TxtVw.setText("0");

        data = new FirebaseData(0,0,0,0,0,0,0,0,0,""); // Initialize data class. Has most recent data once updated.

        mDatabase = FirebaseDatabase.getInstance().getReference();      // Get reference to database.

        // Adding a listener for detecting new data on server.
        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Quick to make lock. Not the best idea probably because of context where this flag is being set.
                if(mLockFlag == 0) {    // Be sure to set and reset lock on data.
                    mLockFlag = 1;
                    data = dataSnapshot.getValue(FirebaseData.class);   // Grab the latest data.
                    mLockFlag = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        });

        raspPi.listI2C();
        raspPi.setupI2C("I2C1", I2C_SLAVE_ADDR);    // I2C init.

       handler.post(blinkRunnable); // Start the thread.

    }

    /**
     * Runnable Thread
     *
     * Description:
     *  This thread interacts with the hardware and updates firebase with
     *  the results.
     *  Thread reads the following: ADA5, ADC3, ADC4, ADC5
     *  Thread writes to following: DAC1, PWM3, PWM4, PWM5, PWM6.
     *
     *  Thread runs every 2000 milliseconds and uses a flag
     *  to ensure "data" variable is safe to use.
     *
     */
    private Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Thread Running. ");
            handler.postDelayed(blinkRunnable, 2000);
            if (mLockFlag == 0) {   // Check if the flag is not set for updating the data for hardware.
                mLockFlag = 1;
                dataCpy = data;
                mLockFlag = 0;  // Reset the lock.
            }
            if (dataCpy != null) {
                try {
                    dataCpy.ADA5IN = raspPi.readRegWordI2C(ADA5_ADDRESS);  // Read temp ADC.
                    Log.d(TAG, "TEMP: " + dataCpy.ADA5IN);
                    if(dataCpy.ADA5IN > 1023)
                    {
                        dataCpy.ADA5IN = 1023; // Limiting max to 10 bits.
                    }
                    else if(dataCpy.ADA5IN > 0) {
                        // https://www.microchip.com/forums/m589143.aspx        ~helpful link
                       //dataCpy.ADA5IN = (int) ((dataCpy.ADA5IN * ((1.98) / 1024)) * 100);    // Convert to temperature. Converts to Fahrenheit.
                       //dataCpy.ADA5IN = (int) (((dataCpy.ADA5IN-32)*5)/9);   // Convert to celsius.
                        dataCpy.ADA5IN = (data.ADA5IN-500)/10;
                    }
                    else {
                        dataCpy.ADA5IN = 0;
                    }


                    dataCpy.ADC5IN = raspPi.readRegWordI2C(ADC5_ADDRESS) & 0x000003FF; // Read all PWM ADCs.
                    dataCpy.ADC4IN = raspPi.readRegWordI2C(ADC4_ADDRESS) & 0x000003FF;
                    dataCpy.ADC3IN = raspPi.readRegWordI2C(ADC3_ADDRESS) & 0x000003FF;


                } catch (IOException e){
                    Log.d(TAG, "LOG: Read I2C failed. ");
                }
                try {
                    if(dataCpy.ADA5IN > 15 && dataCpy.ADA5IN < 18)    // Setting duty cycle based on temp in Celsius.
                    {
                        dataCpy.PWM3 = (int) (1023*.30);    // Set 30% duty cycle.
                    }
                    else if(dataCpy.ADA5IN > 18 && dataCpy.ADA5IN < 22)
                    {
                        dataCpy.PWM3 = (int) (1023*.50);    // Set 50% duty cycle.
                    } else if(dataCpy.ADA5IN > 22 && dataCpy.ADA5IN < 25)
                    {
                        dataCpy.PWM3 = (int) (1023*.70);    // Set 70% duty cycle.
                    }
                    else if(dataCpy.ADA5IN < 15)
                    {
                        dataCpy.PWM3 = (int) (1023*.80);    // Set 80% duty cycle.
                    } else {
                        dataCpy.PWM3 = (int) (1023*.40);    // Set 40% duty cycle.
                    }
                    raspPi.writeRegWordI2C(PWM3_ADDRESS, (short)(dataCpy.PWM3 & 0x0000FFFF));  // Update motor PWM output.
                } catch (IOException e) {
                    Log.d(TAG, "LOG: PWM3 Write failed. ");
                }
                try {
                    short to_write = (short) (dataCpy.PWM4 & 0x0000FFFF);
                    raspPi.writeRegWordI2C(PWM4_ADDRESS, to_write);     // Update RGB PWM channel.
                } catch (IOException e) {
                    Log.d(TAG, "LOG: PWM4 Write failed. ");
                }
                try {
                    short to_write = (short) (dataCpy.PWM5 & 0x0000FFFF);
                    raspPi.writeRegWordI2C(PWM5_ADDRESS, to_write);     // Update RGB PWM channel.
                } catch (IOException e) {
                    Log.d(TAG, "LOG: PWM5 Write failed. ");
                }
/*
    Had to comment out this portion of the code, because a failure was being given when writing to
    the registers. It's odd that this occurs, because the remaining peripherals work.


                try {
                    short to_write = (short) (dataCpy.PWM6 & 0x0000FFFF);
                    raspPi.writeRegWordI2C(PWM6_ADDRESS, to_write);     // Update RGB PWM channel.
                } catch (IOException e) {
                    Log.d(TAG, "LOG: PWM6 Write failed. ");
                }

                try {
                    byte[] to_write = intToByteArray((dataCpy.DAC1OUT & 0x0000001F));
                    raspPi.writeOneByteI2C(DAC1_ADDRESS, to_write[0]);  // Write to DAC for amplifying PWM output.
                } catch (IOException e) {
                    Log.d(TAG, "LOG: DAC Write failed. ");
                }
                */

                Log.d(TAG, dataCpy.DAC1OUT + " " + dataCpy.PWM3 + " " + dataCpy.PWM4 + " " + dataCpy.PWM5 + " " + dataCpy.PWM6);
                updateFirebase();   // Update Firebase

                mTempTxtVw.setText(String.format(Integer.toString(dataCpy.ADA5IN)));
                mADC3TxtVw.setText(String.format(Integer.toString(dataCpy.ADC3IN)));
                mADC4TxtVw.setText(String.format(Integer.toString(dataCpy.ADC4IN)));
                mADC5TxtVw.setText(String.format(Integer.toString(dataCpy.ADC5IN)));
                mDAC1TxtVw.setText(String.format(Integer.toString(dataCpy.DAC1OUT)));
                mPWM3TxtVw.setText(String.format(Integer.toString(dataCpy.PWM3)));
                mPWM4TxtVw.setText(String.format(Integer.toString(dataCpy.PWM4)));
                mPWM5TxtVw.setText(String.format(Integer.toString(dataCpy.PWM5)));
                mPWM6TxtVw.setText(String.format(Integer.toString(dataCpy.PWM6)));
            }

        }

    };

    /**
     *  Description:
     *      This function returns a byte array with 2 elements at maximum.
     *  https://stackoverflow.com/questions/6374915/java-convert-int-to-byte-array-of-4-bytes
     *  Slightly modified suggestion in link.
     *
     * @param value to convert
     * @return  byte array type return
     */
    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value),
                (byte) (value >> 8),
        };
    }

    /**
     *  Description:
     *  Method to update Firebase.
     *
     *
     */
    private void updateFirebase() {

        if(dataCpy != null) {
            // Get current time: https://stackoverflow.com/questions/36301543/get-todays-date-and-time-as-string#36301666
            data.TIMESTAMP = DateFormat.getDateTimeInstance().format(new Date());
            Map<String, Object> dbaseValues = data.toMap();  // Create map object.
            // Update Firebase.
            mDatabase.updateChildren(dbaseValues);
        }
    }

    /**
     *  Cleanup function for class.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        raspPi.cleanupI2C();   // Close I2C.
    }

}
