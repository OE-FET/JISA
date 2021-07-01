package jisa.devices.probestation;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.ProbeStation;
import jisa.visa.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;

public class Pegasus extends VISADevice implements ProbeStation {
    private static final String          TERMINATOR                    = "\n";
    private static boolean         UP                    = false;

    public static String getDescription() {
        return "Pegasus Probe Station";
    }

    //todo: adapt constructor??

    public Pegasus(Address address) throws IOException, DeviceException {

        super(address);
        setSerialParameters(38400,7, Connection.Parity.EVEN, Connection.StopBits.TWO, Connection.Flow.NONE);
        setEOI(false);
        setWriteTerminator(TERMINATOR);
        setReadTerminator(TERMINATOR);
        addAutoRemove("\n");

        /*
        String idn = this.getIDN();

        if (!idn.contains("Pegasus")) {
            throw new DeviceException("Instrument at \"%s\" is not a Pegasus Probe Station!", address.toString());
        }
        */
    }

    protected void WaitUntilStopped() throws IOException, DeviceException, InterruptedException {
        while(!Stopped()){
            Thread.sleep(500);
        }
    }

    boolean Stopped() throws IOException {
        return query("STA").charAt(5) == '0';
    }

    void CheckDefaultResponse(String str) throws IOException, DeviceException{
        if(str.contains("INF 003")){
            throw new DeviceException("Attempt to move in X or Y outside the current area");
        }
        else if(str.contains("INF 008")){
            throw new DeviceException("Unrecognised prober command");
        }
        else if(str.contains("INF 009")) {
            throw new DeviceException("X increment or Y increment is zero (therefore NXT mode cannot be used)");
        }
        else if(str.contains("INF 010")){
                throw new DeviceException("Attempt to move X, Y or Theta with the Chuck raised");
        }
        else if(!str.contains("INF 000")){
            throw new DeviceException("Error was raised (see Manual):" + str);
        }


    }

    @Override
    public void setXPosition(double xposition) throws IOException, DeviceException, InterruptedException {
        String str = query("GTS X,%f",xposition*1e6);
        CheckDefaultResponse(str);
        WaitUntilStopped();
    }

    @Override
    public void setYPosition(double yposition) throws IOException, DeviceException, InterruptedException {
        String str = query("GTS Y,%f",yposition*1e6);
        CheckDefaultResponse(str);
        WaitUntilStopped();
    }

    @Override
    public double getXPosition() throws IOException, DeviceException{
        String str = query("PSS X");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly/1e6;
    }

    @Override
    public double getYPosition() throws IOException, DeviceException{
        String str = query("PSS Y");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly/1e6;
    }

    @Override
    public void setXYPosition(double xposition, double yposition) throws IOException, DeviceException, InterruptedException {
        String str = query("GTS XY,%d,%d",(int) (xposition * 1e6), (int) (yposition * 1e6));
        CheckDefaultResponse(str);
        WaitUntilStopped();
    }


    @Override
    public void setZPosition(double position) throws IOException, DeviceException{
        String str = query("WKGM %d",(int) position*1e6);
        CheckDefaultResponse(str);
        str = query("GUP");
        CheckDefaultResponse(str);

    }

    @Override
    public double getZPosition() throws IOException, DeviceException{
        String str = query("PSS Z");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly/1e6;
    }

    public void setXYSpeed(double speed) throws IOException, DeviceException{

    }

    public double getXYSpeed() throws IOException, DeviceException{
        return 0.0;
    }

    public void setZSpeed(double speed) throws IOException, DeviceException{

    }

    public double getZSpeed() throws IOException, DeviceException{
        return 0.0;
    }

    @Override
    public void setRotation(double theta) throws IOException, DeviceException, InterruptedException {
        String str = query("GTS C,%d",(int) (theta*1e3));
        CheckDefaultResponse(str);
        WaitUntilStopped();
    }
    @Override
    public double getRotation() throws IOException, DeviceException{
        String str = query("PSS C");
        String[] parts = str.split(",", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly / 1e3;
    }

    @Override
    public String getIDN() throws IOException{
        return query("GID");
    }


    @Override
    public void setLockDistance(double distance) throws IOException, DeviceException{
        String str = query("WKFM %d",(int) distance*1e6);
        CheckDefaultResponse(str);
    }

    @Override
    public double getLockDistance() throws IOException, DeviceException{
        String str = query("RKFM");
        String[] parts = str.split(" ", 2);
        double numberOnly = Double.parseDouble(parts[1]);
        return numberOnly / 1e6;
    }

    @Override
    public void setLocked(boolean locked) throws IOException, DeviceException{
        String str;
        if(locked){
            str = query("CUP");
            UP = true;
        }
        else{
            str = query("CDW");
            UP = false;
        }
        CheckDefaultResponse(str);
    }

    @Override
    public boolean isLocked() throws IOException, DeviceException{
        return UP;
    }


    @Override
    public void stageSetup(double xcenter, double ycenter, double width, double height) throws IOException, DeviceException{
        String str = query("WRZ 1");
        CheckDefaultResponse(str);
        str = query("PCPP %d,%d",(int) xcenter*1e6,(int) ycenter*1e6);
        CheckDefaultResponse(str);
        str = query("WRX %d",(int) width*1e5);
        CheckDefaultResponse(str);
        str = query("WRY %d",(int) height*1e5);
        CheckDefaultResponse(str);
    }

}

