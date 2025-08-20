package jisa.devices.features;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface SyncFilter extends Feature {

    static void addParameters(SyncFilter instrument, Class<?> target, ParameterList parameters) {

        parameters.addValue(
            "Sync Filter",
            instrument::isSyncFilterEnabled,
            false,
            instrument::setSyncFilterEnabled
        );

    }

    void setSyncFilterEnabled(boolean enabled) throws IOException, DeviceException;

    boolean isSyncFilterEnabled() throws IOException, DeviceException;

}
