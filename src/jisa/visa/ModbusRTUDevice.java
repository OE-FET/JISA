package jisa.visa;

import jisa.addresses.Address;
import jisa.addresses.ModbusAddress;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Instrument;
import com.intelligt.modbus.jlibmodbus.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import jssc.SerialPortList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ModbusRTUDevice implements Instrument {

    private final String port;
    private final int    unit;

    private static final Map<String, ModbusMaster> masters = new HashMap<>();
    private final        ModbusMaster              master;

    public ModbusRTUDevice(Address address, SerialPort.BaudRate baud, int dataBits, int stopBits, SerialPort.Parity parity) throws IOException, DeviceException {

        if (!(address instanceof ModbusAddress)) {
            throw new DeviceException("This is a modbus RTU driver, therefore it needs a modbus address.");
        }

        ModbusAddress mba = (ModbusAddress) address;

        port = mba.getPortName();
        unit = mba.getAddress();

        String[] portNames = SerialPortList.getPortNames();
        String   found     = null;

        for (String name : portNames) {

            if (name.trim().equals(port.trim())) {
                found = name;
                break;
            }

        }

        if (found == null) {
            throw new IOException(String.format("No native serial port \"%s\" was found.", port.trim()));
        }

        SerialParameters parameters = new SerialParameters(found, baud, dataBits, stopBits, parity);

        if (!masters.containsKey(port.toLowerCase().trim()) || !masters.get(port.toLowerCase().trim()).isConnected()) {

            try {
                master = ModbusMasterFactory.createModbusMasterRTU(parameters);
                setTimeout(2000);
                master.connect();
                masters.put(port.toLowerCase().trim(), master);
            } catch (SerialPortException | ModbusIOException e) {
                throw new IOException(e.getMessage());
            }

        } else {
            master = masters.get(port.toLowerCase().trim());
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
    public String getName() {
        return "Modbus Device";
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
