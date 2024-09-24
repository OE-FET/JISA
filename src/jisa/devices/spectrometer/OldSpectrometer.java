package jisa.devices.spectrometer;

import jisa.devices.Instrument;
import jisa.devices.ParameterList;

public interface OldSpectrometer extends Instrument {

    // Potentially add more general spectrometer methods?

    String getDescription();

    String takeScan(String[] scan_params) throws Exception;

    void setAccessory(boolean using_accessory);

    static void addParameters(OldSpectrometer inst, Class target, ParameterList parameters) {

        parameters.add(new Parameter<>("Using TM/RF Accessory?", false, inst::setAccessory));

    }

}
