package jisa.devices.interfaces;

import jisa.devices.DeviceException;
import jisa.experiment.Spectrum;

import java.io.IOException;

public interface Spectrometer extends Instrument {

    public static String getDescription() {
        return "Spectrometer";
    }

    /**
     * Returns the integration time currently being used by the spectrometer when measuring.
     *
     * @return Integration time, in seconds
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    double getIntegrationTime() throws IOException, DeviceException;

    /**
     * Sets the integration time for the spectrometer to use when measuring.
     *
     * @param time Integration time, in seconds
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    void setIntegrationTime(double time) throws IOException, DeviceException;

    /**
     * Returns the minimum wavelength in the spectrometer's current measurement range.
     *
     * @return Minimum wavelength, in metres
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    double getMinFrequency() throws IOException, DeviceException;

    /**
     * Sets the minimum wavelength to use in the spectrometer's measurement range.
     *
     * @param min Minimum wavelength, in metres
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    void setMinFrequency(double min) throws IOException, DeviceException;

    /**
     * Returns the maximum wavelength in the spectrometer's current measurement range.
     *
     * @return Minimum wavelength, in metres
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    double getMaxFrequency() throws IOException, DeviceException;

    /**
     * Sets the maximum wavelength to use in the spectrometer's measurement range.
     *
     * @param max Minimum wavelength, in metres
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    void setMaxFrequency(double max) throws IOException, DeviceException;

    /**
     * Executes a measurement using the currently set parameters, returning the spectral data as a Spectrum object.
     *
     * @return Spectral data
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    Spectrum measureSpectrum() throws IOException, DeviceException;

}
