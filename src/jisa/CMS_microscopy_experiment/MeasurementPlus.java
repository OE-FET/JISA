package jisa.CMS_microscopy_experiment;

import jisa.experiment.Measurement;

public abstract class MeasurementPlus extends Measurement {
    /**
     * Update the configurator default setting using the latest information from the test configs.
     */
    public abstract void updateConfigurators();

    /**
     * Update the test configs using the latest information from the configurator.
     */
    public abstract void updateConfigs();

    /**
     * Checks if the output path is valid. If output path is "", it is fine.
     * @return true if the path is valid
     */
    public abstract boolean checkExportPath();

    /**
     * Write the current configuration to a file.
     * This function assumes that the path has been checked!
     */
    public abstract void writeConfig(String filename);

    /**
     * Get the output file names.
     * @return {Config output file name, data output file name}
     */
    public abstract String[] getOutputFileNames();

}
