package jisa;

import jisa.CMS_microscopy_experiment.ACGatingMeasurement;
import jisa.CMS_microscopy_experiment.MeasurementPlus;
import jisa.CMS_microscopy_experiment.TransferCurveMeasurement;
import jisa.addresses.GPIBAddress;
import jisa.addresses.USBAddress;
import jisa.devices.DeviceException;
import jisa.devices.amp.SR830;
import jisa.devices.function_generator.K3390;
import jisa.devices.interfaces.FunctionGenerator;
import jisa.devices.smu.K2400;
import jisa.enums.Input;
import jisa.gui.*;
import jisa.results.ResultStream;
import jisa.results.ResultTable;

import java.io.IOException;

public class ACGatingAndTransferTestSequence {
    // measurement settings
    // number of points to take for each AC gating measurement (1 s per point)
    static int nPointsACGating = 180;
    // the frequencies at which the measurement should be done.
    static double[] freqList = {11, 23, 57, 103, 211, 400, 800, 1000};
    // wave type used for AC gating
    static FunctionGenerator.SupportedWaveforms waveType = FunctionGenerator.SupportedWaveforms.SineWave;
    // the high voltage for AC gating. The low voltage is always - 50;
    static double ACGatingHigh = -0;
    // output directory
    static String outputPath = "C:\\Users\\Zhang Yansheng\\Desktop\\summer project 2022\\data storage\\12 Aug Gosia device 7\\freq sweep sine wave";
    // overall test name
    static String overallTestName = "freq_sweep";

    static volatile boolean isStopped = true;
    static ACGatingMeasurement acGatingMeasurement;
    static TransferCurveMeasurement transferCurveMeasurement;

    static Table table = new Table("table of results");
    static Plot plot1  = new Plot("AC Gating"             , "time [ms]", "current [A]");
    static Plot plot2  = new Plot("Transfer Curve"        , "V_G [V]"  , "I_G [A]");
    static Grid window = new Grid("Yansheng's Measurement", table      , plot1          , plot2);

    public static void main(String[] args) throws IOException, DeviceException, InterruptedException {
        // define the measurement units
        K3390 funcGen   = new K3390(new USBAddress(0x05E6, 0x3390, "1242550"));
        SR830 lockInAmp = new SR830(new GPIBAddress(8));
        lockInAmp.setInput(Input.DIFF);
        K2400 smu       = new K2400(new GPIBAddress(28));

        // define the measurement sequence

        acGatingMeasurement      = new ACGatingMeasurement(funcGen     , lockInAmp, smu   , null);
        transferCurveMeasurement = new TransferCurveMeasurement(funcGen, smu      , null);

        window.addToolbarButton("Start Sequence", ACGatingAndTransferTestSequence::runMeasurementSequence);
        window.addToolbarButton("Stop"          , ACGatingAndTransferTestSequence::onStopButton);
        window.show();
    }

    public static void runMeasurementSequence() throws IOException {
        if (!isStopped)
        {
            GUI.errorAlert("Measurement already running");
            return;
        }
        isStopped = false;
        // define configurations
        TransferCurveMeasurement.TestConfigs linearTransferCurveConfig;
        linearTransferCurveConfig = new TransferCurveMeasurement.TestConfigs("None",
                -5, -50, 5, 56, 1000, outputPath);

        TransferCurveMeasurement.TestConfigs saturationTransferCurveConfig;
        saturationTransferCurveConfig = new TransferCurveMeasurement.TestConfigs("None",
                -50, -50, 5, 56, 1000, outputPath);

        ACGatingMeasurement.TestConfigs acGatingConfig;
        acGatingConfig = new ACGatingMeasurement.TestConfigs("None",
                ACGatingHigh, -50, 1000, waveType,
                -5, 300,146, 1000,
                nPointsACGating, outputPath);

        // saturation transfer curve before
        saturationTransferCurveConfig.setTestName(overallTestName + "_saturation_transfer_curve_before");
        transferCurveMeasurement.setConfig(saturationTransferCurveConfig);
        if (isStopped)
            return;
        startMeasurement(transferCurveMeasurement, table, plot2, new int[]{2, 3});

        for (int i = 0; i < freqList.length; i ++) {
            if (isStopped)
            {
                System.out.println("Measurement stopped!!");
                return;
            }
            String transferCurveName = overallTestName + "_linear_transfer_curve_" + i + "_";

            linearTransferCurveConfig.setTestName(transferCurveName);
            transferCurveMeasurement.setConfig(linearTransferCurveConfig);
            startMeasurement(transferCurveMeasurement, table, plot2, new int[]{2, 3});

            if (isStopped)
            {
                System.out.println("Measurement stopped!!");
                return;
            }
            String acGatingName = overallTestName + "_ac_gating_" + i + "_";
            acGatingConfig.setTestName(acGatingName);
            acGatingConfig.setGateFrequency_Hz(freqList[i]);
            acGatingMeasurement.setConfig(acGatingConfig);
            startMeasurement(acGatingMeasurement, table, plot1, new int[]{0, 2});
        }
        if (isStopped)
            return;
        // saturation transfer curve after
        saturationTransferCurveConfig.setTestName(overallTestName + "_saturation_transfer_curve_after");
        transferCurveMeasurement.setConfig(saturationTransferCurveConfig);
        startMeasurement(transferCurveMeasurement, table, plot2, new int[]{2, 3});
        isStopped = true;
    }

    public static void onStopButton()
    {
        isStopped = true;
        System.out.print(isStopped);
        if (acGatingMeasurement.isRunning()) {
            acGatingMeasurement.stop();
            return;
        }
        if (transferCurveMeasurement.isRunning()){
            transferCurveMeasurement.stop();
            return;
        }
        GUI.errorAlert("No measurement is currently running.");
    }

    public static void startMeasurement(MeasurementPlus measurement, Table table, Plot plot, int[] colToWatch) throws IOException{
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

        table.setTitle(measurement.getName());

        table.watch(results);
        plot.createSeries().watch(results,
                measurement.getColumns()[colToWatch[0]],
                measurement.getColumns()[colToWatch[1]]);

        try {
            measurement.start();
        } catch (InterruptedException e) {
            GUI.warningAlert("Measurement Interrupted.");
            isStopped = true;
        } catch (Exception e) {
            GUI.errorAlert(e.getMessage());
            e.printStackTrace();
            isStopped = true;
        } finally {
            //GUI.infoAlert("Measurement Ended.");
            if (results instanceof ResultStream)
                ((ResultStream) results).close();
        }
    }
}
