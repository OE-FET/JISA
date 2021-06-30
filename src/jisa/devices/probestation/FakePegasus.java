package jisa.devices.probestation;


import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.ProbeStation;

import java.io.IOException;

public class FakePegasus implements ProbeStation {

    private static final String          TERMINATOR                    = "\n";

    public static String getDescription() {
        return "Fake Pegasus Probe Station";
    }

    //todo: adapt constructor??
    public FakePegasus(Address address) throws IOException, DeviceException {
        System.out.println("fake pegasus start");
    }

    /*
    setEOI(false);
    setWriteTerminator(TERMINATOR);
    setReadTerminator(TERMINATOR);
    addAutoRemove("\n");
    */

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "Fake Probe Station";
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public void WritezFineLift(double fineheight) throws IOException, DeviceException{
        System.out.println("WKFM_%f");
    }

    @Override
    public void WritezGrossLift(double grossheight) throws IOException, DeviceException{
        System.out.println("WKGM_%f");
    }

    @Override
    public void ChuckGrossUp() throws IOException, DeviceException{
        System.out.println("GUP ");
    }

    @Override
    public void ChuckGrossDown() throws IOException, DeviceException{
        System.out.println("GDW ");
    }

    @Override
    public void ChuckFineUp() throws IOException, DeviceException{
        System.out.println("CUP ");
    }

    @Override
    public void ChuckFineDown() throws IOException, DeviceException{
        System.out.println("CDW ");
    }

    @Override
    public void setXposition(double xposition) throws IOException, DeviceException{
        System.out.println("GTS_X,%f");
    }

    @Override
    public void setYposition(double yposition) throws IOException, DeviceException{
        System.out.println("GTS_Y,%f");
    }

    @Override
    public double getXposition() throws IOException, DeviceException{
        return 0;

    }

    @Override
    public double getYposition() throws IOException, DeviceException{
        return 0;
    }


    @Override
    public void setAngle(double theta) throws IOException, DeviceException{
        System.out.println("GTS_C,%f");
    }

    @Override
    public String getModel() throws IOException, DeviceException {
        return "Fake Pegasus";
    }

    @Override
    public double getAngle() throws IOException, DeviceException{
        //todo!
        return 0;
    }


}


