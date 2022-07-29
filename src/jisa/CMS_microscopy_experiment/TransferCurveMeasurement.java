package jisa.CMS_microscopy_experiment;

import jisa.Util;
import jisa.devices.DeviceException;
import jisa.devices.amp.SR830;
import jisa.devices.function_generator.K3390;
import jisa.devices.smu.K2400;
import jisa.enums.Source;
import jisa.experiment.Measurement;
import jisa.results.Col;
import jisa.results.Column;
import jisa.results.ResultTable;
import jnr.ffi.annotations.In;
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

public class TransferCurveMeasurement extends Measurement {

    private final double MAX_ABS_VOLTAGE = 50.0;

    private StringParameter testName;
    private DoubleParameter V_DS;
    private DoubleParameter minV_G;
    private DoubleParameter maxV_G;
    private IntegerParameter nPoints;
    private DoubleParameter timeBetweenPoints;
    private ChoiceParameter amplificationRatio;
    private StringParameter outputPath;

    private final K3390 funcGen;
    private final K2400 smu;

    private TestConfigs currentConfig;

    public TransferCurveMeasurement(K3390 funcGen, K2400 smu, TestConfigs configs) throws IOException, InterruptedException, DeviceException {
        this.funcGen = funcGen;
        this.smu = smu;
        this.currentConfig = configs;
        if (this.currentConfig == null)
        {
            this.currentConfig = new TestConfigs("Transfer curve",
                    -5, -10, 5, 20, 1000,
                    "");
        }
        initializeConfigurators();
    }

    private void initializedMeasurementInstruments() throws IOException, InterruptedException, DeviceException {
        funcGen.reset();
        funcGen.enableLogger("Func Gen K3390", null);
        funcGen.setStandardImpedanceMode();
        funcGen.outputDC(0);

        smu.reset();
        smu.enableLogger("SMU K2400", null);
        smu.setFourProbeEnabled(false);
        smu.setSource(Source.VOLTAGE);
        // smu.useAutoVoltageRange();
        //smu.setVoltageLimit(MAX_ABS_VOLTAGE);    // 50 V voltage limit
        //smu.setCurrentLimit(100e-3); // 100 mA current limit
        smu.setVoltage(currentConfig.V_DS);
        smu.turnOn();
    }
    /**
     * Initialize the UI configurators.
     */
    public void initializeConfigurators(){
        testName           = new StringParameter("Basic Info"        , "Test Name"          , "" , currentConfig.getTestName());
        V_DS               = new DoubleParameter("Scan Settings"     , "V_DS"               , "V", currentConfig.getV_DS());
        minV_G             = new DoubleParameter("Scan Settings"     , "Min V_G"            , "V", currentConfig.getMinV_G());
        maxV_G             = new DoubleParameter("Scan Settings"     , "Max V_G"            , "V", currentConfig.getMaxV_G());
        nPoints            = new IntegerParameter("Scan Settings"    , "# Points"           , "" , currentConfig.getnPoints());
        amplificationRatio = new ChoiceParameter("Scan Settings"     , "Amplification Ratio", 0  , String.valueOf(currentConfig.amplificationRatio));
        outputPath         = new StringParameter("Measurement Config", "Output path"        , "" , currentConfig.outputPath);
    }

    public void updateConfigurators() {
        testName.setValue(currentConfig.testName);
        V_DS.setValue(currentConfig.V_DS);
        minV_G.setValue(currentConfig.minV_G);
        maxV_G.setValue(currentConfig.maxV_G);
        nPoints.setValue(currentConfig.nPoints);
        outputPath.setValue(currentConfig.outputPath);
    }

    public void updateConfigs(){
        currentConfig.testName   = testName.getValue();
        currentConfig.V_DS       = V_DS.getValue();
        currentConfig.minV_G     = minV_G.getValue();
        currentConfig.maxV_G     = maxV_G.getValue();
        currentConfig.nPoints    = nPoints.getValue();
        currentConfig.outputPath = outputPath.getValue();
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

    @Override
    public String getName() {
        return "DIY transfer curve";
    }

    @Override
    protected void run(ResultTable results) throws Exception {
        updateConfigs();
        initializedMeasurementInstruments();

        double VStart, VEnd;
        if (Math.abs(currentConfig.minV_G) < Math.abs(currentConfig.maxV_G))
        {
            VStart = currentConfig.minV_G;
            VEnd = currentConfig.maxV_G;
        }else{
            VEnd = currentConfig.minV_G;
            VStart = currentConfig.maxV_G;
        }

        System.out.print("Measurement started!");
        double V_step = (VEnd - VStart)/ currentConfig.nPoints;

        // hold at zero volt for a while to discharge everything.
        System.out.println("Waiting for discharge");
        funcGen.outputDC(0);
        Thread.sleep(10000);

        long testStartTime = System.currentTimeMillis();
        for (int i = 0; i <= currentConfig.nPoints; i++)
        {
            long relativeTime = System.currentTimeMillis() - testStartTime;
            double currentV_G = VStart + i*V_step;
            funcGen.outputDC(currentV_G/currentConfig.amplificationRatio);
            // wait for half the time to stabilize the reading
            Thread.sleep((long) currentConfig.timeBetweenPoints/2);
            double I_D = smu.getCurrent();
            double curr_V_DS = smu.getVoltage();
            results.addData((double) relativeTime, curr_V_DS, currentV_G, I_D);

            // wait for the specified amount of time before the next measurement.
            long timeElapsed =  System.currentTimeMillis() - testStartTime - relativeTime;
            long timeToSleep = Math.round(currentConfig.timeBetweenPoints) - timeElapsed;
            if (timeToSleep < 0)
                timeToSleep = 0;
            // the timing might not be very precise...
            Thread.sleep(timeToSleep);
        }

        for (int i = currentConfig.nPoints; i >=0; i--)
        {
            long relativeTime = System.currentTimeMillis() - testStartTime;
            double currentV_G = VStart + i*V_step;
            funcGen.outputDC(currentV_G/currentConfig.amplificationRatio);
            // wait for half the time to stabilize the reading
            Thread.sleep((long) currentConfig.timeBetweenPoints/2);
            double I_D = smu.getCurrent();
            double curr_V_DS = smu.getVoltage();
            results.addData((double) relativeTime, curr_V_DS, currentV_G, I_D);

            // wait for the specified amount of time before the next measurement.
            long timeElapsed =  System.currentTimeMillis() - testStartTime - relativeTime;
            long timeToSleep = Math.round(currentConfig.timeBetweenPoints) - timeElapsed;
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
        Util.runRegardless(smu::turnOff);
    }

    @Override
    public Column[] getColumns() {
        return new Col[]{
                new Col("Time", "ms"),
                new Col("V_DS", "V"),
                new Col("V_G" , "V"),
                new Col("I_D" , "A")
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
                String.valueOf(Paths.get(currentConfig.outputPath, currentConfig.testName + i + "_data.json"))};
    }

    static class TestConfigs{
        private String testName;
        private double V_DS;
        private double minV_G;
        private double maxV_G;
        private int nPoints;
        private double timeBetweenPoints;
        private final double amplificationRatio = 20;
        private String outputPath;

        public TestConfigs(String testName,
                          double V_DS,
                          double minV_G, double maxV_G, int nPoints, double timeBetweenPoints,
                          String outputPath) {
            this.testName = testName;
            this.V_DS = V_DS;
            this.minV_G = minV_G;
            this.maxV_G = maxV_G;
            this.nPoints = nPoints;
            this.timeBetweenPoints = timeBetweenPoints;
            this.outputPath = outputPath;
        }

        /**
         * Convert the data in the class into a json string.
         * @return json string
         */
        public String toString(){
            JSONObject json = new JSONObject();
            try {
                json.put("testName"            , testName);
                json.put("V_DS"                , V_DS);
                json.put("minV_G"              , minV_G);
                json.put("maxV_G"              , maxV_G);
                json.put("nPoints"             , nPoints);
                json.put("timeBetweenPoints_ms", timeBetweenPoints);
                json.put("amplificationRatio"  , amplificationRatio);
                json.put("outputPath"          , outputPath);

                // add a time stamp as well.
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                json.put("timeStamp", formatter.format(date));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json.toString();
        }

        public String getTestName() {
            return testName;
        }

        public double getV_DS() {
            return V_DS;
        }

        public double getMinV_G() {
            return minV_G;
        }

        public double getMaxV_G() {
            return maxV_G;
        }

        public int getnPoints() {
            return nPoints;
        }

        public double getTimeBetweenPoints() {
            return timeBetweenPoints;
        }

        public double getAmplificationRatio() {
            return amplificationRatio;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public void setV_DS(double v_DS) {
            V_DS = v_DS;
        }

        public void setMinV_G(double minV_G) {
            this.minV_G = minV_G;
        }

        public void setMaxV_G(double maxV_G) {
            this.maxV_G = maxV_G;
        }

        public void setnPoints(int nPoints) {
            this.nPoints = nPoints;
        }

        public void setTimeBetweenPoints(double timeBetweenPoints) {
            this.timeBetweenPoints = timeBetweenPoints;
        }

        public void setOutputPath(String outputPath) {
            this.outputPath = outputPath;
        }
    }
}
