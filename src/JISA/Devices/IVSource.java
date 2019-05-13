package JISA.Devices;

import JISA.Enums.Source;

import java.io.IOException;

public interface IVSource extends ISource, VSource {

    Source getSource() throws IOException, DeviceException;

    void setSource(Source source) throws IOException, DeviceException;

}
