package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.spectrometer.feature.AdjustableSlit;
import jisa.devices.spectrometer.feature.Shuttered;
import jisa.devices.spectrometer.nat.ATSpectrograph;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

public class Kymera extends NativeDevice implements Spectrograph, AdjustableSlit, Shuttered {

    public final SwappableGrating SWAPPABLE_GRATING = new SwappableGrating();
    public final Flipper          INPUT_MIRROR      = new Flipper(0, "Input Port");
    public final Flipper          OUTPUT_MIRROR     = new Flipper(1, "Output Port");

    public final Grating GRATING_1;
    public final Grating GRATING_2;
    public final Grating GRATING_3;
    public final Grating GRATING_4;

    private final List<Grating> gratings = new LinkedList<>();

    protected final ATSpectrograph sdk;
    private final   int            device;

    protected Kymera(Object indexObject) throws IOException, DeviceException {

        super("Andor Kymera Spectrograph");

        sdk = findLibrary(ATSpectrograph.class, "atspectrograph");

        if (indexObject instanceof Integer) {
            device = (Integer) indexObject;
        } else if (indexObject instanceof IDAddress) {

            String id = ((IDAddress) indexObject).getID();

            try {
                device = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                throw new DeviceException("Invalid ID: " + id);
            }

        } else {
            throw new DeviceException("Address must either be an integer or an integer wrapped in an IDAddress object.");
        }

        IntBuffer num = IntBuffer.allocate(1);
        handle(sdk.ATSpectrographGetNumberDevices(num));

        int count = num.get(0);

        if (device >= count) {
            throw new DeviceException("No spectrograph with index %d found.", device);
        }

        handle(sdk.ATSpectrographGetNumberGratings(device, num.rewind()));

        FloatBuffer lines  = FloatBuffer.allocate(1);
        ByteBuffer  blaze  = ByteBuffer.allocate(1024);
        IntBuffer   home   = IntBuffer.allocate(1);
        IntBuffer   offset = IntBuffer.allocate(1);

        if (count > 0) {
            handle(sdk.ATSpectrographGetGratingInfo(device, 1, lines, blaze, 1024, home, offset));
            GRATING_1 = new Grating(1, "Grating 1", lines.get(0));
            gratings.add(GRATING_1);
        } else {
            GRATING_1 = null;
        }

        if (count > 1) {
            handle(sdk.ATSpectrographGetGratingInfo(device, 2, lines, blaze, 1024, home, offset));
            GRATING_2 = new Grating(2, "Grating 2", lines.get(0));
            gratings.add(GRATING_2);
        } else {
            GRATING_2 = null;
        }

        if (count > 2) {
            handle(sdk.ATSpectrographGetGratingInfo(device, 3, lines, blaze, 1024, home, offset));
            GRATING_3 = new Grating(3, "Grating 3", lines.get(0));
            gratings.add(GRATING_3);
        } else {
            GRATING_3 = null;
        }

        if (count > 3) {
            handle(sdk.ATSpectrographGetGratingInfo(device, 4, lines.rewind(), blaze.clear().rewind(), 1024, home, offset));
            GRATING_4 = new Grating(4, "Grating 4", lines.get(0));
            gratings.add(GRATING_4);
        } else {
            GRATING_4 = null;
        }

    }

    protected void handle(int result) throws IOException, DeviceException {

        switch (result) {

            case ATSpectrograph.ERROR_CODE_SUCCESS:
                return;

            case ATSpectrograph.ERROR_CODE_COMMUNICATION_ERROR:
                throw new IOException("Communication error.");

            case ATSpectrograph.ERROR_CODE_ERROR:
                throw new DeviceException("Command Failed.");

            case ATSpectrograph.ERROR_CODE_P1INVALID:
                throw new DeviceException("Parameter 1 invalid.");

            case ATSpectrograph.ERROR_CODE_P2INVALID:
                throw new DeviceException("Parameter 2 invalid.");

            case ATSpectrograph.ERROR_CODE_P3INVALID:
                throw new DeviceException("Parameter 3 invalid.");

            case ATSpectrograph.ERROR_CODE_P4INVALID:
                throw new DeviceException("Parameter 4 invalid.");

            case ATSpectrograph.ERROR_CODE_P5INVALID:
                throw new DeviceException("Parameter 5 invalid.");

            case ATSpectrograph.ERROR_CODE_NOT_INITIALIZED:
                throw new DeviceException("ATSpectrograph library not initialized.");

            case ATSpectrograph.ERROR_CODE_NOT_AVAILABLE:
                throw new DeviceException("Device not available.");

        }

    }

    public Kymera(int index) throws IOException, DeviceException {
        this((Object) index);
    }

    public Kymera(Address address) throws IOException, DeviceException {
        this((Object) address);
    }

    @Override
    public List<Component> getComponents() {
        return List.of(SWAPPABLE_GRATING, INPUT_MIRROR, OUTPUT_MIRROR);
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "Andor Kymera Spectrograph";
    }

    @Override
    public String getName() {
        return "Andor Kymera Spectrograph";
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public double getSlitWidth() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setSlitWidth(double width) throws IOException, DeviceException {

    }

    @Override
    public void setShutterMode(Mode mode) throws IOException, DeviceException {

    }

    @Override
    public Mode getShutterMode() throws IOException, DeviceException {
        return Mode.OPEN;
    }

    public class Flipper implements Spectrograph.Flipper<Kymera> {

        private final int    index;
        private final String name;

        public Flipper(int index, String name) {
            this.index = index;
            this.name  = name;
        }

        @Override
        public Kymera getParentInstrument() {
            return Kymera.this;
        }

        @Override
        public int getRouteCount() throws IOException, DeviceException {
            return 2;
        }

        @Override
        public int getRoute() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public void setRoute(int route) throws IOException, DeviceException {

        }

        @Override
        public String getName() {
            return name;
        }

    }

    public class SwappableGrating implements Spectrograph.SwappableGrating<Kymera> {

        protected SwappableGrating() {

        }

        @Override
        public Grating getValue() throws IOException, DeviceException {
            return GRATING_1;
        }

        @Override
        public void setValue(Grating value) throws IOException, DeviceException {

        }

        @Override
        public List<Grating> getPossibleValues() throws IOException, DeviceException {
            return gratings;
        }

        @Override
        public Kymera getParentInstrument() {
            return Kymera.this;
        }

        @Override
        public String getName() {
            return "Swappable Grating";
        }
    }

}
