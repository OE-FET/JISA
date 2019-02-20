package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;
import JISA.VISA.VISADevice;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPS120 extends VISADevice {

    private static final String TERMINATOR       = "\r";
    private static final String C_SET_COMM_MODE  = "Q2";
    private static final String C_READ_CHANNEL   = "R%d";
    //private static final String C_SET_LOC_REM_STATUS            = "C%d";
    private static final String C_SET_REM_STATUS = "C%d";

    private static final String C_SET_ACTIVITY           = "A%d";
    private static final String C_SET_MODE               = "M%d";
    private static final String C_SET_POLARITY           = "P%d";
    private static final String C_SET_CURRENT_SWEEP_RATE = "S%f";
    private static final String C_SET_FIELD_SWEEP_RATE   = "T%f";
    private static final String C_SET_TARGET_CURRENT     = "I%f";
    private static final String C_SET_TARGET_FIELD       = "J%f";
    private static final String C_SET_SWITCH_HEATER      = "H%d";
    private static final String C_QUERY_STATUS           = "X";

    private static final int MODE_AMPS  = 8;
    private static final int MODE_TESLA = 9;

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

        public static HeaterState fromCode(int code) {

            for (HeaterState h : values()) {

                if (h.code == code) {
                    return h;
                }

            }

            return null;

        }

        HeaterState(int c) {
            code = c;
        }

        public int getCode() {
            return code;
        }

    }

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

    public static final RampRates SUPER_CON_SLOW = new RampRates(
            new Ramp(-98.8, -80, 1),
            new Ramp(-80, -65, 2),
            new Ramp(-65, 0, 6),
            new Ramp(0, 65, 6),
            new Ramp(65, 80, 2),
            new Ramp(80, 98.8, 1)
    );

    public static final RampRates SUPER_CON_FAST = new RampRates(
            new Ramp(-98.8, 0, 9.88),
            new Ramp(0, 98.8, 9.88)
    );

    private RampRates rampRates = SUPER_CON_SLOW;

    /**
     * Opens the device at the specified address
     *
     * @param address Some form of InstrumentAddress (eg GPIBAddress, USBAddress etc)
     *
     * @throws IOException Upon communications error
     */
    public IPS120(InstrumentAddress address) throws IOException, DeviceException {

        super(address);
        setEOI(false);
        setTerminator(TERMINATOR);
        write(C_SET_COMM_MODE);
        setReadTerminationCharacter(EOS_RETURN);

        clearRead();

        setMode(Mode.REMOTE_UNLOCKED);

        try {
            String idn = query("V");
            if (!idn.split(" ")[0].trim().equals("IPS120-10")) {
                throw new DeviceException("Device at address %s is not an IPS120!", address.getVISAAddress());
            }
        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }

        if (examineStatus().activity == Activity.CLAMP) {
            setActivity(Activity.HOLD);
        }

    }

    private void setRampRates(RampRates rates) {
        rampRates = rates;
    }

    private double readChannel(int channel) throws IOException {
        String response = query(C_READ_CHANNEL, channel);
        return Double.valueOf(response.substring(1));
    }

    public double getField() throws IOException {
        return readChannel(CHANNEL_DEMAND_FIELD);
    }

    public void setActivity(Activity activity) throws IOException {
        query(C_SET_ACTIVITY, activity.ordinal());
    }

    public Activity getActivity() throws IOException {
        return examineStatus().activity;
    }

    public void turnOff() throws IOException, DeviceException {
        rampToCurrent(0.0);
        setHeater(false);
    }

    public void rampToField(double field) throws IOException, DeviceException {
        rampToCurrent(AMPS_PER_TESLA * field);
    }

    public void rampToCurrent(double current) throws IOException, DeviceException {

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

        Ramp[] legs = rampRates.getLegs(getMagnetCurrent(), current);

        for (Ramp leg : legs) {
            System.out.printf("Will Do: From: %s A, To: %s A, Rate: %s A/min\n", leg.minI, leg.maxI, leg.rate);
        }

        for (Ramp leg : legs) {

            System.out.printf("Now: From: %s A, To: %s A, Rate: %s A/min\n", leg.minI, leg.maxI, leg.rate);
            setTargetCurrent(leg.maxI);
            setCurrentRamp(leg.rate);
            setActivity(Activity.GO_SETPOINT);
            Util.sleep(1000);
            waitUntilStable();
            setActivity(Activity.HOLD);

        }

    }

    private void setTargetCurrent(double current) throws IOException {
        query(C_SET_TARGET_CURRENT, current);
    }

    private void setCurrentRamp(double rate) throws IOException {
        query(C_SET_CURRENT_SWEEP_RATE, rate);
    }

    public void setHeater(boolean on) throws IOException {
        query(C_SET_SWITCH_HEATER, on ? 1 : 0);
        Util.sleep(30000);
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

    public void waitUntilStable() throws IOException {

        while (true) {

            if (isStable()) {
                break;
            }

            Util.sleep(1000);

        }

    }

    public boolean isStable() throws IOException {
        return examineStatus().status2 == 0;
    }

    public void setMode(Mode mode) throws IOException {
        query(C_SET_REM_STATUS, mode.toInt());
    }


    public enum Mode {

        LOCAL_LOCKED(0),
        REMOTE_LOCKED(1),
        LOCAL_UNLOCKED(2),
        REMOTE_UNLOCKED(3);

        private        int                           c;
        private static HashMap<Integer, ITC503.Mode> lookup = new HashMap<>();

        static ITC503.Mode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (ITC503.Mode mode : ITC503.Mode.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        Mode(int code) {
            c = code;
        }

        int toInt() {
            return c;
        }

    }

    private static class Status {

        public int         status1;
        public int         status2;
        public Activity    activity;
        public CStatus     commState;
        public HeaterState heaterState;
        public int         mode1;
        public int         mode2;
        public int         polarity;

        private static final Pattern PATTERN = Pattern.compile("X([0-8])([0-8])A([0-4])C([0-7])H([0-8])M([0-5])([0-3])P([0-7])([1-4])");

        public Status(String response) throws IOException {

            Matcher match = PATTERN.matcher(response);

            if (!match.find()) {
                throw new IOException("Improperly formatted response from IPS120");
            }

            status1 = Integer.valueOf(match.group(1));
            status2 = Integer.valueOf(match.group(2));
            activity = Activity.values()[Integer.valueOf(match.group(3))];
            commState = CStatus.values()[Integer.valueOf(match.group(4))];
            heaterState = HeaterState.fromCode(Integer.valueOf(match.group(5)));
            status1 = Integer.valueOf(match.group(6));
            status2 = Integer.valueOf(match.group(7));

        }
    }

    public static class RampRates {

        private Ramp[] rows;

        public RampRates(Ramp... ramps) {
            rows = ramps;
        }

        public Ramp getRamp(double current) {
            int i = getRampIndex(current);
            return i > -1 ? rows[i] : null;
        }

        public int getRampIndex(double current) {

            for (int i = 0; i < rows.length; i++) {

                Ramp r = rows[i];

                if (r.maxI >= current && r.minI <= current) {
                    return i;
                }

            }

            return -1;
        }

        public Ramp[] getLegs(double from, double to) throws DeviceException {

            int              i    = getRampIndex(from);
            int              d    = to > from ? +1 : -1;
            LinkedList<Ramp> legs = new LinkedList<>();

            double last = from;

            while (true) {

                if (i == getRampIndex(to)) {
                    legs.add(new Ramp(last, to, rows[i].rate));
                    break;
                }

                int next = i + d;
                if (next < 0 || next >= rows.length) {
                    throw new DeviceException("You cannot ramp to that value!");
                }

                Ramp r = rows[next];

                double border = d > 0 ? (r.minI) : (r.maxI);

                if (d > 0 ? (border < to) : (border > to)) {
                    legs.add(new Ramp(last, border, rows[i].rate));
                    i = next;
                    last = border;
                } else {
                    legs.add(new Ramp(last, to, rows[i].rate));
                    break;
                }


            }

            return legs.toArray(new Ramp[0]);

        }

    }

    public static class Ramp {

        public double minI;
        public double maxI;
        public double rate;

        public Ramp(double minCurrent, double maxCurrent, double rampRate) {
            minI = minCurrent;
            maxI = maxCurrent;
            rate = rampRate;
        }

    }

}