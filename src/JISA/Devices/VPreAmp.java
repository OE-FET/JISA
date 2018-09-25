package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.VISA.VISADevice;

import java.io.IOException;

public abstract class VPreAmp extends VISADevice {

    public VPreAmp(InstrumentAddress address) throws IOException {
        super(address);
    }

    public abstract void setGain(double gain) throws IOException, DeviceException;

    public abstract void setSource(Source source) throws IOException, DeviceException;

    public abstract void setCoupling(Coupling mode) throws IOException, DeviceException;

    public abstract void setFilterMode(Filter mode) throws IOException, DeviceException;

    public abstract void setFilterLevel(double dbLevel) throws IOException, DeviceException;

    public abstract void setFilterHighFrequency(double frequency) throws IOException, DeviceException;

    public abstract void setFilterLowFrequency(double frequency) throws IOException, DeviceException;

    public abstract double getGain() throws IOException, DeviceException;

    public abstract Source getSource() throws IOException, DeviceException;

    public abstract Coupling getCoupling() throws IOException, DeviceException;

    public abstract Filter getFilterMode() throws IOException, DeviceException;

    public abstract double getFilterLevel() throws IOException, DeviceException;

    public abstract double getFilterHighFrequency() throws IOException, DeviceException;

    public abstract double getFilterLowFrequency() throws IOException, DeviceException;

    public enum Coupling {
        GROUND,
        DC,
        AC
    }

    public enum Source {
        A,
        B,
        DIFF
    }

    public enum Filter {
        NONE,
        HIGH_PASS,
        LOW_PASS,
        BAND_PASS
    }

}
