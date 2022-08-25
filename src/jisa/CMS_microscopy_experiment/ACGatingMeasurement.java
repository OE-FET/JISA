package jisa.CMS_microscopy_experiment;

import jisa.Util;
import jisa.control.Nameable;
import jisa.devices.DeviceException;
import jisa.devices.amp.SR830;
import jisa.devices.function_generator.K3390;
import jisa.devices.interfaces.FunctionGenerator;
import jisa.devices.interfaces.LockIn;
import jisa.devices.interfaces.SMU;
import jisa.devices.smu.K2400;
import jisa.enums.Input;
import jisa.enums.Shield;
import jisa.enums.Source;
import jisa.experiment.Measurement;
import jisa.results.Col;
import jisa.results.Column;
import jisa.results.ResultTable;
import jisa.visa.VISADevice;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * In this measurement, the FET under test is gated periodically with an AC voltage. The drain current is measured
 * by a lock-in amplifier (using a small resistor to convert the current to a voltage) while a small drain source
 * voltage is applied.
 *
 * Note that the convention is that the source is grounded.
 */

public class ACGatingMeasurement extends MeasurementPlus {
    // safety limits parameters (will implement this later...)
    private final double MAX_ABS_VOLTAGE = 50.0;

    // The waveform that is actually applied on the gate
    private FunctionGenerator.Waveform gate_waveform;
    private double amplification_ratio;               // The ratio at which the waveform from the function generator
    private final boolean smuAvailable;
    private final K3390 funcGen;
    private final SR830 lockInAmp;
    private final K2400 smu;

    // configurators
    private StringParameter testName;
    private DoubleParameter gateVoltageHigh_V;
    private DoubleParameter gateVoltageLow_V;
    private DoubleParameter gateFrequency_Hz;
    private ChoiceParameter waveType;
    private ChoiceParameter amplificationRatio;

    private DoubleParameter V_DS;
    private DoubleParameter timeConstant_ms;
    private DoubleParameter currentSenseResistance;
    private DoubleParameter timeBetweenDataPoints_ms;
    private IntegerParameter nDataPoints;
    private StringParameter outputPath;

    private TestConfigs currentConfig;

    /**
     * Construct the measurement. All measurement instruments are owned externally.
     * If currentConfig is null, some default values will be given here.
     */
    public ACGatingMeasurement(K3390 funcGen, SR830 lockInAmp, K2400 smu, TestConfigs currentConfig){
        super();
        this.funcGen = funcGen;
        this.lockInAmp = lockInAmp;
        this.smu = smu;
        smuAvailable = (smu != null);

        if (currentConfig == null)
        {
            this.currentConfig = new TestConfigs("Test",
                    0, -1, 10,
                    FunctionGenerator.SupportedWaveforms.SineWave,
                    -5,900, 146,
                    1000, 50, "");
        }
        initializeConfigurators();
    }

    /**
     * Change the test configuration
     * @param configs the new configuration
     */
    public void setConfig(TestConfigs configs){
        this.currentConfig = configs;
        updateConfigurators();
    }

    public TestConfigs getConfig(TestConfigs configs){
        return currentConfig;
    }

    /**
     * Initialize the UI configurators.
     */
    private void initializeConfigurators()
    {
        testName                 = new StringParameter("Basic Info"         , "Test Name"                , ""   , currentConfig.testName);
        gateVoltageHigh_V        = new DoubleParameter("Gate Signal"        , "Gate Voltage High"        , "V"  , currentConfig.gateVoltageHigh_V);
        gateVoltageLow_V         = new DoubleParameter("Gate Signal"        , "Gate Voltage Low"         , "V"  , currentConfig.gateVoltageLow_V);
        gateFrequency_Hz         = new DoubleParameter("Gate Signal"        , "Gate Frequency"           , "Hz" , currentConfig.gateFrequency_Hz);
        waveType                 = new ChoiceParameter("Gate Signal"        , "Wave Form"                , 0    , FunctionGenerator.SupportedWaveforms.getAllNames());
        amplificationRatio       = new ChoiceParameter("Gate Signal"        , "Amplification Ratio"      , 0    , String.valueOf(currentConfig.amplificationRatio));
        timeConstant_ms          = new DoubleParameter("Measurement Config" , "Time Constant"            , "ms" , currentConfig.timeConstant_ms);
        V_DS                     = new DoubleParameter("Measurement Config" , "V_DS"                     , "V"  , currentConfig.V_DS);
        currentSenseResistance   = new DoubleParameter("Measurement Config" , "Current Sense Resistance" , "Ohm", currentConfig.currentSenseResistance);
        timeBetweenDataPoints_ms = new DoubleParameter("Measurement Config" , "Time Between Measurements", "ms" , currentConfig.timeBetweenDataPoints_ms);
        nDataPoints              = new IntegerParameter("Measurement Config", "# Points"                 , ""   , currentConfig.nDataPoints);
        outputPath               = new StringParameter("Measurement Config" , "Output path"              , ""   , currentConfig.outputPath);
    }

    /**
     * Change the default values of the UI configurator to the current test configuration.
     */
    public void updateConfigurators()
    {
        testName.setValue(currentConfig.testName);
        gateVoltageHigh_V.setValue(currentConfig.gateVoltageHigh_V);
        gateVoltageLow_V.setValue(currentConfig.gateVoltageLow_V);
        gateFrequency_Hz.setValue(currentConfig.gateFrequency_Hz);
        waveType.setValue(currentConfig.waveType.toInt());
        timeConstant_ms.setValue(currentConfig.timeConstant_ms);
        V_DS.setValue(currentConfig.V_DS);
        timeBetweenDataPoints_ms.setValue(currentConfig.timeBetweenDataPoints_ms);
        nDataPoints.setValue(currentConfig.nDataPoints);
        outputPath.setValue(currentConfig.outputPath);
    }

    /**
     * Update the current test config using the values from the test configurator.
     */
    public void updateConfigs()
    {
        currentConfig.testName                 = testName.getValue();
        currentConfig.gateVoltageHigh_V        = gateVoltageHigh_V.getValue();
        currentConfig.gateVoltageLow_V         = gateVoltageLow_V.getValue();
        currentConfig.gateFrequency_Hz         = gateFrequency_Hz.getValue();
        currentConfig.waveType                 = FunctionGenerator.SupportedWaveforms.fromInt(waveType.getValue());
        currentConfig.timeConstant_ms          = timeConstant_ms.getValue();
        currentConfig.V_DS                     = V_DS.getValue();
        currentConfig.timeBetweenDataPoints_ms = timeBetweenDataPoints_ms.getValue();
        currentConfig.nDataPoints              = nDataPoints.getValue();
        currentConfig.outputPath               = outputPath.getValue();
    }

    /**
     * Calculate the waveform the function generator is supposed to output based on the current config
     * @return the waveform that is to be passed on to the function generator
     */
    private FunctionGenerator.Waveform createWaveform(){
        double amplitude           = Math.abs(currentConfig.gateVoltageHigh_V - currentConfig.gateVoltageLow_V) / 2.0;
        double offset              = (currentConfig.gateVoltageHigh_V + currentConfig.gateVoltageLow_V) / 2.0;
        amplitude                  = amplitude / currentConfig.amplificationRatio;
        offset                     = offset / currentConfig.amplificationRatio;

        if (currentConfig.waveType == FunctionGenerator.SupportedWaveforms.SineWave){
            return new FunctionGenerator.SineWave(amplitude, currentConfig.gateFrequency_Hz, offset, 0);
        }
        if (currentConfig.waveType == FunctionGenerator.SupportedWaveforms.SquareWave){
            return new FunctionGenerator.SquareWave(amplitude, currentConfig.gateFrequency_Hz, offset);
        }
        return null;
    }

    /**
     * Reset the instruments and prepare for measurements
     */
    private void initializedMeasurementInstruments() throws IOException, InterruptedException, DeviceException {

        funcGen.reset();
        funcGen.enableLogger("Func Gen K3390", null);
        // might need to change it to low impedance mode later!
        funcGen.setStandardImpedanceMode();
        funcGen.turnOnSynchronizationSignal();
        funcGen.outputWaveform(createWaveform());

        // set up the SMU
        if (smuAvailable){
            smu.reset();
            smu.enableLogger("SMU K2400", null);
            smu.setSource(Source.VOLTAGE);
            // smu.useAutoVoltageRange();
            smu.setVoltageLimit(MAX_ABS_VOLTAGE);    // 5 V voltage limit
            smu.setCurrentLimit(100e-3); // 100 mA current limit
            smu.setVoltage(currentConfig.V_DS);
            smu.setFourProbeEnabled(false);
            smu.turnOn();
            // configure and output V_DS
        }

        lockInAmp.reset();

        lockInAmp.enableLogger("Lock-in SR830", null);
        lockInAmp.setRefMode(LockIn.RefMode.EXTERNAL);
        lockInAmp.setExternalTriggerMode(LockIn.TrigMode.POS_TTL);
        // wait for the PLL to get locked.
        // there seems to be no way to check if the amplifier is locked
        while (!lockInAmp.isLocked())
            Thread.sleep(500);
        lockInAmp.setOscPhase(0);

        // configure differential A-B input
        lockInAmp.setSource(Source.VOLTAGE);
        lockInAmp.setInput(Input.DIFF);
        // how should I set this ?
        lockInAmp.setShielding(Shield.FLOAT);
        lockInAmp.setLineFilterHarmonics();

        if (currentConfig.gateFrequency_Hz < 200)
            lockInAmp.setSyncFilterEnabled(true);
        lockInAmp.setTimeConstant(currentConfig.timeConstant_ms*0.001);
        Thread.sleep(20000);
        //lockInAmp.autoGain();
        // get a bit of reserve for the lock-in amp
        //lockInAmp.setRange(lockInAmp.getRange()*2);
        lockInAmp.setRange(0.05);
    }

    @Override
    public String getName() {
        return currentConfig.getTestName();
    }

    @Override
    protected void run(ResultTable results) throws Exception {
        updateConfigs();
        initializedMeasurementInstruments();
        System.out.print("Measurement started!");
        long testStartTime = System.currentTimeMillis();
        for (int i = 0; i < currentConfig.nDataPoints; i++)
        {
            long relativeTime = System.currentTimeMillis() - testStartTime;
            double lockInVoltage = lockInAmp.getLockedAmplitude();
            double lockInCurrent = lockInVoltage/ currentConfig.currentSenseResistance;
            double lockInPhase = lockInAmp.getLockedPhase();
            results.addData((double) relativeTime, lockInVoltage, lockInCurrent, lockInPhase);

            // wait for the specified amount of time before the next measurement.
            long timeElapsed =  System.currentTimeMillis() - testStartTime - relativeTime;
            long timeToSleep = Math.round(currentConfig.timeBetweenDataPoints_ms) - timeElapsed;
            if (timeToSleep < 0)
                timeToSleep = 0;
            // the timing might not be very precise...
            Thread.sleep(timeToSleep);
        }
        System.out.print("Measurement ended!");
    }

    @Override
    protected void onInterrupt() throws Exception {
        System.out.print("Interrupted");
    }

    @Override
    protected void onError() throws Exception {
        System.out.print("Something wrong!");
    }

    @Override
    protected void onFinish() throws Exception {
        Util.runRegardless(funcGen::turnOff);
        Util.runRegardless(()-> {if (smuAvailable) smu.turnOff();});
        // smu turn off
    }

    @Override
    public Column[] getColumns() {
        return new Col[]{
                new Col("Time"           , "ms"),
                new Col("Lock-in Voltage", "V"),
                new Col("Lock-in Current", "A"),
                new Col("Lock-in Phase"  , "deg")
        };
    }

    /**
     * Checks if the output path is valid. If output path is "", it is fine.
     * @return true if the path is valid
     */
    public boolean checkExportPath(){
        if (Objects.equals(currentConfig.outputPath, ""))
            return true;
        if (Files.exists(Paths.get(currentConfig.outputPath)))
            return true;
        return false;
    }

    /**
     * Write the current configuration to a file.
     * This function assumes that the path has been checked!
     */
    public void writeConfig(String filename)
    {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            out.write(currentConfig.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the output file names.
     * @return {Config output file name, data output file name}
     */
    public String[] getOutputFileNames()
    {
        if (Objects.equals(currentConfig.outputPath, ""))
            return null;
        if (!Files.exists(Paths.get(currentConfig.outputPath)))
            return null;
        Path outputFilePath = Paths.get(currentConfig.outputPath, currentConfig.testName + "0_config.json");
        int i = 0;
        while (Files.exists(outputFilePath)){
            i = i + 1;
            outputFilePath = Paths.get(currentConfig.outputPath, currentConfig.testName + i + "_config.json");
        }
        return new String[]{
                String.valueOf(Paths.get(currentConfig.outputPath, currentConfig.testName + i + "_config.json")),
                String.valueOf(Paths.get(currentConfig.outputPath, currentConfig.testName + i + "_data.csv"))};
    }

    public static class TestConfigs{
        private String testName;
        private double gateVoltageHigh_V;
        private double gateVoltageLow_V;
        private double gateFrequency_Hz;
        private FunctionGenerator.SupportedWaveforms waveType;

        private final double amplificationRatio = 20;

        private double V_DS;
        private double timeConstant_ms;
        private double currentSenseResistance;
        private double timeBetweenDataPoints_ms;
        private int nDataPoints;
        private String outputPath;

        public TestConfigs(String testName,
                           double gateVoltageHigh_V,
                           double gateVoltageLow_V,
                           double gateFrequency_Hz,
                           FunctionGenerator.SupportedWaveforms waveType,
                           //double amplificationRatio,
                           double V_DS,
                           double timeConstant_ms,
                           double currentSenseResistance,
                           double timeBetweenDataPoints_ms,
                           int nDataPoints,
                           String outputPath) {
            this.testName = testName;
            this.gateVoltageHigh_V = gateVoltageHigh_V;
            this.gateVoltageLow_V = gateVoltageLow_V;
            this.gateFrequency_Hz = gateFrequency_Hz;
            this.waveType = waveType;
            //this.amplificationRatio = amplificationRatio;
            this.V_DS = V_DS;
            this.timeConstant_ms = timeConstant_ms;
            this.currentSenseResistance = currentSenseResistance;
            this.timeBetweenDataPoints_ms = timeBetweenDataPoints_ms;
            this.nDataPoints = nDataPoints;
            this.outputPath = outputPath;
        }

        /**
         * Convert the data in the class into a json string.
         * @return json string
         */
        public String toString(){
            JSONObject json = new JSONObject();
            try {
                json.put("testName"                , testName);
                json.put("gateVoltageHigh_V"       , gateVoltageHigh_V);
                json.put("gateVoltageLow_V"        , gateVoltageLow_V);
                json.put("gateFrequency_Hz"        , gateFrequency_Hz);
                json.put("waveType"                , waveType.toString());
                json.put("amplificationRatio"      , amplificationRatio);
                json.put("V_DS"                    , V_DS);
                json.put("timeConstant_ms"         , timeConstant_ms);
                json.put("currentSenseResistance"  , currentSenseResistance);
                json.put("timeBetweenDataPoints_ms", timeBetweenDataPoints_ms);
                json.put("nDataPoints"             , nDataPoints);

                // add a time stamp as well.
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                json.put("timeStamp", formatter.format(date));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public void setGateVoltageHigh_V(double gateVoltageHigh_V) {
            this.gateVoltageHigh_V = gateVoltageHigh_V;
        }

        public void setGateVoltageLow_V(double gateVoltageLow_V) {
            this.gateVoltageLow_V = gateVoltageLow_V;
        }

        public void setGateFrequency_Hz(double gateFrequency_Hz) {
            this.gateFrequency_Hz = gateFrequency_Hz;
        }

        public void setWaveType(FunctionGenerator.SupportedWaveforms waveType) {
            this.waveType = waveType;
        }

//        public void setAmplificationRatio(double amplificationRatio){
//            this.amplificationRatio = amplificationRatio;
//        }

        public void setTimeConstant_ms(double timeConstant_ms) {
            this.timeConstant_ms = timeConstant_ms;
        }

        public void setTimeBetweenDataPoints_ms(double timeBetweenDataPoints_ms) {
            this.timeBetweenDataPoints_ms = timeBetweenDataPoints_ms;
        }

        public void setNDataPoints(int nDataPoints) {
            this.nDataPoints = nDataPoints;
        }

        public void setCurrentSenseResistance(double currentSenseResistance) {
            this.currentSenseResistance = currentSenseResistance;
        }

        public void setOutputPath(String outputPath) {
            this.outputPath = outputPath;
        }

        public String getTestName() {
            return testName;
        }

        public double getGateVoltageHigh_V() {
            return gateVoltageHigh_V;
        }

        public double getGateVoltageLow_V() {
            return gateVoltageLow_V;
        }

        public double getGateFrequency_Hz() {
            return gateFrequency_Hz;
        }

        public FunctionGenerator.SupportedWaveforms getWaveType() {
            return waveType;
        }

        public double getTimeConstant_ms() {
            return timeConstant_ms;
        }

        public double getTimeBetweenDataPoints_ms() {
            return timeBetweenDataPoints_ms;
        }

        public int getNDataPoints() {
            return nDataPoints;
        }

        public double getCurrentSenseResistance() {
            return currentSenseResistance;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public void setV_DS(double v_DS) {
            V_DS = v_DS;
        }

        public double getV_DS() {
            return V_DS;
        }
    }
}
