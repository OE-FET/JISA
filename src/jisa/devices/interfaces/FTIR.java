package jisa.devices.interfaces;

public interface FTIR extends Instrument {

    public static String getDescription() {
        return "Fourier Transform Infrared (FTIR) Spectrometer";
    }

    String takeScan(String exp_file, String sample_name, String save_path, int num_scans) throws Exception;

    String sendBenchCommand(String bench_command) throws Exception;

    String loadReference(String file_path, String file_name) throws Exception;
}
