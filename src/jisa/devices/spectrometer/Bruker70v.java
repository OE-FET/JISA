package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.addresses.TCPIPAddress;
import jisa.visa.DDEDevice;
import jisa.devices.interfaces.FTIR;

public class Bruker70v extends DDEDevice implements FTIR{

    public Bruker70v(TCPIPAddress address, int timeout) throws Exception {
        super(address, "Opus", "System", timeout);
    }

    public Bruker70v(Address address) throws Exception {
        super(address);
    }

    public static String getDescription() {
        return "Bruker 70v FTIR";
    }

    public String takeScan(String scan_params) throws Exception{
        return super.sendRequest("COMMAND_LINE MeasureSample(0, {" + scan_params + "});");
    }

    public String sendBenchCommand(String bench_command) throws Exception{
        return super.sendRequest("COMMAND_LINE SendCommand(0, {" + bench_command + "});");
    }
}
