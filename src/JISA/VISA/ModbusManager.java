package JISA.VISA;

import JISA.Addresses.SerialAddress;
import JISA.Util;
import JISA.VISA.ModbusDevice.ModbusFrame;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ModbusManager {

    private static HashMap<Integer, ModbusManager> managers = new HashMap<>();

    private Connection                           connection;
    private HashMap<Integer, Queue<ModbusFrame>> frames  = new HashMap<>();
    private boolean                              running = false;

    public static ModbusManager getManager(int port) throws VISAException {

        if (managers.containsKey(port)) {
            return managers.get(port);
        } else {
            Connection connection = VISA.openInstrument(new SerialAddress(port));
            return new ModbusManager(port, connection);
        }

    }

    private ModbusManager(int port, Connection connection) throws VISAException {

        this.connection = connection;
        managers.put(port, this);

        connection.setTMO(0);
        connection.setEOS(0);

        (new Thread(this::run)).start();

    }

    public void registerSlave(int address) {
        frames.put(address, new ConcurrentLinkedQueue<>());
    }

    public void run() {

        byte   head;
        byte   func;
        byte   count;
        byte[] data;

        while (running) {
            try {
                head = connection.readBytes(1)[0];
                func = connection.readBytes(1)[0];
                count = connection.readBytes(1)[0];
                data = connection.readBytes(count);
                connection.readBytes(2);
                frames.get((int) head).add(new ModbusFrame(head, func, data));
            } catch (VISAException e) {
                continue;
            }

        }

    }

    public synchronized void sendFrame(ModbusFrame frame) throws VISAException {
        connection.writeBytes(frame.getBytes());
    }

    public ModbusFrame getNextFrame(int slave) {

        ModbusFrame frame;

        while ((frame = frames.get(slave).poll()) == null) {
            Util.sleep(10);
        }

        return frame;
    }

}
