package jisa.devices.translator;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.SubInstrument;
import jisa.devices.translator.feature.Backlash;
import jisa.devices.translator.feature.DriftCorrection;
import jisa.devices.translator.feature.Encoder;
import jisa.devices.translator.feature.ThreadedRotor;
import jisa.visa.VISADevice;
import jisa.visa.connections.SerialConnection;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class PriorProScan extends VISADevice implements Stage.Mixed<PriorProScan.Linear, PriorProScan.Rotational, PriorProScan.Translator> {

    public final Linear     X_AXIS;
    public final Linear     Y_AXIS;
    public final Linear     Z_AXIS;
    public final Rotational FILTER_1;
    public final Rotational FILTER_2;

    private final List<Linear>     linearAxes;
    private final List<Rotational> rotationalAxes;

    public static class II extends PriorProScan {

        public II(Address address) throws IOException, DeviceException {
            super(address, 1);
        }

    }

    public static class III extends PriorProScan {

        public III(Address address) throws IOException, DeviceException {
            super(address, 2);
        }

    }

    protected PriorProScan(Address address, int stopBits) throws IOException, DeviceException {

        super(address);

        configSerial(serial -> serial.setSerialParameters(9600, 8, SerialConnection.Parity.NONE, stopBits));

        setWriteTerminator("\r");
        setReadTerminator("\r");

        addAutoRemove("\r", "\n");

        write("COMP O");

        List<Linear>     linearAxes     = new LinkedList<>();
        List<Rotational> rotationalAxes = new LinkedList<>();

        for (String infoLine : queryLines("?")) {

            if (infoLine.contains("=")) {

                String[] parts = infoLine.split(" = ");

                if (parts[0].equals("STAGE")) {

                    if (!parts[1].equals("NONE")) {
                        linearAxes.add(new Linear("X"));
                        linearAxes.add(new Linear("Y"));
                    }

                } else if (parts[0].equals("FOCUS")) {

                    if (!parts[1].equals("NONE")) {
                        linearAxes.add(new Linear("Z"));
                    }

                } else if (parts[0].startsWith("FILTER_")) {

                    if (!parts[1].equals("NONE")) {
                        rotationalAxes.add(new Rotational(parts[0].replace("FILTER_", "F")));
                    }

                }

            }

        }

        this.linearAxes     = List.copyOf(linearAxes);
        this.rotationalAxes = List.copyOf(rotationalAxes);

        X_AXIS   = linearAxes.stream().filter(a -> a.getName().equals("X Axis")).findFirst().orElse(null);
        Y_AXIS   = linearAxes.stream().filter(a -> a.getName().equals("Y Axis")).findFirst().orElse(null);
        Z_AXIS   = linearAxes.stream().filter(a -> a.getName().equals("Z Axis")).findFirst().orElse(null);
        FILTER_1 = rotationalAxes.stream().filter(a -> a.getName().equals("FILTER 1")).findFirst().orElse(null);
        FILTER_2 = rotationalAxes.stream().filter(a -> a.getName().equals("FILTER 2")).findFirst().orElse(null);

        if (Z_AXIS != null) {
            write("UPR,z,100");
        }

        write("BLSH,0");

        for (String part : queryLines("STAGE")) {

            if (part.contains("=")) {

                String[] parts = part.split(" = ");

                if (parts[0].equals("SIZE_X") && X_AXIS != null) {
                    X_AXIS.setLimit(Double.parseDouble(parts[1].replace("MM", "").trim()) * 1e-3);
                }

                if (parts[0].equals("SIZE_Y") && Y_AXIS != null) {
                    Y_AXIS.setLimit(Double.parseDouble(parts[1].replace("MM", "").trim()) * 1e-3);
                }

                if (parts[0].equals("MICROSTEPS/MICRON")) {

                    double mpr = Double.parseDouble(parts[1].trim());
                    double res = 1e-6 / mpr;

                    if (X_AXIS != null) { X_AXIS.setResolution(res); }
                    if (Y_AXIS != null) { Y_AXIS.setResolution(res); }
                    if (Z_AXIS != null) { Z_AXIS.setResolution(res); }

                }

            }

        }

    }

    protected synchronized List<String> queryLines(String command) throws IOException {

        manuallyClearReadBuffer();

        List<String> lines = new LinkedList<>();
        String       lastLine;
        write(command);

        do {

            lastLine = read().trim();
            lines.add(lastLine);

        } while (!lastLine.equals("END"));

        return lines;

    }

    @Override
    public void setPosition(double... coordinates) throws IOException, DeviceException {

        // First three will be X,Y,Z
        String args = DoubleStream.of(coordinates).mapToObj(v -> String.format("%d", (int) Math.round(v))).limit(3).collect(Collectors.joining(","));
        write("G,%s", args);

        if (coordinates.length > 3) {

            List<Translator> axes = getAllAxes();

            // The rest we will just have to do manually
            for (int i = 3; i < coordinates.length; i++) {
                axes.get(i).setPosition(coordinates[i]);
            }

        }

    }

    @Override
    public void moveBy(double... coordinates) throws IOException, DeviceException {

        // First three will be X,Y,Z
        String args = DoubleStream.of(coordinates).mapToObj(v -> String.format("%d", (int) Math.round(v))).limit(3).collect(Collectors.joining(","));
        write("GR,%s", args);

        if (coordinates.length > 3) {

            List<Translator> axes = getAllAxes();

            // The rest we will just have to do manually
            for (int i = 3; i < coordinates.length; i++) {
                axes.get(i).moveBy(coordinates[i]);
            }

        }

    }

    @Override
    public void moveToHome() throws IOException, DeviceException {
        write("Z");
    }

    @Override
    public void stop() throws IOException, DeviceException {
        write("K");
    }

    @Override
    public boolean isMoving() throws IOException, DeviceException {
        return queryInt("$") > 0;
    }

    @Override
    public List<Linear> getLinearAxes() {
        return linearAxes;
    }

    @Override
    public List<Rotational> getRotationalAxes() {
        return rotationalAxes;
    }

    @Override
    public String getName() {
        return "Prior ProScan II Controller";
    }

    public abstract class Translator implements jisa.devices.translator.Translator, SubInstrument<PriorProScan> {

    }

    public class Linear extends Translator implements Translator.Linear, Encoder, Backlash, DriftCorrection, ThreadedRotor, SubInstrument<PriorProScan> {

        private final String identifier;

        private double  resolution;
        private double  limit;
        private boolean locked = false;

        public Linear(String identifier) throws IOException, DeviceException {
            this.identifier = identifier;
            this.resolution = queryDouble("RES,%s", identifier) * 1e-6;
        }

        @Override
        public PriorProScan getParentInstrument() {
            return PriorProScan.this;
        }

        protected void setLimit(double limit) {
            this.limit = limit;
        }

        public void setResolution(double resolution) throws IOException, DeviceException {

            write("RES,%s,%e", identifier, resolution * 1e6);

            this.resolution = queryDouble("RES,%s", identifier) * 1e-6;

        }

        @Override
        public void setPosition(double position) throws IOException, DeviceException {

            if (locked) {
                throw new DeviceException("Cannot move translation axis \"%s\" because it is locked.", identifier);
            }

            if (!Util.isBetween(position, getMinPosition(), getMaxPosition())) {
                throw new DeviceException("The specified position for axis \"%s\" of %e m is out of range.", identifier, position);
            }

            write("G%s %d", identifier, (int) Math.round(position / resolution));

        }

        @Override
        public double getPosition() throws IOException, DeviceException {
            return queryDouble("P%s", identifier);
        }

        @Override
        public double getMinPosition() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public double getMaxPosition() throws IOException, DeviceException {
            return limit;
        }

        @Override
        public void setMaxSpeed(double speed) throws IOException, DeviceException {
            write("SMX,%.02f", speed);
        }

        @Override
        public double getMaxSpeed() throws IOException, DeviceException {
            return queryDouble("SMX");
        }

        @Override
        public void moveBy(double distance) throws IOException, DeviceException {

            if (locked) {
                throw new DeviceException("Cannot move translation axis \"%s\" because it is locked.", identifier);
            }

            write("GR%s %d", identifier, (int) Math.round(distance / resolution));

        }

        @Override
        public void moveToHome() throws IOException, DeviceException {

            if (locked) {
                throw new DeviceException("Cannot move translation axis \"%s\" because it is locked.", identifier);
            }

            setPosition(0);

        }

        @Override
        public synchronized void setLocked(boolean locked) throws IOException, DeviceException {
            this.locked = locked;
        }

        @Override
        public synchronized boolean isLocked() throws IOException, DeviceException {
            return locked;
        }

        @Override
        public boolean isMoving() throws IOException, DeviceException {
            return queryInt("$,%s", identifier) > 0;
        }

        @Override
        public void stop() throws IOException, DeviceException {
            write("K");
        }

        @Override
        public String getName() {
            return String.format("%s Axis", identifier);
        }

        @Override
        public void setEncoderEnabled(boolean enabled) throws IOException, DeviceException {
            write("ENCODER,%s,%d", identifier, enabled ? 1 : 0);
        }

        @Override
        public boolean isEncoderEnabled() throws IOException, DeviceException {

            switch (queryInt("ENCODER,%s", identifier)) {

                case 0:
                    return false;

                case 1:
                    return true;

                default:
                    throw new IOException("Invalid response from instrument.");

            }

        }

        @Override
        public void setBacklashEnabled(boolean enabled) throws IOException, DeviceException {
            write("BLSH,%d", enabled ? 1 : 0);
        }

        @Override
        public boolean isBacklashEnabled() throws IOException, DeviceException {
            return querySplitInt("BLSH", ",")[0] == 1;
        }

        @Override
        public void setBacklashSteps(int steps) throws IOException, DeviceException {
            write("BLSH,%d,%d", isBacklashEnabled() ? 1 : 0, steps);
        }

        @Override
        public int getBacklashSteps() throws IOException, DeviceException {
            return querySplitInt("BLSH", ",")[1];
        }

        @Override
        public void setDriftCorrectionEnabled(boolean enabled) throws IOException, DeviceException {
            write("SERVO,%s,%d", identifier, enabled ? 1 : 0);
        }

        @Override
        public boolean isDriftCorrectionEnabled() throws IOException, DeviceException {
            return queryInt("SERVO,%s", identifier) == 1;
        }

        @Override
        public void setDistancePerRevolution(double distancePerRevolution) throws IOException, DeviceException {
            write("UPR%s,%f", identifier, distancePerRevolution * 1e6);
        }

        @Override
        public double getDistancePerRevolution() throws IOException, DeviceException {
            return queryDouble("UPR%s", identifier) * 1e-6;
        }
    }

    public class Rotational extends Translator implements Translator.Rotational, Encoder, Backlash, SubInstrument<PriorProScan> {

        private final String identifier;

        private double  resolution;
        private double  limit;
        private boolean locked = false;

        public Rotational(String identifier) throws IOException, DeviceException {
            this.identifier = identifier;
            this.resolution = queryDouble("RES,%s", identifier) * 1e-6;
        }

        @Override
        public PriorProScan getParentInstrument() {
            return PriorProScan.this;
        }

        protected void setLimit(double limit) {
            this.limit = limit;
        }

        public void setResolution(double resolution) throws IOException, DeviceException {
            write("RES,%s,%e", identifier, resolution * 1e6);
        }

        @Override
        public void setPosition(double position) throws IOException, DeviceException {

            if (locked) {
                throw new DeviceException("Cannot move translation axis \"%s\" because it is locked.", identifier);
            }

            write("G%s %d", identifier, (int) Math.round(position / resolution));

        }

        @Override
        public double getPosition() throws IOException, DeviceException {
            return queryDouble("P%s", identifier);
        }

        @Override
        public double getMinPosition() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public double getMaxPosition() throws IOException, DeviceException {
            return 360;
        }

        @Override
        public void setMaxSpeed(double speed) throws IOException, DeviceException {
            write("SMX,%.02f", speed);
        }

        @Override
        public double getMaxSpeed() throws IOException, DeviceException {
            return queryDouble("SMX");
        }

        @Override
        public void moveBy(double distance) throws IOException, DeviceException {

            if (locked) {
                throw new DeviceException("Cannot move translation axis \"%s\" because it is locked.", identifier);
            }

            write("GR%s %d", identifier, (int) Math.round(distance / resolution));

        }

        @Override
        public void moveToHome() throws IOException, DeviceException {

            if (locked) {
                throw new DeviceException("Cannot move translation axis \"%s\" because it is locked.", identifier);
            }

            setPosition(0);

        }

        @Override
        public synchronized void setLocked(boolean locked) throws IOException, DeviceException {
            this.locked = locked;
        }

        @Override
        public synchronized boolean isLocked() throws IOException, DeviceException {
            return locked;
        }

        @Override
        public boolean isMoving() throws IOException, DeviceException {
            return queryInt("$,%s", identifier) > 0;
        }

        @Override
        public void stop() throws IOException, DeviceException {
            write("K");
        }

        @Override
        public String getName() {
            return String.format("%s Axis", identifier);
        }

        @Override
        public void setEncoderEnabled(boolean enabled) throws IOException, DeviceException {
            write("ENCODER,%s,%d", identifier, enabled ? 1 : 0);
        }

        @Override
        public boolean isEncoderEnabled() throws IOException, DeviceException {

            switch (queryInt("ENCODER,%s", identifier)) {

                case 0:
                    return false;

                case 1:
                    return true;

                default:
                    throw new IOException("Invalid response from instrument.");

            }

        }

        @Override
        public void setBacklashEnabled(boolean enabled) throws IOException, DeviceException {
            write("BLSH,%d", enabled ? 1 : 0);
        }

        @Override
        public boolean isBacklashEnabled() throws IOException, DeviceException {
            return querySplitInt("BLSH", ",")[0] == 1;
        }

        @Override
        public void setBacklashSteps(int steps) throws IOException, DeviceException {
            write("BLSH,%d,%d", isBacklashEnabled() ? 1 : 0, steps);
        }

        @Override
        public int getBacklashSteps() throws IOException, DeviceException {
            return querySplitInt("BLSH", ",")[1];
        }

    }

}
