package JISA.Devices;

import JISA.Addresses.Address;
import JISA.Enums.Thermocouple;
import JISA.Util;
import JISA.VISA.NativeDevice;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class USBTC08 extends NativeDevice<USBTC08.NativeInterface> implements MSTMeter {

    public static final String                         LIBRARY_NAME     = "usbtc08";
    public static final Class<USBTC08.NativeInterface> LIBRARY_CLASS    = USBTC08.NativeInterface.class;
    public static final int                            SENSORS_PER_UNIT = 9;

    private static final short ERROR_OK                     = 0;
    private static final short ERROR_OS_NOT_SUPPORTED       = 1;
    private static final short ERROR_NO_CHANNELS_SET        = 2;
    private static final short ERROR_INVALID_PARAMETER      = 3;
    private static final short ERROR_VARIANT_NOT_SUPPORTED  = 4;
    private static final short ERROR_INCORRECT_MODE         = 5;
    private static final short ERROR_ENUMERATION_INCOMPLETE = 6;
    private static final short UNITS_KELVIN                 = 2;

    private static USBTC08.NativeInterface INSTANCE;

    static {

        try {
            INSTANCE = Native.loadLibrary(LIBRARY_NAME, LIBRARY_CLASS);
        } catch (Throwable e) {
            INSTANCE = null;
        }

    }

    private final short          handle;
    private final Thermocouple[] types = {
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE,
            Thermocouple.NONE
    };

    private float[] lastValues = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private long     lastTime   = 0;

    public static List<USBTC08> find() {

        List<USBTC08> devices = new LinkedList<>();

        while (true) {

            try {
                devices.add(new USBTC08());
            } catch (Throwable e) {
                break;
            }

        }

        return devices;

    }

    public USBTC08() throws IOException, DeviceException {

        // Load native library
        super(LIBRARY_NAME, LIBRARY_CLASS, INSTANCE);

        if (INSTANCE == null) {
            throw new IOException("Error loading usbtc08 library!");
        }

        short handle = lib.usb_tc08_open_unit();

        if (handle > 0) {
            this.handle = handle;
        } else if (handle == 0) {
            throw new IOException("No USB TC-08 unit found!");
        } else {
            throw new DeviceException(getLastError((short) 0));
        }

    }

    @Override
    public double getTemperature(int sensor) throws DeviceException {

        checkSensor(sensor);

        if ((System.currentTimeMillis() - lastTime) < lib.usb_tc08_get_minimum_interval_ms(handle)) {
            return (double) lastValues[sensor];
        }

        // Need a pointer to some memory to store our returned values
        Memory tempPointer = new Memory(9 * Native.getNativeSize(Float.TYPE));

        int result = lib.usb_tc08_get_single(handle, tempPointer, new ShortByReference((short) 0), UNITS_KELVIN);

        // If zero, then something's gone wrong.
        if (result == 0) {
            throw new DeviceException(getLastError(handle));
        }

        lastValues = tempPointer.getFloatArray(0, SENSORS_PER_UNIT);

        lastTime   = System.currentTimeMillis();

        return (double) lastValues[sensor];

    }

    @Override
    public int getNumSensors() {
        return SENSORS_PER_UNIT;
    }

    @Override
    public double[] getTemperatures() throws DeviceException {

        double[] temperatures = new double[getNumSensors()];

        // Pointer to memory reserved for returned temperatures
        Memory tempPointer = new Memory(9 * Native.getNativeSize(Float.TYPE));

        int result = lib.usb_tc08_get_single(handle, tempPointer, new ShortByReference((short) 0), (short) 2);

        // If it returned zero, something's gone wrong
        if (result == 0) {
            throw new DeviceException(getLastError(handle));
        }

        float[] values = tempPointer.getFloatArray(0, SENSORS_PER_UNIT);

        // Convert to doubles
        for (int i = 0; i < values.length; i++) {
            temperatures[i] = (double) values[i];
        }

        return temperatures;

    }

    /**
     * Configures the sensor on the TC-08, specifying which type of thermocouple is installed.
     *
     * @param sensor Sensor number
     * @param type   Thermocouple type
     *
     * @throws DeviceException Upon device error
     */
    public void setSensorType(int sensor, Thermocouple type) throws DeviceException {

        checkSensor(sensor);

        int result = lib.usb_tc08_set_channel(handle, (short) sensor, type.getCode());

        if (result == 0) {
            throw new DeviceException(getLastError(handle));
        } else {
            types[sensor] = type;
        }

    }

    public Thermocouple getSensorType(int sensor) throws DeviceException {

        checkSensor(sensor);
        return types[sensor];

    }

    /**
     * Sets the line-frequency rejection.
     *
     * @param frequency 50 Hz or 60 Hz
     *
     * @throws DeviceException Upon device error
     */
    public void setLineFrequency(Frequency frequency) throws DeviceException {


        int result = lib.usb_tc08_set_mains(handle, (short) frequency.ordinal());

        if (result == 0) {
            throw new DeviceException(getLastError(handle));
        }


    }

    @Override
    public String getIDN() {
        return "PICO TC-08";
    }

    @Override
    public void close() throws DeviceException {

        int result = lib.usb_tc08_close_unit(handle);

        if (result == 0) {
            throw new DeviceException(getLastError(handle));
        }

    }

    @Override
    public Address getAddress() {
        return null;
    }

    private void checkSensor(int sensor) throws DeviceException {

        if (!Util.isBetween(sensor, 0, getNumSensors() - 1)) {
            throw new DeviceException("Sensor number %d does not exist", sensor);
        }

    }

    private String getLastError(short handle) {

        int error = lib.usb_tc08_get_last_error(handle);

        switch (error) {

            case ERROR_OK:
                return "None";

            case ERROR_OS_NOT_SUPPORTED:
                return "OS not supported";

            case ERROR_NO_CHANNELS_SET:
                return "No channels set";

            case ERROR_INVALID_PARAMETER:
                return "Invalid parameter";

            case ERROR_VARIANT_NOT_SUPPORTED:
                return "Variant not supported";

            case ERROR_INCORRECT_MODE:
                return "Incorrect mode";

            case ERROR_ENUMERATION_INCOMPLETE:
                return "Enumeration incomplete";

            default:
                return "Unknown error";

        }

    }

    /**
     * Enumeration of line-frequency rejection modes.
     */
    public enum Frequency {
        FIFTY_HERTZ,
        SIXTY_HERTZ
    }

    protected interface NativeInterface extends Library {


        // Method definitions from usbtc08.h C header file
        // ===============================================

        // C prototype definition :
        // int16_t (usb_tc08_open_unit) (void);
        short usb_tc08_open_unit();

        // C prototype definition :
        // int16_t (usb_tc08_close_unit) (int16_t handle);
        short usb_tc08_close_unit(short handle);

        // C prototype definition :
        // int16_t (usb_tc08_set_mains) (int16_t handle, int16_t sixty_hertz);
        short usb_tc08_set_mains(short handle, short sixty_hertz);

        // C prototype definition :
        // int16_t (usb_tc08_set_channel) (int16_t handle, int16_t channel, int8_t  tc_type);
        short usb_tc08_set_channel(short handle, short channel, byte tc_type);

        // C prototype definition :
        // int32_t (usb_tc08_get_minimum_interval_ms) (int16_t handle);
        int usb_tc08_get_minimum_interval_ms(short handle);

        // C prototype definition :
        // int16_t (usb_tc08_get_formatted_info) (int16_t  handle, int8_t  *unit_info, int16_t  string_length);
        short usb_tc08_get_formatted_info(short handle, byte[] unitInfo, short stringLength);

        // C prototype definition :
        // int16_t (usb_tc08_get_single) (int16_t handle, float * temp, int16_t * overflow_flags, int16_t units);
        short usb_tc08_get_single(short handle, Memory temp, ShortByReference overflowFlags, short units);

        short usb_tc08_get_last_error(short handle);

        // Constants
        // =========
        int USBTC08_MAX_CHANNELS = 8;

        byte USB_TC08_THERMOCOUPLE_TYPE_B = (byte) 'B';
        byte USB_TC08_THERMOCOUPLE_TYPE_E = (byte) 'E';
        byte USB_TC08_THERMOCOUPLE_TYPE_J = (byte) 'J';
        byte USB_TC08_THERMOCOUPLE_TYPE_K = (byte) 'K';
        byte USB_TC08_THERMOCOUPLE_TYPE_N = (byte) 'N';
        byte USB_TC08_THERMOCOUPLE_TYPE_R = (byte) 'R';
        byte USB_TC08_THERMOCOUPLE_TYPE_S = (byte) 'S';
        byte USB_TC08_THERMOCOUPLE_TYPE_T = (byte) 'T';
        byte USB_TC08_VOLTAGE_READINGS    = (byte) 'X';
        byte USB_TC08_DISABLE_CHANNEL     = (byte) ' ';


        // Enumerations
        // ============

        enum USBTC08Channels {
            USBTC08_CHANNEL_CJC,
            USBTC08_CHANNEL_1,
            USBTC08_CHANNEL_2,
            USBTC08_CHANNEL_3,
            USBTC08_CHANNEL_4,
            USBTC08_CHANNEL_5,
            USBTC08_CHANNEL_6,
            USBTC08_CHANNEL_7,
            USBTC08_CHANNEL_8;
        }

        enum USBTC08Units {
            USBTC08_UNITS_CENTIGRADE,
            USBTC08_UNITS_FAHRENHEIT,
            USBTC08_UNITS_KELVIN,
            USBTC08_UNITS_RANKINE;
        }

        enum USBTC08MainsFrequency {
            USBTC08_FIFTY_HERTZ,
            USBTC08_SIXTY_HERTZ;
        }
    }

}
