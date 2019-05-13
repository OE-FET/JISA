package JISA.Devices;

import JISA.Addresses.Address;
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

    private final Short[]  handles;
    private final TCType[] types;

    public static USBTC08[] search() {

        try {
            USBTC08 attempt = new USBTC08();
            return new USBTC08[]{attempt};
        } catch (Exception e) {
            return new USBTC08[0];
        }

    }

    public USBTC08() throws IOException {
        this(null);
    }

    public USBTC08(Address address) throws IOException {

        // Load native library
        super(LIBRARY_NAME, LIBRARY_CLASS);

        List<Short> units = new LinkedList<>();

        short found;

        // Look for all USB-TC-08 units connected
        while ((found = lib.usb_tc08_open_unit()) > 0) {
            units.add(found);
        }

        // If none are found, throw exception
        if (units.isEmpty()) {
            throw new IOException("No USB TC-08 units found!");
        }

        handles = units.toArray(new Short[0]);
        types = new TCType[getNumSensors()];

        for (int i = 0; i < types.length; i++) {
            types[i] = TCType.NONE;
        }

    }

    @Override
    public double getTemperature(int sensor) throws DeviceException {

        checkSensor(sensor);

        // Figure out which units we're talking about
        short handle = handles[sensor / SENSORS_PER_UNIT];
        int   sense  = sensor % SENSORS_PER_UNIT;

        // Need a pointer to some memory to store our returned values
        Memory tempPointer = new Memory(9 * Native.getNativeSize(Float.TYPE));

        int result = lib.usb_tc08_get_single(handle, tempPointer, new ShortByReference((short) 0), (short) 2);

        // If zero, then something's gone wrong.
        if (result == 0) {
            throw new DeviceException(getLastError(handle));
        }

        float[] values = tempPointer.getFloatArray(0, SENSORS_PER_UNIT);

        return (double) values[sense];

    }

    @Override
    public int getNumSensors() {

        return handles.length * SENSORS_PER_UNIT;

    }

    @Override
    public double[] getTemperatures() throws DeviceException {

        double[] temperatures = new double[getNumSensors()];

        int i = 0;

        // Loop over all units
        for (Short handle : handles) {

            // Pointer to memory reserved for returned temperatures
            Memory tempPointer = new Memory(9 * Native.getNativeSize(Float.TYPE));

            int result = lib.usb_tc08_get_single(handle, tempPointer, new ShortByReference((short) 0), (short) 2);

            // If it returned zero, something's gone wrong
            if (result == 0) {
                throw new DeviceException(getLastError(handle));
            }

            float[] values = tempPointer.getFloatArray(0, SENSORS_PER_UNIT);

            // Add to over-all array of doubles
            for (float v : values) {
                temperatures[i] = (double) v;
                i++;
            }

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
    public void setSensorType(int sensor, TCType type) throws DeviceException {

        checkSensor(sensor);

        int result = lib.usb_tc08_set_channel(handles[sensor / SENSORS_PER_UNIT], (short) (sensor % SENSORS_PER_UNIT), type.getCode());

        if (result == 0) {
            throw new DeviceException(getLastError(handles[sensor / SENSORS_PER_UNIT]));
        } else {
            types[sensor] = type;
        }

    }

    public TCType getSensorType(int sensor) throws DeviceException {

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

        for (Short handle : handles) {

            int result = lib.usb_tc08_set_mains(handle, (short) frequency.ordinal());

            if (result == 0) {
                throw new DeviceException(getLastError(handle));
            }

        }

    }

    @Override
    public String getIDN() {
        return "PICO TC-08";
    }

    @Override
    public void close() throws DeviceException {

        for (Short handle : handles) {

            int result = lib.usb_tc08_close_unit(handle);

            if (result == 0) {
                throw new DeviceException(getLastError(handle));
            }

        }

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
     * Enumeration of TC-08 thermocouple types
     */
    public enum TCType {

        NONE((char) 0),
        B('B'),
        E('E'),
        J('J'),
        K('K'),
        N('N'),
        R('R'),
        S('S'),
        T('T'),
        X('X');

        private final byte code;

        TCType(char code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
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
        public static final int USBTC08_MAX_CHANNELS = 8;

        public static final byte USB_TC08_THERMOCOUPLE_TYPE_B = (byte) 'B';
        public static final byte USB_TC08_THERMOCOUPLE_TYPE_E = (byte) 'E';
        public static final byte USB_TC08_THERMOCOUPLE_TYPE_J = (byte) 'J';
        public static final byte USB_TC08_THERMOCOUPLE_TYPE_K = (byte) 'K';
        public static final byte USB_TC08_THERMOCOUPLE_TYPE_N = (byte) 'N';
        public static final byte USB_TC08_THERMOCOUPLE_TYPE_R = (byte) 'R';
        public static final byte USB_TC08_THERMOCOUPLE_TYPE_S = (byte) 'S';
        public static final byte USB_TC08_THERMOCOUPLE_TYPE_T = (byte) 'T';
        public static final byte USB_TC08_VOLTAGE_READINGS    = (byte) 'X';
        public static final byte USB_TC08_DISABLE_CHANNEL     = (byte) ' ';


        // Enumerations
        // ============

        public enum USBTC08Channels {
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

        public enum USBTC08Units {
            USBTC08_UNITS_CENTIGRADE,
            USBTC08_UNITS_FAHRENHEIT,
            USBTC08_UNITS_KELVIN,
            USBTC08_UNITS_RANKINE;
        }

        public enum USBTC08MainsFrequency {
            USBTC08_FIFTY_HERTZ,
            USBTC08_SIXTY_HERTZ;
        }
    }

}
