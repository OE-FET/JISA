package jisa.devices.camera.nat;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.*;

public interface ATCoreLibrary extends Library {

    String        JNA_LIBRARY_NAME = "atcore";
    NativeLibrary JNA_NATIVE_LIB   = NativeLibrary.getInstance(ATCoreLibrary.JNA_LIBRARY_NAME);
    ATCoreLibrary INSTANCE         = Native.loadLibrary(ATCoreLibrary.JNA_LIBRARY_NAME, ATCoreLibrary.class);

    interface FeatureCallback extends Callback {
        int apply(int Hndl, WString Feature, Pointer Context);
    }

    int AT_InitialiseLibrary();

    int AT_FinaliseLibrary();

    @Deprecated
    int AT_Open(int CameraIndex, IntByReference Hndl);

    int AT_Open(int CameraIndex, IntBuffer Hndl);

    int AT_OpenDevice(WString Device, IntBuffer Hndl);

    int AT_Close(int Hndl);

    int AT_RegisterFeatureCallback(int Hndl, WString Feature, ATCoreLibrary.FeatureCallback EvCallback, Pointer Context);

    int AT_UnregisterFeatureCallback(int Hndl, WString Feature, ATCoreLibrary.FeatureCallback EvCallback, Pointer Context);

    int AT_IsImplemented(int Hndl, WString Feature, IntBuffer Implemented);

    int AT_IsReadable(int Hndl, WString Feature, IntBuffer Readable);

    int AT_IsWritable(int Hndl, WString Feature, IntBuffer Writable);

    int AT_IsReadOnly(int Hndl, WString Feature, IntBuffer ReadOnly);

    int AT_SetInt(int Hndl, WString Feature, long Value);

    int AT_GetInt(int Hndl, WString Feature, LongBuffer Value);

    int AT_GetIntMax(int Hndl, WString Feature, LongBuffer MaxValue);

    int AT_GetIntMin(int Hndl, WString Feature, LongBuffer MinValue);

    int AT_SetFloat(int Hndl, WString Feature, double Value);

    int AT_GetFloat(int Hndl, WString Feature, DoubleBuffer Value);

    int AT_GetFloatMax(int Hndl, WString Feature, DoubleBuffer MaxValue);

    int AT_GetFloatMin(int Hndl, WString Feature, DoubleBuffer MinValue);

    int AT_SetBool(int Hndl, WString Feature, int Value);

    int AT_GetBool(int Hndl, WString Feature, IntBuffer Value);

    int AT_SetEnumerated(int Hndl, WString Feature, int Value);

    int AT_SetEnumeratedString(int Hndl, WString Feature, WString String);

    int AT_GetEnumerated(int Hndl, WString Feature, IntBuffer Value);

    int AT_GetEnumeratedCount(int Hndl, WString Feature, IntBuffer Count);

    int AT_IsEnumeratedIndexAvailable(int Hndl, WString Feature, int Index, IntBuffer Available);

    int AT_IsEnumeratedIndexImplemented(int Hndl, WString Feature, int Index, IntBuffer Implemented);

    int AT_GetEnumeratedString(int Hndl, WString Feature, int Index, CharBuffer String, int StringLength);

    int AT_SetEnumIndex(int Hndl, WString Feature, int Value);

    int AT_SetEnumString(int Hndl, WString Feature, WString String);

    int AT_GetEnumIndex(int Hndl, WString Feature, IntBuffer Value);

    int AT_GetEnumCount(int Hndl, WString Feature, IntBuffer Count);

    int AT_IsEnumIndexAvailable(int Hndl, WString Feature, int Index, IntBuffer Available);

    int AT_IsEnumIndexImplemented(int Hndl, WString Feature, int Index, IntBuffer Implemented);

    int AT_GetEnumStringByIndex(int Hndl, WString Feature, int Index, CharBuffer String, int StringLength);

    int AT_Command(int Hndl, WString Feature);

    int AT_SetString(int Hndl, WString Feature, WString String);

    int AT_GetString(int Hndl, WString Feature, CharBuffer String, int StringLength);

    int AT_GetStringMaxLength(int Hndl, WString Feature, IntBuffer MaxStringLength);

    int AT_QueueBuffer(int Hndl, ByteBuffer Ptr, int PtrSize);

    int AT_WaitBuffer(int Hndl, PointerByReference Ptr, IntBuffer PtrSize, long Timeout);

    int AT_Flush(int Hndl);

}
