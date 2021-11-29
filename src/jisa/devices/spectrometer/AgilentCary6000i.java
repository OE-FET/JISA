package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Spectrometer;
import org.apache.commons.lang3.StringUtils;


import java.io.*;

public class AgilentCary6000i implements Spectrometer {

    public AgilentCary6000i(Address address) throws Exception {

    }

    @Override
    public String takeScan(String file_directory, String sample_name, String scan_file_path, int num_scans) throws Exception {

        String config_path = "C:\\Varian\\CaryWinUV\\ADL\\measurementconfig.cfg";
        File file = new File(config_path);

        // Modifies the file directory and sample name in the config file to match those provided
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String content = br.readLine();

            int dir_start = StringUtils.ordinalIndexOf(content, ",", 10);
            int dir_end = StringUtils.ordinalIndexOf(content, ",", 11);
            int samp_end = StringUtils.ordinalIndexOf(content, ",", 12);

            String old_dir = content.substring(dir_start+1, dir_end);
            String old_samp = content.substring(dir_end+1, samp_end);

            content = content.replace(old_dir, file_directory);
            content = content.replace(old_samp, sample_name);

            FileOutputStream fileOut = new FileOutputStream(file);
            fileOut.write(content.getBytes());
            fileOut.close();
        }

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
