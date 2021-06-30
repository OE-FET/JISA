package jisa.devices.probestation;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.ProbeStation;
import jisa.visa.VISADevice;

import java.io.IOException;

public class Pegasus extends VISADevice implements ProbeStation {
    private static final String          TERMINATOR                    = "\n";

    public static String getDescription() {
        return "Pegasus Probe Station";
    }

    //todo: adapt constructor??

    public Pegasus(Address address) throws IOException, DeviceException {

        super(address);



        setEOI(false);
        setWriteTerminator(TERMINATOR);
        setReadTerminator(TERMINATOR);
        addAutoRemove("\n");

        String idn = getModel();
        /*
        if (!idn.contains("Pegasus")) {
            throw new DeviceException("Instrument at \"%s\" is not a Pegasus Probe Station!", address.toString());
        }
        */

    }



    @Override
    public void setXPosition(double xposition) throws IOException, DeviceException{
        String str = query("GTS_X,%f",xposition);
    }

    @Override
    public void setYPosition(double yposition) throws IOException, DeviceException{
        String str = query("GTS_Y,%f",yposition);
    }

    @Override
    public double getXPosition() throws IOException, DeviceException{
        String str = query("PSS_X");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly/1000/1000;
    }

    @Override
    public double getYPosition() throws IOException, DeviceException{
        String str = query("PSS_Y");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly/1000/1000;
    }

    @Override
    public void setXYPosition(double xposition, double yposition) throws IOException, DeviceException{
        String str = query("GTS_XY,%f,%f",xposition,yposition);
    }

    @Override
    public void setZPosition(double zposition) throws IOException, DeviceException{
        String str = query("GTS_Y,%f",zposition);
    }

    @Override
    public double getZPosition() throws IOException, DeviceException{
        String str = query("PSS_Z");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly/1000/1000;
    }

    public void setXYSpeed(double speed) throws IOException, DeviceException{
        //TODO
    }

    public double getXYSpeed() throws IOException, DeviceException{
        //TODO
        return 0.0;
    }

    public void setZSpeed(double speed) throws IOException, DeviceException{
        //TODO
    }

    public double getZSpeed() throws IOException, DeviceException{
        //TODO
        return 0.0;
    }

    @Override
    public void setRotation(double theta) throws IOException, DeviceException{
        String str = query("GTS_C,%f",theta*1000);
    }
    @Override
    public double getRotation() throws IOException, DeviceException{
        String str = query("PSS_C");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly / 1000;
    }

    @Override
    public String getModel() throws IOException, DeviceException{
        return query("GID");
    }

    @Override
    public void setLockDistance(double distance) throws IOException, DeviceException{
        String str = query("PSS_Z,%f",distance*1000);
    }

    @Override
    public double getLockDistance() throws IOException, DeviceException{
        String str = query("PSS_Z");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly/1000/1000;
    }
    @Override
    public void setLocked(boolean locked) throws IOException, DeviceException{
        //todo
    }

    @Override
    public boolean isLocked() throws IOException, DeviceException{
        //todo
        return false;
    }

}

