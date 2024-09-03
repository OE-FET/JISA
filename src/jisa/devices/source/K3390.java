package jisa.devices.source;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.GPIBAddress;
import jisa.control.SRunnable;
import jisa.devices.DeviceException;
import jisa.enums.OscMode;
import jisa.enums.WaveForm;
import jisa.gui.form.Field;
import jisa.gui.form.Form;
import jisa.visa.VISADevice;
import jisa.visa.drivers.NIGPIBDriver;

import java.io.IOException;

public class K3390 extends VISADevice implements FunctionGenerator {

    public static String getDescription() {
        return "Keithley 3390 Arbitrary Waveform Generator";
    }

    // == CONSTANTS ================================================================================================
    private static final String C_SET_FUNCTION     = "FUNCtion %s";
    private static final String C_READ_FUNCTION    = "FUNCtion?";
    private static final String C_SET_FREQUENCY    = "FREQuency %f";
    private static final String C_READ_FREQUENCY   = "FREQuency?";
    private static final String C_SET_Vpp          = "VOLTage %f";
    private static final String C_READ_Vpp         = "VOLTage?";
    private static final String C_SET_Vos          = "VOLTage:OFFSet %f";
    private static final String C_READ_Vos         = "VOLTage:OFFSet?";
    private static final String C_SET_SIN          = "APPLy:SINusoid %f,%f,%f"; //frequency, Vpp (amplitude), Vos (offset)
    private static final String C_SET_DUTYC        = "FUNCtion:SQUare:DCYCle %f";
    private static final String C_READ_DUTYC       = "FUNCtion:SQUare:DCYCle?";
    private static final String C_TURN_ON_OFF      = "OUTPut %s"; // OFF | ON
    private static       double MIN_FREQUENCY      = +1e-6; // for SIN function (to implement for other waveforms)
    private static       double MAX_FREQUENCY      = +50e6; // for SIN function (to implement for other waveforms)
    private static       double Vmax               = 5;
    private static final int    MIN_WRITE_INTERVAL = 5;

    // == INTERNAL VARIABLES =======================================================================================
    private WaveForm waveForm       = WaveForm.SIN;
    private double   frequencyLevel = 100;
    private double   VppLevel       = 0;
    private double   VosLevel       = 0;
    private double   dutyCycleLevel = 50; // only for SQUARE function

    public K3390(Address address) throws IOException, DeviceException {

        super(address, NIGPIBDriver.class);

        // Config options for when connection is over GPIB
        configGPIB(gpib -> {
            gpib.setEOIEnabled(true);
            setIOLimit(MIN_WRITE_INTERVAL, true, true);
        });

        /**
         Command terminators
         A command string sent to the Model 3390 must terminate with a "new line" character (<nl>). The
         IEEE-488 end-or-identify (EOI) message is interpreted as a new line character and can be used to
         terminate a command string in place of a new line character. A carriage return (<cr>) followed by
         a <nl> is also accepted. A command string terminator will reset the current SCPI command path
         to the root level.
         */
        // Adds <nl> to the end of all outgoing messages
        setWriteTerminator("\n");

        // Tells JISA to look for <nl> when reading incoming messages
        setReadTerminator("\n");
        addAutoRemove("\n");

        setTimeout(500);
        setRetryCount(1);

        // Ask instrument for identification
        String idn = query("*IDN?");

        // Check that the returned identification is as expected
        if (!idn.contains("3390")) {
            throw new DeviceException("This is not a K3390!");
        }
    }


    //Waveform function frequency ranges (default is [1uHz, 50MHz])
    public void setFunctionValue(WaveForm waveForm) throws IOException, DeviceException {
        //System.out.println("Inside setFunctionValue. waForm=" + waveForm);
        switch (waveForm) {

            //FREQuency {<frequency>|MINimum|MAXimum}
            case SIN:
                MAX_FREQUENCY = 50e6;
                break;
            case SQU:
                MAX_FREQUENCY = 25e6;
                break;
            case RAMP:
                MAX_FREQUENCY = 200e3;
                break;
            case PULS:
                MIN_FREQUENCY = 500e-6;
                MAX_FREQUENCY = 10e6;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + waveForm);
        }
        setFunction(waveForm);
    }

    //Set frequency or period from a remote interface:
    public void setFrequencyValue(double level, OscMode oscMode) throws IOException, DeviceException {

        switch (oscMode) {

            //FREQuency {<frequency>|MINimum|MAXimum}
            case FREQUENCY:
                if (!Util.isBetween(level, MIN_FREQUENCY, MAX_FREQUENCY)) {
                    throw new DeviceException("Frequency value of %e Hz is out of range.", level);
                }
                frequencyLevel = level;
                break;

            case PERIOD:
                if (!Util.isBetween(1 / level, MIN_FREQUENCY, MAX_FREQUENCY)) {
                    throw new DeviceException("Period value of %e s is out of range.", level);
                }
                frequencyLevel = 1 / level;
                break;

        }

        setFrequency();
    }

    /**
     * The output amplitude and DC offset values are constrained by the equation below:
     * //V_peak-to-peak ≤ 2× ( Vmax - |Voffset| )
     * //Where: Vmax is the maximum allowed peak voltage for the selected output termination (5 V
     * //for a 50 Ω load, or 10 V for a high-impedance load). When the output termination setting is
     * //changed, the output amplitude automatically adjusts.
     */
    public void setVppVosValues(double level1, double level2) throws IOException, DeviceException {

        double Vhigh = level2 + level1 / 2;
        double Vlow  = level2 - level1 / 2;

        if (Vhigh > Vmax || Vlow < -Vmax) {
            throw new DeviceException("Potential out of range. Vmax is %s", Vmax);
        }

        VppLevel = level1;
        VosLevel = level2;

        setVpp();
        setVos();
    }

    //Set frequency or period from a remote interface:
    public void setDutyCycleLevel(double level) throws IOException, DeviceException {

        dutyCycleLevel = level;

        setDutyCycle();
    }

    public void setFunction(WaveForm waveForm) throws IOException {
        //System.out.println("Inside setFunction. waveForm=" + waveForm);
        write(C_SET_FUNCTION, waveForm);
    }

    public WaveForm readFunction() throws IOException {
        waveForm = WaveForm.valueOf(query(C_READ_FUNCTION));
        return waveForm;
    }

    public void setFrequency() throws IOException {
        write(C_SET_FREQUENCY, frequencyLevel);
    }

    public double readFrequency() throws IOException {
        frequencyLevel = queryDouble(C_READ_FREQUENCY);
        return frequencyLevel;
    }

    public void setVpp() throws IOException {
        write(C_SET_Vpp, VppLevel);
    }

    public double readVpp() throws IOException {
        VppLevel = queryDouble(C_READ_Vpp);
        return VppLevel;
    }

    public void setVos() throws IOException {
        write(C_SET_Vos, VosLevel);
    }

    public double readVos() throws IOException {
        VosLevel = queryDouble(C_READ_Vos);
        return VosLevel;
    }

    public void setSin() throws IOException {
        write(C_SET_SIN, frequencyLevel, VppLevel, VosLevel);
    }

    public void setDutyCycle() throws IOException {
        write(C_SET_DUTYC, dutyCycleLevel);
    }

    public double readDutyCycle() throws IOException {
        dutyCycleLevel = queryDouble(C_READ_DUTYC);
        return dutyCycleLevel;
    }

    public void turnON() throws IOException, DeviceException {
        write(C_TURN_ON_OFF, "ON");
        System.out.println("Output ON");
    }

    public void turnOFF() throws IOException, DeviceException {
        write(C_TURN_ON_OFF, "OFF");
        System.out.println("Output OFF");
    }


    // Method to read the current settings of the k3390
    public WaveformParam readChanges(WaveformParam values) {
        try {
            WaveForm function  = readFunction();
            double   frequency = readFrequency();
            double   Vos       = readVos();
            double   Vpp       = readVpp();
            double   dutyCycle = readDutyCycle();

            values.function  = function;
            values.frequency = frequency;
            values.Vos       = Vos;
            values.Vpp       = Vpp;
            values.DutyCycle = dutyCycle;

            // Print values if needed
            System.out.printf("Function: %s\n", function);
            System.out.printf("Frequency: %.2f Hz\n", frequency);
            System.out.printf("Vos: %.2f V\n", Vos);
            System.out.printf("Vpp: %.2f V\n", Vpp);
            if (function == WaveForm.SQU) {
                System.out.printf("Duty Cycle: %.2f %%\n", dutyCycle);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while applying changes: " + e.getMessage());
            e.printStackTrace();
        }
        return values;
    }

    // Method to write new settings
    public SRunnable applyChanges(Field<Integer> functionField, Field<Integer> choiceBox,
                                  Field<Double> dblFieldFrq, Field<Double> dblFieldVos, Field<Double> dblFieldVpp,
                                  Field<Double> dblFieldDutyCycle) {
        try {

            int      intFunction  = functionField.get();
            WaveForm waveForm     = WaveForm.values()[intFunction];
            int      intOscMode   = choiceBox.get();
            OscMode  selectedMode = OscMode.values()[intOscMode];
            double   dblFrq       = dblFieldFrq.get();
            double   dblVos       = dblFieldVos.get();
            double   dblVpp       = dblFieldVpp.get();
            double   dblDutyCycle = dblFieldDutyCycle.get();

            // Apply function
            setFunctionValue(waveForm);
            if (waveForm == WaveForm.SQU) { setDutyCycleLevel(dblDutyCycle); }
            // Switch case for frequency or period selection
            setFrequencyValue(dblFrq, selectedMode);
            // Apply Vos and Vpp
            setVppVosValues(dblVpp, dblVos);

            System.out.println("Changes applied!");

        } catch (IOException | DeviceException e) {
            System.out.println("An error occurred while applying changes: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ======= GUI to control K3390 ===================================================================================
    public static void main(String[] args) {
        try {
            K3390 k3390 = new K3390(new GPIBAddress(16));

            WaveformParam levels = new WaveformParam();

            // Fetch instrument values
            WaveformParam WFvalues  = k3390.readChanges(levels);
            WaveForm      function  = WFvalues.function;
            double        frequency = WFvalues.frequency;
            double        Vos       = WFvalues.Vos;
            double        Vpp       = WFvalues.Vpp;
            double        dutyCycle = WFvalues.DutyCycle;

            // Fields initialization
            Form fields = new Form("K3390 - GUI");
            fields.show();
            fields.setWindowSize(300, 400);

            // Turn off the instrument and exit the program when closing the GUI
            fields.addCloseListener(() -> {
                try {
                    // Turn off the instrument
                    k3390.turnOFF();
                    // Close the instrument connection
                    k3390.close();
                } catch (IOException | DeviceException e) {
                    System.out.println("An error occurred while closing: " + e.getMessage());
                    e.printStackTrace();
                }
                System.exit(0); // Exit the program
            });

            // FUNCTION field
            Field<Integer> functionField = fields.addChoice("Function:", "Sine", "Square");
            functionField.set(function.ordinal());
            // DUTY CYCLE field for SQU function
            Field<Double> dblFieldDutyCycle = fields.addDoubleField("Duty Cycle (%)");
            if (function == WaveForm.SQU) {
                dblFieldDutyCycle.set(dutyCycle);
                dblFieldDutyCycle.setVisible(true); // Initially show the field
            } else {
                dblFieldDutyCycle.setVisible(false); // Initially hide the field
            }
            // update function and make DutyCycle field appear if SQU is selected
            functionField.addChangeListener(nv -> {
                int      intFunction = functionField.get();
                WaveForm waveForm    = WaveForm.values()[intFunction];
                System.out.printf("Selected function: %s\n", waveForm);
                //DutyCycle for SQUARE function
                if (waveForm == WaveForm.SQU) {
                    dblFieldDutyCycle.setVisible(true); // Show the field if the waveform is SQU
                    dblFieldDutyCycle.set(dutyCycle);
                } else {
                    dblFieldDutyCycle.setVisible(false); // Hide the field if the waveform is not SQU
                }
            });

            // FREQUENCY/PERIOD field
            Field<Integer> choiceBox   = fields.addChoice("Frequency/Period", "Frequency", "Period");
            Field<Double>  dblFieldFrq = fields.addDoubleField("Frequency (Hz)");
            choiceBox.set(0); //Set default to "Frequency"
            dblFieldFrq.set(frequency); // Set fetched frequency

            choiceBox.addChangeListener(nv -> {
                int     intOscMode   = choiceBox.get();
                OscMode selectedMode = OscMode.values()[intOscMode]; // Retrieve the OscMode based on the integer
                System.out.printf("Selected mode: %s\n", selectedMode);
                if (selectedMode == OscMode.FREQUENCY) {
                    dblFieldFrq.setText("Frequency (Hz)");
                    dblFieldFrq.set(frequency);
                } else if (selectedMode == OscMode.PERIOD) {
                    dblFieldFrq.setText("Period (s)");
                    dblFieldFrq.set(1 / frequency);
                }
            });

            dblFieldFrq.addChangeListener(nv -> {
                double dblFrq = dblFieldFrq.get();
                //System.out.printf("Frequency changed to: %.2f Hz\n", dblFrq);
            });

            // Vos and Vpp fields
            Field<Double> dblFieldVos = fields.addDoubleField("Vos (V)");
            dblFieldVos.set(Vos); // Set fetched Vos
            dblFieldVos.addChangeListener(nv -> {
                double dblVos = dblFieldVos.get();
                //System.out.printf("Vos changed to: %.2f V\n", dblVos);
            });

            Field<Double> dblFieldVpp = fields.addDoubleField("Vpp (V)");
            dblFieldVpp.set(Vpp); // Set fetched Vpp
            dblFieldVpp.addChangeListener(nv -> {
                double dblVpp = dblFieldVpp.get();
                //System.out.printf("Vpp changed to: %.2f V\n", dblVpp);
            });

            // Apply button
            fields.addToolbarButton(
                "Apply",
                () -> {
                    k3390.applyChanges(functionField, choiceBox, dblFieldFrq, dblFieldVos, dblFieldVpp, dblFieldDutyCycle);
                    k3390.readChanges(levels);
                });

            // Turn ON and OFF buttons
            fields.addToolbarButton(
                "Turn ON",
                () -> {
                    try {
                        k3390.turnON();
                    } catch (IOException | DeviceException e) {
                        System.out.println("An error occurred while turning ON the instrument: " + e.getMessage());
                        e.printStackTrace();
                    }
                });

            fields.addToolbarButton(
                "Turn OFF",
                () -> {
                    try {
                        k3390.turnOFF();
                    } catch (IOException | DeviceException e) {
                        System.out.println("An error occurred while turning ON the instrument: " + e.getMessage());
                        e.printStackTrace();
                    }
                });


        } catch (IOException | DeviceException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            //scanner.close();
        }
    }
}
