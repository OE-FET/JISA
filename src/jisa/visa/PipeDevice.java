package jisa.visa;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.PipeAddress;
import jisa.devices.Instrument;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * Abstract base class for creating device drivers that communicate using Windows Named Pipes (eugh).
 */
public abstract class PipeDevice implements Instrument {

    private final RandomAccessFile pipe;
    private final PipeAddress      address;
    private       int              timeOut = 500;

    public PipeDevice(Address address) throws IOException {

        if (!(address instanceof PipeAddress)) {
            throw new IOException("This instrument requires a windows named pipe address.");
        }

        PipeAddress pipeAddress = (PipeAddress) address;

        this.pipe    = new RandomAccessFile(pipeAddress.getPipeName(), "rw");
        this.address = pipeAddress;

    }

    public void close() throws IOException {
        pipe.close();
    }

    @Override
    public Address getAddress() {
        return address;
    }

    public String read() throws IOException {

        Thread current = Thread.currentThread();

        (new Thread(() -> {
            Util.sleep(timeOut);
            current.interrupt();
        })).start();

        return pipe.readLine();

    }

    public int readInt() throws IOException {
        return Integer.parseInt(read());
    }

    public double readDouble() throws IOException {
        return Double.parseDouble(read());
    }

    public byte[] readBytes() throws IOException {
        return read().getBytes(StandardCharsets.UTF_8);
    }

    public void write(String toWrite, Object... params) throws IOException {
        pipe.writeChars(String.format(toWrite, params) + "\n");
    }

    @Override
    public void setTimeout(int mSec) {
        timeOut = mSec;
    }

}
