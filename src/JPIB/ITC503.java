package JPIB;

import java.io.IOException;

public class ITC503 extends GPIBDevice {

    private static final String TERMINATOR      = "\r";
    private static final String C_SET_COMM_MODE = "Q2";
    private static final String C_READ          = "R%d";

    public ITC503(int bus, int address) throws IOException, DeviceException {

        super(bus, address, DEFAULT_TIMEOUT, 0, EOS_RETURN);

        setTerminator(TERMINATOR);

        write(C_SET_COMM_MODE);

        try {
            String[] idn = query("V").split(" ");

            if (!idn[0].trim().equals("ITC503")) {
                throw new DeviceException("Device at address %d on bus %d is not an ITC503!", address, bus);
            }
        } catch (IOException e) {
            throw new DeviceException("Device at address %d on bus %d is not responding!", address, bus);
        }

    }

    public double getTargetTemperature() throws IOException {
        String reply = query(C_READ, 0);
        return Double.parseDouble(reply.substring(1));
    }

    public double getTemperature(int sensor) throws IOException, DeviceException {

        if (sensor < 1 || sensor > 3) {
            throw new DeviceException("Sensor index, %d, out of range!", sensor);
        }

        String reply = query(C_READ, sensor);
        return Double.parseDouble(reply.substring(1));

    }
}
