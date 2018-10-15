package JISA.VISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GPIBDriver implements Driver {

    private static       GPIBNativeInterface       lib;
    private static final String                    OS_NAME          = System.getProperty("os.name").toLowerCase();
    private static       String                    libName;
    private static final String                    responseEncoding = "UTF8";
    private static final long                      VISA_ERROR       = 0x7FFFFFFF;
    private static final int                       _VI_ERROR        = -2147483648;
    private static final int                       VI_SUCCESS       = 0;
    private static final int                       VI_NULL          = 0;
    private static final int                       VI_TRUE          = 1;
    private static final int                       VI_FALSE         = 0;
    private static       NativeLong                visaResourceManagerHandle;
    private static       HashMap<Long, NativeLong> instruments      = new HashMap<>();

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

        private int  code;
        private long value;

        TMO(int code, long value) {
            this.code = code;
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
                lib = (GPIBNativeInterface) Native.loadLibrary(libName, GPIBNativeInterface.class);
            } else if (OS_NAME.contains("linux")) {
                libName = "gpib";
                lib = (GPIBNativeInterface) Native.loadLibrary(libName, GPIBNativeInterface.class);
            } else {
                System.err.println("This system is not yet supported!");
                System.exit(1);
            }
        } catch (UnsatisfiedLinkError e) {
            lib = null;
        }

        if (lib == null) {
            throw new VISAException("Could not load GPIB library");
        }

    }

    private static boolean wasError() {
        return (lib.Ibsta() * GPIBNativeInterface.ERR) != 0;
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
    public long open(InstrumentAddress address) throws VISAException {

        GPIBAddress addr = (new StrAddress(address.getVISAAddress())).toGPIBAddress();

        if (addr == null) {
            throw new VISAException("Can only open GPIB devices using GPIB driver!");
        }

        int ud = lib.ibdev(addr.getBus(), addr.getAddress(), 0, 7, 1, 0);

        if (wasError()) {
            throw new VISAException("Could not open %s using GPIB.", addr.getVISAAddress());
        }

        return (long) ud;

    }

    @Override
    public void close(long instrument) throws VISAException {

        lib.ibonl((int) instrument, 0);

        if (wasError()) {
            throw new VISAException("Could not close instrument.");
        }

    }

    @Override
    public void write(long instrument, String toWrite) throws VISAException {

        Pointer ptr = (new PointerByReference()).getValue();
        ptr.setString(0, toWrite);

        lib.ibwrt(
                (int) instrument,
                ptr,
                toWrite.length()
        );

        if (wasError()) {
            throw new VISAException("Could not write to instrument.");
        }

    }

    @Override
    public String read(long instrument, int bufferSize) throws VISAException {

        Pointer ptr = (new PointerByReference()).getValue();

        lib.ibrd(
                (int) instrument,
                ptr,
                bufferSize
        );

        if (wasError()) {
            throw new VISAException("Error reading from instrument.");
        }

        return ptr.getString(0);

    }

    @Override
    public void setEOI(long instrument, boolean set) throws VISAException {

        lib.ibconfig(
                (int) instrument,
                GPIBNativeInterface.IbcEOT,
                set ? 1 : 0
        );

        if (wasError()) {
            throw new VISAException("Error setting EOI");
        }

    }

    @Override
    public void setEOS(long instrument, long character) throws VISAException {

        lib.ibconfig(
                (int) instrument,
                GPIBNativeInterface.IbcEOS,
                (int) character
        );

        if (wasError()) {
            throw new VISAException("Error setting EOI");
        }

    }

    @Override
    public void setTMO(long instrument, long duration) throws VISAException {

        lib.ibconfig(
                (int) instrument,
                GPIBNativeInterface.IbcTMO,
                TMO.fromMSec(duration).getCode()
        );

        if (wasError()) {
            throw new VISAException("Error setting TMO");
        }

    }

    @Override
    public StrAddress[] search() throws VISAException {

        ArrayList<StrAddress> addresses = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            try {
                addresses.addAll(search(i));
            } catch (VISAException ignored) {}
        }

        return addresses.toArray(new StrAddress[0]);

    }

    public List<StrAddress> search(int board) throws VISAException {

        short[] addrList = new short[31];

        for (short i = 0; i < 31; i++) {
            addrList[i] = i;
        }

        addrList[30] = -1;

        ShortBuffer buffer = ShortBuffer.allocate(Short.BYTES * 31);

        lib.FindLstn(board, addrList, buffer, 31);

        if (wasError()) {
            throw new VISAException("Could not search for listeners");
        }

        short[]               listList = buffer.array();
        ArrayList<StrAddress> list     = new ArrayList<>();

        for (short n : listList) {

            if (n >= 0) {
                list.add(new StrAddress((new GPIBAddress(board, (int) n)).getVISAAddress()));
            }

        }

        return list;

    }

}