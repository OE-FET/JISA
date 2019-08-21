package jisa.gui;

import jisa.Util;
import javafx.beans.property.*;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.chart.ValueAxis;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class SmartAxis extends ValueAxis<Double> {

    private final StringProperty   currentFormatterProperty = new SimpleStringProperty(this, "currentFormatter", "");
    private final DefaultFormatter defaultFormatter         = new DefaultFormatter(this);
    private final LogFormatter     logFormatter             = new LogFormatter();
    private       Object           currentAnimationID;
    private       int              numTicks                 = 11;
    private       String           label                    = "";
    private       String           labelSuffix              = "";
    private       double           minValue                 = 1.0;
    private       double           minNonZero               = Double.POSITIVE_INFINITY;
    private       double           maxValue                 = 1.0;
    private       double           maxNonZero               = Double.NEGATIVE_INFINITY;
    private       double           minValueInRange          = 0.0;
    private       Mode             mode                     = Mode.LINEAR;
    private       double           range                    = Double.POSITIVE_INFINITY;
    private       List<Double>     data                     = new LinkedList<>();
    private       boolean          empty                    = true;
    private       List<Double>     majorTicks               = new LinkedList<>();

    private BooleanProperty forceZeroInRange = new BooleanPropertyBase(true) {
        @Override
        protected void invalidated() {
            // This will affect layout if we are auto ranging
            if (isAutoRanging()) {
                requestAxisLayout();
                invalidateRange();
            }
        }

        @Override
        public Object getBean() {

            return SmartAxis.this;
        }

        @Override
        public String getName() {

            return "forceZeroInRange";
        }
    };


    public SmartAxis() {

        super();
        setMode(Mode.LINEAR);
    }

    public Mode getMode() {

        return mode;
    }

    public void setMode(Mode mode) {

        this.mode = mode;

        switch (mode) {

            case LINEAR:
                setTickLabelFormatter(defaultFormatter);
                break;

            case LOGARITHMIC:
                setTickLabelFormatter(logFormatter);

                if (getLowerBound() <= 0.0) {
                    setLowerBound(1.0);
                }

                break;

        }

        invalidateRange();

    }

    @Override
    public double getDisplayPosition(Double value) {

        switch (mode) {

            case LOGARITHMIC:

                if (value == 0) {
                    return getSide().isVertical() ? 10 * getHeight() : -9 * getWidth();
                }

                double logUpperBound = Math.log10(getUpperBound());
                double logLowerBound = Math.log10(getLowerBound());

                double delta = logUpperBound - logLowerBound;
                double deltaV = Math.log10(value) - logLowerBound;

                if (getSide().isVertical()) {
                    return (1. - ((deltaV) / delta)) * getHeight();
                } else {
                    return ((deltaV) / delta) * getWidth();
                }

            default:
            case LINEAR:
                double v;
                if (getSide().isVertical()) {
                    v = (1. - (value - getLowerBound()) / (getUpperBound() - getLowerBound())) * getHeight();
                } else {
                    v = ((value - getLowerBound()) / (getUpperBound() - getLowerBound())) * getWidth();
                }
                return v;

        }

    }

    @Override
    public Double getValueForDisplay(double displayPosition) {

        switch (mode) {

            case LOGARITHMIC:

                double logUpperBound = Math.log10(getUpperBound());
                double logLowerBound = Math.log10(getLowerBound());

                double delta = logUpperBound - logLowerBound;
                if (getSide().isVertical()) {
                    return Math.pow(10, (((displayPosition - getHeight()) / -getHeight()) * delta) + logLowerBound);
                } else {
                    return Math.pow(10, (((displayPosition / getWidth()) * delta) + logLowerBound));
                }

            default:
            case LINEAR:
                double deltaL = getUpperBound() - getLowerBound();
                if (getSide().isVertical()) {
                    return (((displayPosition - getHeight()) / -getHeight()) * deltaL) + getLowerBound();
                } else {
                    return (((displayPosition / getWidth()) * deltaL) + getLowerBound());
                }


        }

    }

    public void setLabelText(String label) {

        this.label = label;
        setLabel(String.format("%s%s", label, labelSuffix));
    }

    public void setLabelSuffix(String suffix) {

        this.labelSuffix = suffix;
        setLabel(String.format("%s%s", label, labelSuffix));
    }

    public final boolean isForceZeroInRange() {

        return forceZeroInRange.getValue();
    }

    public final void setForceZeroInRange(boolean value) {

        forceZeroInRange.setValue(value);
    }

    public final BooleanProperty forceZeroInRangeProperty() {

        return forceZeroInRange;
    }

    public final double getTickUnit() {

        return 10;
    }

    public final void setTickUnit(double value) {

    }

    public final DoubleProperty tickUnitProperty() {

        return null;
    }


    @Override
    protected String getTickMarkLabel(Double value) {

        StringConverter<Double> formatter = getTickLabelFormatter();
        if (formatter == null) {
            formatter = defaultFormatter;
        }
        return formatter.toString(value);
    }


    @Override
    protected Object getRange() {

        return new Object[]{
                getLowerBound(),
                getUpperBound(),
                getTickUnit(),
                getScale(),
                currentFormatterProperty.get()
        };
    }


    @Override
    protected void setRange(Object range, boolean animate) {

        final Object[] rangeProps = (Object[]) range;
        final double   lowerBound = (Double) rangeProps[0];
        final double   upperBound = (Double) rangeProps[1];
        final double   tickUnit   = (Double) rangeProps[2];
        final double   scale      = (Double) rangeProps[3];
        final String   formatter  = (String) rangeProps[4];
        currentFormatterProperty.set(formatter);
        final double oldLowerBound = getLowerBound();
        setLowerBound(lowerBound);
        setUpperBound(upperBound);
        setTickUnit(tickUnit);
        currentLowerBound.set(lowerBound);
        setScale(scale);
    }

    public void invalidateRange(List<Double> data) {

        this.data = data;

        int mag = 0;
        if (!data.isEmpty()) {

            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;

            for (Number n : data) {
                min = Math.min(min, n.doubleValue());
                max = Math.max(max, n.doubleValue());

                double abs = Math.abs(n.doubleValue());

                if (abs > 0) {
                    minNonZero = Math.min(minNonZero, abs);
                    maxNonZero = Math.max(maxNonZero, abs);
                }

            }

            minValue = min;
            maxValue = max;
            if (Math.max(Math.abs(min), Math.abs(max)) > 0) {
                mag = (int) Math.floor(Math.floor(Math.log10(Math.max(Math.abs(min), Math.abs(max)))) / 3) * 3;
            }

            empty = false;

        } else {

            minValue = 0;
            maxValue = 100;
            mag      = 0;

            empty = true;

        }


        defaultFormatter.magnitude = Math.pow(10, mag);

        switch (mode) {

            case LINEAR:
                if (mag != 0) {
                    setLabelSuffix(String.format(" (E%+d)", mag));
                } else {
                    setLabelSuffix("");
                }
                break;

            case LOGARITHMIC:
                setLabelSuffix("");
                break;

        }

        super.invalidateRange(data);

    }

    @Override
    protected List<Double> calculateTickValues(double length, Object range) {

        double lowerBound = getLowerBound();
        double upperBound = getUpperBound();

        if (upperBound == lowerBound || data.size() == 1) {
            double amount = Math.abs(0.1 * lowerBound);
            lowerBound -= amount;
            upperBound += amount;
        }

        if (upperBound == lowerBound) {
            lowerBound -= 50;
            upperBound += 50;
        }

        final double       tickUnit   = Math.abs(Util.oneSigFigCeil((upperBound - lowerBound) / numTicks));
        final List<Double> tickValues = new ArrayList<>();

        double minInRange = Double.POSITIVE_INFINITY;

        for (Double d : data) {

            if (Util.isBetween(d, lowerBound, upperBound)) {
                minInRange = Math.min(minInRange, d);
            }

        }

        if (minInRange == Double.POSITIVE_INFINITY) {
            minInRange = Util.oneSigFigCeil(lowerBound);
        }

        switch (mode) {

            case LINEAR:

                double distance = Double.POSITIVE_INFINITY;

                int counter = 0;
                for (double v = lowerBound; v <= upperBound; v += tickUnit) {

                    if ((Math.abs(upperBound - lowerBound) / tickUnit) > 2.0 * numTicks) {
                        System.err.println("Tick unit too small! This is a bug.");
                        tickValues.add(lowerBound);
                        tickValues.add(upperBound);
                        break;
                    }

                    if (tickUnit <= 0) {
                        System.err.println("Tick unit <= 0! This is a bug.");
                        tickValues.add(lowerBound);
                        tickValues.add(upperBound);
                        break;
                    }

                    tickValues.add(v);

                    if (Math.abs(v - minInRange) < Math.abs(distance)) {
                        distance = v - minInRange;
                    }

                    if (++counter > 1.5 * numTicks) {
                        System.err.println("Had to break out of tick loop!");
                        break;
                    }

                }


                for (int i = 0; i < tickValues.size(); i++) {
                    tickValues.set(i, tickValues.get(i) - distance);
                }


                break;

            case LOGARITHMIC:

                int logStart = (int) Math.floor(Math.log10(lowerBound));
                int logStop = (int) Math.ceil(Math.log10(upperBound));

                for (int i = logStart; i <= logStop; i++) {
                    tickValues.add(Math.pow(10, i));
                }

                break;

        }

        majorTicks = tickValues;

        return tickValues;

    }

    public List<Double> getMajorTicks() {

        return calculateTickValues(10.0, null);
    }

    public List<Double> getMinorTicks() {

        return calculateMinorTickMarks();
    }

    protected List<Double> calculateMinorTickMarks() {

        final double       lowerBound = getLowerBound();
        final double       upperBound = getUpperBound();
        final double       tickUnit   = Util.oneSigFigCeil((upperBound - lowerBound) / numTicks);
        final double       minUnit    = tickUnit / 5;
        final List<Double> ticks      = new ArrayList<>();

        if (majorTicks.isEmpty()) {
            return ticks;
        }

        switch (mode) {

            case LINEAR:

                double[] startTicks = Util.makeLinearArray(majorTicks.get(0) - tickUnit, majorTicks.get(0), 6);

                for (int i = 1; i < startTicks.length - 1; i++) {
                    double v = startTicks[i];
                    if (Util.isBetween(v, lowerBound, upperBound)) {
                        ticks.add(v);
                    }
                }

                for (int i = 0; i < majorTicks.size() - 1; i++) {

                    double[] newTicks = Util.makeLinearArray(majorTicks.get(i), majorTicks.get(i + 1), 6);

                    for (int j = 1; j < newTicks.length - 1; j++) {
                        ticks.add(newTicks[j]);
                    }

                }

                double[] endTicks = Util.makeLinearArray(
                        majorTicks.get(majorTicks.size() - 1),
                        majorTicks.get(majorTicks.size() - 1) + tickUnit,
                        6
                );

                for (int i = 1; i < endTicks.length - 1; i++) {

                    double v = endTicks[i];
                    if (Util.isBetween(v, lowerBound, upperBound)) {
                        ticks.add(v);
                    }
                }

                break;

            case LOGARITHMIC:

                int logStart = (int) Math.ceil(Math.log10(lowerBound));
                int logStop = (int) Math.floor(Math.log10(upperBound));

                for (int i = logStart; i <= logStop + 1; i++) {

                    double minVal = Math.pow(10, i - 1);
                    double maxVal = Math.pow(10, i);

                    for (double v = minVal; v < maxVal; v += minVal) {

                        if (Util.isBetween(v, lowerBound, upperBound) && !ticks.contains(v)) {
                            ticks.add(v);
                        }
                    }

                }


        }

        return ticks;

    }

    protected void layoutChildren() {

        super.layoutChildren();
    }

    public double getMaxRange() {

        return range;
    }

    public void setMaxRange(double range) {

        this.range = range;
    }

    @Override
    protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {

        if (isAutoRanging()) {

            switch (mode) {

                case LOGARITHMIC:

                    if (empty) {
                        minValue = 1;
                        maxValue = 100;
                    }

                    double minExp = Math.log10(Math.abs(minNonZero));
                    double maxExp = Math.log10(Math.abs(maxNonZero));
                    double expRange = maxExp - minExp;

                    if (expRange < 2) {
                        double diff = (2 - expRange) / 2;
                        minExp -= diff;
                        maxExp += diff;
                        expRange = 2;
                    }

                    return new Object[]{
                            Math.pow(10, minExp - (0.05 * expRange)),
                            Math.pow(10, maxExp + (0.05 * expRange)),
                            getTickUnit(),
                            getScale(),
                            currentFormatterProperty.get()
                    };


                default:
                case LINEAR:

                    if (empty || (minValue == 0 && maxValue == 0)) {
                        minValue = -100;
                        maxValue = 100;
                    }

                    double range = maxValue - minValue;
                    if (range == 0) {
                        range = maxValue;
                    }

                    return new Object[]{
                            Math.max(maxValue - this.range, minValue - 0.5 * (range / numTicks)),
                            maxValue + 0.5 * (range / numTicks),
                            getTickUnit(),
                            getScale(),
                            currentFormatterProperty.get()
                    };


            }

        } else {
            return getRange();
        }

    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {

        return getClassCssMetaData();
    }

    public enum Mode {
        LINEAR,
        LOGARITHMIC
    }

    public static class LogFormatter extends StringConverter<Double> {


        @Override
        public String toString(Double number) {

            return String.format("10^%+d", (int) Math.log10(number));
        }

        @Override
        public Double fromString(String string) {

            String[] values = string.split("\\^");
            return Math.pow(Double.valueOf(values[0]), Double.valueOf(values[1]));
        }

    }

    public static class DefaultFormatter extends StringConverter<Double> {
        private String    prefix    = null;
        private String    suffix    = null;
        private double    magnitude = 1;
        private SmartAxis axis;

        public DefaultFormatter(final SmartAxis axis) {

            this.axis = axis;
        }

        public DefaultFormatter(SmartAxis axis, String prefix, String suffix) {

            this(axis);
            this.prefix = prefix;
            this.suffix = suffix;
        }

        @Override
        public String toString(Double number) {

            return String.format("%.02f", number / magnitude);
        }

        private String toString(Double number, String numFormatter) {

            return toString(number);
        }

        private String toString(Double number, DecimalFormat formatter) {

            return toString(number);
        }

        @Override
        public Double fromString(String string) {

            return Double.valueOf(string) * magnitude;
        }

    }

}