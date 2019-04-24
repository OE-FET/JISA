package JISA.VISA;

import JISA.Addresses.Address;
import JISA.Addresses.StrAddress;
import JISA.Util;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static JISA.VISA.VISANativeInterface.*;

public class VISADriver implements Driver {

    private static       VISANativeInterface lib;
    private static final String              OS_NAME          = System.getProperty("os.name").toLowerCase();
    private static       String              libName;
    private static final String              responseEncoding = "UTF8";
    private static final long                VISA_ERROR       = 0x7FFFFFFF;
    private static final int                 _VI_ERROR        = -2147483648;
    private static final int                 VI_SUCCESS       = 0;
    private static final int                 VI_NULL          = 0;
    private static final int                 VI_TRUE          = 1;
    private static final int                 VI_FALSE         = 0;
    private static       NativeLong          visaResourceManagerHandle;

    public static void init() throws VISAException {

        try {
            if (OS_NAME.contains("win")) {
                libName = "nivisa64";
                lib = Native.loadLibrary(libName, VISANativeInterface.class);
            } else if (OS_NAME.contains("linux") || OS_NAME.contains("mac")) {
                libName = "visa";
                lib = Native.loadLibrary(libName, VISANativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported!");
            }
        } catch (UnsatisfiedLinkError e) {
            lib = null;
        }

        if (lib == null) {
            throw new VISAException("Could not load VISA library");
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
    public Connection open(Address address) throws VISAException {

        NativeLongByReference pViInstrument = new NativeLongByReference();

        ByteBuffer pViString = stringToByteBuffer(address.toString());
        if (pViString == null) {
            throw new VISAException("Error encoding address to ByteBuffer.");
        }
        NativeLong status = lib.viOpen(
                visaResourceManagerHandle,
                pViString,         // byte buffer for instrument string
                new NativeLong(0), // access mode (locking or not). 0:Use Visa default
                new NativeLong(0), // timeout, only when access mode equals locking
                pViInstrument      // pointer to instrument object
        );

        if (status.longValue() == VI_SUCCESS) {
            return new VISAConnection(pViInstrument.getValue());
        } else {

            switch (status.intValue()) {

                case VI_ERROR_INV_OBJECT:
                    throw new VISAException("No resource manager is open to open \"%s\".", address.toString());

                case VI_ERROR_INV_RSRC_NAME:
                    throw new VISAException("Invalid address: \"%s\".", address.toString());

                case VI_ERROR_RSRC_NFOUND:
                    throw new VISAException("No resource found at \"%s\".", address.toString());

                case VI_ERROR_RSRC_BUSY:
                    throw new VISAException("Resource busy at \"%s\".", address.toString());

                case VI_ERROR_TMO:
                    throw new VISAException("Open operation timed out.");

                default:
                    throw new VISAException("Error writing to instrument.");

            }
        }

    }

    public class VISAConnection implements Connection {

        private NativeLong handle;

        public VISAConnection(NativeLong viHandle) {
            handle = viHandle;
        }

        @Override
        public void writeBytes(byte[] bytes) throws VISAException {

            ByteBuffer pBuffer = ByteBuffer.wrap(bytes);

            long writeLength = bytes.length;

            NativeLongByReference returnCount = new NativeLongByReference();

            NativeLong status = lib.viWrite(
                    handle,
                    pBuffer,
                    new NativeLong(writeLength),
                    returnCount
            );

            if (status.longValue() != VI_SUCCESS) {

                switch (status.intValue()) {

                    case VI_ERROR_INV_OBJECT:
                        throw new VISAException("That connection is not open.");

                    case VI_ERROR_TMO:
                        throw new VISAException("Write operation timed out.");

                    default:
                        throw new VISAException("Error writing to instrument.");

                }
            }

            if (returnCount.getValue().longValue() != writeLength) {
                throw new VISAException("Command was not fully sent!");
            }

        }

        @Override
        public void write(String toWrite) throws VISAException {

            // Convert string to bytes to send
            ByteBuffer pBuffer = stringToByteBuffer(toWrite);
            if (pBuffer == null) {
                throw new VISAException("Error converting command to ByteBuffer");
            }

            long writeLength = toWrite.length();

            NativeLongByReference returnCount = new NativeLongByReference();

            NativeLong status = lib.viWrite(
                    handle,
                    pBuffer,
                    new NativeLong(writeLength),
                    returnCount
            );

            if (status.longValue() != VI_SUCCESS) {

                switch (status.intValue()) {

                    case VI_ERROR_INV_OBJECT:
                        throw new VISAException("That connection is not open.");

                    case VI_ERROR_TMO:
                        throw new VISAException("Write operation timed out.");

                    default:
                        throw new VISAException("Error writing to instrument.");

                }
            }

            if (returnCount.getValue().longValue() != writeLength) {
                throw new VISAException("Command was not fully sent!");
            }

        }

        @Override
        public byte[] readBytes(int bufferSize) throws VISAException {

            ByteBuffer            response    = ByteBuffer.allocate(bufferSize);
            NativeLongByReference returnCount = new NativeLongByReference();

            NativeLong status = lib.viRead(
                    handle,
                    response,
                    new NativeLong(bufferSize),
                    returnCount
            );

            if (status.longValue() != VI_SUCCESS) {

                switch (status.intValue()) {

                    case VI_ERROR_INV_OBJECT:
                        throw new VISAException("That connection is not open.");

                    case VI_ERROR_TMO:
                        throw new VISAException("Read operation timed out.");

                    default:
                        throw new VISAException("Error reading from instrument.");

                }
            }

            return Util.trimArray(response.array());

        }

        @Override
        public void setEOI(boolean set) throws VISAException {
            setAttribute(VI_ATTR_SEND_END_EN, set ? VI_TRUE : VI_FALSE);
        }

        @Override
        public void setEOS(long character) throws VISAException {
            setAttribute(VI_ATTR_TERMCHAR, character);
            setAttribute(VI_ATTR_TERMCHAR_EN, character != 0 ? VI_TRUE : VI_FALSE);
        }

        @Override
        public void setTMO(long duration) throws VISAException {
            setAttribute(VI_ATTR_TMO_VALUE, duration);
        }

        @Override
        public void setSerial(int baud, int data, Parity parity, StopBits stop, Flow flow) throws VISAException {

            setAttribute(VI_ATTR_ASRL_BAUD, baud);
            setAttribute(VI_ATTR_ASRL_DATA_BITS, data);
            setAttribute(VI_ATTR_ASRL_PARITY, parity.toInt());
            setAttribute(VI_ATTR_ASRL_STOP_BITS, stop.toInt());
            setAttribute(VI_ATTR_ASRL_FLOW_CNTRL, flow.toInt());

        }

        @Override
        public void close() throws VISAException {

            NativeLong status = lib.viClose(handle);

            if (status.longValue() != VI_SUCCESS) {
                throw new VISAException("Error closing instrument!");
            }

        }

        public void setAttribute(long attribute, long value) throws VISAException {

            NativeLong status = lib.viSetAttribute(
                    handle,
                    new NativeLong(attribute),
                    new NativeLong(value)
            );

            if (status.longValue() != VI_SUCCESS) {
                throw new VISAException("Error setting attribute.");
            }

        }

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

        if (status.longValue() == VI_ERROR_RSRC_NFOUND) {
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

            desc = ByteBuffer.allocate(1024);

        } while (lib.viFindNext(handle, desc).longValue() == VI_SUCCESS);


        lib.viClose(handle);

        return addresses.toArray(new StrAddress[0]);
    }

}
