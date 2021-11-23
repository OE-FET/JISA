package jisa.devices.interfaces;

public interface UVvis extends Instrument{
    public static String getDescription() {
        return "UV-vis Spectrometer";
    }

    String takeScan(String sample_name) throws Exception;

    String sendBenchCommand(String bench_command) throws Exception;

    String loadReference(String file_path, String file_name) throws Exception;

    String takeReference() throws Exception;
}
