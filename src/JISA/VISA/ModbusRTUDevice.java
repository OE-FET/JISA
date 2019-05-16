package JISA.VISA;

import JISA.Addresses.Address;
import JISA.Addresses.ModbusAddress;
import JISA.Addresses.SerialAddress;
import JISA.Devices.DeviceException;
import JISA.Devices.Instrument;
import JISA.Util;
import com.intelligt.modbus.jlibmodbus.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import jssc.SerialPortList;

import java.io.IOException;
import java.util.regex.Pattern;

public class ModbusRTUDevice implements Instrument {

    private final int port;
    private final int unit;

    private ModbusMaster master;

    public ModbusRTUDevice(Address address, SerialPort.BaudRate baud, int dataBits, int stopBits, SerialPort.Parity parity) throws IOException, DeviceException {

        ModbusAddress mba = address.toModbusAddress();

        if (mba == null) {
            throw new DeviceException("This is a modbus RTU driver, therefore it needs a modbus address.");
        }

        port = mba.getPort();
        unit = mba.getAddress();

        String[] portNames = SerialPortList.getPortNames(Pattern.compile(String.valueOf(port)));

        if (portNames.length == 0) {
            throw new IOException(String.format("Serial port %d does not exist.", port));
        }

        SerialParameters parameters = new SerialParameters(portNames[0], baud, dataBits, stopBits, parity);

        try {
            master = ModbusMasterFactory.createModbusMasterRTU(parameters);
            setTimeout(2000);
            master.connect();
        } catch (SerialPortException | ModbusIOException e) {
            throw new IOException(e.getMessage());
        }

    }

    public void setTimeout(int msec) {
        master.setResponseTimeout(msec);
    }

    @Override
    public String getIDN() throws IOException {
        return String.format("Modbus RTU device, unit address %d", unit);
    }

    @Override
    public void close() throws IOException {

        try {
            master.disconnect();
        } catch (ModbusIOException e) {
            throw new IOException(e.getMessage());
        }

    }

    @Override
    public Address getAddress() {
        return new ModbusAddress(port, unit);
    }

    public class RWCoil {

        private final int register;

        public RWCoil(int register) {
            this.register = register;
        }

        public boolean get() throws IOException {

            try {
                return master.readCoils(unit, register, 1)[0];
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

        }

        public void set(boolean value) throws IOException {

            try {
                master.writeSingleCoil(unit, register, value);
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

        }

    }

    public class ROCoil {

        private final int register;

        public ROCoil(int register) {
            this.register = register;
        }

        public boolean get() throws IOException {

            try {
                return master.readDiscreteInputs(unit, register, 1)[0];
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

        }

    }

    public class RWRegister {

        private final int register;

        public RWRegister(int register) {
            this.register = register;
        }

        public int get() throws IOException {

            try {
                return master.readHoldingRegisters(unit, register, 1)[0];
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

        }

        public void set(int value) throws IOException {

            try {
                master.writeSingleRegister(unit, register, value);
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

        }

    }

    public class RORegister {

        private final int register;

        public RORegister(int register) {
            this.register = register;
        }

        public int get() throws IOException {

            try {
                return master.readInputRegisters(unit, register, 1)[0];
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

        }

    }

}
