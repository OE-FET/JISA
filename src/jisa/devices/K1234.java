package jisa.devices;

import jisa.addresses.Address;

import java.io.IOException;

public class K1234 extends DummyMCSMU {

    public static String getDescription() {
        return "Kiethley Fake 1234";
    }

    public K1234(Address address) throws IOException {
        super();
    }

}
