package jisa.devices.mux;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.MultiInstrument;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public interface MultiMultiplexer<MuxChannel extends Multiplexer> extends Instrument, MultiInstrument {

    /**
     * Returns a list of the multiplexer channels contained in this instrument.
     *
     * @return List of multiplexers
     */
    List<MuxChannel> getMultiplexers();

    default MuxChannel getMultiplexer(int index) {
        return getMultiplexers().get(index);
    }

    default MuxChannel getMultiplexer(String name) {

        return getMultiplexers().stream()
                                .filter(m -> m.getName().equalsIgnoreCase(name))
                                .findFirst()
                                .orElseThrow(() -> new NoSuchElementException("No multiplexer found with name " + name));

    }

    default void setRoutes(int index) throws IOException, DeviceException {

        for (MuxChannel multiplexer : getMultiplexers()) {
            multiplexer.setRoute(index);
        }

    }

    default void setRoutes(int... indices) throws IOException, DeviceException {

        List<MuxChannel> multiplexers = getMultiplexers();

        if (indices.length != multiplexers.size()) {
            throw new IllegalArgumentException("Index array length does not match number of multiplexers");
        }

        for (int i = 0; i < indices.length; i++) {
            multiplexers.get(i).setRoute(indices[i]);
        }

    }

    default Map<MuxChannel, Integer> getRoutes() {

        return getMultiplexers().stream().collect(Collectors.toMap(
            m -> m,
            m -> {
                try {
                    return m.getRoute();
                } catch (IOException | DeviceException e) {
                    return 0;
                }
            }
        ));

    }

    default List<? extends Instrument> getSubInstruments() {
        return getMultiplexers();
    }

}
