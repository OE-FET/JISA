package jisa.visa.drivers;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.SerialAddress;
import jisa.addresses.StrAddress;
import jisa.visa.VISAException;
import jisa.visa.VISANativeInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.visa.VISANativeInterface.*;

public class RSVISADriver implements Driver
{

    private static final String              OS_NAME          = System.getProperty("os.name").toLowerCase();
    private static final String              responseEncoding = "UTF8";
    private static final long                VISA_ERROR       = 0x7FFFFFFF;
    private static final int                 _VI_ERROR        = -2147483648;
    private static final int                 VI_SUCCESS       = 0;
    private static final int                 VI_NULL          = 0;
    private static final int                 VI_TRUE          = 1;
    private static final int                 VI_FALSE         = 0;
    private static jisa.visa.VISANativeInterface libStatic;
    private static       String              libName;
    private static       NativeLong          visaResourceManagerHandleStatic;
    private static final List<Integer>       SUCCESS_CODES = List.of(VI_SUCCESS, VI_SUCCESS_TERM_CHAR, VI_SUCCESS_MAX_CNT);

    protected jisa.visa.VISANativeInterface lib;
    protected NativeLong          visaResourceManagerHandle;

    public RSVISADriver() {

        lib                       = RSVISADriver.libStatic;
        visaResourceManagerHandle = RSVISADriver.visaResourceManagerHandleStatic;

        Util.addShutdownHook(() -> {

            if (visaResourceManagerHandle != null) {
                RSVISADriver.libStatic.viClose(RSVISADriver.visaResourceManagerHandleStatic);
            }

        });

    }

    public static void init() throws jisa.visa.VISAException
    {

        try {

            if (OS_NAME.contains("win")) {
                libName   = "RsVisa32";
                libStatic = Native.loadLibrary(RSVISADriver.libName, jisa.visa.VISANativeInterface.class);
            } else if (OS_NAME.contains("linux") || OS_NAME.contains("mac")) {
                libName   = "visa";
                libStatic = Native.loadLibrary(RSVISADriver.libName, VISANativeInterface.class);
            } else {
                throw new jisa.visa.VISAException("Platform not yet supported!");
            }

        } catch (UnsatisfiedLinkError e) {
            libStatic = null;
        }

        if (libStatic == null) {
            throw new jisa.visa.VISAException("Could not load VISA library");
        }

        // Attempt to get a resource manager handle
        try {
            visaResourceManagerHandleStatic = getResourceManager();
        } catch (jisa.visa.VISAException e) {
            throw new jisa.visa.VISAException("Could not get resource manager");
        }

    }

    /**
     * Sets up the resource manager for this session. Used only internally.
     *
     * @return Resource manager handle.
     *
     * @throws jisa.visa.VISAException When VISA does go gone screw it up
     */
    protected static NativeLong getResourceManager() throws jisa.visa.VISAException
    {

        NativeLongByReference pViSession = new NativeLongByReference();
        NativeLong            visaStatus = RSVISADriver.libStatic.viOpenDefaultRM(pViSession);

        if (visaStatus.longValue() != VI_SUCCESS) {
            throw new jisa.visa.VISAException("Error opening resource manager!");
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
    protected static ByteBuffer stringToByteBuffer(String source) {
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
    public Connection open(Address address) throws jisa.visa.VISAException
    {

        NativeLongByReference pViInstrument = new NativeLongByReference();

        SerialAddress sAddr = address.toSerialAddress();
        ByteBuffer    pViString;

        if (sAddr != null) {
            pViString = stringToByteBuffer(toVISASerial(sAddr));
        } else {
            pViString = stringToByteBuffer(address.toString());
        }

        if (pViString == null) {
            throw new jisa.visa.VISAException("Error encoding address to ByteBuffer.");
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
                    throw new jisa.visa.VISAException("No resource manager is open to open \"%s\".", address.toString());

                case VI_ERROR_INV_RSRC_NAME:
                    throw new jisa.visa.VISAException("Invalid address: \"%s\".", address.toString());

                case VI_ERROR_RSRC_NFOUND:
                    throw new jisa.visa.VISAException("No resource found at \"%s\".", address.toString());

                case VI_ERROR_RSRC_BUSY:
                    throw new jisa.visa.VISAException("Resource busy at \"%s\".", address.toString());

                case VI_ERROR_TMO:
                    throw new jisa.visa.VISAException("Open operation timed out.");

                default:
                    throw new jisa.visa.VISAException("Error trying to open instrument connection. Status: %d", status.intValue());

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

    protected String toVISASerial(SerialAddress address) throws jisa.visa.VISAException
    {

        String raw = address.toString();

        Pattern windows = Pattern.compile("ASRL::COM([0-9]*)::INSTR");
        Matcher matcher = windows.matcher(raw);

        if (matcher.find()) {

            return String.format("ASRL%s::INSTR", matcher.group(1));

        } else {

            Pattern asrl  = Pattern.compile("ASRL([0-9]*?)::INSTR");
            Pattern dfind = Pattern.compile("(COM([0-9]*))|(/dev/tty((S)|(USB))([0-9]*))");

            for (Address found : search(false)) {

                Matcher aMatch = asrl.matcher(found.toString());

                if (aMatch.find()) {

                    String         number      = aMatch.group(1);
                    VISAConnection con         = (VISAConnection) open(found);
                    String         desc        = con.getAttributeString(VI_ATTR_INTF_INST_NAME);
                    Matcher        portMatcher = dfind.matcher(desc);

                    if (portMatcher.find()) {

                        String port = portMatcher.group(0);

                        if (port.trim().equals(address.getPort().trim())) {
                            return found.toString();
                        }

                    }

                }

            }

            throw new jisa.visa.VISAException("No resource found at \"%s\"", address.toString());

        }

    }

    @Override
    public List<Address> search() throws jisa.visa.VISAException
    {
        return search(true);
    }

    @Override
    public boolean worksWith(Address address) {

        switch (address.getType()) {

            case ID:
            case MODBUS:
                return false;

            default:
                return true;

        }

    }

    public List<Address> search(boolean changeSerial) throws jisa.visa.VISAException
    {

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
            throw new jisa.visa.VISAException("Error searching for devices.");
        }

        int                   count     = listCount.getValue().intValue();
        ArrayList<Address>    addresses = new ArrayList<>();
        NativeLong            handle    = listHandle.getValue();
        String                address;
        Pattern               dfind     = Pattern.compile("(COM([0-9]*))|(/dev/tty((S)|(USB))([0-9]*))");
        do {

            try {
                address = new String(desc.array(), 0, 1024, responseEncoding);
            } catch (UnsupportedEncodingException e) {
                throw new jisa.visa.VISAException("Unable to encode address!");
            }

            Address strAddress = new StrAddress(address);

            if (changeSerial && address.contains("ASRL")) {

                try {

                    VISAConnection c    = (VISAConnection) open(strAddress);
                    String         intf = c.getAttributeString(VI_ATTR_INTF_INST_NAME);
                    c.close();
                    Matcher matcher = dfind.matcher(intf);

                    if (matcher.find()) {
                        String port = matcher.group(0);
                        strAddress = new SerialAddress(port.trim().replaceAll("[^0-9]", ""));
                    }

                } catch (Exception ignored) {}

            }

            addresses.add(strAddress);

            desc = ByteBuffer.allocate(1024);

        } while (lib.viFindNext(handle, desc).longValue() == VI_SUCCESS);

        lib.viClose(handle);

        return addresses;
    }

    public class VISAConnection implements Connection
    {

        protected NativeLong handle;

        public VISAConnection(NativeLong viHandle) {
            handle = viHandle;
        }

        @Override
        public void writeBytes(byte[] bytes) throws jisa.visa.VISAException
        {

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
                        throw new jisa.visa.VISAException("That connection is not open.");

                    case VI_ERROR_TMO:
                        throw new jisa.visa.VISAException("Write operation timed out.");

                    default:
                        throw new jisa.visa.VISAException("Error writing to instrument.");

                }
            }

            if (returnCount.getValue().longValue() != writeLength) {
                throw new jisa.visa.VISAException("Command was not fully sent!");
            }

        }

        @Override
        public void clear() throws jisa.visa.VISAException
        {

            NativeLong status = lib.viClear(handle);

            if (status.intValue() != VI_SUCCESS) {
                throw new jisa.visa.VISAException("Unable to clear connection.");
            }

        }

        @Override
        public void write(String toWrite) throws jisa.visa.VISAException
        {

            // Convert string to bytes to send
            ByteBuffer pBuffer = stringToByteBuffer(toWrite);
            if (pBuffer == null) {
                throw new jisa.visa.VISAException("Error converting command to ByteBuffer");
            }

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
                        throw new jisa.visa.VISAException("That connection is not open.");

                    case VI_ERROR_TMO:
                        throw new jisa.visa.VISAException("Write operation timed out.");

                    default:
                        throw new jisa.visa.VISAException("Error writing to instrument.");

                }
            }

            if (returnCount.getValue().longValue() != writeLength) {
                throw new jisa.visa.VISAException("Command was not fully sent!");
            }

        }

        @Override
        public byte[] readBytes(int bufferSize) throws jisa.visa.VISAException
        {

            ByteBuffer            response    = ByteBuffer.allocate(bufferSize);
            NativeLongByReference returnCount = new NativeLongByReference();
            ByteArrayOutputStream longResponse = new ByteArrayOutputStream();

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
                    try
                    {
                        longResponse.write(Arrays.copyOfRange(response.array(),0,returnCount.getValue().intValue()));
                        while (status.intValue() == VI_SUCCESS_MAX_CNT || status.intValue() == VI_SUCCESS_TERM_CHAR)
                        {
                            status = lib.viRead(
                                    handle,
                                    response,
                                    new NativeLong(bufferSize),
                                    returnCount
                            );
                            longResponse.write(Arrays.copyOfRange(response.array(),0,returnCount.getValue().intValue()));
                        }
                        return longResponse.toByteArray();
                    }
                    catch (IOException e)
                    {
                        throw new jisa.visa.VISAException("Error reading all bytes from instrument, code: 0x%08X", status.intValue());
                    }
                case VI_ERROR_INV_OBJECT:
                    throw new jisa.visa.VISAException("That connection is not open.");

                case VI_ERROR_TMO:
                    throw new jisa.visa.VISAException("Read operation timed out.");

                default:
                    throw new jisa.visa.VISAException("Error reading from instrument, code: 0x%08X", status.intValue());

            }

        }

        @Override
        public void setEOI(boolean set) throws jisa.visa.VISAException
        {
            setAttribute(VI_ATTR_SEND_END_EN, set ? VI_TRUE : VI_FALSE);
        }

        @Override
        public void setReadTerminator(long character) throws jisa.visa.VISAException
        {
            setAttribute(VI_ATTR_TERMCHAR_EN, character != 0 ? VI_TRUE : VI_FALSE);
            setAttribute(VI_ATTR_TERMCHAR, character);
        }

        @Override
        public void setTimeout(int duration) throws jisa.visa.VISAException
        {
            setAttribute(VI_ATTR_TMO_VALUE, duration);
        }

        @Override
        public void setSerial(int baud, int data, Parity parity, StopBits stop, Flow flow) throws jisa.visa.VISAException
        {

            setAttribute(VI_ATTR_ASRL_BAUD, baud);
            setAttribute(VI_ATTR_ASRL_DATA_BITS, data);
            setAttribute(VI_ATTR_ASRL_PARITY, parity.toInt());
            setAttribute(VI_ATTR_ASRL_STOP_BITS, stop.toInt());
            setAttribute(VI_ATTR_ASRL_FLOW_CNTRL, flow.toInt());

        }

        @Override
        public void close() throws jisa.visa.VISAException
        {

            NativeLong status = lib.viClose(handle);

            if (status.longValue() != VI_SUCCESS) {
                throw new jisa.visa.VISAException("Error closing instrument!");
            }

        }

        public void setAttribute(long attribute, long value) throws jisa.visa.VISAException
        {

            NativeLong status = lib.viSetAttribute(
                    handle,
                    new NativeLong(attribute),
                    new NativeLong(value)
            );

            if (status.longValue() != VI_SUCCESS) {
                throw new jisa.visa.VISAException("Error setting attribute.");
            }

        }

        public long getAttributeLong(long attribute) throws jisa.visa.VISAException
        {

            NativeLongByReference pointer = new NativeLongByReference();

            NativeLong status = lib.viGetAttribute(
                    handle,
                    new NativeLong(attribute),
                    pointer.getPointer()
            );

            return pointer.getValue().longValue();

        }

        public String getAttributeString(long attribute) throws VISAException
        {

            Pointer pointer = new Memory(VI_FIND_BUFLEN);

            NativeLong status = lib.viGetAttribute(
                    handle,
                    new NativeLong(attribute),
                    pointer
            );

            return pointer.getString(0);

        }

    }

}