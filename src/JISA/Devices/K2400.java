package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import com.sun.javafx.UnmodifiableArrayList;

import java.io.IOException;
import java.util.List;

public class K2400 extends SMU {

    public K2400(InstrumentAddress address) throws IOException {
        super(address);
    }

    @Override
    public double getVoltage() throws DeviceException, IOException {
        return queryDouble(":MEAS:VOLT?");
    }

    @Override
    public double getCurrent() throws DeviceException, IOException {
        return queryDouble(":MEAS:CURR?");
    }

    @Override
    public void setVoltage(double voltage) throws DeviceException, IOException {
        write(":SOUR:VOLT %f", voltage);
        write(":SOUR:FUNC VOLT");
    }

    @Override
    public void setCurrent(double current) throws DeviceException, IOException {
        write(":SOUR:CURR %f", current);
        write(":SOUR:FUNC CURR");
    }

    @Override
    public void turnOn() throws DeviceException, IOException {
        write(":OUTP:STATE ON");
    }

    @Override
    public void turnOff() throws DeviceException, IOException {
        write(":OUTP:STATE OFF");
    }

    @Override
    public boolean isOn() throws DeviceException, IOException {
        return query(":OUTP:STATE?").trim().equals("1");
    }

    @Override
    public void setSource(Source source) throws DeviceException, IOException {

        switch (source) {

            case VOLTAGE:
                write(":SOUR:FUNC VOLT");
                break;

            case CURRENT:
                write(":SOUR:FUNC CURR");
                break;

        }

    }

    @Override
    public Source getSource() throws DeviceException, IOException {

        String response = query(":SOUR:FUNC?").trim();

        if (response.equals("VOLT")) {
            return Source.VOLTAGE;
        } else if (response.equals("CURR")) {
            return Source.CURRENT;
        } else {
            throw new IOException("Invalid response from Keithley 2400");
        }

    }

    @Override
    public void setBias(double level) throws DeviceException, IOException {

        switch (getSource()) {

            case VOLTAGE:
                setVoltage(level);
                break;

            case CURRENT:
                setCurrent(level);
                break;

        }

    }

    @Override
    public double getSourceValue() throws DeviceException, IOException {

        switch (getSource()) {

            case VOLTAGE:
                return getVoltage();

            case CURRENT:
                return getCurrent();

            default:
                return getVoltage();

        }

    }

    @Override
    public double getMeasureValue() throws DeviceException, IOException {

        switch (getSource()) {

            case VOLTAGE:
                return getCurrent();

            case CURRENT:
                return getVoltage();

            default:
                return getCurrent();

        }

    }

    @Override
    public void useFourProbe(boolean fourProbes) throws DeviceException, IOException {

    }

    @Override
    public boolean isUsingFourProbe() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setAverageMode(AMode mode) throws DeviceException, IOException {

    }

    @Override
    public void setAverageCount(int count) throws DeviceException, IOException {

    }

    @Override
    public AMode getAverageMode() throws DeviceException, IOException {
        return null;
    }

    @Override
    public int getAverageCount() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setSourceRange(double value) throws DeviceException, IOException {

    }

    @Override
    public double getSourceRange() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoSourceRange() throws DeviceException, IOException {

    }

    @Override
    public boolean isSourceRangeAuto() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setMeasureRange(double value) throws DeviceException, IOException {

    }

    @Override
    public double getMeasureRange() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoMeasureRange() throws DeviceException, IOException {

    }

    @Override
    public boolean isMeasureRangeAuto() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setVoltageRange(double value) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageRange() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoVoltageRange() throws DeviceException, IOException {

    }

    @Override
    public boolean isVoltageRangeAuto() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setCurrentRange(double value) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentRange() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoCurrentRange() throws DeviceException, IOException {

    }

    @Override
    public boolean isCurrentRangeAuto() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setOutputLimit(double value) throws DeviceException, IOException {

    }

    @Override
    public double getOutputLimit() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setVoltageLimit(double voltage) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageLimit() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setCurrentLimit(double current) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentLimit() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setIntegrationTime(double time) throws DeviceException, IOException {

    }

    @Override
    public double getIntegrationTime() throws DeviceException, IOException {
        return 0;
    }

    public TType getTerminalType(Terminals terminals) {
        return TType.BANANA;
    }

    @Override
    public void setTerminals(Terminals terminals) throws DeviceException, IOException {

        switch (terminals) {

            case FRONT:
                write(":ROUT:TERM FRONT");
                break;

            case REAR:
                write(":ROUT:TERM REAR");
                break;

        }

    }

    @Override
    public Terminals getTerminals() throws IOException {

        String response = query(":ROUT:TERM?");

        if (response.contains("FRON")) {
            return Terminals.FRONT;
        } else if (response.contains("REAR")) {
            return Terminals.REAR;
        } else {
            throw new IOException("Invalid response from Keithley 2400");
        }

    }
}
