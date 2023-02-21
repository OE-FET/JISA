package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.addresses.LXIAddress;
import jisa.devices.interfaces.Spectrometer;
import jisa.visa.DDEDevice;

public class Bruker70v extends DDEDevice implements Spectrometer {

    public Bruker70v(LXIAddress address, int timeout) throws Exception {
        super(address, "Opus", "System", timeout);
    }

    public Bruker70v(Address address) throws Exception {
        super(address);
    }

    public String getDescription() {
        return "Bruker 70v FTIR";
    }

    //String exp_file, String sample_name, String save_path, int num_scans
    public String takeScan(String[] scan_params) throws Exception {
        String exp_file = scan_params[0];
        String sample_name = scan_params[1];
        String save_path = scan_params[2];
        String num_scans = scan_params[3];
        String scan_type = scan_params[4];

        String response = super.sendRequest("COMMAND_LINE MeasureSample(0, {EXP='" + exp_file
                + "', XPP='G:\\\\User XPM files', SNM='" + sample_name
                + "', PTH='" + save_path + "', NSS='" + num_scans + "'});");
        String[] split = response.split("\n");

        //If scan fails, rerun it
        if (split.length == 2) {
            System.out.println("OPUS did not respond properly to scan command: ");
            System.out.println(response);
            String unload = super.sendRequest("UNLOAD_FILE " + save_path + sample_name + ".0");
            System.out.println(unload);
            return takeScan(scan_params);
        }
        else {
            String response2 = "";
            String response3 = "";

            try {
                response2 = super.sendRequest("COMMAND_LINE SaveAs ([<" + split[3] + ">:" + scan_type + "], {DAP='" + save_path + "', OEX='1', SAN='"
                        + sample_name + ".dpt', COF=64});");

                response3 = super.sendRequest("UNLOAD_FILE " + split[3]);
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("Scan Failed!");
            }

            return response + "\n" + response2 + "\n" + response3;
        }

    }

    public String sendBenchCommand(String bench_command) throws Exception{
        return super.sendRequest("COMMAND_LINE SendCommand(0, {" + bench_command + "});");
    }

    public String loadReference(String file_path, String file_name) throws Exception {
        String load_str = super.sendRequest("COMMAND_LINE Load(0, {DAP=\'" + file_path + "\', DAF=\'" + file_name + "\'});");
        String[] split = load_str.split("\n");

        return super.sendRequest("COMMAND_LINE LoadReference([<" + split[3] + ">:ScRf], { });");
    }

    public String takeReference(String exp_file, String save_path, int num_scans) throws Exception{
        String response = super.sendRequest("COMMAND_LINE MeasureReference(0, {EXP='" + exp_file
                + "', XPP='G:\\\\User XPM files', PTH='" + save_path + "', NSR='" + num_scans + "'});");

        String[] split = response.split("\n");

        return response;
    }

    @Override
    public String getName() {
        return "Bruker 70v FTIR";
    }
}
