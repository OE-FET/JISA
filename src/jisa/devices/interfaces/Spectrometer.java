package jisa.devices.interfaces;

import jisa.enums.Terminals;

import java.util.LinkedList;
import java.util.List;

public interface Spectrometer extends Instrument {

    // Potentially add more general spectrometer methods?

    String getDescription();

    String takeScan(String[] scan_params) throws Exception;

    void setAccessory(boolean using_accessory);

    @Override
    default List<Parameter<?>> getConfigurationParameters(Class<?> target) {

        List<Parameter<?>> parameters = new LinkedList<>();

        parameters.add(new Parameter<>("Using TM/RF Accessory?", false, this::setAccessory));

        return parameters;

    }

}
