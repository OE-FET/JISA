package jisa.devices.source;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface FSource extends Instrument {

    /**
     * Sets the frequency to output from this frequency source.
     *
     * @param frequency Frequency, in Hz
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setFrequency(double frequency) throws IOException, DeviceException;

    /**
     * Sets the RMS amplitude of the output of this frequency source.
     *
     * @param amplitude RMS amplitude, in Volts or Amps.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setAmplitude(double amplitude) throws IOException, DeviceException;

    /**
     * Sets the phase offset of the output of this frequency source.
     *
     * @param phase Phase offset, in degrees.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void setPhase(double phase) throws IOException, DeviceException;

    /**
     * Returns the frequency this source is set to output.
     *
     * @return The set output frequency, in Hz.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getFrequency() throws IOException, DeviceException;

    /**
     * Returns the RMS amplitude this source is set to output.
     *
     * @return The set output RMS amplitude, in Volts (voltage) or Amps (current).
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getAmplitude() throws IOException, DeviceException;


    /**
     * Returns the phase offset of the output of this frequency source.
     *
     * @return Phase offset, in degrees.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    double getPhase() throws IOException, DeviceException;


    default ParameterList parameters(Class<?> target) {

        ParameterList parameters = new ParameterList();

        parameters.addValue("Frequency [Hz]", this::getFrequency, 100.0, this::setFrequency);
        parameters.addValue("RMS Amplitude [V or A]", this::getAmplitude, 1.0, this::setAmplitude);

        return parameters;

    }

}
