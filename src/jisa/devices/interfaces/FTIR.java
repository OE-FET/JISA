package jisa.devices.interfaces;

public interface FTIR extends Instrument {

    public static String getDescription() {
        return "FTIR";
    }

    String takeScan(String scan_params) throws Exception;

    String sendBenchCommand(String bench_command) throws Exception;
}
