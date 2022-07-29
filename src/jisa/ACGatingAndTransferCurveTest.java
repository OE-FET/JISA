package jisa;

import jisa.CMS_microscopy_experiment.ACGatingMeasurement;
import jisa.CMS_microscopy_experiment.TransferCurveMeasurement;
import jisa.addresses.GPIBAddress;
import jisa.addresses.USBAddress;
import jisa.devices.DeviceException;
import jisa.devices.amp.SR830;
import jisa.devices.function_generator.K3390;
import jisa.devices.smu.K2400;
import jisa.enums.Input;
import jisa.gui.*;
import jisa.results.ResultStream;
import jisa.results.ResultTable;

import java.io.IOException;

public class ACGatingAndTransferCurveTest {

    public static void main(String[] args) throws IOException, DeviceException, InterruptedException {
        // define the measurement units
        K3390 funcGen = new K3390(new USBAddress(0x05E6, 0x3390, "1242550"));
        SR830 lockInAmp = new SR830(new GPIBAddress(8));
        lockInAmp.setInput(Input.DIFF);
        K2400 smu = new K2400(new GPIBAddress(28));

        ACGatingMeasurement measurement = new ACGatingMeasurement(funcGen, lockInAmp, smu, null);
        TransferCurveMeasurement measurement2 = new TransferCurveMeasurement(funcGen, smu, null);

        Table table = new Table("table of results");
        Plot plot1  = new Plot("AC Gating"             , "time [ms]", "current [A]");
        Plot plot2  = new Plot("Transfer Curve"        , "V_G [V]"  , "I_G [A]");
        Grid window = new Grid("Yansheng's Measurement", table      , plot1          , plot2);

        window.addToolbarButton("Start AC Gating", ()->onStartACGating(measurement, table, plot1));
        window.addToolbarButton("Start Transfer Curve", ()->onStartTransferCurve(measurement2, table, plot2));
        window.addToolbarButton("Stop", ()->{
            if (measurement.isRunning()) {
                measurement.stop();
                return;
            }
            if (measurement2.isRunning()){
                measurement2.stop();
                return;
            }
            GUI.errorAlert("No measurement is currently running.");

        });
        window.show();
    }

    public static void onStartTransferCurve(TransferCurveMeasurement measurement, Table table, Plot plot) throws IOException{
        if (measurement.isRunning()) {
            GUI.errorAlert("A measurement is already running.");
            return;
        }

        // check & configure measurements
        MeasurementConfigurator configurator = new MeasurementConfigurator(measurement);
        // if the configuration is cancelled, then no measurement will be run.
        if (!configurator.showInput())
            return;

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

        table.setTitle("Transfer curve measurement");
//        plot.setXLabel("V_G", "V");
//        plot.setYLabel("I_D", "A");

        table.watch(results);
        plot.createSeries().watch(results, measurement.getColumns()[2], measurement.getColumns()[3]);

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

    public static void onStartACGating(ACGatingMeasurement measurement, Table table, Plot plot) throws IOException {
        if (measurement.isRunning()) {
            GUI.errorAlert("A measurement is already running.");
            return;
        }

        // check & configure measurements
        MeasurementConfigurator configurator = new MeasurementConfigurator(measurement);
        // if the configuration is cancelled, then no measurement will be run.
        if (!configurator.showInput())
            return;

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

        table.setTitle("AC gating measurement");
//        plot.setXLabel("time [ms]");
//        plot.setYLabel("current [A]");

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
