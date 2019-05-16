package JISA.Devices;

import JISA.Addresses.Address;
import JISA.VISA.ModbusRTUDevice;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;

import java.io.IOException;

public class ET2408 extends ModbusRTUDevice implements TC {

    // Constants
    private static final int UNITS_CELSIUS    = 0;
    private static final int UNITS_FAHRENHEIT = 1;
    private static final int UNITS_KELVIN     = 2;
    private static final int UNITS_NONE       = 3;
    private static final int MODE_CONFIG      = 2;
    private static final int MODE_NORMAL      = 0;

    // Auto-zoner
    private Zoner zoner = null;

    // Registers and Coils
    private final RORegister sensor   = new RORegister(1);
    private final RWRegister setPoint = new RWRegister(2);
    private final RWRegister output   = new RWRegister(3);
    private final RWRegister P        = new RWRegister(6);
    private final RWRegister I        = new RWRegister(8);
    private final RWRegister D        = new RWRegister(9);
    private final RWRegister mode     = new RWRegister(199);
    private final RWRegister units    = new RWRegister(516);
    private final RWCoil     manual   = new RWCoil(273);

    public ET2408(Address address, SerialPort.BaudRate baud, int dataBits, int stopBits, SerialPort.Parity parity) throws IOException, DeviceException {

        super(address, baud, dataBits, stopBits, parity);

        if (units.get() != UNITS_KELVIN) {

            mode.set(MODE_CONFIG);
            units.set(UNITS_KELVIN);
            mode.set(MODE_NORMAL);

        }

    }

    /**
     * Opens a connection to a Eurotherm 2408 process controller at the given modbus address, using default serial parameters.
     *
     * @param address Modbus address
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon incompatibility error
     */
    public ET2408(Address address) throws IOException, DeviceException {
        this(address, SerialPort.BaudRate.BAUD_RATE_9600, 8, 1, SerialPort.Parity.NONE);
    }

    @Override
    public void setTargetTemperature(double temperature) throws IOException {
        setPoint.set((int) temperature);
    }

    @Override
    public double getTemperature() throws IOException {
        return (double) sensor.get();
    }

    @Override
    public double getTargetTemperature() throws IOException {
        return (double) setPoint.get();
    }

    @Override
    public double getHeaterPower() throws IOException {
        return (double) output.get();
    }

    @Override
    public double getGasFlow() {
        return 0;
    }

    @Override
    public void useAutoHeater() throws IOException {
        manual.set(false);
    }

    @Override
    public void setManualHeater(double powerPCT) throws IOException {
        manual.set(true);
        output.set((int) (powerPCT));
    }

    @Override
    public boolean isHeaterAuto() throws IOException {
        return !manual.get();
    }

    @Override
    public void useAutoFlow() {

    }

    @Override
    public void setManualFlow(double outputPCT) {

    }

    @Override
    public boolean isFlowAuto() {
        return false;
    }

    @Override
    public void setPValue(double value) throws IOException {
        P.set((int) (value));
    }

    @Override
    public void setIValue(double value) throws IOException {
        I.set((int) (value));
    }

    @Override
    public void setDValue(double value) throws IOException {
        D.set((int) (value));
    }

    @Override
    public double getPValue() throws IOException {
        return (double) P.get();
    }

    @Override
    public double getIValue() throws IOException {
        return (double) I.get();
    }

    @Override
    public double getDValue() throws IOException {
        return (double) D.get();
    }

    @Override
    public void setHeaterRange(double rangePCT) {

    }

    @Override
    public double getHeaterRange() {
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
