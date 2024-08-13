package jisa.devices.mux;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;

import java.io.IOException;

/**
 * Instrument interface for multiplexers
 */
public interface Multiplexer extends Instrument {

    /**
     * Returns the total number of possible routes/channels that this multiplexer can select.
     *
     * @return Number of routes.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility error
     */
    int getNumRoutes() throws IOException, DeviceException;

    /**
     * Returns the index of the current selected route/channel.
     *
     * @return Current route/channel.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility error
     */
    int getRoute() throws IOException, DeviceException;

    /**
     * Sets which route/channel the multiplexer should select.
     *
     * @param route Route/channel to select.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility error
     */
    void setRoute(int route) throws IOException, DeviceException;

}
