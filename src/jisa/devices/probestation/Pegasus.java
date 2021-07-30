package jisa.devices.probestation;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.ProbeStation;
import jisa.visa.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;

public class Pegasus extends VISADevice implements ProbeStation {

    private static final String TERMINATOR = "\n";

    public static String getDescription() {
        return "Pegasus Probe Station";
    }

    public Pegasus(Address address) throws IOException, DeviceException {

        super(address);
        setSerialParameters(38400, 7, Connection.Parity.EVEN, Connection.StopBits.TWO, Connection.Flow.NONE);
        setEOI(false);
        setWriteTerminator(TERMINATOR);
        setReadTerminator(TERMINATOR);
        addAutoRemove(TERMINATOR);

        if (!getIDN().toLowerCase().contains("pegasus")) {
            throw new DeviceException("Instrument at \"%s\" is not a Pegasus Probe Station!", address.toString());
        }

        slowQuery("LDI");
        slowQuery("LDC");

    }

    protected synchronized String slowQuery(String message, Object... params) throws IOException, DeviceException {

        write(message, params);

        String response  = null;
        long   timeStart = System.currentTimeMillis();

        do {

            try {
                response = read();
            } catch (IOException ignored) {}

        } while (response == null && (System.currentTimeMillis() - timeStart) < 60000);

        if (response == null) {
            throw new IOException("Slow query timed-out (> 60 seconds).");
        }

        checkDefaultResponse(response);

        return response;

    }

    protected void checkDefaultResponse(String str) throws DeviceException {

        if (str.contains("INF 003")) {
            throw new DeviceException("Attempt to move in X or Y outside the current area");
        } else if (str.contains("INF 008")) {
            throw new DeviceException("Unrecognised prober command");
        } else if (str.contains("INF 009")) {
            throw new DeviceException("X increment or Y increment is zero (therefore NXT mode cannot be used)");
        } else if (str.contains("INF 010")) {
            throw new DeviceException("Attempt to move X, Y or Theta with the Chuck raised");
        } else if (!str.contains("INF 000")) {
            throw new DeviceException("Error was raised (see manual): \"%s\"", str);
        }

    }

    @Override
    public void setXPosition(double xposition) throws IOException, DeviceException {
        if(!isLocked() && !getStatus().isLiftedGross) {
            slowQuery("GTS X,%d", (int) Math.round(xposition * 1e6));
        }
    }

    @Override
    public void setYPosition(double yposition) throws IOException, DeviceException {
        if(!isLocked() && !getStatus().isLiftedGross) {
            slowQuery("GTS Y,%d", (int) Math.round(yposition * 1e6));
        }
    }

    protected double parsePosition(String response) throws IOException {
        return parsePosition(response, 1e6);
    }

    protected double parsePosition(String response, double divisor) throws IOException {
        try {
            return Double.parseDouble(response.split(",", 2)[1]) / divisor;
        } catch (Throwable e) {
            throw new IOException("Improperly formatted response from Pegasus.");
        }
    }

    @Override
    public double getXPosition() throws IOException {
        return parsePosition(query("PSS X"));
    }

    @Override
    public void continMovement(String axis,double velocityPercentage) throws IOException, DeviceException {
        if(!isLocked() && !getStatus().isLiftedGross){
            slowQuery("CMOV %s,%f,",axis,velocityPercentage);
        }
    }


    @Override
    public double getYPosition() throws IOException {
        return parsePosition(query("PSS Y"));
    }

    @Override
    public void setXYPosition(double xposition, double yposition) throws IOException, DeviceException {
        slowQuery("GTS XY,%d,%d", (int) (xposition * 1e6), (int) (yposition * 1e6));
    }


    @Override
    public void setZPosition(double position) throws IOException, DeviceException {
            slowQuery("WKGM %d", (int) (position * 1e6));
            slowQuery("GDW");
            slowQuery("GUP");
    }

    @Override
    public void setGrossUpDistance(double position) throws IOException, DeviceException {
        slowQuery("WKGM %d", (int) (position * 1e6));

    }
    @Override
    public void setGrossUp(boolean locked) throws IOException, DeviceException {
        if (locked) {
            slowQuery("GUP");
        } else {
            slowQuery("GDW");
        }
    }
    @Override
    public boolean isGrossLocked() throws IOException {
        return getStatus().isLiftedGross;
    }

    @Override
    public double getGrossUpDistance() throws IOException, DeviceException {
        return 0.0;
    }


    @Override
    public double getZPosition() throws IOException {
        return parsePosition(query("PSS Z"));
    }

    public void setXYSpeed(double speed) {

    }

    public double getXYSpeed() {
        return 0.0;
    }

    public void setZSpeed(double speed) {

    }

    public double getZSpeed() {
        return 0.0;
    }

    @Override
    public void setRotation(double theta) throws IOException, DeviceException {
        slowQuery("GTS C,%d", (int) (theta * 1e3));
    }

    @Override
    public double getRotation() throws IOException, DeviceException {
        return parsePosition(query("PSS C"), 1e3);
    }

    @Override
    public String getIDN() throws IOException {
        return query("GID");
    }


    @Override
    public void setLockDistance(double distance) throws IOException, DeviceException {
        String str = query("WKFM %d", (int) (distance * 1e6));
        checkDefaultResponse(str);
    }

    @Override
    public double getLockDistance() throws IOException {
        String   str        = query("RKFM");
        String[] parts      = str.split(" ", 2);
        double   numberOnly = Double.parseDouble(parts[1]);
        return numberOnly / 1e6;
    }

    public Status getStatus() throws IOException {
        return new Status(query("STA"));
    }




    @Override
    public void setLocked(boolean locked) throws IOException, DeviceException {
        if (locked) {
            slowQuery("CUP");
        } else {
            slowQuery("CDW");
        }

    }

    @Override
    public boolean isLocked() throws IOException {
        return getStatus().isLiftedFine;
    }

    @Override
    public void setLightOn(boolean lightOn) throws IOException, DeviceException {
        if(lightOn) slowQuery("LI1");
        else slowQuery("LI0");
    }

    @Override
    public boolean getLightOn() throws IOException{
        return false;
    }



    public static class Status {

        public final boolean isFault1;
        public final boolean isFault2;
        public final boolean isLiftedGross;
        public final boolean isMoving;
        public final boolean isLiftedFine;
        public final boolean isEdgeSensorOpen;
        public final boolean isSRQ;
        public final boolean isFaultCondition;

        public Status(String response) {

            int status = Integer.parseInt(response.substring(0, 2));
            isFault1         = (status & 1) != 0;
            isFault2         = (status & 2) != 0;
            isLiftedGross    = (status & 4) != 0;
            isMoving         = (status & 8) != 0;
            isLiftedFine     = (status & 16) != 0;
            isEdgeSensorOpen = (status & 32) != 0;
            isSRQ            = (status & 64) != 0;
            isFaultCondition = (status & 128) != 0;

        }

    }

}

