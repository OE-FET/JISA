package jisa.devices.camera;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.camera.frame.FrameQueue;
import jisa.devices.camera.frame.U16Frame;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class NPAdapter<F extends U16Frame, C extends Camera<F>> {

    private final C             camera;
    private final FrameQueue<F> queue;

    public NPAdapter(C camera) {
        this.camera = camera;
        queue       = camera.openFrameQueue(1);
    }

    public synchronized int[][] get_next_frame(long timeout, long discard_frames, boolean assert_live_view, boolean raw) throws InterruptedException, TimeoutException, IOException, DeviceException {

        boolean isAcquiring = camera.isAcquiring();

        assert !assert_live_view || isAcquiring;

        if (isAcquiring) {
            queue.clear();
            return queue.nextFrame(timeout).image();
        } else {
            return camera.getFrame().image();
        }

    }

    public int[][] raw_snapshot() throws IOException, InterruptedException, DeviceException, TimeoutException {
        return camera.getFrame().image();
    }

    public boolean live_view() throws IOException, DeviceException {
        return camera.isAcquiring();
    }

    public void live_view(boolean live_view) throws IOException, DeviceException {

        if (live_view) {
            camera.startAcquisition();
        } else {
            camera.stopAcquisition();
        }

    }

    public List<Instrument.Parameter<?>> getParameters() {
        return camera.getAllParameters(Camera.class);
    }

    public C getCamera() {
        return camera;
    }

}
