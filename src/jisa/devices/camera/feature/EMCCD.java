package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;
import java.util.List;

public interface EMCCD extends Feature {

    static void addParameters(EMCCD inst, Class<?> target, ParameterList parameters) {

        if (inst.isEMAvailable()) {

            List<? extends EMGainMode> modes = inst.getEMGainModes();

            if (!modes.isEmpty()) {
                parameters.addChoice("EM-CCD", "Mode", inst::getEMGainMode, EMGainMode.NONE, inst::setEMGainMode, inst.getEMGainModes().toArray(EMGainMode[]::new));
            }

            parameters.addValue("EM-CCD", "Gain", inst::getEMGain, 0, inst::setEMGain);

        }

    }

    /**
     * Returns whether this camera's EMCCD features are available for use.
     *
     * @return EM-CCD features available?
     */
    default boolean isEMAvailable() {
        return true;
    }

    /**
     * Sets the gain to use for this camera's electron-multiplying CCD register.
     *
     * @param gain The gain to set.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device or compatibility error.
     */
    void setEMGain(int gain) throws IOException, DeviceException;

    /**
     * Returns the gain being used by this camera's electron-multiplying CCD register.
     *
     * @return The gain being used.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device or compatibility error.
     */
    int getEMGain() throws IOException, DeviceException;

    /**
     * Sets the mode to be used by this camera's electron-multiplying CCD register.
     *
     * @param mode The mode to use.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device or compatibility error.
     */
    void setEMGainMode(EMGainMode mode) throws IOException, DeviceException;

    /**
     * Returns the mode being used by this camera's electron-multiplying CCD register.
     *
     * @return The mode being used.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device or compatibility error.
     */
    EMGainMode getEMGainMode() throws IOException, DeviceException;

    /**
     * Returns the available modes that this camera can use for it's electron-multiplying CCD register. An empty list
     * indicates that there are no configurable options.
     *
     * @return List of modes.
     */
    List<? extends EMGainMode> getEMGainModes();

    class EMGainMode {

        public static final EMGainMode NONE = new EMGainMode(-1, "None");

        private final int    index;
        private final String name;

        public EMGainMode(int index, String name) {
            this.index = index;
            this.name  = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return getName();
        }

    }

}
