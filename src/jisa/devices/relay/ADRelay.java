package jisa.devices.relay;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.SubInstrument;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ADRelay extends VISADevice implements MSwitch<ADRelay.Channel> {

    public static String getDescription() {
        return "Arduino-Controlled Relay";
    }

    private final int           NUM_CHANNELS;
    private final List<Channel> channels;

    public ADRelay(Address address) throws IOException, DeviceException {

        super(address);

        configSerial(serial -> serial.setSerialParameters(9600, 8));

        setTimeout(2000);

        setReadTerminator("\n");
        setWriteTerminator("\n");
        addAutoRemove("\r");
        addAutoRemove("\n");

        Util.sleep(2000); // Need to wait for Arduino to initialise serial routines.

        String idn = getIDN().trim();

        if (!idn.equals("Arduino Controlled Relay")) {
            throw new DeviceException("Device at \"%s\" is not an Arduino Controlled Relay.", address.toString());
        }

        NUM_CHANNELS = queryInt("NCHANS?");
        channels     = IntStream.rangeClosed(1, NUM_CHANNELS)
                                .mapToObj(Channel::new)
                                .collect(Collectors.toUnmodifiableList());

    }

    @Override
    public List<Channel> getSwitches() {
        return channels;
    }

    public class Channel implements Switch, SubInstrument<ADRelay> {

        private final int channel;

        public Channel(int channel) {
            this.channel = channel;
        }

        @Override
        public ADRelay getParentInstrument() {
            return ADRelay.this;
        }

        @Override
        public void turnOn() throws IOException, DeviceException {
            write("OUTP %d 1", channel);
        }

        @Override
        public void turnOff() throws IOException, DeviceException {
            write("OUTP %d 0", channel);
        }

        @Override
        public boolean isOn() throws IOException, DeviceException {
            return queryInt("OUTP? %d", channel) == 1;
        }

        /**
         * Returns the name of the instrument or channel.
         *
         * @return Name
         */
        @Override
        public String getName() {
            return String.format("Relay %d", channel);
        }

    }

}
