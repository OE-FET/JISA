package jisa.devices.interfaces;

public interface Spectrometer extends Instrument {

    // Potentially add more general spectrometer methods?

    String getDescription();

    String takeScan(String[] scan_params) throws Exception;
}
