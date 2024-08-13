package jisa.devices.translator;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.SubInstrument;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class PriorProScanII extends VISADevice implements Stage.Mixed<PriorProScanII.Linear, PriorProScanII.Rotational, PriorProScanII.Translator> {

    public final Linear     X_AXIS;
    public final Linear     Y_AXIS;
    public final Linear     Z_AXIS;
    public final Rotational FILTER_1;
    public final Rotational FILTER_2;

    private final List<Linear>     linearAxes;
    private final List<Rotational> rotationalAxes;

    public PriorProScanII(Address address) throws IOException, DeviceException {

        super(address);

        configSerial(serial -> serial.setSerialParameters(9600, 8));

        setWriteTerminator("\r");
        setReadTerminator("\r");

        addAutoRemove("\r", "\n");

        List<Linear>     linearAxes     = new LinkedList<>();
        List<Rotational> rotationalAxes = new LinkedList<>();

        for (String infoLine : getInfoLines()) {

            if (infoLine.contains("=")) {

                String[] parts = infoLine.split(" = ");

                if (parts[0].equals("STAGE")) {

                    if (!parts[1].equals("NONE")) {
                        linearAxes.add(new Linear("X", queryDouble("RES X")));
                        linearAxes.add(new Linear("Y", queryDouble("RES Y")));
                    }

                } else if (parts[0].equals("FOCUS")) {

                    if (!parts[1].equals("NONE")) {
                        linearAxes.add(new Linear("Z", 1.0));
                    }

                } else if (parts[0].startsWith("FILTER_")) {

                    String id = parts[0].replace("FILTER_", "");

                    if (!parts[1].equals("NONE")) {
                        rotationalAxes.add(new Rotational(id, 1.0));
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

    }

    protected synchronized List<String> getInfoLines() throws IOException {

        manuallyClearReadBuffer();

        List<String> lines = new LinkedList<>();
        String lastLine;
        write("?");

        do {

            lastLine = read().trim();
            lines.add(lastLine);

        } while (!lastLine.equals("END"));

        return lines;

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

    public abstract class Translator implements jisa.devices.translator.Translator, SubInstrument<PriorProScanII> {

    }

    public class Linear extends Translator implements Translator.Linear, SubInstrument<PriorProScanII> {

        private final String identifier;
        private final double resolution;

        public Linear(String identifier, double resolution) throws IOException {
            this.identifier = identifier;
            this.resolution = resolution;
        }

        @Override
        public PriorProScanII getParentInstrument() {
            return PriorProScanII.this;
        }

        @Override
        public void setPosition(double position) throws IOException, DeviceException {
            write("G%s %d", identifier, Math.round(position / resolution));
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
            return 0;
        }

        @Override
        public void setSpeed(double speed) throws IOException, DeviceException {

        }

        @Override
        public double getSpeed() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public double getMinSpeed() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public double getMaxSpeed() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public void moveBy(double distance) throws IOException, DeviceException {

        }

        @Override
        public void moveToHome() throws IOException, DeviceException {

        }

        @Override
        public void setLocked(boolean locked) throws IOException, DeviceException {

        }

        @Override
        public boolean isLocked() throws IOException, DeviceException {
            return false;
        }

        @Override
        public boolean isMoving() throws IOException, DeviceException {
            return false;
        }

        @Override
        public void stop() throws IOException, DeviceException {

        }

        @Override
        public String getName() {
            return String.format("%s Axis", identifier);
        }
    }

    public class Rotational extends Translator implements Translator.Rotational, SubInstrument<PriorProScanII> {

        private final String identifier;
        private final double resolution;

        public Rotational(String identifier, double resolution) throws IOException {
            this.identifier = identifier;
            this.resolution = resolution;
        }

        @Override
        public PriorProScanII getParentInstrument() {
            return PriorProScanII.this;
        }

        @Override
        public void setPosition(double position) throws IOException, DeviceException {

        }

        @Override
        public double getPosition() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public double getMinPosition() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public double getMaxPosition() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public void setSpeed(double speed) throws IOException, DeviceException {

        }

        @Override
        public double getSpeed() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public double getMinSpeed() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public double getMaxSpeed() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public void moveBy(double distance) throws IOException, DeviceException {

        }

        @Override
        public void moveToHome() throws IOException, DeviceException {

        }

        @Override
        public void setLocked(boolean locked) throws IOException, DeviceException {

        }

        @Override
        public boolean isLocked() throws IOException, DeviceException {
            return false;
        }

        @Override
        public boolean isMoving() throws IOException, DeviceException {
            return false;
        }

        @Override
        public void stop() throws IOException, DeviceException {

        }

        @Override
        public String getName() {
            return "";
        }
    }

}
