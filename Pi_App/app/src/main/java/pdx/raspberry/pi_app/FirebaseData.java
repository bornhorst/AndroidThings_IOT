/**
 * FireBaseData.java
 *
 * Name: Andrew Capatina / Ryan Bornhorst
 *
 * Description:
 *      This file contains a class for storing data
 *      read from Firebase on the android app. Every
 *      member created in firebase is in this class.
 *      The class can initialize and set the members
 *      it contains. Google resources for reading/writing
 *      firebase were used for the class.
 *
 *
 */

package pdx.raspberry.pi_app;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/** Class
 *
 *  Description:
 *      This class has all the firebase members being
 *      written to/read from. Only one instance is used in
 *      the android app for keeping track of the data and
 *      keeping the most up to date results.
 *
 */
@IgnoreExtraProperties
public class FirebaseData {

    public int ADA5IN;
    public int ADC3IN;
    public int ADC4IN;
    public int ADC5IN;
    public int DAC1OUT;
    public int PWM3;
    public int PWM4;
    public int PWM5;
    public int PWM6;
    public String TIMESTAMP;
    public Map<String, Boolean> temp = new HashMap<>();

    public FirebaseData() {
        // Default constructor required for calls to DataSnapshot.getValue(FirebaseData.class)
    }

    public FirebaseData(int ADA5IN, int ADC3IN , int ADC4IN, int ADC5IN, int DAC1OUT, int PWM3, int PWM4, int PWM5, int PWM6, String TIMESTAMP) {
        this.ADA5IN = ADA5IN;
        this.ADC3IN = ADC3IN;
        this.ADC4IN = ADC4IN;
        this.ADC5IN = ADC5IN;
        this.DAC1OUT = DAC1OUT;
        this.PWM3 = PWM3;
        this.PWM4 = PWM4;
        this.PWM5 = PWM5;
        this.PWM6 = PWM6;
        this.TIMESTAMP = TIMESTAMP;
    }

    @Exclude
    public Map<String, Object> toMap() {    // Creates appropriate object for updating Firebase.
        HashMap<String, Object> result = new HashMap<>();
        result.put("ADA5IN", ADA5IN);
        result.put("ADC3IN", ADC3IN);
        result.put("ADC4IN", ADC4IN);
        result.put("ADC5IN", ADC5IN);
        result.put("DAC1OUT", DAC1OUT);
        result.put("PWM3", PWM3);
        result.put("PWM4", PWM4);
        result.put("PWM5", PWM5);
        result.put("PWM6", PWM6);
        result.put("TIMESTAMP", TIMESTAMP);

        return result;
    }
}