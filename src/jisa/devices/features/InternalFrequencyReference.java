package jisa.devices.features;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.MultiInstrument;
import jisa.devices.ParameterList;
import jisa.devices.source.FSource;

import java.io.IOException;
import java.util.List;

public interface InternalFrequencyReference<F extends FSource> extends Feature, MultiInstrument {

    static void addParameters(InternalFrequencyReference instrument, Class<?> target, ParameterList parameters) {

        FSource osc = instrument.getInternalReferenceOscillator();

        parameters.addValue("Use Internal Reference", instrument::isInternalReferenceEnabled, false, instrument::setInternalReferenceEnabled);
        parameters.addValue("Internal Oscillator Frequency [Hz]", osc::getFrequency, 100.0, osc::setFrequency);
        parameters.addValue("Internal Oscillator Amplitude [rms V]", osc::getAmplitude, 1.0, osc::setAmplitude);

    }

    /**
     * Sets whether this instrument should use its internal oscillator as its reference signal.
     *
     * @param enabled Use?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    void setInternalReferenceEnabled(boolean enabled) throws IOException, DeviceException;

    /**
     * Returns whether this instrument is using its internal oscillator as its reference signal.
     *
     * @return In use?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    boolean isInternalReferenceEnabled() throws IOException, DeviceException;

    /**
     * Returns an object representing the internal oscillator used for internal frequency referencing in this instrument.
     *
     * @return Internal oscillator
     */
    F getInternalReferenceOscillator();

    default List<? extends Instrument> getSubInstruments() {
        return List.of(getInternalReferenceOscillator());
    }

}
