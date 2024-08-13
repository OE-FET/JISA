package jisa.devices.source;

import jisa.control.SRunnable;
import jisa.devices.DeviceException;
import jisa.enums.OscMode;
import jisa.enums.WaveForm;
import jisa.gui.Field;

import java.io.IOException;

public interface FunctionGenerator {

    class  WaveformParam {
        public WaveForm function;
        public double frequency;
        public double Vos;
        public double Vpp;
        public double DutyCycle;
    }

    void setFunctionValue (WaveForm waveForm) throws IOException, DeviceException;

    void setFrequencyValue (double level, OscMode oscMode) throws IOException, DeviceException;

    void setVppVosValues (double level1, double level2) throws IOException, DeviceException;

    void setDutyCycleLevel (double level) throws IOException, DeviceException;

    void setFunction(WaveForm waveForm) throws IOException;

    WaveForm readFunction() throws IOException;

    void setFrequency() throws IOException;

    double readFrequency() throws IOException;

    void setVpp() throws IOException;

    double readVpp() throws IOException;

    void setVos() throws IOException;

    double readVos() throws IOException;

    void setSin() throws IOException;

    void setDutyCycle() throws IOException;

    double readDutyCycle() throws IOException;

    void turnON() throws IOException, DeviceException;

    void turnOFF() throws IOException, DeviceException;

    // Method to read the current settings of the k3390
    WaveformParam readChanges(WaveformParam values);

    SRunnable applyChanges(Field<Integer> functionField, Field<Integer> choiceBox,Field<Double> dblFieldFrq, Field<Double> dblFieldVos, Field<Double> dblFieldVpp,Field<Double> dblFieldDutyCycle);

}
