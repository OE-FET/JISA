package jisa.devices.camera.nat;

import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.PointerByReference;
import jisa.Util;
import jisa.visa.InitialisableLibrary;

import java.nio.*;

public interface ThorCamLibrary extends InitialisableLibrary {

    default void initialise() throws Exception {

        tl_camera_open_sdk();
        Util.addShutdownHook(this::tl_camera_close_sdk);

    }

    int tl_camera_arm(Pointer tl_camera_handle, int number_of_frames_to_buffer);

    int tl_camera_close_camera(Pointer tl_camera_handle);

    int tl_camera_close_sdk();

    void tl_camera_connect_callback(String cameraserialnumber, int usb_port_type, Pointer context);

    int tl_camera_convert_decibels_to_gain(Pointer tl_camera_handle, double gain_db, IntBuffer index_of_gain_value);

    int tl_camera_convert_gain_to_decibels(Pointer tl_camera_handle, int index_of_gain_value, DoubleBuffer gain_db);

    int tl_camera_disarm(Pointer tl_camera_handle);

    void tl_camera_disconnect_callback(String cameraserialnumber, Pointer context);

    int tl_camera_discover_available_cameras(ByteBuffer serial_numbers, int str_length);

    void tl_camera_frame_available_callback(Pointer tl_camera_handle_sender, ShortBuffer image_buffer, int frame_count, ByteBuffer metadata, int metadata_size_in_bytes, Pointer context);

    int tl_camera_get_binx(Pointer tl_camera_handle, IntBuffer binx);

    int tl_camera_get_binx_range(Pointer tl_camera_handle, IntBuffer hbin_min, IntBuffer hbin_max);

    int tl_camera_get_biny(Pointer tl_camera_handle, IntBuffer biny);

    int tl_camera_get_biny_range(Pointer tl_camera_handle, IntBuffer vbin_min, IntBuffer vbin_max);

    int tl_camera_get_bit_depth(Pointer tl_camera_handle, IntBuffer pixel_bit_depth);

    int tl_camera_get_black_level(Pointer tl_camera_handle, IntBuffer black_level);

    int tl_camera_get_black_level_range(Pointer tl_camera_handle, IntBuffer min, IntBuffer max);

    int tl_camera_get_camera_color_correction_matrix_output_color_space(Pointer tl_camera_handle, String output_color_space);

    int tl_camera_get_camera_sensor_type(Pointer tl_camera_handle, IntBuffer camera_sensor_type);

    int tl_camera_get_color_correction_matrix(Pointer tl_camera_handle, FloatBuffer matrix);

    int tl_camera_get_color_filter_array_phase(Pointer tl_camera_handle, IntBuffer cfaphase);

    int tl_camera_get_is_cooling_enabled(Pointer tl_camera_handle, IntBuffer is_cooling_enabled);

    int tl_camera_get_data_rate(Pointer tl_camera_handle, IntBuffer data_rate);

    int tl_camera_get_default_white_balance_matrix(Pointer tl_camera_handle, FloatBuffer matrix);

    int tl_camera_get_exposure_time(Pointer tl_camera_handle, LongBuffer exposure_time_us);

    int tl_camera_get_exposure_time_range(Pointer tl_camera_handle, LongBuffer exposure_time_us_min, LongBuffer exposure_time_us_max);

    int tl_camera_get_firmware_version(Pointer tl_camera_handle, String firmware_version, int str_length);

    int tl_camera_get_frame_available_callback(Pointer tl_camera_handle, ThorCamLibrary.tl_camera_frame_available_callback handler);

    int tl_camera_get_frame_rate_control_value(Pointer tl_camera_handle, DoubleBuffer frame_rate_fps);

    int tl_camera_get_frame_time(Pointer tl_camera_handle, IntBuffer frame_time_us);

    int tl_camera_get_image_poll_timeout(Pointer tl_camera_handle, IntBuffer timeout_ms);

    int tl_camera_get_communication_interface(Pointer tl_camera_handle, IntBuffer communication_interface);

    int tl_camera_get_eep_status(Pointer tl_camera_handle, IntBuffer eep_status_enum);

    int tl_camera_get_frames_per_trigger_range(Pointer tl_camera_handle, IntBuffer number_of_frames_per_trigger_min, IntBuffer number_of_frames_per_trigger_max);

    int tl_camera_get_frames_per_trigger_zero_for_unlimited(Pointer tl_camera_handle, IntBuffer number_of_frames_per_trigger_or_zero_for_unlimited);

    int tl_camera_get_frame_rate_control_value_range(Pointer tl_camera_handle, DoubleBuffer frame_rate_fps_min, DoubleBuffer frame_rate_fps_max);

    int tl_camera_get_gain(Pointer tl_camera_handle, IntBuffer gain);

    int tl_camera_get_gain_range(Pointer tl_camera_handle, IntBuffer gain_min, IntBuffer gain_max);

    int tl_camera_get_hot_pixel_correction_threshold(Pointer tl_camera_handle, IntBuffer hot_pixel_correction_threshold);

    int tl_camera_get_hot_pixel_correction_threshold_range(Pointer tl_camera_handle, IntBuffer hot_pixel_correction_threshold_min, IntBuffer hot_pixel_correction_threshold_max);

    int tl_camera_get_image_height(Pointer tl_camera_handle, IntBuffer height_pixels);

    int tl_camera_get_image_height_range(Pointer tl_camera_handle, IntBuffer image_height_pixels_min, IntBuffer image_height_pixels_max);

    int tl_camera_get_image_width(Pointer tl_camera_handle, IntBuffer width_pixels);

    int tl_camera_get_image_width_range(Pointer tl_camera_handle, IntBuffer image_width_pixels_min, IntBuffer image_width_pixels_max);

    int tl_camera_get_is_armed(Pointer tl_camera_handle, IntBuffer is_armed);

    int tl_camera_get_is_cooling_supported(Pointer tl_camera_handle, IntBuffer is_cooling_supported);

    int tl_camera_get_is_data_rate_supported(Pointer tl_camera_handle, int data_rate, IntBuffer is_data_rate_supported);

    int tl_camera_get_is_eep_supported(Pointer tl_camera_handle, IntBuffer is_eep_supported);

    int tl_camera_get_is_frame_rate_control_enabled(Pointer tl_camera_handle, IntBuffer is_enabled);

    int tl_camera_get_is_led_on(Pointer tl_camera_handle, IntBuffer is_led_on);

    int tl_camera_get_is_led_supported(Pointer tl_camera_handle, IntBuffer is_led_supported);

    int tl_camera_get_is_hot_pixel_correction_enabled(Pointer tl_camera_handle, IntBuffer is_hot_pixel_correction_enabled);

    int tl_camera_get_is_nir_boost_supported(Pointer tl_camera_handle, IntBuffer is_nir_boost_supported);

    int tl_camera_get_is_operation_mode_supported(Pointer tl_camera_handle, int operation_mode, IntBuffer is_operation_mode_supported);

    int tl_camera_get_is_taps_supported(Pointer tl_camera_handle, IntBuffer is_taps_supported, int tap);

    int tl_camera_get_measured_frame_rate(Pointer tl_camera_handle, DoubleBuffer frames_per_second);

    int tl_camera_get_model(Pointer tl_camera_handle, String model, int str_length);

    int tl_camera_get_model_string_length_range(Pointer tl_camera_handle, IntBuffer model_min, IntBuffer model_max);

    int tl_camera_get_name(Pointer tl_camera_handle, String name, int str_length);

    int tl_camera_get_name_string_length_range(Pointer tl_camera_handle, IntBuffer name_min, IntBuffer name_max);

    int tl_camera_get_nir_boost_enable(Pointer tl_camera_handle, IntBuffer nir_boost_enable);

    int tl_camera_get_operation_mode(Pointer tl_camera_handle, IntBuffer operation_mode);

    int tl_camera_get_pending_frame_or_null(Pointer tl_camera_handle, PointerByReference image_buffer, IntBuffer frame_count, PointerByReference metadata, IntBuffer metadata_size_in_bytes);

    int tl_camera_get_polar_phase(Pointer tl_camera_handle, IntBuffer polar_phase);

    int tl_camera_get_roi(Pointer tl_camera_handle, IntBuffer upper_left_x_pixels, IntBuffer upper_left_y_pixels, IntBuffer lower_right_x_pixels, IntBuffer lower_right_y_pixels);

    int tl_camera_get_roi_range(Pointer tl_camera_handle, IntBuffer upper_left_x_pixels_min, IntBuffer upper_left_y_pixels_min, IntBuffer lower_right_x_pixels_min, IntBuffer lower_right_y_pixels_min, IntBuffer upper_left_x_pixels_max, IntBuffer upper_left_y_pixels_max, IntBuffer lower_right_x_pixels_max, IntBuffer lower_right_y_pixels_max);

    int tl_camera_get_sensor_height(Pointer tl_camera_handle, IntBuffer height_pixels);

    int tl_camera_get_sensor_pixel_height(Pointer tl_camera_handle, DoubleBuffer pixel_height_um);

    int tl_camera_get_sensor_pixel_size_bytes(Pointer tl_camera_handle, IntBuffer sensor_pixel_size_bytes);

    int tl_camera_get_sensor_pixel_width(Pointer tl_camera_handle, DoubleBuffer pixel_width_um);

    int tl_camera_get_sensor_readout_time(Pointer tl_camera_handle, IntBuffer sensor_readout_time_ns);

    int tl_camera_get_sensor_width(Pointer tl_camera_handle, IntBuffer width_pixels);

    int tl_camera_get_serial_number(Pointer tl_camera_handle, ByteBuffer serial_number, int str_length);

    int tl_camera_get_serial_number_string_length_range(Pointer tl_camera_handle, IntBuffer serial_number_min, IntBuffer serial_number_max);

    int tl_camera_get_tap_balance_enable(Pointer tl_camera_handle, IntBuffer taps_balance_enable);

    int tl_camera_get_taps(Pointer tl_camera_handle, IntBuffer taps);

    int tl_camera_get_timestamp_clock_frequency(Pointer tl_camera_handle, IntBuffer timestamp_clock_frequency_hz_or_zero);

    int tl_camera_get_trigger_polarity(Pointer tl_camera_handle, IntBuffer trigger_polarity_enum);

    int tl_camera_get_usb_port_type(Pointer tl_camera_handle, IntBuffer usb_port_type);

    int tl_camera_get_user_memory(Pointer tl_camera_handle, ByteBuffer destination_data_buffer, long number_of_bytes_to_read, long camera_user_memory_offset_bytes);

    int tl_camera_get_user_memory_maximum_size(Pointer tl_camera_handle, LongBuffer maximum_size_bytes);

    int tl_camera_issue_software_trigger(Pointer tl_camera_handle);

    int tl_camera_open_camera(String camera_serial_number, PointerByReference tl_camera_handle);

    int tl_camera_open_sdk();

    int tl_camera_set_binx(Pointer tl_camera_handle, int binx);

    int tl_camera_set_biny(Pointer tl_camera_handle, int biny);

    int tl_camera_set_black_level(Pointer tl_camera_handle, int black_level);

    int tl_camera_set_data_rate(Pointer tl_camera_handle, int data_rate);

    int tl_camera_set_exposure_time(Pointer tl_camera_handle, long exposure_time_us);

    int tl_camera_set_frame_available_callback(Pointer tl_camera_handle, ThorCamLibrary.tl_camera_frame_available_callback handler, Pointer context);

    int tl_camera_set_frame_rate_control_value(Pointer tl_camera_handle, double frame_rate_fps);

    int tl_camera_set_frames_per_trigger_zero_for_unlimited(Pointer tl_camera_handle, int number_of_frames_per_trigger_or_zero_for_unlimited);

    int tl_camera_set_gain(Pointer tl_camera_handle, int gain);

    int tl_camera_set_hot_pixel_correction_threshold(Pointer tl_camera_handle, int hot_pixel_correction_threshold);

    int tl_camera_set_image_poll_timeout(Pointer tl_camera_handle, int timeout_ms);

    int tl_camera_set_is_eep_enabled(Pointer tl_camera_handle, int is_eep_enabled);

    int tl_camera_set_is_frame_rate_control_enabled(Pointer tl_camera_handle, int is_enabled);

    int tl_camera_set_is_hot_pixel_correction_enabled(Pointer tl_camera_handle, int is_hot_pixel_correction_enabled);

    int tl_camera_set_is_led_on(Pointer tl_camera_handle, int is_led_on);

    int tl_camera_set_name(Pointer tl_camera_handle, String name);

    int tl_camera_set_nir_boost_enable(Pointer tl_camera_handle, int nir_boost_enable);

    int tl_camera_set_operation_mode(Pointer tl_camera_handle, int operation_mode);

    int tl_camera_set_roi(Pointer tl_camera_handle, int upper_left_x_pixels, int upper_left_y_pixels, int lower_right_x_pixels, int lower_right_y_pixels);

    int tl_camera_set_tap_balance_enable(Pointer tl_camera_handle, int taps_balance_enable);

    int tl_camera_set_taps(Pointer tl_camera_handle, int taps);

    int tl_camera_set_trigger_polarity(Pointer tl_camera_handle, int trigger_polarity_enum);

    int tl_camera_set_user_memory(Pointer tl_camera_handle, ByteBuffer source_data_buffer, long number_of_bytes_to_write, long camera_user_memory_offset_bytes);

    Pointer tl_camera_get_last_error();

    /**
     * Pointer to unknown (opaque) type
     */
    class tl_camera_frame_available_callback extends PointerType {
        public tl_camera_frame_available_callback(Pointer address) {
            super(address);
        }

        public tl_camera_frame_available_callback() {
            super();
        }
    }

}
