package jisa.devices.function_generator;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.FunctionGenerator;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.List;

public class K3390 extends VISADevice implements FunctionGenerator {

    // Basic functions seem to be ok. The high impedance mode works and gives roughly the correct voltage.

    // output state control
    protected static final String C_SET_OUTPUT_OFF              = "OUTP OFF";
    protected static final String C_SET_OUTPUT_ON               = "OUTP ON";
    protected static final String C_QUERY_OUTPUT_STATE          = "OUTP?";
    protected static final String C_QUERY_LOAD_IMPEDANCE        = "OUTP:LOAD?";
    protected static final String C_SET_STANDARD_LOAD_IMPEDANCE = "OUTP:LOAD 50";
    protected static final String C_SET_HIGH_Z_LOAD_IMPEDANCE   = "OUTP:LOAD INF";
    // output function selection
    protected static final String C_SET_SINE_WAVE               = "FUNC SIN";
    protected static final String C_SET_SQUARE_WAVE             = "FUNC SQU";
    protected static final String C_SET_SQUARE_WAVE_DUTY_CYCLE  = "FUNC:SQU:DCYC %f";
    protected static final String C_QUERY_FUNC                  = "FUNC?";

    // set frequency
    protected static final String C_SET_FREQ_HZ = "FREQ %f";

    // set voltage
    protected static final String C_SET_VOLTAGE_UNIT_TO_VPP = "VOLT:UNIT Vpp";
    protected static final String C_SET_VOLTAGE_AUTO_RANGE  = "VOLT:RANG:AUTO ON"; // good to always enable it
    protected static final String C_SET_AMPLITUDE_VPP       = "VOLT %f";
    protected static final String C_SET_VOLTAGE_OFFSET      = "VOLT:OFFS %f";

    // set synchronization (non-volatile setting)
    protected static final String C_SET_SYNC_OUTPUT_ON      = "OUTP:SYNC ON";
    protected static final String C_SET_SYNC_OUTPUT_OFF     = "OUTP:SYNC OFF";
    protected static final String C_QUERY_SYNC_OUTPUT_STATE = "OUTP:SYNC?";

    protected static final String C_RESET = "*RST";

    // the max voltage in the standard impedance mode
    protected static final double SINE_WAVE_MAX_VOLTAGE   = 5.0;
    protected static final double SQUARE_WAVE_MAX_VOLTAGE = 5.0;

    private boolean is_high_impedance_mode;

    public static String getDescription() {
        return "Keithley 3390 Function Generator";
    }

    @Override
    public boolean validateWaveform(Waveform waveform) {
        double voltage_limit_multiplier = 1.0;
        if (is_high_impedance_mode)
            voltage_limit_multiplier = 2.0;

        if (waveform instanceof SineWave) {
            // freq range given on page 35 "Setting frequency or period" section of the manual
            if (((SineWave) waveform).getFrequency() > 5e7)
                return false;
            if (((SineWave) waveform).getFrequency() < 1e-6)
                return false;
            // check voltage range (pm 5V at 50 Ohm output impedance)
            if (((SineWave) waveform).getAmplitude() + Math.abs(((SineWave) waveform).getOffset()) > SINE_WAVE_MAX_VOLTAGE*voltage_limit_multiplier)
                return false;
            return true;
        }

        if (waveform instanceof  SquareWave)
        {
            // freq range given on page 35 "Setting frequency or period" section of the manual
            if (((SquareWave) waveform).getFrequency() > 2.5e7)
                return false;
            if (((SquareWave) waveform).getFrequency() < 1e-6)
                return false;

            // check voltage range (pm 5V at 50 Ohm output impedance)
            if (((SquareWave) waveform).getAmplitude() + Math.abs(((SquareWave) waveform).getOffset())> SQUARE_WAVE_MAX_VOLTAGE*voltage_limit_multiplier)
                return false;

            // Duty cycle range given on page 45 of the manual
            if (((SquareWave) waveform).getFrequency() > 1e7){
                if (((SquareWave) waveform).getDutyCycle() > 0.6)
                    return false;
                if (((SquareWave) waveform).getDutyCycle() < 0.4)
                    return false;
            }
            else {
                if (((SquareWave) waveform).getDutyCycle() > 0.8)
                    return false;
                if (((SquareWave) waveform).getDutyCycle() < 0.2)
                    return false;
            }
            return true;
        }
        return false;
    }

    public void turnOnSynchronizationSignal() throws IOException{
        write(C_SET_SYNC_OUTPUT_ON);
    }

    public void turnOffSynchronizationSignal() throws IOException{
        write(C_SET_SYNC_OUTPUT_OFF);
    }

    public void setHighImpedanceMode() throws IOException{
        write(C_SET_HIGH_Z_LOAD_IMPEDANCE);
        is_high_impedance_mode = true;
    }

    public void setStandardImpedanceMode() throws IOException{
        write(C_SET_STANDARD_LOAD_IMPEDANCE);
        is_high_impedance_mode = false;
    }

    @Override
    public void turnOn() throws IOException, DeviceException {
        write(C_SET_OUTPUT_ON);
    }

    @Override
    public void turnOff() throws IOException, DeviceException {
        write(C_SET_OUTPUT_OFF);
    }

    @Override
    public boolean isOn() throws IOException, DeviceException {
        return query(C_QUERY_OUTPUT_STATE).trim().equals("1");
    }

    @Override
    public void setWaveform(Waveform waveform) throws DeviceException, IOException {
        if (!validateWaveform(waveform))
            throw new DeviceException("Waveform not supported!");
        if (waveform instanceof SineWave) {
            write(C_SET_SINE_WAVE);
            write(C_SET_FREQ_HZ, ((SineWave) waveform).getFrequency());
            write(C_SET_AMPLITUDE_VPP, ((SineWave) waveform).getAmplitude()*2);
            write(C_SET_VOLTAGE_OFFSET, ((SineWave) waveform).getOffset());
        }

        if (waveform instanceof SquareWave) {
            write(C_SET_SQUARE_WAVE);
            write(C_SET_FREQ_HZ, ((SquareWave) waveform).getFrequency());
            write(C_SET_AMPLITUDE_VPP, ((SquareWave) waveform).getAmplitude()*2);
            write(C_SET_VOLTAGE_OFFSET, ((SquareWave) waveform).getOffset());
            write(C_SET_SQUARE_WAVE_DUTY_CYCLE, ((SquareWave) waveform).getDutyCycle()*100);
        }
    }

    @Override
    public void outputWaveform(Waveform waveform) throws DeviceException, IOException {
        setWaveform(waveform);
        if (!isOn())
            turnOn();
    }

    @Override
    public List<Class<? extends Waveform>> getSupportedWaveforms() {
        // not so sure about the implementations here.
        return null;
    }

    @Override
    public void reset() throws IOException {
        write(C_RESET);
        is_high_impedance_mode = false;
    }

    public K3390(Address address) throws IOException, DeviceException {
        super(address);
        addAutoRemove("\r");
        addAutoRemove("\n");
        setWriteTerminator("\n");
        setReadTerminator("\n");
        reset();
        try {
            String[] idn = query("*IDN?").split(",");
            if (!idn[1].trim().equals("K3390")) {
                throw new DeviceException("Device at address %s is not an SR830!", address.toString());
            }
        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.toString());
        }

        write(C_SET_VOLTAGE_UNIT_TO_VPP);
        turnOffSynchronizationSignal();
        if (Double.compare(queryDouble(C_QUERY_LOAD_IMPEDANCE), 50) != 0)
        {
            System.out.println("Output impedance currently at non-standard value. Set to 50 Ohms!");
            write(C_SET_STANDARD_LOAD_IMPEDANCE);
            is_high_impedance_mode = false;
        }
    }
}
