package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.camera.frame.Frame;
import jisa.devices.features.Feature;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface KineticSeries<F extends Frame> extends Feature {

    /**
     * Performs a tightly time-controlled series of captures, returning each resulting frame in a list.
     *
     * @param frameCount  The total number of frames to acquire
     * @param accPerFrame The number of accumulations per frame
     * @param frameCycle  The amount of time, in seconds, from the start of one frame to the next
     * @param accCycle    The amount of time, in seconds, from the start of each accumulation within a frame to the next
     *
     * @return List of captures frames.
     *
     * @throws IOException          Upon communications error.
     * @throws DeviceException      Upon device error.
     * @throws TimeoutException     Upon operation timing out before completion.
     * @throws InterruptedException Upon operation being interrupted before completion.
     */
    List<F> getKineticFrameSeries(int frameCount, int accPerFrame, double frameCycle, double accCycle) throws IOException, DeviceException, TimeoutException, InterruptedException;

}
