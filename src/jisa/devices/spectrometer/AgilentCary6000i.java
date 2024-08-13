package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

public class AgilentCary6000i implements OldSpectrometer {

    public AgilentCary6000i(Address address) throws Exception {

    }

    @Override
    public void setAccessory(boolean using_accessory) {

    }

    public String getDescription() {
        return "Agilent Cary6000i UV-Vis Spectrophotometer";
    }

    @Override
    public String takeScan(String[] scan_params) throws Exception {

        String scan_adl    = scan_params[0];
        String script_path = "C:\\Varian\\CaryWinUV\\ADL\\" + scan_adl;
        String exe_path    = "C:\\Varian\\CaryWinUV\\ADLShell.exe";

        // Runs the runScan.adl script in a minimized window
        String[] cmdArr = {"cmd", "/c", "start", "/min", exe_path, script_path};
        Runtime.getRuntime ().exec (cmdArr);

        return "Scan Launched";
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "Agilent Cary6000i UV-vis Spectrometer";
    }

    @Override
    public String getName() {
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
