package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Spectrometer;

import java.io.IOException;

public class AgilentCary6000i implements Spectrometer {

    public AgilentCary6000i(Address address) throws Exception {

    }

    @Override
    public String takeScan(String exp_file, String sample_name, String save_path, int num_scans) throws Exception {
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
    public String takeReference(String exp_file, String save_path, int num_scans) throws Exception {
        return null;
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "Agilent Cary6000i UV-vis Spectrometer";
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }
}
