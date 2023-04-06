package jisa.experiment;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure to hold I-V data from a multi-channel sweep
 */
public class MCIVPoint {

    private final HashMap<Integer, IVPoint> channels = new HashMap<>();

    /**
     * Add IVPoint for the given channel.
     *
     * @param channel The channel
     * @param point   Data point
     */
    public void addChannel(int channel, IVPoint point) {
        channels.put(channel, point);
    }

    /**
     * Add a map of channel number and IVPoint objects.
     *
     * @param c Map
     */
    public void addAll(Map<Integer, IVPoint> c) {
        channels.putAll(c);
    }

    /**
     * Returns the IVPoint corresponding to the given channel number. Returns null if it does not exist.
     *
     * @param channel Channel number
     *
     * @return IVPoint of channel
     */
    public IVPoint getChannel(int channel) {
        return channels.getOrDefault(channel, null);
    }

}