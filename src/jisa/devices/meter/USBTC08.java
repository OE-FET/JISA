package jisa.devices.meter;

import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.ShortByReference;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Driver class for Picotech USB-TC08 thermocouple data loggers. Requires proprietary usbtc08 library to be installed.
 */
public class USBTC08 extends NativeDevice implements MSTMeter<USBTC08.TC08TMeter> {

    public static String getDescription() {
        return "PicoTech USB-TC08";
    }

    // Constants
    private static final String                         LIBRARY_NAME                 = "usbtc08";
    private static final Class<USBTC08.NativeInterface> LIBRARY_CLASS                = USBTC08.NativeInterface.class;
    private static final int                            SENSORS_PER_UNIT             = 9;
    private static final short                          ACTION_FAILED                = 0;
    private static final short                          ERROR_NONE                   = 0;
    private static final short                          ERROR_OS_NOT_SUPPORTED       = 1;
    private static final short                          ERROR_NO_CHANNELS_SET        = 2;
    private static final short                          ERROR_INVALID_PARAMETER      = 3;
    private static final short                          ERROR_VARIANT_NOT_SUPPORTED  = 4;
    private static final short                          ERROR_INCORRECT_MODE         = 5;
    private static final short                          ERROR_ENUMERATION_INCOMPLETE = 6;
    private static final short                          ERROR_NOT_RESPONDING         = 7;
    private static final short                          ERROR_FW_FAIL                = 8;
    private static final short                          ERROR_CONFIG_FAIL            = 9;
    private static final short                          ERROR_NOT_FOUND              = 10;
    private static final short                          ERROR_THREAD_FAIL            = 11;
    private static final short                          ERROR_PIPE_INFO_FAIL         = 12;
    private static final short                          ERROR_NOT_CALIBRATED         = 13;
    private static final short                          ERROR_PICOPP_TOO_OLD         = 14;
    private static final short                          ERROR_COMMUNICATION          = 15;
    private static final short                          UNITS_KELVIN                 = 2;

    /**
     * Static instance of loaded library
     */
    private static USBTC08.NativeInterface INSTANCE;
    private static Throwable               EXCEPTION;

    private final short handle;

    public final TC08TMeter CHANNEL_0 = new TC08TMeter((short) 0);
    public final TC08TMeter CHANNEL_1 = new TC08TMeter((short) 1);
    public final TC08TMeter CHANNEL_2 = new TC08TMeter((short) 2);
    public final TC08TMeter CHANNEL_3 = new TC08TMeter((short) 3);
    public final TC08TMeter CHANNEL_4 = new TC08TMeter((short) 4);
    public final TC08TMeter CHANNEL_5 = new TC08TMeter((short) 5);
    public final TC08TMeter CHANNEL_6 = new TC08TMeter((short) 6);
    public final TC08TMeter CHANNEL_7 = new TC08TMeter((short) 7);
    public final TC08TMeter CHANNEL_8 = new TC08TMeter((short) 8);

    private final List<TC08TMeter> channels = List.of(CHANNEL_0, CHANNEL_1, CHANNEL_2, CHANNEL_3, CHANNEL_4, CHANNEL_5, CHANNEL_6, CHANNEL_7, CHANNEL_8);

    private float[]   lastValues    = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    private long      lastTime      = 0;
    private long      interval      = 0;
    private Frequency lineFrequency = Frequency.FIFTY_HERTZ;

    private final USBTC08.NativeInterface usbtc08;

    /**
     * Connects to the first USB-TC08 unit found connected to the system.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    public USBTC08() throws IOException, DeviceException {

        super("USB-TC08 Temperature Data Logger");

        usbtc08 = findLibrary(LIBRARY_CLASS, LIBRARY_NAME);

        short handle = usbtc08.usb_tc08_open_unit();

        if (handle > 0) {
            this.handle = handle;
        } else if (handle == ACTION_FAILED) {
            throw new IOException("No USB TC-08 unit found!");
        } else {
            throw new DeviceException(getLastError((short) 0));
        }

        interval = usbtc08.usb_tc08_get_minimum_interval_ms(handle);

    }

    /**
     * Connects to the first USB-TC08 unit with matching serial number, given in the form of an IDAddress object.
     *
     * @param address Serial number, as IDAddress object
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    public USBTC08(Address address) throws IOException, DeviceException {

        super("USB-TC08 Temperature Data Logger");

        usbtc08 = findLibrary(LIBRARY_CLASS, LIBRARY_NAME);

        if (!(address instanceof IDAddress)) {
            throw new DeviceException("This driver requires an ID or serial number address.");
        }

        String serial = ((IDAddress) address).getID();

        if (INSTANCE == null) {
            throw new IOException("Error loading usbtc08 library!");
        }

        // Search for all connected units
        List<USBTC08> found = findUnits();

        if (found.isEmpty()) {
            throw new IOException("No USB TC-08 unit found!");
        }

        Short value = null;

        for (USBTC08 unit : found) {

            // If it's the one we want, give this instance the handle, otherwise close the connection
            if (unit.getSerial().toLowerCase().equals(serial.toLowerCase().trim())) {
                value = unit.handle;
            } else {
                unit.close();
            }

        }

        // If nothing was found, then the serial number is wrong
        if (value == null) {
            throw new IOException(String.format("No USB TC-08 unit with serial number \"%s\" was found.", serial));
        }

        handle   = value;
        interval = usbtc08.usb_tc08_get_minimum_interval_ms(handle);

    }

    /**
     * Connects to the first USB-TC08 unit with matching serial number, given as a String.
     *
     * @param serial Serial number, as a String
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    public USBTC08(String serial) throws IOException, DeviceException {
        this(new IDAddress(serial));
    }

    @Override
    public List<TC08TMeter> getThermometers() {
        return channels;
    }

    @Override
    public Map<TC08TMeter, Double> getTemperatures(TC08TMeter... channels) throws IOException, DeviceException {

        if (channels.length == 0) {
            channels = this.channels.toArray(TC08TMeter[]::new);
        }

        // If it's been long enough, update the readings buffer
        if ((System.currentTimeMillis() - lastTime) > interval) {
            updateReadings();
        }

        return Arrays.stream(channels).collect(Collectors.toMap(c -> c, c -> (double) lastValues[c.channel]));

    }

    /**
     * Returns a list of all USB-TC08 units found connected to this computer.
     *
     * @return List of USBTC08 objects representing the found units
     */
    public static List<USBTC08> findUnits() {

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

    /**
     * Returns the serial number of this unit.
     *
     * @return Serial number
     *
     * @throws DeviceException Upon instrument error.
     */
    public String getSerial() throws DeviceException {

        byte[] read   = new byte[256];
        short  result = usbtc08.usb_tc08_get_unit_info2(handle, read, (short) 256, NativeInterface.USBTC08LINE_BATCH_AND_SERIAL);

        if (result == ACTION_FAILED) {
            throw new DeviceException(getLastError(handle));
        }

        return new String(read).trim();

    }

    /**
     * Updates the currently held temperature readings for each sensor. This should be updated at most every minimum
     * measurement interval, as calculated by the USB-TC08 unit.
     *
     * @throws DeviceException Upon instrument error
     */
    private synchronized void updateReadings() throws DeviceException {

        lastTime = System.currentTimeMillis();
        interval = usbtc08.usb_tc08_get_minimum_interval_ms(handle);

        // Need a pointer to some memory to store our returned values
        Memory tempPointer = new Memory(9L * Native.getNativeSize(Float.TYPE));

        int result = usbtc08.usb_tc08_get_single(handle, tempPointer, new ShortByReference((short) 0), UNITS_KELVIN);

        // If zero, then something's gone wrong.
        if (result == ACTION_FAILED) {
            throw new DeviceException(getLastError(handle));
        }

        lastValues = tempPointer.getFloatArray(0, SENSORS_PER_UNIT);
        lastTime   = System.currentTimeMillis();

        tempPointer.close();

    }

    public double getTemperatureRange(int sensor) {
        return 999.999;
    }

    /**
     * Returns the line-frequency rejection mode currently in use.
     *
     * @return Frequency, 50 Hz or 60 Hz.
     */
    public Frequency getLineFrequency() {
        return lineFrequency;
    }

    /**
     * Sets the line-frequency rejection mode to be used.
     *
     * @param frequency 50 Hz or 60 Hz
     *
     * @throws DeviceException Upon instrument error
     */
    public void setLineFrequency(Frequency frequency) throws DeviceException {


        int result = usbtc08.usb_tc08_set_mains(handle, (short) frequency.ordinal());

        if (result == ACTION_FAILED) {
            throw new DeviceException(getLastError(handle));
        } else {
            lineFrequency = frequency;
        }


    }

    @Override
    public String getIDN() throws DeviceException {
        return String.format("PICO TC-08, S/N: \"%s\"", getSerial());
    }

    @Override
    public String getName() {
        return "PICO USB-TC-08";
    }

    @Override
    public void close() throws DeviceException {

        int result = usbtc08.usb_tc08_close_unit(handle);

        if (result == ACTION_FAILED) {
            throw new DeviceException(getLastError(handle));
        }

    }

    @Override
    public Address getAddress() {

        try {
            return new IDAddress(getSerial());
        } catch (Exception e) {
            return null;
        }

    }

    private String getLastError(short handle) {

        int error = usbtc08.usb_tc08_get_last_error(handle);

        switch (error) {

            case ERROR_NONE:
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

            case ERROR_NOT_RESPONDING:
                return "TC-08 unit not responding";

            case ERROR_FW_FAIL:
                return "Failed to download firmware to TC-08 unit";

            case ERROR_CONFIG_FAIL:
                return "Missing or corrupted EEPROM on TC-08 unit";

            case ERROR_NOT_FOUND:
                return "Cannot find enumerated device";

            case ERROR_THREAD_FAIL:
                return "A threading function failed";

            case ERROR_PIPE_INFO_FAIL:
                return "Can not get USB pipe information";

            case ERROR_NOT_CALIBRATED:
                return "No calibration date was found";

            case ERROR_PICOPP_TOO_OLD:
                return "The picopp.sys driver is too old";

            case ERROR_COMMUNICATION:
                return "Communication lost with TC-08 unit";

            default:
                return String.format("Unknown TC-08 error (code: %d)", error);

        }

    }


    public class TC08TMeter implements TCouple {

        private final short channel;
        private       Type  type = Type.UNKNOWN;

        public TC08TMeter(short channel) {
            this.channel = channel;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return USBTC08.this.getIDN();
        }

        @Override
        public String getName() {
            return String.format("Channel %d", channel);
        }

        @Override
        public void close() throws IOException, DeviceException {

        }

        @Override
        public Address getAddress() {
            return USBTC08.this.getAddress();
        }

        @Override
        public double getTemperature() throws IOException, DeviceException {

            // If it's been long enough, update the readings buffer
            if ((System.currentTimeMillis() - lastTime) > interval) {
                updateReadings();
            }

            return lastValues[channel];

        }

        @Override
        public void setTemperatureRange(double range) throws IOException, DeviceException {
            /* nothing to do */
        }

        @Override
        public double getTemperatureRange() throws IOException, DeviceException {
            return 999.9;
        }

        @Override
        public void setSensorType(Type type) throws IOException, DeviceException {

            int result = usbtc08.usb_tc08_set_channel(
                handle,
                channel,
                NativeInterface.TYPE_MAP.getOrDefault(type, NativeInterface.USB_TC08_DISABLE_CHANNEL)
            );

            if (result == ACTION_FAILED) {
                throw new DeviceException(getLastError(handle));
            } else {
                this.type = type;
            }

        }

        @Override
        public Type getSensorType() throws IOException, DeviceException {
            return type;
        }
    }

    /**
     * Enumeration of line-frequency rejection modes.
     */
    public enum Frequency {
        FIFTY_HERTZ,
        SIXTY_HERTZ
    }

    /**
     * Interface corresponding to native usbtc08 library methods.
     */
    protected interface NativeInterface extends Library {

        int   USBTC08_MAX_CHANNELS         = 8;
        short USBTC08LINE_BATCH_AND_SERIAL = 4;
        byte  USB_TC08_THERMOCOUPLE_TYPE_B = (byte) 'B';
        byte  USB_TC08_THERMOCOUPLE_TYPE_E = (byte) 'E';
        byte  USB_TC08_THERMOCOUPLE_TYPE_J = (byte) 'J';
        byte  USB_TC08_THERMOCOUPLE_TYPE_K = (byte) 'K';
        byte  USB_TC08_THERMOCOUPLE_TYPE_N = (byte) 'N';
        byte  USB_TC08_THERMOCOUPLE_TYPE_R = (byte) 'R';
        byte  USB_TC08_THERMOCOUPLE_TYPE_S = (byte) 'S';
        byte  USB_TC08_THERMOCOUPLE_TYPE_T = (byte) 'T';
        byte  USB_TC08_VOLTAGE_READINGS    = (byte) 'X';
        byte  USB_TC08_DISABLE_CHANNEL     = (byte) ' ';

        Map<TCouple.Type, Byte> TYPE_MAP = Map.of(
            TCouple.Type.B, USB_TC08_THERMOCOUPLE_TYPE_B,
            TCouple.Type.E, USB_TC08_THERMOCOUPLE_TYPE_E,
            TCouple.Type.J, USB_TC08_THERMOCOUPLE_TYPE_J,
            TCouple.Type.K, USB_TC08_THERMOCOUPLE_TYPE_K,
            TCouple.Type.N, USB_TC08_THERMOCOUPLE_TYPE_N,
            TCouple.Type.R, USB_TC08_THERMOCOUPLE_TYPE_R,
            TCouple.Type.S, USB_TC08_THERMOCOUPLE_TYPE_S,
            TCouple.Type.T, USB_TC08_THERMOCOUPLE_TYPE_T
        );

        short usb_tc08_open_unit();

        short usb_tc08_close_unit(short handle);

        short usb_tc08_set_mains(short handle, short sixty_hertz);

        short usb_tc08_set_channel(short handle, short channel, byte tc_type);

        int usb_tc08_get_minimum_interval_ms(short handle);

        short usb_tc08_get_formatted_info(short handle, byte[] unitInfo, short stringLength);

        short usb_tc08_get_unit_info2(short handle, byte[] unitInfo, short stringLength, short line);

        short usb_tc08_get_single(short handle, Memory temp, ShortByReference overflowFlags, short units);

        short usb_tc08_get_last_error(short handle);

    }

}
