package jisa.devices.spectrometer;

import jisa.devices.Instrument;

import java.util.LinkedList;
import java.util.List;

public interface OldSpectrometer extends Instrument {

    // Potentially add more general spectrometer methods?

    String getDescription();

    String takeScan(String[] scan_params) throws Exception;

    void setAccessory(boolean using_accessory);

    @Override
    default List<Parameter<?>> parameters(Class<?> target) {

        List<Parameter<?>> parameters = new LinkedList<>();

        parameters.add(new Parameter<>("Using TM/RF Accessory?", false, this::setAccessory));

        return parameters;

    }

}
