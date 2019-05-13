package JISA.Devices;

import java.io.IOException;

public interface Instrument {

    String getIDN() throws IOException;

    void close() throws IOException, DeviceException;

}
