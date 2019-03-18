package JISA.Devices;

import JISA.Addresses.Address;
import JISA.VISA.VISADevice;

import java.io.IOException;

public abstract class VPreAmp extends VISADevice {

    public VPreAmp(Address address) throws IOException {
        super(address);
    }

    /**
     * Sets the gain to the given value, or as close to it as possible.
     *
     * @param gain Gain value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setGain(double gain) throws IOException, DeviceException;

    /**
     * Sets the source terminal to use, either A, B or DIFF (for A - B).
     *
     * @param source Source terminal configuration
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setSource(Source source) throws IOException, DeviceException;

    /**
     * Sets the coupling mode of the pre-amplifier.
     *
     * @param mode Coupling mode: GROUND, DC or AC.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setCoupling(Coupling mode) throws IOException, DeviceException;

    /**
     * Sets the filter mode of the pre-amplifier.
     *
     * @param mode Filter-mode: NONE, LOW_PASS, HIGH_PASS or BAND_PASS
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setFilterMode(Filter mode) throws IOException, DeviceException;

    /**
     * Sets the filter roll-off value in db/decade (or as close to it as one can get).
     *
     * @param dbLevel Roll-off (db/decade)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setFilterRollOff(double dbLevel) throws IOException, DeviceException;

    /**
     * Sets the high frequency value of the filter to the given value or as close as possible.
     *
     * @param frequency High frequency value, in Hz
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setFilterHighFrequency(double frequency) throws IOException, DeviceException;

    /**
     * Sets the low frequency value of the filter to the given value or as close as possible.
     *
     * @param frequency Low frequency value, in Hz
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setFilterLowFrequency(double frequency) throws IOException, DeviceException;

    /**
     * Returns the gain value of the amplifier currently.
     *
     * @return Gain value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getGain() throws IOException, DeviceException;

    /**
     * Returns the source terminal configuration currently being used.
     *
     * @return Source terminal configuration
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract Source getSource() throws IOException, DeviceException;

    /**
     * Returns the coupling configuration currently being used.
     *
     * @return Coupling configuration
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract Coupling getCoupling() throws IOException, DeviceException;

    /**
     * Returns the filter mode currently being used.
     *
     * @return Filter mode
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract Filter getFilterMode() throws IOException, DeviceException;

    /**
     * Returns the filter roll-off value currently being used in db/decade
     *
     * @return Filter roll off (db/decade)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getFilterRollOff() throws IOException, DeviceException;

    /**
     * Returns the high frequency value currently being used by the filter.
     *
     * @return High frequency value, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getFilterHighFrequency() throws IOException, DeviceException;

    /**
     * Returns the low frequency value currently being used by the filter.
     *
     * @return Low frequency value, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
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
