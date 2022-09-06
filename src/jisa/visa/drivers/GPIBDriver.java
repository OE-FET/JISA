package jisa.visa.drivers;

import com.sun.jna.*;
import jisa.addresses.Address;
import jisa.addresses.GPIBAddress;
import jisa.visa.NativeString;
import jisa.visa.VISAException;
import jisa.visa.connections.Connection;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GPIBDriver implements Driver {

    protected static       GPIBNativeInterface       lib;
    protected static final String                    OS_NAME          = System.getProperty("os.name").toLowerCase();
    protected static       String                    libName;
    protected static final String                    responseEncoding = "UTF8";
    protected static final long                      VISA_ERROR       = 0x7FFFFFFF;
    protected static final int                       _VI_ERROR        = -2147483648;
    protected static final int                       VI_SUCCESS       = 0;
    protected static final int                       VI_NULL          = 0;
    protected static final int                       VI_TRUE          = 1;
    protected static final int                       VI_FALSE         = 0;
    protected static       NativeLong                visaResourceManagerHandle;
    protected static       HashMap<Long, NativeLong> instruments      = new HashMap<>();

    private enum TMO {

        TNONE(GPIBNativeInterface.TNONE, 0),
        T10us(GPIBNativeInterface.T10us, 0),
        T30us(GPIBNativeInterface.T30us, 0),
        T100us(GPIBNativeInterface.T100us, 0),
        T300us(GPIBNativeInterface.T300us, 0),
        T1ms(GPIBNativeInterface.T1ms, 1),
        T3ms(GPIBNativeInterface.T3ms, 3),
        T10ms(GPIBNativeInterface.T10ms, 10),
        T30ms(GPIBNativeInterface.T30ms, 30),
        T100ms(GPIBNativeInterface.T100ms, 100),
        T300ms(GPIBNativeInterface.T300ms, 300),
        T1s(GPIBNativeInterface.T1s, 1000),
        T3s(GPIBNativeInterface.T3s, 3000),
        T10s(GPIBNativeInterface.T10s, 10000),
        T30s(GPIBNativeInterface.T30s, 30000),
        T100s(GPIBNativeInterface.T100s, 100000),
        T300s(GPIBNativeInterface.T300s, 300000),
        T1000s(GPIBNativeInterface.T1000s, 1000000);

        public static TMO fromMSec(long mSec) {

            TMO closest = T1000s;

            for (TMO t : values()) {

                if (t.getValue() >= mSec && t.getValue() < closest.getValue()) {
                    closest = t;
                }

            }

            return closest;

        }

        private final int  code;
        private final long value;

        TMO(int code, long value) {
            this.code  = code;
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public long getValue() {
            return value;
        }

    }

    public static void init() throws VISAException {

        try {
            if (OS_NAME.contains("win")) {
                libName = "ni4882";
                lib     = Native.loadLibrary(libName, GPIBNativeInterface.class);
            } else if (OS_NAME.contains("linux")) {
                libName = "gpib";
                lib     = Native.loadLibrary(libName, GPIBNativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported!");
            }
        } catch (UnsatisfiedLinkError e) {
            lib = null;
        }

        if (lib == null) {
            throw new VISAException("Could not load GPIB library");
        }

        try {
            NativeLibrary nLib = NativeLibrary.getInstance(libName);
            lib.ibsta.setPointer(nLib.getGlobalVariableAddress("ibsta"));
            lib.iberr.setPointer(nLib.getGlobalVariableAddress("iberr"));
            lib.ibcnt.setPointer(nLib.getGlobalVariableAddress("ibcnt"));
        } catch (Exception | Error e) {
            throw new VISAException("Could not link global variables");
        }

    }

    protected boolean wasError() {
        return (Ibsta() & GPIBNativeInterface.ERR) != 0;
    }

    protected int Ibsta() {
        return lib.ibsta.getValue();
    }

    protected int Iberr() {
        return lib.iberr.getValue();
    }

    protected int Ibcnt() {
        return lib.ibcnt.getValue();
    }

    /**
     * Converts a string to bytes in a ByteBuffer, used for sending to VISA library which expects binary strings.
     *
     * @param source The string, damn you.
     *
     * @return The ByteBuffer that I mentioned.
     */
    private static ByteBuffer stringToByteBuffer(String source) {
        try {
            ByteBuffer dest = ByteBuffer.allocate(source.length() + 1);
            dest.put(source.getBytes(responseEncoding));
            dest.position(0);
            return dest;
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    @Override
    public Connection open(Address address) throws VISAException {

        GPIBAddress addr = address.toGPIBAddress();

        if (addr == null) {
            throw new VISAException("Can only open GPIB devices using GPIB driver!");
        }

        lib.SendIFC(addr.getBus());

        int ud = lib.ibdev(addr.getBus(), addr.getAddress(), 0, GPIBNativeInterface.T3s, 1, 0);

        if (wasError()) {
            throw new VISAException("Could not open %s using GPIB.", addr.toString());
        }

        lib.EnableRemote(addr.getBus(), new short[]{(short) addr.getAddress(), -1});

        if (wasError()) {
            throw new VISAException("Error putting %s into remote mode using GPIB.", addr.toString());
        }

        return new GPIBConnection(ud);

    }

    /**
     * Implementation of Connection for GPIB-based connections
     */
    public class GPIBConnection implements jisa.visa.connections.GPIBConnection {

        private final int     handle;
        private       Charset charset = StandardCharsets.UTF_8;

        public GPIBConnection(int ibHandle) {
            handle = ibHandle;
        }

        @Override
        public void writeBytes(byte[] bytes) throws VISAException {
            write(new String(bytes, charset));
        }

        @Override
        public void clear() throws VISAException {

            lib.ibclr(handle);

            if (wasError()) {
                throw new VISAException("Error clearing GPIB device.");
            }

        }

        @Override
        public void setEncoding(Charset charset) {
            this.charset = charset;
        }

        @Override
        public Charset getEncoding() {
            return charset;
        }

        @Override
        public void write(String toWrite) throws VISAException {

            NativeString nToWrite = new NativeString(toWrite, charset.name());

            lib.ibwrt(
                handle,
                nToWrite.getPointer(),
                toWrite.length()
            );

            if (wasError()) {
                throw new VISAException("Could not write to instrument.");
            }

        }

        @Override
        public byte[] readBytes(int bufferSize) throws VISAException {

            Pointer ptr = new Memory(bufferSize);

            lib.ibrd(handle, ptr, bufferSize);

            if (wasError()) {
                throw new VISAException("Error reading from instrument.");
            }

            return ptr.getByteArray(0, Ibcnt());

        }

        @Override
        public void setEOIEnabled(boolean set) throws VISAException {

            lib.ibconfig(
                handle,
                GPIBNativeInterface.IbcEOT,
                set ? 1 : 0
            );

            if (wasError()) {
                throw new VISAException("Error setting EOI");
            }

        }

        @Override
        public void setReadTerminator(long character) throws VISAException {

            lib.ibconfig(
                handle,
                GPIBNativeInterface.IbcEOS,
                (int) character
            );

            if (wasError()) {

                lib.ibeos(handle, ((int) character));

                lib.ibconfig(
                    handle,
                    GPIBNativeInterface.IbcEOSrd,
                    (int) character
                );

                if (wasError()) {
                    throw new VISAException("Error setting read terminator...");
                }

            }

        }

        @Override
        public void setTimeout(int duration) throws VISAException {

            lib.ibconfig(
                handle,
                GPIBNativeInterface.IbcTMO,
                TMO.fromMSec(duration).getCode()
            );

            if (wasError()) {
                throw new VISAException("Error setting TMO");
            }

        }

        @Override
        public void close() throws VISAException {

            lib.ibonl(handle, 0);

            if (wasError()) {
                throw new VISAException("Could not close instrument.");
            }

        }

    }

    @Override
    public List<GPIBAddress> search() {

        ArrayList<GPIBAddress> addresses = new ArrayList<>();

        for (int i = 0; i < 16; i++) {

            try {
                addresses.addAll(search(i));
            } catch (VISAException ignored) {
            }
        }

        return addresses;

    }

    @Override
    public boolean worksWith(Address address) {
        return address.getType() == Address.Type.GPIB;
    }

    public List<GPIBAddress> search(int board) throws VISAException {

        lib.SendIFC(board);

        short[] addrList = new short[31];

        for (short i = 0; i < 31; i++) {
            addrList[i] = (short) (i + 1);
        }

        addrList[30] = -1;

        ShortBuffer buffer = ShortBuffer.allocate(Short.BYTES * 31);

        lib.FindLstn(board, addrList, buffer, 31);

        if (wasError()) {
            throw new VISAException("Could not search for listeners");
        }

        short[]                listList = buffer.array();
        ArrayList<GPIBAddress> list     = new ArrayList<>();

        for (short n : listList) {

            if (n > 0) {
                list.add(new GPIBAddress(board, n));
            }

        }

        return list;

    }

}
