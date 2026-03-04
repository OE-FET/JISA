package jisa.devices.camera.nat;

import com.sun.jna.WString;
import com.sun.jna.ptr.LongByReference;
import jisa.Util;
import jisa.devices.DeviceException;
import jisa.visa.Library;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static jisa.devices.camera.Andor3.AT_SUCCESS;
import static jisa.devices.camera.Andor3.ERROR_NAMES;

/**
 * JNA interface for atutility AndorSDK3 library.
 */
public interface ATUtilityLibrary extends Library {

    String                            JNA_LIBRARY_NAME = "atutility";
    AtomicReference<ATUtilityLibrary> INSTANCE         = new AtomicReference<>();
    AtomicBoolean                     INITIALISED      = new AtomicBoolean(false);

    default void initialise() throws Exception {

        int result = AT_InitialiseUtilityLibrary();

        if (result != AT_SUCCESS) {
            throw new DeviceException("%d (%s)", result, ERROR_NAMES.getOrDefault(result, "UNKNOWN"));
        }

        Util.addShutdownHook(this::AT_FinaliseUtilityLibrary);

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
