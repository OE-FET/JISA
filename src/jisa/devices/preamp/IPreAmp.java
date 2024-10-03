package jisa.devices.preamp;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface IPreAmp extends Instrument {

    static String getDescription() {
        return "Current Pre-Amplifier";
    }

    static void addParameters(IPreAmp inst, Class target, ParameterList parameters) {

        parameters.addValue("Gain [V/A]", inst::getGain, 1.0, inst::setGain);
        parameters.addValue("Filter Roll-Off [dB/oct]", inst::getFilterRollOff, 6.0, inst::setFilterRollOff);
        parameters.addOptional("Low-Pass Filter [Hz]", false, 3.0, v -> inst.setLowPassFrequency(0.0), inst::setLowPassFrequency);
        parameters.addOptional("High-Pass Filter [Hz]", false, 3.0, v -> inst.setHighPassFrequency(0.0), inst::setHighPassFrequency);

    }

    /**
     * Sets the gain to the given value, or as close to it as possible.
     *
     * @param gain Gain value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setGain(double gain) throws IOException, DeviceException;

    /**
     * Sets the frequency cut-off for the pre-amp's high-pass filter. Disabled if set to zero.
     *
     * @param frequency Frequency cut-off, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setHighPassFrequency(double frequency) throws IOException, DeviceException;

    /**
     * Returns the frequency cut-off for the pre-amp's high-pass filter. Disabled if set to zero.
     *
     * @return Frequency cut-off, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getHighPassFrequency() throws IOException, DeviceException;

    /**
     * Sets the frequency cut-off for the pre-amp's low-pass filter. Disabled if set to zero.
     *
     * @param frequency Frequency cut-off, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setLowPassFrequency(double frequency) throws IOException, DeviceException;

    /**
     * Returns the frequency cut-off for the pre-amp's low-pass filter. Disabled if set to zero.
     *
     * @return Frequency cut-off, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getLowPassFrequency() throws IOException, DeviceException;

    /**
     * Sets the filter roll-off value in db/decade (or as close to it as one can get).
     *
     * @param dbLevel Roll-off (db/decade)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setFilterRollOff(double dbLevel) throws IOException, DeviceException;

    /**
     * Returns the gain value of the amplifier currently.
     *
     * @return Gain value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getGain() throws IOException, DeviceException;

    /**
     * Returns the filter roll-off value currently being used in db/decade
     *
     * @return Filter roll off (db/decade)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getFilterRollOff() throws IOException, DeviceException;

    boolean isInverting() throws IOException, DeviceException;

    void setInverting(boolean inverting) throws IOException, DeviceException;

}
