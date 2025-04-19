package jisa.devices.camera.nat;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import jisa.Util;
import jisa.visa.InitialisableLibrary;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public interface ThorCamMosaicLibrary extends InitialisableLibrary {

    @Override
    default void initialise() throws Exception {
        tl_mono_to_color_processing_module_initialize();
        Util.addShutdownHook(this::tl_mono_to_color_processing_module_terminate);
    }


    int tl_mono_to_color_processing_module_initialize();

    int tl_mono_to_color_create_mono_to_color_processor(int sensor_type, int filter_type, FloatBuffer colour_correction_matrix, FloatBuffer white_balance_matrix, int bit_depth, PointerByReference handle);

    int tl_mono_to_color_destroy_mono_to_color_processor(Pointer handle);

    int tl_mono_to_color_transform_to_48(Pointer handle, ByteBuffer input, int width, int height, ByteBuffer output);

    int tl_mono_to_color_processing_module_terminate();

}
