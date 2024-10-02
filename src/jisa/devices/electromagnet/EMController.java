package jisa.devices.electromagnet;

import jisa.Util;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import jisa.results.Column;
import jisa.results.DataList;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface EMController extends Instrument {

    static String getDescription() {
        return "Electromagnet Controller";
    }

    static void addParameters(EMController inst, Class target, ParameterList list) {

        List<List<Double>> def = inst.getRampRates().stream()
                                     .map(r -> List.of(r.getMinI(), r.getMaxI(), r.getRate()))
                                     .collect(Collectors.toList());

        Column<Double> MIN_I = Column.ofDoubles("Min I", "A");
        Column<Double> MAX_I = Column.ofDoubles("Max I", "A");
        Column<Double> RATE  = Column.ofDoubles("Rate", "A/min");

        DataList table = inst.getRampRates()
                             .stream()
                             .map(r -> Map.of(MIN_I, r.getMinI(), MAX_I, r.getMaxI(), RATE, r.getRate()))
                             .collect(DataList.mapCollector());

        list.addValue(
            "Ramp Zones",
            table,
            v -> inst.setRampRates(v.stream().map(r -> new Ramp(r.get(MIN_I), r.get(MAX_I), r.get(RATE))).toArray(Ramp[]::new))
        );

    }

    /**
     * Returns the current field being produced by the electromagnet.
     *
     * @return Field, in Tesla
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    double getField() throws IOException, DeviceException;

    /**
     * Ramps the electromagnet to the specified field at automatically determined ramping rates. Does not return until
     * ramping is complete.
     *
     * @param field Field to ramp to, in Tesla
     *
     * @throws IOException          Upon communications error
     * @throws DeviceException      Upon instrument error
     * @throws InterruptedException Upon waiting for ramping to complete being interrupted
     */
    void setField(double field) throws IOException, DeviceException, InterruptedException;

    /**
     * Returns the current currently being sourced by the controller.
     *
     * @return Current, in Amperes
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon instrument error
     */
    double getCurrent() throws IOException, DeviceException;

    /**
     * Ramps the electromagnet to the specified current at automatically determined ramping rates. Does not return until
     * ramping is complete.
     *
     * @param current Current to ramp to, in Amperes
     *
     * @throws IOException          Upon communications error
     * @throws DeviceException      Upon instrument error
     * @throws InterruptedException Upon waiting for ramping to complete being interrupted
     */
    void setCurrent(double current) throws IOException, DeviceException, InterruptedException;

    /**
     * Safely turns off the electromagnet. Does not return until magnet is safely off.
     *
     * @throws IOException          Upon communications error
     * @throws DeviceException      Upon instrument error
     * @throws InterruptedException Upon waiting for ramping down to complete being interrupted
     */
    void turnOff() throws IOException, DeviceException, InterruptedException;


    /**
     * Returns the rates being used to determine the ramping legs of each change in current.
     *
     * @return List of ramp rates and ranges.
     */
    List<Ramp> getRampRates();

    /**
     * Sets the rates at which the magnet should ramp for different current ranges.
     *
     * @param ramps Ramp rates
     */
    void setRampRates(Ramp... ramps);

    default Ramp getRampForCurrent(double current) {

        System.out.printf("Finding zone for current: %e A\n", current);

        return getRampRates().stream()
                             .filter(r -> Util.isBetween(current, r.getMinI(), r.getMaxI()))
                             .findFirst()
                             .orElse(null);

    }


    default List<Ramp> getRampLegs(double from, double to) throws DeviceException {

        List<Ramp> ramps = getRampRates();

        int        i    = ramps.indexOf(getRampForCurrent(from));
        int        d    = to > from ? +1 : -1;
        List<Ramp> legs = new LinkedList<>();

        double last = from;

        while (true) {

            if (i == ramps.indexOf(getRampForCurrent(to))) {
                legs.add(new Ramp(last, to, ramps.get(i).getRate()));
                break;
            }

            int next = i + d;
            if (next < 0 || next >= ramps.size()) {
                throw new DeviceException("You cannot ramp to that value!");
            }

            Ramp r = ramps.get(next);

            double border = d > 0 ? (r.getMinI()) : (r.getMaxI());

            if (d > 0 ? (border < to) : (border > to)) {
                legs.add(new Ramp(last, border, ramps.get(i).getRate()));
                i    = next;
                last = border;
            } else {
                legs.add(new Ramp(last, to, ramps.get(i).getRate()));
                break;
            }


        }

        return legs;

    }

    class Ramp {

        private final double minI;
        private final double maxI;
        private final double rate;

        public Ramp(double minI, double maxI, double rate) {
            this.minI = minI;
            this.maxI = maxI;
            this.rate = rate;
        }

        public double getMinI() {
            return minI;
        }

        public double getMaxI() {
            return maxI;
        }

        public double getRate() {
            return rate;
        }

    }

}
