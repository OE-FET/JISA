package jisa.devices.translator.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;

public interface Encoder extends Feature {

    static void addParameters(Encoder instrument, Class<?> target, ParameterList parameters) {
        parameters.addValue("Encoder", instrument::isEncoderEnabled, false, instrument::setEncoderEnabled);
    }

    void setEncoderEnabled(boolean enabled) throws IOException, DeviceException;

    boolean isEncoderEnabled() throws IOException, DeviceException;

}
