package jisa.devices.camera.nat;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.ptr.LongByReference;
import jisa.Util;
import jisa.devices.DeviceException;
import jisa.devices.MissingLibraryException;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * JNA interface for atutility AndorSDK3 library.
 */
public interface ATUtilityLibrary extends Library {

    String                            JNA_LIBRARY_NAME = "atutility";
    AtomicReference<ATUtilityLibrary> INSTANCE         = new AtomicReference<>();
    AtomicBoolean                     INITIALISED      = new AtomicBoolean(false);

    static ATUtilityLibrary getInstance() throws DeviceException {

        if (INSTANCE.get() == null) {

            try {
                INSTANCE.set(Native.loadLibrary(JNA_LIBRARY_NAME, ATUtilityLibrary.class));
            } catch (UnsatisfiedLinkError e) {
                throw new MissingLibraryException("atutility", "Andor sCMOS Camera");
            }

        }

        ATUtilityLibrary INSTANCE = ATUtilityLibrary.INSTANCE.get();

        if (!INITIALISED.get()) {

            int result = INSTANCE.AT_InitialiseUtilityLibrary();

            if (result != 0) {
                throw new DeviceException("Unable to initialise AndorSDK3 Utility Library (atutility).");
            }

            Util.addShutdownHook(INSTANCE::AT_FinaliseUtilityLibrary);

            INITIALISED.set(true);

        }

        return INSTANCE;

    }

    int AT_ConvertBuffer(ByteBuffer inputBuffer, ByteBuffer outputBuffer, long width, long height, long stride, WString inputPixelEncoding, WString outputPixelEncoding);

    int AT_ConvertBufferUsingMetadata(ByteBuffer inputBuffer, ByteBuffer outputBuffer, long imagesizebytes, WString outputPixelEncoding);

    int AT_GetWidthFromMetadata(ByteBuffer inputBuffer, long imagesizebytes, LongBuffer width);

    int AT_GetHeightFromMetadata(ByteBuffer inputBuffer, long imagesizebytes, LongBuffer height);

    int AT_GetStrideFromMetadata(ByteBuffer inputBuffer, long imagesizebytes, LongBuffer stride);

    int AT_GetPixelEncodingFromMetadata(ByteBuffer inputBuffer, long imagesizebytes, WString pixelEncoding, byte pixelEncodingSize);

    int AT_GetTimeStampFromMetadata(ByteBuffer inputBuffer, long imagesizebytes, LongByReference timeStamp);

    int AT_GetIRIGFromMetadata(ByteBuffer inputBuffer, long imagesizebytes, LongBuffer seconds, LongBuffer minutes, LongBuffer hours, LongBuffer days, LongBuffer years);

    int AT_GetExtendedIRIGFromMetadata(ByteBuffer inputBuffer, long imagesizebytes, long clockfrequency, DoubleBuffer nanoseconds, LongBuffer seconds, LongBuffer minutes, LongBuffer hours, LongBuffer days, LongBuffer years);

    int AT_ConfigureSpooling(int camera, WString format, WString path);

    int AT_GetSpoolProgress(int camera, IntBuffer imageNumber);

    int AT_GetMostRecentImage(int camera, ByteBuffer buffer, int bufferSize);

    int AT_InitialiseUtilityLibrary();

    int AT_FinaliseUtilityLibrary();

}
