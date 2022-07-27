package jisa;

import javafx.scene.control.Tab;
import javassist.expr.Instanceof;
import jisa.CMS_microscopy_experiment.ACGatingMeasurement;
import jisa.addresses.GPIBAddress;
import jisa.addresses.USBAddress;
import jisa.devices.DeviceException;
import jisa.devices.amp.SR830;
import jisa.devices.function_generator.K3390;
import jisa.devices.smu.K2400;
import jisa.gui.*;
import jisa.results.ResultStream;
import jisa.results.ResultTable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ACGatingTest {
    public static void main(String[] args) throws IOException, DeviceException {

        // define the measurement units
        K3390 funcGen = new K3390(new USBAddress(0x05E6, 0x3390, "1242550"));
        SR830 lockInAmp = new SR830(new GPIBAddress(8));
        K2400 smu = new K2400(new GPIBAddress(28));

        ACGatingMeasurement measurement = new ACGatingMeasurement(funcGen, lockInAmp, smu, null);
        MeasurementConfigurator configurator = new MeasurementConfigurator(measurement);

        Table table = new Table("table of results");
        Plot plot   = new Plot("plot" ,"time [ms]","current [A]");
        Grid window = new Grid("AC gating", table, plot);

        window.addToolbarButton("Start", ()->onStart(measurement, table, plot));

        window.addToolbarButton("Stop", () -> {

            if (measurement.isRunning()) {
                measurement.stop();
            } else {
                GUI.errorAlert("No measurement is currently running.");
            }

        });

        window.addToolbarButton("Exit", window::close);

        window.show();
    }

    public static void onStart(ACGatingMeasurement measurement, Table table, Plot plot) throws IOException {
        if (measurement.isRunning()) {
            GUI.errorAlert("A measurement is already running.");
            return;
        }

        // check & configure measurements
        MeasurementConfigurator configurator = new MeasurementConfigurator(measurement);
        // if the configuration is cancelled, then no measurement will be run.
        if (!configurator.showInput())
            return;

        // start running the measurement
        measurement.updateConfigs();

        // handle the data IO issues.
        if (!measurement.checkExportPath()) {
            GUI.errorAlert("File path not correct! Check again!");
            return;
        }
        ResultTable results;
        String[] filenames = measurement.getOutputFileNames();
        if (filenames != null) {
            measurement.writeConfig(filenames[0]);
            results = measurement.newResults(filenames[1]);
        }
        else{
            results = measurement.newResults();
        }

        table.clear();
        plot.clear();
        table.watch(results);
        plot.createSeries().watch(results, measurement.getColumns()[0], measurement.getColumns()[2]);

        try {
            measurement.start();
        } catch (InterruptedException e) {
            GUI.warningAlert("Measurement Interrupted.");
        } catch (Exception e) {
            GUI.errorAlert(e.getMessage());
            e.printStackTrace();
        } finally {
            GUI.infoAlert("Measurement Ended.");
            if (results instanceof ResultStream)
                ((ResultStream) results).close();
        }
    }
}
