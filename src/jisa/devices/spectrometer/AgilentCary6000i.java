package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Spectrometer;
import jisa.visa.PseudoDevice;

import java.io.IOException;

public class AgilentCary6000i extends PseudoDevice implements Spectrometer {

    public AgilentCary6000i() throws Exception {
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
}
