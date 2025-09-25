package jisa.devices.translator.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface Notched extends Feature {

    static void addParameters(Notched instrument, Class<?> target, ParameterList parameters) {

        parameters.addValue("Notch Count", instrument::getNotchCount, 0, instrument::setNotchCount);
        parameters.addValue("Notch Spacing", instrument::getNotchSpacing, 0.0, instrument::setNotchSpacing);

    }

    /**
     * Returns how many discrete notches there are on this translator.
     *
     * @return Number of discrete notches.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    int getNotchCount() throws IOException, DeviceException;

    /**
     * Sets the number of discrete notches there should be on this translator.
     *
     * @param notchCount Number of discrete notches.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    void setNotchCount(int notchCount) throws IOException, DeviceException;

    /**
     * Returns the index of the notch that the translator is currently at.
     *
     * @return Index of current notch.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    int getNotch() throws IOException, DeviceException;

    /**
     * Instructs the translator to move to the notch with the specified index.
     *
     * @param index Index of notch to move to.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    void setNotch(int index) throws IOException, DeviceException;

    /**
     * Returns the spacing between notches on the translator axis.
     *
     * @return Spacing, in metres or degrees.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    double getNotchSpacing() throws IOException, DeviceException;

    /**
     * Sets the spacing between notches on the translator axis.
     *
     * @param notchSpacing Spacing, in metres or degrees.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device/compatibility error.
     */
    void setNotchSpacing(double notchSpacing) throws IOException, DeviceException;

}
