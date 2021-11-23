package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.UVvis;

import java.io.IOException;

public class AgilentCary6000i implements UVvis {

    @Override
    public String getIDN() throws IOException, DeviceException {
        return null;
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public String takeScan(String sample_name) throws Exception {
        String script_path = "C:\\Varian\\CaryWinUV\\ADL\\startscan.adl";
        String exe_path = "C:\\Varian\\CaryWinUV\\ADLShell.exe";

        String cmdArr [] = {exe_path, script_path};
        Runtime.getRuntime ().exec (cmdArr);

        return "Scan Launched";
    }

    @Override
    public String sendBenchCommand(String bench_command) throws Exception {
        return null;
    }

    @Override
    public String loadReference(String file_path, String file_name) throws Exception {
        return null;
    }

    @Override
    public String takeReference() throws Exception {
        return null;
    }
}
