package jisa.devices.interfaces;

public interface Spectrometer extends Instrument {

    //Initial commit for this.  Going to work on making more general for both UV-vis and FTIR

    String takeScan(String exp_file, String sample_name, String save_path, int num_scans) throws Exception;

    String sendBenchCommand(String bench_command) throws Exception;

    String loadReference(String file_path, String file_name) throws Exception;

    String takeReference(String exp_file, String save_path, int num_scans) throws Exception;
}
