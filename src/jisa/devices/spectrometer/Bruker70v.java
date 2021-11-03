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

    public String takeScan(String exp_file, String sample_name, String save_path, int num_scans) throws Exception{
        String response = super.sendRequest("COMMAND_LINE MeasureSample(0, {EXP='" + exp_file
                + "', XPP='G:\\\\User XPM files', SNM='" + sample_name
                + "', PTH='" + save_path + "', NSS='" + num_scans + "'});");
        String[] split = response.split("\n");

        int i = 1;
        for (String str : split) {
            System.out.println("LINE " + i + ": " + str);
            i++;
        }

//        String response2 = super.sendRequest("COMMAND_LINE SaveAs ([<\"" + save_path + sample_name +
//                ".0\" 1>:AB], {DAP='" + save_path + "', OEX='1', SAN='" + sample_name + ".dpt', COF=64});");
        String response2 = super.sendRequest("COMMAND_LINE SaveAs ([<" + split[3] + ">:AB], {DAP='" + save_path + "', OEX='1', SAN='"
                + sample_name + ".dpt', COF=64});");

        String response3 = super.sendRequest("UNLOAD_FILE " + split[3]);

        return response + "\n" + response2 + "\n" + response3;
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
}
