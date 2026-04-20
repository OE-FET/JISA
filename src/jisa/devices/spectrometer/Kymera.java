package jisa.devices.spectrometer;

import com.sun.jna.Memory;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.spectrometer.feature.Shuttered;
import jisa.devices.spectrometer.nat.ATSpectrograph;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

public class Kymera extends NativeDevice implements Spectrograph, Shuttered {

    public final SwappableGrating     SWAPPABLE_GRATING;
    public final FilterWheel          FILTER_WHEEL;
    public final List<Flipper>        FLIPPERS;
    public final List<AdjustableSlit> SLITS;
    public final List<Iris>           IRISES;
    public final List<Component>      COMPONENTS;

    public final List<Grating> GRATINGS;
    public final List<Filter>  FILTERS;

    protected final ATSpectrograph sdk;
    private final   int            device;

    protected Kymera(Object indexObject) throws IOException, DeviceException {

        super("Andor Kymera Spectrograph");

        if (indexObject == null) {

            sdk    = null;
            device = -1;

            SWAPPABLE_GRATING = new SwappableGrating();
            FILTER_WHEEL      = new FilterWheel();
            FLIPPERS          = List.of(new Flipper(1, "Input Port Flipper"), new Flipper(2, "Output Port Flipper"));
            SLITS             = List.of(new AdjustableSlit(1, "Adjustable Slit 1"));
            GRATINGS          = List.of(new Grating(1, "Grating 1"), new Grating(2, "Grating 2"), new Grating(3, "Grating 3"), new Grating(4, "Grating 4"));
            FILTERS           = List.of(new Filter(1, "Filter 1"), new Filter(2, "Filter 2"), new Filter(3, "Filter 3"), new Filter(4, "Filter 4"), new Filter(5, "Filter 5"), new Filter(6, "Filter 6"));
            IRISES            = List.of(new Iris(1, "Iris 1"), new Iris(2, "Iris 2"));
            COMPONENTS        = Util.joinLists(List.of(SWAPPABLE_GRATING, FILTER_WHEEL), FLIPPERS, SLITS, IRISES);

            return;

        }

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

        // Check how many devices there are
        int count = getIntByReference(sdk::ATSpectrographGetNumberDevices);

        if (device >= count) {
            throw new DeviceException("No spectrograph with index %d found.", device);
        }

        List<Flipper>        flippers = new LinkedList<>();
        List<AdjustableSlit> slits    = new LinkedList<>();
        List<Grating>        gratings = new LinkedList<>();
        List<Iris>           irises   = new LinkedList<>();
        List<Filter>         filters  = new LinkedList<>();

        for (int i = 0; i < 2; i++) {

            int fi     = i;
            int result = getIntByReference(buffer -> sdk.ATSpectrographFlipperMirrorIsPresent(device, fi, buffer));

            if (result == 1) {
                flippers.add(new Flipper(i + 1, String.format("%s Port Flipper", i == 0 ? "Input" : "Output")));
            }

        }

        for (int i = 0; i < 4; i++) {

            int fi     = i;
            int result = getIntByReference(buffer -> sdk.ATSpectrographSlitIsPresent(device, fi, buffer));

            if (result == 1) {
                slits.add(new AdjustableSlit(i + 1, String.format("Adjustable Slit %d", i + 1)));
            }

        }

        for (int i = 0; i < 4; i++) {

            int fi     = i;
            int result = getIntByReference(buffer -> sdk.ATSpectrographIrisIsPresent(device, fi, buffer));

            if (result == 1) {
                irises.add(new Iris(i + 1, String.format("Iris %d", i + 1)));
            }

        }


        int gratingCount = getIntByReference(buffer -> sdk.ATSpectrographGetNumberGratings(device, buffer));

        for (int i = 0; i < gratingCount; i++) {
            gratings.add(new Grating(i + 1, String.format("Grating %d", i + 1)));
        }

        boolean gratingPresent = getIntByReference(buffer -> sdk.ATSpectrographGratingIsPresent(device, buffer)) == 1;
        boolean filterPresent  = getIntByReference(buffer -> sdk.ATSpectrographFilterIsPresent(device, buffer)) == 1;

        List<Component> singles = new LinkedList<>();

        if (gratingPresent) {
            SWAPPABLE_GRATING = new SwappableGrating();
            singles.add(SWAPPABLE_GRATING);
        } else {
            SWAPPABLE_GRATING = null;
        }

        if (filterPresent) {
            FILTER_WHEEL = new FilterWheel();
            singles.add(FILTER_WHEEL);
        } else {
            FILTER_WHEEL = null;
        }

        for (int i = 0; i < 6; i++) {
            filters.add(new Filter(i + 1, String.format("Filter %d", i + 1)));
        }

        FLIPPERS   = List.copyOf(flippers);
        SLITS      = List.copyOf(slits);
        IRISES     = List.copyOf(irises);
        COMPONENTS = Util.joinLists(singles, FLIPPERS, SLITS, IRISES);

        GRATINGS = List.copyOf(gratings);
        FILTERS  = List.copyOf(filters);

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

    public interface ByReference<B extends Buffer> {
        int execute(B buffer);
    }

    protected double getDoubleByReference(ByReference<FloatBuffer> method) throws IOException, DeviceException {

        try (Memory memory = new Memory(Float.BYTES)) {

            FloatBuffer buffer = memory.getByteBuffer(0, Float.BYTES).asFloatBuffer();
            handle(method.execute(buffer));

            return memory.getFloat(0);

        }

    }

    protected int getIntByReference(ByReference<IntBuffer> method) throws IOException, DeviceException {

        try (Memory memory = new Memory(Integer.BYTES)) {

            IntBuffer buffer = memory.getByteBuffer(0, Integer.BYTES).asIntBuffer();
            handle(method.execute(buffer));

            return memory.getInt(0);

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
        return COMPONENTS;
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
        /* nothing to do */
    }

    @Override
    public Address getAddress() {
        return new IDAddress(String.valueOf(device));
    }

    @Override
    public void setShutterMode(Mode mode) throws IOException, DeviceException {

        int index;

        switch (mode) {

            case CLOSED:
                index = 0;
                break;

            case OPEN:
                index = 1;
                break;

            case EXTERNAL:
                index = 2;
                break;

            default:
                throw new DeviceException("Unknown mode: " + mode);

        }

        handle(sdk.ATSpectrographSetShutter(device, index));

    }

    @Override
    public Mode getShutterMode() throws IOException, DeviceException {

        int index = getIntByReference(buffer -> sdk.ATSpectrographGetShutter(device, buffer));

        switch (index) {

            case 0:
                return Mode.CLOSED;

            case 1:
                return Mode.OPEN;

            case 2:
                return Mode.EXTERNAL;

            default:
                throw new IOException("Unknown mode response from Kymera: " + index);

        }

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
            return getIntByReference(buffer -> sdk.ATSpectrographGetFlipperMirror(device, index, buffer)) - 1;
        }

        @Override
        public void setRoute(int route) throws IOException, DeviceException {
            handle(sdk.ATSpectrographSetFlipperMirror(device, index, route + 1));
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

            int index = getIntByReference(buffer -> sdk.ATSpectrographGetGrating(device, buffer));

            return GRATINGS.stream()
                           .filter(grating -> grating.getIndex() == index)
                           .findFirst()
                           .orElseThrow(() -> new IOException("Invalid grating index response from Kymera."));

        }

        @Override
        public void setValue(Grating value) throws IOException, DeviceException {

            if (!GRATINGS.contains(value)) {
                throw new DeviceException("Invalid grating specified.");
            }

            handle(sdk.ATSpectrographSetGrating(device, value.getIndex()));

        }

        @Override
        public List<Grating> getPossibleValues() throws IOException, DeviceException {
            return GRATINGS;
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

    public class AdjustableSlit implements Spectrograph.AdjustableSlit<Kymera> {

        private final String name;
        private final int    index;

        public AdjustableSlit(int index, String name) {
            this.name  = name;
            this.index = index;
        }

        @Override
        public Double getValue() throws IOException, DeviceException {
            return getDoubleByReference(reference -> sdk.ATSpectrographGetSlitWidth(device, index, reference));
        }

        @Override
        public void setValue(Double value) throws IOException, DeviceException {
            handle(sdk.ATSpectrographSetSlitWidth(device, index, value.floatValue()));
        }

        @Override
        public Double getMin() throws IOException, DeviceException {
            return 0.0;
        }

        @Override
        public Double getMax() throws IOException, DeviceException {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        public Kymera getParentInstrument() {
            return Kymera.this;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    public class Iris implements Spectrograph.Iris<Kymera> {

        private final int    index;
        private final String name;

        public Iris(int index, String name) {
            this.index = index;
            this.name  = name;
        }

        @Override
        public Double getValue() throws IOException, DeviceException {
            return (double) getIntByReference(buffer -> sdk.ATSpectrographGetIris(device, index, buffer));
        }

        @Override
        public void setValue(Double value) throws IOException, DeviceException {
            handle(sdk.ATSpectrographSetIris(device, index, (int) Math.round(value)));
        }

        @Override
        public List<Double> getPossibleValues() throws IOException, DeviceException {
            return List.of();
        }

        @Override
        public Double getMin() throws IOException, DeviceException {
            return 0.0;
        }

        @Override
        public Double getMax() throws IOException, DeviceException {
            return 100.0;
        }

        @Override
        public Kymera getParentInstrument() {
            return Kymera.this;
        }

        @Override
        public String getName() {
            return name;
        }

    }

    public class FilterWheel implements Spectrograph.FilterWheel<Kymera> {


        @Override
        public Filter getValue() throws IOException, DeviceException {

            int index = getIntByReference(buffer -> sdk.ATSpectrographGetFilter(device, buffer));

            return FILTERS.stream()
                          .filter(grating -> grating.getIndex() == index)
                          .findFirst()
                          .orElseThrow(() -> new IOException("Invalid filter index response from Kymera."));
        }

        @Override
        public void setValue(Filter value) throws IOException, DeviceException {

            if (!FILTERS.contains(value)) {
                throw new DeviceException("Invalid filter specified.");
            }

            handle(sdk.ATSpectrographSetFilter(device, value.getIndex()));

        }

        @Override
        public List<Filter> getPossibleValues() throws IOException, DeviceException {
            return FILTERS;
        }

        @Override
        public Kymera getParentInstrument() {
            return Kymera.this;
        }

        @Override
        public String getName() {
            return "Filter Wheel";
        }
    }

}
