package JISA.Experiment;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure to hold I-V data from a multi-channel sweep
 */
public class MCIVPoint {

    private HashMap<Integer, IVPoint> channels = new HashMap<>();

    public void addChannel(int channel, IVPoint point) {
        channels.put(channel, point);
    }

    public void addAll(Map<Integer, IVPoint> c) {
        channels.putAll(c);
    }

    public IVPoint getChannel(int channel) {
        return channels.getOrDefault(channel, null);
    }

}