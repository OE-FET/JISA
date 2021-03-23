package jisa.devices.interfaces;

public interface FTIR extends Instrument {

    public static String getDescription() {
        return "Fourier Transform Infrared (FTIR) Spectrometer";
    }

    String takeScan(String scan_params) throws Exception;

    String sendBenchCommand(String bench_command) throws Exception;
}
