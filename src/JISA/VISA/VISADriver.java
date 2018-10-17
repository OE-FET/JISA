package JISA.VISA;

import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class VISADriver implements Driver {

    private static       VISANativeInterface       lib;
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

    public static void init() throws VISAException {

        try {
            if (OS_NAME.contains("win")) {
                libName = "nivisa64";
                lib = (VISANativeInterface) Native.loadLibrary(libName, VISANativeInterface.class);
            } else if (OS_NAME.contains("linux")) {
                libName = "visa";
                lib = (VISANativeInterface) Native.loadLibrary(libName, VISANativeInterface.class);
            } else {
                System.err.println("This system is not yet supported!");
                System.exit(1);
            }
        } catch (UnsatisfiedLinkError e) {
            lib = null;
        }

        if (lib == null) {
            System.out.println("VISA driver not loaded.");
            throw new VISAException("Could not load VISA library");
        } else {
            System.out.println("VISA driver loaded.");
        }

        // Attempt to get a resource manager handle
        try {
            visaResourceManagerHandle = getResourceManager();
        } catch (VISAException e) {
            throw new VISAException("Could not get resource manager");
        }

    }

    /**
     * Sets up the resource manager for this session. Used only internally.
     *
     * @return Resource manager handle.
     *
     * @throws VISAException When VISA does go gone screw it up
     */
    private static NativeLong getResourceManager() throws VISAException {

        NativeLongByReference pViSession = new NativeLongByReference();
        NativeLong            visaStatus = lib.viOpenDefaultRM(pViSession);

        if (visaStatus.longValue() != VI_SUCCESS) {
            throw new VISAException("Error opening resource manager!");
        }
        return pViSession.getValue();

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

        NativeLong            visaStatus;
        NativeLongByReference pViInstrument = new NativeLongByReference();

        ByteBuffer pViString = stringToByteBuffer(address.getVISAAddress());
        if (pViString == null) {
            throw new VISAException("Error encoding address to ByteBuffer.");
        }
        visaStatus = lib.viOpen(
                visaResourceManagerHandle,
                pViString,         // byte buffer for instrument string
                new NativeLong(0), // access mode (locking or not). 0:Use Visa default
                new NativeLong(0), // timeout, only when access mode equals locking
                pViInstrument      // pointer to instrument object
        );

        if (visaStatus.longValue() == VI_SUCCESS) {
            instruments.put(pViInstrument.getValue().longValue(), pViInstrument.getValue());
            return pViInstrument.getValue().longValue();
        } else {
            throw new VISAException("Could not open device: \"%s\"!", address);
        }

    }

    @Override
    public void close(long instrument) throws VISAException {

        if (!instruments.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        NativeLong status = lib.viClose(instruments.get(instrument));

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Error closing instrument!");
        }

        instruments.remove(instrument);

    }

    @Override
    public void write(long instrument, String toWrite) throws VISAException {

        // Check that we have actually opened this device
        if (!instruments.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        // Convert string to bytes to send
        ByteBuffer pBuffer = stringToByteBuffer(toWrite);
        if (pBuffer == null) {
            throw new VISAException("Error converting command to ByteBuffer");
        }

        long writeLength = toWrite.length();

        NativeLongByReference returnCount = new NativeLongByReference();

        NativeLong status = lib.viWrite(
                instruments.get(instrument),
                pBuffer,
                new NativeLong(writeLength),
                returnCount
        );

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Could not write to instrument!");
        }

        if (returnCount.getValue().longValue() != writeLength) {
            throw new VISAException("Command was not fully sent!");
        }

    }

    @Override
    public String read(long instrument, int bufferSize) throws VISAException {

        if (!instruments.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        ByteBuffer            response    = ByteBuffer.allocate(bufferSize);
        NativeLongByReference returnCount = new NativeLongByReference();

        NativeLong status = lib.viRead(
                instruments.get(instrument),
                response,
                new NativeLong(bufferSize),
                returnCount
        );

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Error reading from instrument!");
        }

        try {
            return new String(response.array(), 0, returnCount.getValue().intValue(), responseEncoding);
        } catch (UnsupportedEncodingException e) {
            throw new VISAException("Could not encode returned string!");
        }

    }

    @Override
    public void setEOI(long instrument, boolean set) throws VISAException {
        setAttribute(instrument, VISANativeInterface.VI_ATTR_SEND_END_EN, set ? VI_TRUE : VI_FALSE);
    }

    @Override
    public void setEOS(long instrument, long character) throws VISAException {
        setAttribute(instrument, VISANativeInterface.VI_ATTR_TERMCHAR, character);
        setAttribute(instrument, VISANativeInterface.VI_ATTR_TERMCHAR_EN, character != 0 ? VI_TRUE : VI_FALSE);
    }

    @Override
    public void setTMO(long instrument, long duration) throws VISAException {
        setAttribute(instrument, VISANativeInterface.VI_ATTR_TMO_VALUE, duration);
    }

    @Override
    public StrAddress[] search() throws VISAException {
        // VISA RegEx for "Anything" (should be .* but they seem to use their own standard)
        ByteBuffer            expr       = stringToByteBuffer("?*");
        ByteBuffer            desc       = ByteBuffer.allocate(1024);
        NativeLongByReference listHandle = new NativeLongByReference();
        NativeLongByReference listCount  = new NativeLongByReference();

        // Perform the native call
        NativeLong status = lib.viFindRsrc(
                visaResourceManagerHandle,
                expr,
                listHandle,
                listCount,
                desc
        );

        if (status.longValue() == VISANativeInterface.VI_ERROR_RSRC_NFOUND) {
            lib.viClose(listHandle.getValue());
            return new StrAddress[0];
        }

        if (status.longValue() != VI_SUCCESS) {
            lib.viClose(listHandle.getValue());
            throw new VISAException("Error searching for devices.");
        }

        int                   count     = listCount.getValue().intValue();
        ArrayList<StrAddress> addresses = new ArrayList<>();
        NativeLong            handle    = listHandle.getValue();
        String                address;

        do {

            try {
                address = new String(desc.array(), 0, 1024, responseEncoding);
            } catch (UnsupportedEncodingException e) {
                throw new VISAException("Unable to encode address!");
            }
            addresses.add(new StrAddress(address));

        } while (lib.viFindNext(handle, desc).longValue() == VI_SUCCESS);


        lib.viClose(handle);

        return addresses.toArray(new StrAddress[0]);
    }


    /**
     * Sets a VISA attribute for an instrument
     *
     * @param instrument Instrument handle from openInstrument()
     * @param attribute  The attribute to set (defined in VISANativeInterface)
     * @param value      The value to give it
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static void setAttribute(long instrument, long attribute, long value) throws VISAException {

        if (!instruments.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        NativeLong status = lib.viSetAttribute(
                instruments.get(instrument),
                new NativeLong(attribute),
                new NativeLong(value)
        );

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Error setting EOI flag!");
        }

    }



    /**
     * Returns the value of the given VISA Attribute for the given instrument
     *
     * @param instrument Instrument handle from openInstrument()
     * @param attribute  The attribute to read
     *
     * @return Value assigned to the attribute
     *
     * @throws VISAException Upon error with VISA interface
     */
    public static long getAttribute(long instrument, long attribute) throws VISAException {

        if (!instruments.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        NativeLongByReference value = new NativeLongByReference();

        NativeLong status = lib.viGetAttribute(
                instruments.get(instrument),
                new NativeLong(attribute),
                value.getPointer()
        );

        if (status.longValue() != VI_SUCCESS) {
            throw new VISAException("Error reading attribute!");
        }

        return value.getValue().longValue();

    }

}
