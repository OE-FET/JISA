package jisa.devices.power;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.EMController;
import jisa.devices.temperature.ITC503;
import jisa.visa.VISADevice;
import jisa.visa.connections.Connection;
import jisa.visa.connections.GPIBConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPS120 extends VISADevice implements EMController {

    public static String getDescription() {
        return "Oxford Instruments IPS-120";
    }

    private static final String TERMINATOR                        = "\r";
    private static final String C_SET_COMM_MODE                   = "Q2";
    private static final String C_READ_CHANNEL                    = "R%d";
    private static final String C_SET_REM_STATUS                  = "C%d";
    private static final String C_SET_ACTIVITY                    = "A%d";
    private static final String C_SET_MODE                        = "M%d";
    private static final String C_SET_POLARITY                    = "P%d";
    private static final String C_SET_CURRENT_SWEEP_RATE          = "S%f";
    private static final String C_SET_FIELD_SWEEP_RATE            = "T%f";
    private static final String C_SET_TARGET_CURRENT              = "I%f";
    private static final String C_SET_TARGET_FIELD                = "J%f";
    private static final String C_SET_SWITCH_HEATER               = "H%d";
    private static final String C_QUERY_STATUS                    = "X";
    private static final int    MODE_AMPS                         = 8;
    private static final int    MODE_TESLA                        = 9;
    private static final int    CHANNEL_DEMAND_CURRENT            = 0;
    private static final int    CHANNEL_POWER_SUPPLY_VOLTAGE      = 1;
    private static final int    CHANNEL_DEMAND_CURRENT_SET_POINT  = 5;
    private static final int    CHANNEL_DEMAND_CURRENT_SWEEP_RATE = 6;
    private static final int    CHANNEL_DEMAND_FIELD              = 7;
    private static final int    CHANNEL_DEMAND_FIELD_SET_POINT    = 8;
    private static final int    CHANNEL_DEMAND_FIELD_SWEEP_RATE   = 9;
    private static final int    CHANNEL_MEASURED_MAGNET_CURRENT   = 2;
    private static final int    CHANNEL_PERSISTENT_MAGNET_CURRENT = 16;
    private static final double AMPS_PER_TESLA                    = 9.8793;

    private final List<Ramp> ramps = new ArrayList<>(List.of(
        new Ramp(-200.0, -80, 1),
        new Ramp(-80, -65, 2),
        new Ramp(-65, 0, 6),
        new Ramp(0, 65, 6),
        new Ramp(65, 80, 2),
        new Ramp(80, 200.0, 1)
    ));

    /**
     * Opens the device at the specified address
     *
     * @param address Some form of InstrumentAddress (eg GPIBAddress, USBAddress etc)
     *
     * @throws IOException Upon communications error
     */
    public IPS120(Address address) throws IOException, DeviceException {

        super(address);

        Connection connection = getConnection();

        if (connection instanceof GPIBConnection) {
            ((GPIBConnection) connection).setEOIEnabled(false);
        }

        setWriteTerminator(TERMINATOR);
        write(C_SET_COMM_MODE);
        setReadTerminator(EOS_RETURN);

        manuallyClearReadBuffer();

        setMode(Mode.REMOTE_UNLOCKED);

        try {
            String idn = query("V");
            if (!idn.split(" ")[0].trim().equals("IPS120-10")) {
                throw new DeviceException("Device at address %s is not an IPS120!", address.toString());
            }
        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.toString());
        }

        if (examineStatus().activity == Activity.CLAMP) {
            setActivity(Activity.HOLD);
        }

    }

    private double readChannel(int channel) throws IOException {
        String response = query(C_READ_CHANNEL, channel);
        return Double.parseDouble(response.substring(1));
    }

    public double getField() throws IOException {
        return readChannel(CHANNEL_DEMAND_FIELD);
    }

    public void setField(double field) throws IOException, DeviceException, InterruptedException {

        if (Math.abs(field) > 10.0) {
            throw new DeviceException("Valid field values are +/- 10 T");
        }

        rampToCurrent(AMPS_PER_TESLA * field);
    }

    public Activity getActivity() throws IOException {
        return examineStatus().activity;
    }

    public void setActivity(Activity activity) throws IOException {
        query(C_SET_ACTIVITY, activity.ordinal());
    }

    public void turnOff() throws IOException, DeviceException, InterruptedException {
        rampToCurrent(0.0);
        setHeater(false);
    }

    @Override
    public List<Ramp> getRampRates() {
        return ramps;
    }

    public void setRampRates(Ramp... rates) {
        ramps.clear();
        ramps.addAll(Arrays.asList(rates));
    }

    @Override
    public double getCurrent() throws IOException, DeviceException {
        return readChannel(CHANNEL_DEMAND_CURRENT);
    }

    @Override
    public void setCurrent(double current) throws IOException, DeviceException, InterruptedException {
        rampToCurrent(current);
    }

    private void rampToCurrent(double current) throws IOException, DeviceException, InterruptedException {

        try {

            switch (examineStatus().heaterState) {

                case OFF_ZERO:
                    setActivity(Activity.GO_ZERO);
                    waitUntilStable();
                    setHeater(true);
                    break;

                case OFF_NOT_ZERO:
                    setTargetCurrent(getPersistentCurrent());
                    setActivity(Activity.GO_SETPOINT);
                    waitUntilStable();
                    setHeater(true);
                    break;

            }

            setActivity(Activity.HOLD);

            List<Ramp> legs = getRampLegs(getMagnetCurrent(), current);

            for (Ramp leg : legs) {
                System.out.printf("Will Do: From: %s A, To: %s A, Rate: %s A/min\n", leg.getMinI(), leg.getMaxI(), leg.getRate());
            }

            for (Ramp leg : legs) {

                System.out.printf("Now: From: %s A, To: %s A, Rate: %s A/min\n", leg.getMinI(), leg.getMaxI(), leg.getRate());
                setTargetCurrent(leg.getMaxI());
                setCurrentRamp(leg.getRate());
                setActivity(Activity.GO_SETPOINT);
                Util.sleep(1000);
                waitUntilStable();
                setActivity(Activity.HOLD);

            }

        } catch (Exception e) {
            setActivity(Activity.HOLD);
            throw e;
        }

    }

    private void setTargetCurrent(double current) throws IOException {
        query(C_SET_TARGET_CURRENT, current);
    }

    private void setCurrentRamp(double rate) throws IOException {
        query(C_SET_CURRENT_SWEEP_RATE, rate);
    }

    public void setHeater(boolean on) throws IOException, InterruptedException, DeviceException {

        query(C_SET_SWITCH_HEATER, on ? 1 : 0);
        Thread.sleep(30000);

        HeaterState heaterState = examineStatus().heaterState;

        if (on && heaterState != HeaterState.ON) {
            throw new DeviceException("Heater failed to engage");
        }

        if (!on && heaterState != HeaterState.OFF_ZERO && heaterState != HeaterState.OFF_NOT_ZERO) {
            throw new DeviceException("Heater failed to disengage");
        }

    }

    public double getMagnetCurrent() throws IOException {
        return readChannel(CHANNEL_MEASURED_MAGNET_CURRENT);
    }

    public double getPersistentCurrent() throws IOException {
        return readChannel(CHANNEL_PERSISTENT_MAGNET_CURRENT);
    }

    private Status examineStatus() throws IOException {
        return new Status(query(C_QUERY_STATUS));
    }

    public void waitUntilStable() throws IOException, InterruptedException {

        while (!isStable()) {

            Thread.sleep(1000);

        }

    }

    public boolean isStable() throws IOException {
        return examineStatus().status2 == 0;
    }

    public void setMode(Mode mode) throws IOException {
        query(C_SET_REM_STATUS, mode.toInt());
    }

    public enum Activity {

        HOLD,
        GO_SETPOINT,
        GO_ZERO,
        UNK,
        CLAMP

    }

    public enum CStatus {

        LOCAL_LOCKED,
        REMOTE_LOCKED,
        LOCAL_UNLOCKED,
        REMOTE_UNLOCKED

    }

    public enum HeaterState {

        OFF_ZERO(0),
        ON(1),
        OFF_NOT_ZERO(2),
        HEATER_FAULT(5),
        NO_SWITCH_FITTED(8);

        public int code;

        HeaterState(int c) {
            code = c;
        }

        public static HeaterState fromCode(int code) {

            for (HeaterState h : values()) {

                if (h.code == code) {
                    return h;
                }

            }

            return null;

        }

        public int getCode() {
            return code;
        }

    }


    public enum Mode {

        LOCAL_LOCKED(0),
        REMOTE_LOCKED(1),
        LOCAL_UNLOCKED(2),
        REMOTE_UNLOCKED(3);

        private static final HashMap<Integer, ITC503.Mode> LOOKUP = new HashMap<>();

        static {
            for (ITC503.Mode mode : ITC503.Mode.values()) {
                LOOKUP.put(mode.toInt(), mode);
            }
        }

        private final int c;

        Mode(int code) {
            c = code;
        }

        static ITC503.Mode fromInt(int i) {
            return LOOKUP.getOrDefault(i, null);
        }

        int toInt() {
            return c;
        }

    }

    private static class Status {

        private static final Pattern     PATTERN = Pattern.compile("X([0-8])([0-8])A([0-4])C([0-7])H([0-8])M([0-5])([0-3])P([0-7])([1-4])");
        public               int         status1;
        public               int         status2;
        public               Activity    activity;
        public               CStatus     commState;
        public               HeaterState heaterState;
        public               int         mode1;
        public               int         mode2;
        public               int         polarity;

        public Status(String response) throws IOException {

            Matcher match = PATTERN.matcher(response);

            if (!match.find()) {
                throw new IOException("Improperly formatted response from IPS120");
            }

            status1     = Integer.parseInt(match.group(1));
            status2     = Integer.parseInt(match.group(2));
            activity    = Activity.values()[Integer.parseInt(match.group(3))];
            commState   = CStatus.values()[Integer.parseInt(match.group(4))];
            heaterState = HeaterState.fromCode(Integer.parseInt(match.group(5)));
            status1     = Integer.parseInt(match.group(6));
            status2     = Integer.parseInt(match.group(7));

        }
    }

}