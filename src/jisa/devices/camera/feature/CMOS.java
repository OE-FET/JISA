package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public interface CMOS extends Feature {

    /**
     * Returns whether the CMOS electronic shutter is rolling or global.
     *
     * @return Rolling?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isRollingElectronicShutterEnabled() throws IOException, DeviceException;

    /**
     * Sets whether the CMOS electronic shutter should be rolling or global.
     *
     * @param enabled Rolling?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setRollingElectronicShutterEnabled(boolean enabled) throws IOException, DeviceException;

    /**
     * Returns the rate at which the camera reads pixels for each frame, in Hz.
     * @return Pixel readout rate, in Hz
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    double getPixelReadoutRate() throws IOException, DeviceException;

    /**
     * Returns a list of all available pixel readout rates for this camera (in Hz).
     *
     * @return Available readout rates, in Hz
     */
    List<Double> getPixelReadoutRates();

    /**
     * Sets the rate at which the camera reads pixels (choosing the available option closest to that specified), in Hz.
     *
     * @param rate Pixel readout rate, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setPixelReadoutRate(double rate) throws IOException, DeviceException;

    static void addParameters(CMOS inst, Class<?> target, ParameterList parameters) {

        List<Double> rates = inst.getPixelReadoutRates();
        List<String> units = List.of("Hz", "KHz", "MHz", "GHz", "THz");
        List<String> names = rates.stream().map(v -> {
            int base3 = (int) (Math.log10(v) / 3.0);
            return String.format("%.01f %s", v / Math.pow(10, base3 * 3), units.get(base3));
        }).collect(Collectors.toList());

        parameters.addChoice("Pixel Readout Rate", () -> names.get(rates.indexOf(inst.getPixelReadoutRate())), names.get(0), v -> inst.setPixelReadoutRate(rates.get(names.indexOf(v))), names.toArray(String[]::new));

        parameters.addValue("Rolling Electronic Shutter", inst::isRollingElectronicShutterEnabled, false, inst::setRollingElectronicShutterEnabled);


    }

}
