package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.features.Feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public interface KineticSeries<F extends Frame> extends Feature {

    /**
     * Initiates a tightly time-controlled series of captures, returning a FrameQueue which will be populated by each incoming frame as it arrives.
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
    FrameQueue<F> startKineticFrameSeries(int frameCount, int accPerFrame, double frameCycle, double accCycle) throws IOException, DeviceException, TimeoutException, InterruptedException;

    /**
     * Performs a tightly time-controlled series of captures, returning a list containing all acquired frames once completed.
     *
     * @param frameCount  The total number of frames to acquire
     * @param accPerFrame The number of accumulations per frame
     * @param frameCycle  The amount of time, in seconds, from the start of one frame to the next
     * @param accCycle    The amount of time, in seconds, from the start of each accumulation within a frame to the next
     * @param timeoutPerFrameMS   The amount of time, in milliseconds, to wait for each frame before giving up and throwing a TimeoutException
     *
     * @return List of captures frames.
     *
     * @throws IOException          Upon communications error.
     * @throws DeviceException      Upon device error.
     * @throws TimeoutException     Upon operation timing out before completion.
     * @throws InterruptedException Upon operation being interrupted before completion.
     */
    default List<F> getKineticFrameSeries(int frameCount, int accPerFrame, double frameCycle, double accCycle, int timeoutPerFrameMS) throws IOException, DeviceException, TimeoutException, InterruptedException {

        List<F>       list  = new ArrayList<>(frameCount);
        FrameQueue<F> queue = startKineticFrameSeries(frameCount, accPerFrame, frameCycle, accCycle);

        for (int i = 0; i < frameCount; i++) {
            list.add(queue.nextFrame(timeoutPerFrameMS));
        }

        return list;

    }

}
