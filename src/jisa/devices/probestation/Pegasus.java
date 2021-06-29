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
        if (!idn.contains("Pegasus")) {
            throw new DeviceException("Instrument at \"%s\" is not a Pegasus Probe Station!", address.toString());
        }
    }

    @Override
    public void WritezFineLift(double fineheight) throws IOException, DeviceException{
        write("WKFM_%f",fineheight);
    }

    @Override
    public void WritezGrossLift(double grossheight) throws IOException, DeviceException{
        write("WKGM_%f",grossheight);
    }

    @Override
    public void ChuckGrossUp() throws IOException, DeviceException{
        write("GUP ");
    }

    @Override
    public void ChuckGrossDown() throws IOException, DeviceException{
        write("GDW ");
    }

    @Override
    public void ChuckFineUp() throws IOException, DeviceException{
        write("CUP ");
    }

    @Override
    public void ChuckFineDown() throws IOException, DeviceException{
        write("CDW ");
    }

    @Override
    public void setXposition(double xposition) throws IOException, DeviceException{
        write("GTS_X,%f",xposition);
    }

    @Override
    public void setYposition(double yposition) throws IOException, DeviceException{
        write("GTS_Y,%f",yposition);
    }

    @Override
    public double getXposition() throws IOException, DeviceException{
        String str = query("PSS_X");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly;
    }

    @Override
    public double getYposition() throws IOException, DeviceException{
        String str = query("PSS_Y");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly;
    }


    @Override
    public void setAngle(double theta) throws IOException, DeviceException{
        write("GTS_C,%f",theta);
    }
    @Override
    public double getAngle() throws IOException, DeviceException{
        String str = query("PSS_C");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly;
    }

    @Override
    public String getModel() throws IOException, DeviceException{
        return query("GID");
    }


}

