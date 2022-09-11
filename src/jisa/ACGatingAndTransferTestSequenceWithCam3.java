package jisa;

import jisa.CMS_microscopy_experiment.ACGatingMeasurement;
import jisa.CMS_microscopy_experiment.CameraWrapper;
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
import jisa.gui.GUI;
import jisa.gui.Grid;
import jisa.gui.Plot;
import jisa.gui.Table;
import jisa.results.ResultStream;
import jisa.results.ResultTable;

import java.io.IOException;

public class ACGatingAndTransferTestSequenceWithCam3 {
    // measurement settings
    // number of points to take for each AC gating measurement (1 s per point)
    static int transferCurveNPoint = 56;
    static int nPointsACGating = 20000;
    static int nFrames = 5000;
    static double gating_freq = 11.0;
    // wave type used for AC gating
    static FunctionGenerator.SupportedWaveforms waveType = FunctionGenerator.SupportedWaveforms.SquareWave;
    // the high voltage for AC gating. The low voltage is always - 50;
    static double ACGatingHigh = -0;
    static double[] ACGatingLowList = {-50, -50, -50, -50, -50, -50}; //, -30, -70, -40, -1, -80, -60, -1, -35, -75, -65, -1, -45, -55};

    // output directory
    static String outputPath = "D:\\01_iSCAT_Working_Folders\\Yansheng\\run 4";
    // overall test name
    static String overallTestName = "gating";

    static volatile boolean isStopped = true;
    static ACGatingMeasurement acGatingMeasurement;
    static TransferCurveMeasurement transferCurveMeasurement;

    static Table table = new Table("table of results");
    static Plot plot1  = new Plot("AC Gating"             , "time [ms]", "current [A]");
    static Plot plot2  = new Plot("Transfer Curve"        , "V_G [V]"  , "I_G [A]");
    static Grid window = new Grid("Yansheng's Measurement", table      , plot1          , plot2);

    static boolean next_step = true;

    public static void main(String[] args) throws IOException, DeviceException, InterruptedException {
        // define the measurement units
        K3390 funcGen   = new K3390(new USBAddress(0x05E6, 0x3390, "1242550"));
        funcGen.turnOnSynchronizationSignal();
        SR830 lockInAmp = new SR830(new GPIBAddress(8));
        lockInAmp.setInput(Input.DIFF);
        K2400 smu       = new K2400(new GPIBAddress(28));

        // define the measurement sequence
        CameraWrapper camera = new CameraWrapper("localhost", 4920);
        acGatingMeasurement      = new ACGatingMeasurement(funcGen     , lockInAmp, smu   , camera, null);
        acGatingMeasurement.setUseCamera(true);
        acGatingMeasurement.setCameraNFrames(nFrames);
        transferCurveMeasurement = new TransferCurveMeasurement(funcGen, smu      , null);

        window.addToolbarButton("Start Sequence", ACGatingAndTransferTestSequenceWithCam3::runMeasurementSequence);
        window.addToolbarButton("Stop"          , ACGatingAndTransferTestSequenceWithCam3::onStopButton);
        window.addToolbarButton("Next step"     , ACGatingAndTransferTestSequenceWithCam3::startNextStep);
        window.show();
    }

    public static void startNextStep(){
        next_step = true;
    }


    public static void runMeasurementSequence() throws IOException, InterruptedException {
        if (!isStopped)
        {
            GUI.errorAlert("Measurement already running");
            return;
        }
        isStopped = false;
        // define configurations
        TransferCurveMeasurement.TestConfigs linearTransferCurveConfig;
        linearTransferCurveConfig = new TransferCurveMeasurement.TestConfigs("None",
                -5, -50, 5, transferCurveNPoint, 1000, outputPath);

        TransferCurveMeasurement.TestConfigs saturationTransferCurveConfig;
        saturationTransferCurveConfig = new TransferCurveMeasurement.TestConfigs("None",
                -50, -50, 5, transferCurveNPoint, 1000, outputPath);

        ACGatingMeasurement.TestConfigs acGatingConfig;
        acGatingConfig = new ACGatingMeasurement.TestConfigs("None",
                ACGatingHigh, -50, gating_freq, waveType,
                -5, 300,146, 1000,
                nPointsACGating, outputPath);

        // simple procedure
        for (int i = 0; i < ACGatingLowList.length; i++){
            if (isStopped)
                return;
            linearTransferCurveConfig.setTestName(overallTestName + "_linear_transfer_curve " + ACGatingLowList[i] + " V ");
            transferCurveMeasurement.setConfig(linearTransferCurveConfig);
            startMeasurement(transferCurveMeasurement, table, plot2, new int[]{2, 3});

            if (isStopped)
                return;
            String acGatingName = overallTestName + "_ac_gating " + ACGatingLowList[i] + " V ";
            acGatingConfig.setTestName(acGatingName);
            acGatingConfig.setGateVoltageLow_V(ACGatingLowList[i]);
            acGatingMeasurement.setConfig(acGatingConfig);
            startMeasurement(acGatingMeasurement, table, plot1, new int[]{0, 2});

            if (isStopped)
                return;
            acGatingName = overallTestName + "_ac_gating " + "-1 V ";
            acGatingConfig.setTestName(acGatingName);
            acGatingConfig.setGateVoltageLow_V(-1);
            acGatingMeasurement.setConfig(acGatingConfig);
            startMeasurement(acGatingMeasurement, table, plot1, new int[]{0, 2});

            // this is the time for refocusing
            GUI.infoAlert("Please refocus!");
            next_step = false;
            while (!next_step)
                Thread.sleep(1000);
        }
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
