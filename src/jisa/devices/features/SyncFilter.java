package jisa.devices.features;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface SyncFilter extends Feature {

    static void addParameters(SyncFilter instrument, Class<?> target, ParameterList parameters) {

        try {
            parameters.addValue("Sync Filter", instrument.isSyncFilterEnabled(), instrument::setSyncFilterEnabled);
        } catch (Exception e) {
            parameters.addValue("Sync Filter", false, instrument::setSyncFilterEnabled);
        }

    }

    void setSyncFilterEnabled(boolean enabled) throws IOException, DeviceException;

    boolean isSyncFilterEnabled() throws IOException, DeviceException;

}
