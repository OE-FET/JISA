package jisa.devices.mux;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.SubInstrument;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class AMux2x24 extends VISADevice implements MultiMultiplexer<AMux2x24.Bank> {

    public final Bank A = new Bank("A");
    public final Bank B = new Bank("B");

    private final List<Bank> CHANNELS = List.of(A, B);

    public AMux2x24(Address address) throws IOException, DeviceException {

        super(address);

        configSerial(serial -> serial.setSerialParameters(9600, 8));

        setReadTerminator("\n");
        setWriteTerminator("\n");
        addAutoRemove("\r", "\n");

        String ready = read();

        if (!ready.startsWith("READY")) {
            throw new DeviceException("Expected ready signal, but got: " + ready);
        }

        String idn = getIDN();

        if (!idn.contains("ARDUINO-CONTROLLED ELECTROMECHANICAL MULTIPLEXER")) {
            throw new DeviceException("Invalid IDN: " + idn);
        }

    }

    @Override
    public List<Bank> getMultiplexers() {
        return CHANNELS;
    }

    public class Bank implements Multiplexer, SubInstrument<AMux2x24> {

        private final String channel;

        public Bank(String channel) {
            this.channel = channel;
        }

        @Override
        public int getNumRoutes() {
            return 24;
        }

        @Override
        public int getRoute() throws IOException {

            String response = query("ROUTE?");

            return Arrays.stream(response.split(" "))
                         .map(s -> s.split("="))
                         .filter(s -> s[0].equalsIgnoreCase(channel))
                         .map(s -> Integer.parseInt(s[1]))
                         .findAny().orElse(0);

        }

        @Override
        public void setRoute(int route) throws IOException, DeviceException {

            String response = query("ROUTE %s,%d", channel, route);

            if (!response.startsWith("OK")) {
                throw new DeviceException(response);
            }

        }

        @Override
        public String getName() {
            return String.format("Bank %s", channel);
        }

        @Override
        public AMux2x24 getParentInstrument() {
            return AMux2x24.this;
        }

    }

}
