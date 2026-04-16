package jisa.devices.spectrometer;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.spectrometer.feature.AdjustableSlit;
import jisa.devices.spectrometer.feature.Shuttered;
import jisa.visa.NativeDevice;

import java.io.IOException;
import java.util.List;

public class Kymera extends NativeDevice implements Spectrograph, AdjustableSlit, Shuttered {

    public final SwappableGrating SWAPPABLE_GRATING = new SwappableGrating();
    public final Flipper          INPUT_MIRROR      = new Flipper(0, "Input Port");
    public final Flipper          OUTPUT_MIRROR     = new Flipper(1, "Output Port");

    public final Grating GRATING_1 = new Grating(0, "Grating 1", 0.0);
    public final Grating GRATING_2 = new Grating(0, "Grating 2", 0.0);
    public final Grating GRATING_3 = new Grating(0, "Grating 3", 0.0);
    public final Grating GRATING_4 = new Grating(0, "Grating 4", 0.0);

    protected Kymera(Object indexObject) throws IOException, DeviceException {
        super("Andor Kymera Spectrograph");
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
            return List.of(GRATING_1, GRATING_2, GRATING_3, GRATING_4);
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
