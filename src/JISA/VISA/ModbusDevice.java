package JISA.VISA;

import JISA.Addresses.Address;

import java.io.IOException;

public class ModbusDevice extends VISADevice {

    public final static byte FORCE_SINGLE_COIL = (byte) 0x05;
    public final static int  COIL_ON           = 0xFF00;
    public final static int  COIL_OFF          = 0x0000;
    private             byte modbusAddress     = -128;

    /**
     * Opens the device at the specified address
     *
     * @param address Some form of InstrumentAddress (eg GPIBAddress, USBAddress etc)
     *
     * @throws IOException Upon communications error
     */
    public ModbusDevice(Address address) throws IOException {
        super(address);
    }

    public void modbusWrite(ModbusFrame frame) throws IOException {
        write(new String(frame.getBytes()));
    }

    public ModbusFrame modbusRead() throws IOException {
        return new ModbusFrame(read().getBytes());
    }

    public void forceCoil(int coil, boolean value) throws IOException {
        modbusWrite(new ModbusFrame(modbusAddress, FORCE_SINGLE_COIL, coil, value ? COIL_ON : COIL_OFF));
    }

    public static class ModbusFrame {

        private byte[] raw;
        private byte   address;
        private byte   command;
        private byte[] data;
        private int    crc;

        public ModbusFrame(byte[] bytes) {

            int length = bytes.length;

            address = bytes[0];
            command = bytes[1];
            data = new byte[length - 4];

            for (int i = 2; i < length - 2; i++) {
                data[i - 2] = bytes[i];
            }

            crc = ((bytes[length - 2] & 0xff) << 8) | (bytes[length - 1] & 0xff);
            raw = bytes;

        }

        public ModbusFrame(byte address, byte command, int... data) {

            this.address = address;
            this.command = command;
            this.data = new byte[2 * data.length];
            for (int i = 0; i < data.length; i++) {
                this.data[2 * i] = (byte) (data[i] & 0xFF);
                this.data[2 * i + 1] = (byte) ((data[i] >> 8) & 0xFF);
            }

        }

        public ModbusFrame(byte address, byte command, byte... data) {
            this.address = address;
            this.command = command;
            this.data = data;
        }

        private void makeRaw() {
            raw = new byte[4 + data.length];
            raw[0] = address;
            raw[1] = command;
            for (int i = 0; i < data.length; i++) {
                raw[2 + i] = data[i];
            }
            crc = calcCRC(raw, 2 + data.length);
            raw[raw.length - 2] = (byte) (crc & 0xFF);
            raw[raw.length - 1] = (byte) ((crc >> 8) & 0xFF);
        }

        private static int calcCRC(byte[] buf, int len) {
            int crc = 0xFFFF;

            for (int pos = 0; pos < len; pos++) {
                crc ^= (int) buf[pos] & 0xFF;   // XOR byte into least sig. byte of crc

                for (int i = 8; i != 0; i--) {    // Loop over each bit
                    if ((crc & 0x0001) != 0) {      // If the LSB is set
                        crc >>= 1;                    // Shift right and XOR 0xA001
                        crc ^= 0xA001;
                    } else                            // Else LSB is not set
                        crc >>= 1;                    // Just shift right
                }
            }
            // Note, this number has low and high bytes swapped, so use it accordingly (or swap bytes)
            return crc;
        }

        public byte[] getBytes() {
            return raw;
        }

        public byte getAddress() {
            return address;
        }

        public byte getCommand() {
            return command;
        }

        public byte[] getDataBytes() {
            return data;
        }

        public int[] getDataInts() {

            int[] values = new int[data.length / 2];

            for (int i = 0; i < data.length; i += 2) {
                values[i / 2] = ((data[i] & 0xff) << 8) | (data[i + 1] & 0xff);
            }

            return values;

        }

    }

}
