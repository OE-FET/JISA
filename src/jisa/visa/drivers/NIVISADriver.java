package jisa.visa.drivers;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.Util;
import jisa.addresses.*;
import jisa.visa.VISAException;
import jisa.visa.VISANativeInterface;
import jisa.visa.connections.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static jisa.visa.VISANativeInterface.*;

public class NIVISADriver implements Driver {

    private static final String              OS_NAME          = System.getProperty("os.name").toLowerCase();
    private static final String              responseEncoding = "UTF8";
    private static final long                VISA_ERROR       = 0x7FFFFFFF;
    private static final int                 _VI_ERROR        = -2147483648;
    private static final int                 VI_SUCCESS       = 0;
    private static final int                 VI_NULL          = 0;
    private static final int                 VI_TRUE          = 1;
    private static final int                 VI_FALSE         = 0;
    private static       VISANativeInterface libStatic;
    private static       String              libName;
    private static       NativeLong          visaResourceManagerHandleStatic;
    private static final List<Integer>       SUCCESS_CODES    = List.of(VI_SUCCESS, VI_SUCCESS_TERM_CHAR, VI_SUCCESS_MAX_CNT);

    protected VISANativeInterface lib;
    protected NativeLong          visaResourceManagerHandle;

    public NIVISADriver() {

        lib                       = NIVISADriver.libStatic;
        visaResourceManagerHandle = NIVISADriver.visaResourceManagerHandleStatic;

        Util.addShutdownHook(() -> {

            if (visaResourceManagerHandle != null) {
                NIVISADriver.libStatic.viClose(NIVISADriver.visaResourceManagerHandleStatic);
            }

        });

    }

    public static void init() throws VISAException {

        try {

            if (OS_NAME.contains("win")) {
                libName   = "nivisa64";
                libStatic = Native.loadLibrary(NIVISADriver.libName, VISANativeInterface.class);
            } else if (OS_NAME.contains("linux") || OS_NAME.contains("mac")) {
                libName   = "visa";
                libStatic = Native.loadLibrary(NIVISADriver.libName, VISANativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported!");
            }

        } catch (UnsatisfiedLinkError e) {
            libStatic = null;
        }

        if (libStatic == null) {
            throw new VISAException("Could not load VISA library");
        }

        // Attempt to get a resource manager handle
        try {
            visaResourceManagerHandleStatic = getResourceManager();
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
    protected static NativeLong getResourceManager() throws VISAException {

        NativeLongByReference pViSession = new NativeLongByReference();
        NativeLong            visaStatus = NIVISADriver.libStatic.viOpenDefaultRM(pViSession);

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
    protected static ByteBuffer stringToByteBuffer(String source, Charset charset) {
        return ByteBuffer.wrap(source.getBytes(charset));
    }

    protected static ByteBuffer stringToByteBuffer(String source) {
        return stringToByteBuffer(source, StandardCharsets.UTF_8);
    }

    @Override
    public Connection open(Address address) throws VISAException {

        NativeLongByReference pViInstrument = new NativeLongByReference();
        ByteBuffer            pViString     = stringToByteBuffer(address.getVISAString());

        NativeLong status = lib.viOpen(
            visaResourceManagerHandle,
            pViString,         // byte buffer for instrument string
            new NativeLong(0), // access mode (locking or not). 0:Use Visa default
            new NativeLong(0), // timeout, only when access mode equals locking
            pViInstrument      // pointer to instrument object
        );

        if (status.longValue() == VI_SUCCESS) {

            if (address instanceof SerialAddress) {

                return new VISASerialConnection(pViInstrument.getValue());

            } else if (address instanceof GPIBAddress) {

                return new VISAGPIBConnection(pViInstrument.getValue());

            } else if (address instanceof TCPIPAddress) {

                return new VISATCPIPConnection(pViInstrument.getValue());

            } else if (address instanceof LXIAddress) {

                return new VISALXIConnection(pViInstrument.getValue());

            } else if (address instanceof USBAddress) {

                return new VISAUSBConnection(pViInstrument.getValue());

            } else {

                return new VISAConnection(pViInstrument.getValue());

            }

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
                    throw new VISAException("Error trying to open instrument connection. Status: %d", status.intValue());

            }
        }

    }

    protected NativeLong openInstrument(String address) {


        NativeLongByReference pViInstrument = new NativeLongByReference();
        ByteBuffer            pViString     = stringToByteBuffer(address);

        NativeLong status = lib.viOpen(
            visaResourceManagerHandle,
            pViString,
            new NativeLong(0),
            new NativeLong(0),
            pViInstrument
        );

        if (status.longValue() == VI_SUCCESS) {
            return pViInstrument.getValue();
        } else {
            return null;
        }

    }

    @Override
    public boolean worksWith(Address address) {
        return !(address instanceof IDAddress || address instanceof ModbusAddress);
    }

    public List<Address> search() throws VISAException {

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
            return Collections.emptyList();
        }

        if (status.longValue() != VI_SUCCESS) {
            lib.viClose(listHandle.getValue());
            throw new VISAException("Error searching for devices.");
        }

        int                count     = listCount.getValue().intValue();
        ArrayList<Address> addresses = new ArrayList<>();
        NativeLong         handle    = listHandle.getValue();
        String             address;

        do {

            try {
                address = new String(desc.array(), responseEncoding);
            } catch (UnsupportedEncodingException e) {
                throw new VISAException("Unable to encode address!");
            }

            Address strAddress = Address.parse(address);
            addresses.add(strAddress);

            desc.clear();

        } while (lib.viFindNext(handle, desc).longValue() == VI_SUCCESS);

        lib.viClose(handle);

        return addresses;

    }

    public class VISAConnection implements Connection {

        protected NativeLong handle;
        private   Charset    charset = StandardCharsets.UTF_8;

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

            if (status.longValue() < VI_SUCCESS) {

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
        public void clear() throws VISAException {

            NativeLong status = lib.viClear(handle);

            if (status.intValue() != VI_SUCCESS) {
                throw new VISAException("Unable to clear connection.");
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

            // Convert string to bytes to send
            ByteBuffer pBuffer = stringToByteBuffer(toWrite, charset);

            long writeLength = toWrite.length();

            NativeLongByReference returnCount = new NativeLongByReference();

            NativeLong status = lib.viWrite(
                handle,
                pBuffer,
                new NativeLong(writeLength),
                returnCount
            );

            if (status.intValue() != VI_SUCCESS) {

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

            switch (status.intValue()) {

                case VI_SUCCESS:
                case VI_SUCCESS_TERM_CHAR:
                case VI_SUCCESS_MAX_CNT:
                    return Util.trimBytes(response.array(), 0, returnCount.getValue().intValue());

                case VI_ERROR_INV_OBJECT:
                    throw new VISAException("That connection is not open.");

                case VI_ERROR_TMO:
                    throw new VISAException("Read operation timed out.");

                default:
                    throw new VISAException("Error reading from instrument, code: 0x%08X", status.intValue());

            }

        }

        @Override
        public void setReadTerminator(long character) throws VISAException {
            setAttribute(VI_ATTR_TERMCHAR_EN, character != 0 ? VI_TRUE : VI_FALSE);
            setAttribute(VI_ATTR_TERMCHAR, character);
        }

        @Override
        public void setTimeout(int duration) throws VISAException {
            setAttribute(VI_ATTR_TMO_VALUE, duration);
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

        public long getAttributeLong(long attribute) throws VISAException {

            NativeLongByReference pointer = new NativeLongByReference();

            NativeLong status = lib.viGetAttribute(
                handle,
                new NativeLong(attribute),
                pointer.getPointer()
            );

            return pointer.getValue().longValue();

        }

        public String getAttributeString(long attribute) throws VISAException {

            Pointer pointer = new Memory(VI_FIND_BUFLEN);

            NativeLong status = lib.viGetAttribute(
                handle,
                new NativeLong(attribute),
                pointer
            );

            return pointer.getString(0);

        }

    }

    public class VISASerialConnection extends VISAConnection implements SerialConnection {

        public VISASerialConnection(NativeLong viHandle) {
            super(viHandle);
        }

        @Override
        public void setSerialParameters(int baud, int data, SerialConnection.Parity parity, SerialConnection.Stop stop, SerialConnection.FlowControl... flows) throws VISAException {

            setAttribute(VI_ATTR_ASRL_BAUD, baud);
            setAttribute(VI_ATTR_ASRL_DATA_BITS, data);
            setAttribute(VI_ATTR_ASRL_PARITY, parity.toInt());
            setAttribute(VI_ATTR_ASRL_STOP_BITS, stop.toInt());
            setAttribute(VI_ATTR_ASRL_FLOW_CNTRL, Arrays.stream(flows).mapToInt(FlowControl::toInt).reduce((a, b) -> a | b).orElse(0));

        }

    }

    public class VISAGPIBConnection extends VISAConnection implements GPIBConnection {


        public VISAGPIBConnection(NativeLong viHandle) {
            super(viHandle);
        }

        @Override
        public void setEOIEnabled(boolean use) throws VISAException {
            setAttribute(VI_ATTR_SEND_END_EN, use ? VI_TRUE : VI_FALSE);
        }

    }

    public class VISATCPIPConnection extends VISAConnection implements TCPIPConnection {

        public VISATCPIPConnection(NativeLong viHandle) {
            super(viHandle);
        }

        @Override
        public void setKeepAlive(boolean on) throws VISAException {
            setAttribute(VI_ATTR_TCPIP_KEEPALIVE, on ? VI_TRUE : VI_FALSE);
        }

        @Override
        public boolean isKeepAlive() throws VISAException {
            return getAttributeLong(VI_ATTR_TCPIP_KEEPALIVE) == VI_TRUE;
        }

    }

    public class VISAUSBConnection extends VISAConnection implements USBConnection {

        public VISAUSBConnection(NativeLong viHandle) {
            super(viHandle);
        }

    }

    public class VISALXIConnection extends VISAConnection implements LXIConnection {

        public VISALXIConnection(NativeLong viHandle) {
            super(viHandle);
        }

    }

}
