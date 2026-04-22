package jisa.devices.spectrometer;

import com.sun.jna.Memory;
import com.sun.jna.Platform;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.IDAddress;
import jisa.devices.DeviceException;
import jisa.devices.spectrometer.feature.Shuttered;
import jisa.devices.spectrometer.feature.XCalibrated;
import jisa.devices.spectrometer.nat.ATSpectrograph;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class Kymera extends NativeDevice implements Spectrograph, Shuttered, XCalibrated {

    public final SwappableGrating     SWAPPABLE_GRATING;
    public final FilterWheel          FILTER_WHEEL;
    public final List<Flipper>        FLIPPERS;
    public final List<AdjustableSlit> SLITS;
    public final List<Iris>           IRISES;
    public final List<Component>      COMPONENTS;

    public final List<Grating> GRATINGS;
    public final List<Filter>  FILTERS;

    protected final ATSpectrograph sdk;
    protected final int            device;
    protected final IDAddress      address;

    protected Kymera(Object indexObject) throws IOException, DeviceException {

        super("Andor Kymera Spectrograph");

        List<String> extraPaths = new LinkedList<>();

        if (Platform.isWindows() && System.getenv("ProgramFiles") != null) {
            extraPaths.add(Util.joinPath(System.getenv("ProgramFiles"), "Andor SDK", "ATSpectrograph", Platform.is64Bit() ? "64" : "32"));
        }

        sdk = findLibrary(ATSpectrograph.class, "atspectrograph", extraPaths);

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

        address = indexObject instanceof IDAddress ? (IDAddress) indexObject : new IDAddress(String.valueOf(indexObject));

        // Check how many devices there are
        int count = getIntByReference(sdk::ATSpectrographGetNumberDevices, "GetNumberDevices");

        if (device >= count) {
            throw new DeviceException("No spectrograph with index %d found.", device);
        }

        List<Flipper>        flippers = new LinkedList<>();
        List<AdjustableSlit> slits    = new LinkedList<>();
        List<Grating>        gratings = new LinkedList<>();
        List<Iris>           irises   = new LinkedList<>();
        List<Filter>         filters  = new LinkedList<>();

        for (int i = 0; i < 2; i++) {

            int fi = i + 1;

            try {

                int result = getIntByReference(buffer -> sdk.ATSpectrographFlipperMirrorIsPresent(device, fi, buffer), "FlipperMirrorIsPresent(" + fi + ")");

                if (result == 1) {
                    flippers.add(new Flipper(i + 1, String.format("%s Port Flipper", i == 0 ? "Input" : "Output")));
                }

            } catch (Throwable ignored) {
            }

        }

        for (int i = 0; i < 4; i++) {

            int fi = i + 1;

            try {

                int result = getIntByReference(buffer -> sdk.ATSpectrographSlitIsPresent(device, fi, buffer), "SlitIsPresent");

                if (result == 1) {
                    slits.add(new AdjustableSlit(i + 1, String.format("Adjustable Slit %d", i + 1)));
                }

            } catch (Throwable ignored) {
            }

        }

        for (int i = 0; i < 4; i++) {

            int fi = i + 1;

            try {

                int result = getIntByReference(buffer -> sdk.ATSpectrographIrisIsPresent(device, fi, buffer), "IrisIsPresent(" + fi + ")");

                if (result == 1) {
                    irises.add(new Iris(i + 1, String.format("Iris %d", i + 1)));
                }

            } catch (Throwable ignored) {
            }

        }


        int gratingCount = getIntByReference(buffer -> sdk.ATSpectrographGetNumberGratings(device, buffer), "GetNumberGratings");

        for (int i = 0; i < gratingCount; i++) {
            gratings.add(new Grating(i + 1, String.format("Grating %d", i + 1)));
        }

        for (int i = 0; i < 6; i++) {
            filters.add(new Filter(i + 1, String.format("Filter %d", i + 1)));
        }

        boolean gratingPresent = getIntByReference(buffer -> sdk.ATSpectrographGratingIsPresent(device, buffer), "GratingIsPresent") == 1;
        boolean filterPresent  = getIntByReference(buffer -> sdk.ATSpectrographFilterIsPresent(device, buffer), "FilterIsPresent") == 1;

        List<Component> singles = new LinkedList<>();

        if (gratingPresent) {
            SWAPPABLE_GRATING = new SwappableGrating(gratings);
            singles.add(SWAPPABLE_GRATING);
        } else {
            SWAPPABLE_GRATING = null;
        }

        if (filterPresent) {
            FILTER_WHEEL = new FilterWheel(filters);
            singles.add(FILTER_WHEEL);
        } else {
            FILTER_WHEEL = null;
        }

        FLIPPERS   = List.copyOf(flippers);
        SLITS      = List.copyOf(slits);
        IRISES     = List.copyOf(irises);
        COMPONENTS = Util.joinLists(singles, FLIPPERS, SLITS, IRISES);

        GRATINGS = List.copyOf(gratings);
        FILTERS  = List.copyOf(filters);

    }

    protected void handle(int result, String method) throws IOException, DeviceException {

        switch (result) {

            case ATSpectrograph.ERROR_CODE_SUCCESS:
                return;

            case ATSpectrograph.ERROR_CODE_COMMUNICATION_ERROR:
                throw new IOException(String.format("%s: Communication error.", method));

            case ATSpectrograph.ERROR_CODE_ERROR:
                throw new DeviceException(String.format("%s: Command Failed.", method));

            case ATSpectrograph.ERROR_CODE_P1INVALID:
                throw new DeviceException(String.format("%s: Parameter 1 invalid.", method));

            case ATSpectrograph.ERROR_CODE_P2INVALID:
                throw new DeviceException(String.format("%s: Parameter 2 invalid.", method));

            case ATSpectrograph.ERROR_CODE_P3INVALID:
                throw new DeviceException(String.format("%s: Parameter 3 invalid.", method));

            case ATSpectrograph.ERROR_CODE_P4INVALID:
                throw new DeviceException(String.format("%s: Parameter 4 invalid.", method));

            case ATSpectrograph.ERROR_CODE_P5INVALID:
                throw new DeviceException(String.format("%s: Parameter 5 invalid.", method));

            case ATSpectrograph.ERROR_CODE_NOT_INITIALIZED:
                throw new DeviceException(String.format("%s: ATSpectrograph library not initialized.", method));

            case ATSpectrograph.ERROR_CODE_NOT_AVAILABLE:
                throw new DeviceException(String.format("%s: Device not available.", method));

        }

    }

    @Override
    public double[] getWavelengths(int fullWidth, int startX, int width) throws IOException, DeviceException {

        FloatBuffer buffer = FloatBuffer.allocate(fullWidth);
        handle(sdk.ATSpectrographGetCalibration(device, buffer, fullWidth), "GetCalibration");

        float[] wavelengths = new float[width];

        buffer.get(wavelengths, startX, width);

        return IntStream.range(0, wavelengths.length).mapToDouble(i -> wavelengths[i]).toArray();

    }

    public interface ByReference<B extends Buffer> {
        int execute(B buffer);
    }

    protected double getDoubleByReference(ByReference<FloatBuffer> method, String methodName) throws IOException, DeviceException {

        try (Memory memory = new Memory(Float.BYTES)) {

            FloatBuffer buffer = memory.getByteBuffer(0, Float.BYTES).asFloatBuffer();
            handle(method.execute(buffer), methodName);

            return memory.getFloat(0);

        }

    }

    protected int getIntByReference(ByReference<IntBuffer> method, String methodName) throws IOException, DeviceException {

        try (Memory memory = new Memory(Integer.BYTES)) {

            IntBuffer buffer = memory.getByteBuffer(0, Integer.BYTES).asIntBuffer();
            handle(method.execute(buffer), methodName);

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
        return address;
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

        handle(sdk.ATSpectrographSetShutter(device, index), "SetShutter");

    }

    @Override
    public Mode getShutterMode() throws IOException, DeviceException {

        int index = getIntByReference(buffer -> sdk.ATSpectrographGetShutter(device, buffer), "GetShutter");

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
            return getIntByReference(buffer -> sdk.ATSpectrographGetFlipperMirror(device, index, buffer), "GetFlipperMirror") - 1;
        }

        @Override
        public void setRoute(int route) throws IOException, DeviceException {
            handle(sdk.ATSpectrographSetFlipperMirror(device, index, route + 1), "SetFlipperMirror");
        }

        @Override
        public String getName() {
            return name;
        }

    }

    public class SwappableGrating implements Spectrograph.SwappableGrating<Kymera> {

        public final Grating GRATING_1;
        public final Grating GRATING_2;
        public final Grating GRATING_3;
        public final Grating GRATING_4;

        protected SwappableGrating(List<Grating> gratings) {

            GRATING_1 = gratings.stream().filter(g -> g.getIndex() == 1).findFirst().orElse(null);
            GRATING_2 = gratings.stream().filter(g -> g.getIndex() == 2).findFirst().orElse(null);
            GRATING_3 = gratings.stream().filter(g -> g.getIndex() == 3).findFirst().orElse(null);
            GRATING_4 = gratings.stream().filter(g -> g.getIndex() == 4).findFirst().orElse(null);

        }

        @Override
        public Grating getValue() throws IOException, DeviceException {

            int index = getIntByReference(buffer -> sdk.ATSpectrographGetGrating(device, buffer), "GetGrating");

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

            handle(sdk.ATSpectrographSetGrating(device, value.getIndex()), "SetGrating");

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
            return getDoubleByReference(reference -> sdk.ATSpectrographGetSlitWidth(device, index, reference), "GetSlitWidth");
        }

        @Override
        public void setValue(Double value) throws IOException, DeviceException {
            handle(sdk.ATSpectrographSetSlitWidth(device, index, value.floatValue()), "SetSlitWidth");
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
            return (double) getIntByReference(buffer -> sdk.ATSpectrographGetIris(device, index, buffer), "GetIris");
        }

        @Override
        public void setValue(Double value) throws IOException, DeviceException {
            handle(sdk.ATSpectrographSetIris(device, index, (int) Math.round(value)), "SetIris");
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

        public final Filter FILTER_1;
        public final Filter FILTER_2;
        public final Filter FILTER_3;
        public final Filter FILTER_4;
        public final Filter FILTER_5;
        public final Filter FILTER_6;

        public FilterWheel(List<Filter> filters) {

            FILTER_1 = filters.stream().filter(f -> f.getIndex() == 1).findFirst().orElse(null);
            FILTER_2 = filters.stream().filter(f -> f.getIndex() == 2).findFirst().orElse(null);
            FILTER_3 = filters.stream().filter(f -> f.getIndex() == 3).findFirst().orElse(null);
            FILTER_4 = filters.stream().filter(f -> f.getIndex() == 4).findFirst().orElse(null);
            FILTER_5 = filters.stream().filter(f -> f.getIndex() == 5).findFirst().orElse(null);
            FILTER_6 = filters.stream().filter(f -> f.getIndex() == 6).findFirst().orElse(null);

        }

        @Override
        public Filter getValue() throws IOException, DeviceException {

            int index = getIntByReference(buffer -> sdk.ATSpectrographGetFilter(device, buffer), "GetFilter");

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

            handle(sdk.ATSpectrographSetFilter(device, value.getIndex()), "SetFilter");

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
