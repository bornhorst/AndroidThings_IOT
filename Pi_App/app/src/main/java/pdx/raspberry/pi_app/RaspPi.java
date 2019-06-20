/**
 * Andrew Capatina / Ryan Bornhorst
 * 5/25/2019
 *
 * Description: This file contains the methods needed to communicate
 *  with the I2C peripheral. Class serves as an abstraction to I2C for
 *  Android App running on Pi.
 *
 */

package pdx.raspberry.pi_app;

import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.List;

public class RaspPi {

    private static final String TAG = "RaspberryPi";

    private PeripheralManager manager;      // Pi peripheral manager declaration.
    private I2cDevice I2C1;                 // I2C Interface declaration.

    // constructor
    public RaspPi()
    {
        manager = PeripheralManager.getInstance();  // Initialize peripheral manager.
        //Log.d(TAG, "Available GPIO: " + manager.getGpioList());
    }

    /**
     * Lists various I2C slaves on the bus.
     *
     */
    public void listI2C()
    {
        List<String> deviceList = manager.getI2cBusList();
        if(deviceList.isEmpty()) {
            Log.i(TAG, "No I2C bus availabile on device");
        } else {
            Log.i(TAG, "DEVICE LIST: " + deviceList);
        }
    }

    /**
     *  Description:
     *      This file is the initialization function
     *      for I2C interface.
     *
     * @param name  Name of I2C device as String.
     * @param address   Address of device as integer.
     */
    public void setupI2C(String name, int address)
    {
        try {
            I2C1 = this.manager.openI2cDevice(name, address);
        } catch (IOException e) {
            Log.w(TAG, "Can't access I2C", e);
        }
    }

    /**
     * Description:
     *      Function to deallocate all I2C resources.
     *
     */
    public void cleanupI2C()
    {
        if(I2C1 != null)
        {
            try {
                I2C1.close();
                I2C1 = null;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close I2C device", e);
            }
        }
    }

    /**
     *  Description:
     *
     *
     * @param address register address.
     * @return error code (-1) or data
     */
    public int readI2Cbyte(int address)  {
        try {
            byte value = this.I2C1.readRegByte(address);
            int data = value & 0xFF;
            Log.i(TAG, "Byte Read: " + data);
            return data;
        } catch (IOException e) {
            Log.e(TAG, "IO Exception Read",e);
        }
        return -1;
    }

    /**
     *  Description:
     *      Writes 16 bit value to register designated.
     *
     * @param reg   Address of register
     * @param data  Data for register
     * @throws IOException I2C exception
     */
    public void writeRegWordI2C(int reg, short data) throws IOException {
        this.I2C1.writeRegWord(reg, data);
    }

    /**
     *  Description:
     *      Reads a word.
     *
     * @param address  Address to read from.
     * @return  Returns a 16 bit word.
     * @throws IOException I2C exception.
     */
    public int readRegWordI2C(int address) throws IOException {
        return this.I2C1.readRegWord(address);
    }

}
