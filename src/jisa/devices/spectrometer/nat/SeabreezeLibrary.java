package jisa.devices.spectrometer.nat;

import jisa.Util;
import jisa.visa.Library;

import java.nio.*;

public interface SeabreezeLibrary extends Library {

    @Override
    default void initialise() throws Exception {
        Util.addShutdownHook(this::seabreeze_shutdown);
    }

    /**
     *
     * This function opens a device attached to the system.
     *
     * @param index      (Input) The index of a USB device to try to open.
     *                   Valid values will range from 0 to N-1 for N connected devices.
     * @param error_code (Output) A pointer to an integer that can be used
     *                   for storing error codes.
     *
     * @return int: The function will return an integer of 0 if it opened a
     * device successfully, or 1 if no device was opened (in which
     * case the error_code variable will be set).
     * <p>
     * This can be called repeatedly with incrementing index values (until
     * it returns 1) to open all connected devices.
     * <p>
     * Note that the index used to open a device with this function should also
     * be used to communicate with that same device in the other functions
     * provided here.
     */
    int seabreeze_open_spectrometer(int index, IntBuffer error_code);

    int seabreeze_close_spectrometer(int index, IntBuffer error_code);

    int seabreeze_get_error_string(int error_code, CharBuffer buffer, int buffer_length);

    int seabreeze_get_model(int index, IntBuffer error_code, CharBuffer buffer, int buffer_length);

    void seabreeze_set_trigger_mode(int index, IntBuffer error_code, int mode);

    void seabreeze_set_integration_time_microsec(int index, IntBuffer error_code, long integration_time_micros);

    long seabreeze_get_min_integration_time_microsec(int index, IntBuffer error_code);

    void seabreeze_set_shutter_open(int index, IntBuffer error_code, byte opened);

    void seabreeze_set_strobe_enable(int index, IntBuffer error_code, byte strobe_enable);

    int seabreeze_get_light_source_count(int index, IntBuffer error_code);

    void seabreeze_set_light_source_enable(int index, IntBuffer error_code, int light_index, byte enable);

    void seabreeze_set_light_source_intensity(int index, IntBuffer error_code, int light_index, double intensity);

    int seabreeze_read_eeprom_slot(int index, IntBuffer error_code, int slot_number, ByteBuffer buffer, int buffer_length);

    int seabreeze_write_eeprom_slot(int index, IntBuffer error_code, int slot_number, ByteBuffer buffer, int buffer_length);

    int seabreeze_read_irrad_calibration(int index, IntBuffer error_code, FloatBuffer buffer, int buffer_length);

    int seabreeze_write_irrad_calibration(int index, IntBuffer error_code, FloatBuffer buffer, int buffer_length);

    int seabreeze_has_irrad_collection_area(int index, IntBuffer error_code);

    float seabreeze_read_irrad_collection_area(int index, IntBuffer error_code);

    void seabreeze_write_irrad_collection_area(int index, IntBuffer error_code, float area);

    double seabreeze_read_tec_temperature(int index, IntBuffer error_code);

    void seabreeze_set_tec_temperature(int index, IntBuffer error_code, double temperature_degrees_celsius);

    void seabreeze_set_tec_enable(int index, IntBuffer error_code, byte tec_enable);

    void seabreeze_set_tec_fan_enable(int index, IntBuffer error_code, byte tec_fan_enable);

    int seabreeze_get_unformatted_spectrum(int index, IntBuffer error_code, ByteBuffer buffer, int buffer_length);

    int seabreeze_get_formatted_spectrum(int index, IntBuffer error_code, DoubleBuffer buffer, int buffer_length);

    int seabreeze_get_unformatted_spectrum_length(int index, IntBuffer error_code);

    int seabreeze_get_formatted_spectrum_length(int index, IntBuffer error_code);

    int seabreeze_get_wavelengths(int index, IntBuffer error_code, DoubleBuffer wavelengths, int length);

    int seabreeze_get_serial_number(int index, IntBuffer error_code, String buffer, int buffer_length);

    int seabreeze_get_electric_dark_pixel_indices(int index, IntBuffer error_code, IntBuffer indices, int length);

    void seabreeze_shutdown();

    int seabreeze_write_usb(int index, IntBuffer errorCode, byte endpoint, ByteBuffer buffer, int length);

    int seabreeze_read_usb(int index, IntBuffer errorCode, byte endpoint, ByteBuffer buffer, int length);

    int seabreeze_get_api_version_string(String buffer, int len);

    int seabreeze_get_usb_descriptor_string(int index, IntBuffer errorCode, int id, ByteBuffer buffer, int len);

    void seabreeze_set_continuous_strobe_period_microsec(int index, IntBuffer errorCode, short strobe_id, long period_usec);

    void seabreeze_set_acquisition_delay_microsec(int index, IntBuffer errorCode, long delay_usec);

    void seabreeze_clear_buffer(int index, IntBuffer error_code);

    long seabreeze_get_buffer_element_count(int index, IntBuffer error_code);

    long seabreeze_get_buffer_capacity(int index, IntBuffer error_code);

    long seabreeze_get_buffer_capacity_maximum(int index, IntBuffer error_code);

    long seabreeze_get_buffer_capacity_minimum(int index, IntBuffer error_code);

    void seabreeze_set_buffer_capacity(int index, IntBuffer error_code, long capacity);

    void seabreeze_set_verbose(int flag);

    void seabreeze_set_logfile(String pathname, int len);

}
