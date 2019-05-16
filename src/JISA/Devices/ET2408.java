package JISA.Devices;

import JISA.Addresses.Address;
import JISA.VISA.ModbusRTUDevice;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;

import java.io.IOException;

public class ET2408 extends ModbusRTUDevice implements TC {

    private Zoner zoner = null;

    private RORegister sensor   = new RORegister(1);
    private RWRegister setPoint = new RWRegister(2);
    private RWRegister output   = new RWRegister(3);
    private RWRegister P        = new RWRegister(6);
    private RWRegister I        = new RWRegister(8);
    private RWRegister D        = new RWRegister(9);
    private RWCoil     manual   = new RWCoil(273);

    /**
     * Opens the device at the specified address
     *
     * @param address Some form of InstrumentAddress (eg GPIBAddress, USBAddress etc)
     *
     * @throws IOException Upon communications error
     */
    public ET2408(Address address) throws IOException, DeviceException {
        super(address, SerialPort.BaudRate.BAUD_RATE_9600, 7, 1, SerialPort.Parity.EVEN);
    }

    @Override
    public void setTargetTemperature(double temperature) throws IOException, DeviceException {
        setPoint.set((int) (temperature * 100));
    }

    @Override
    public double getTemperature() throws IOException, DeviceException {
        return (double) sensor.get() / 100.0;
    }

    @Override
    public double getTargetTemperature() throws IOException, DeviceException {
        return (double) setPoint.get() / 100.0;
    }

    @Override
    public double getHeaterPower() throws IOException, DeviceException {
        return (double) output.get() / 100.0;
    }

    @Override
    public double getGasFlow() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void useAutoHeater() throws IOException, DeviceException {
        manual.set(false);
    }

    @Override
    public void setManualHeater(double powerPCT) throws IOException, DeviceException {
        manual.set(true);
        output.set((int) (powerPCT * 100.0));
    }

    @Override
    public boolean isHeaterAuto() throws IOException, DeviceException {
        return !manual.get();
    }

    @Override
    public void useAutoFlow() throws IOException, DeviceException {

    }

    @Override
    public void setManualFlow(double outputPCT) throws IOException, DeviceException {

    }

    @Override
    public boolean isFlowAuto() throws IOException, DeviceException {
        return false;
    }

    @Override
    public void setPValue(double value) throws IOException, DeviceException {
        P.set((int) (value * 100.0));
    }

    @Override
    public void setIValue(double value) throws IOException, DeviceException {
        I.set((int) (value * 100.0));
    }

    @Override
    public void setDValue(double value) throws IOException, DeviceException {
        D.set((int) (value * 100.0));
    }

    @Override
    public double getPValue() throws IOException, DeviceException {
        return (double) P.get() / 100.0;
    }

    @Override
    public double getIValue() throws IOException, DeviceException {
        return (double) I.get() / 100.0;
    }

    @Override
    public double getDValue() throws IOException, DeviceException {
        return (double) D.get() / 100.0;
    }

    @Override
    public void setHeaterRange(double rangePCT) throws IOException, DeviceException {

    }

    @Override
    public double getHeaterRange() throws IOException, DeviceException {
        return 100.0;
    }

    @Override
    public Zoner getZoner() {
        return zoner;
    }

    @Override
    public void setZoner(Zoner zoner) {
        this.zoner = zoner;
    }
}
