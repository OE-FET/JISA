/*
 * Copyright (c) 2010, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package JISA.GUI;

import JISA.Util;
import javafx.beans.property.*;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.chart.ValueAxis;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A axis class that plots a range of numbers with major tick marks every "tickUnit". You can use any Number type with
 * this axis, Long, Double, BigDecimal etc.
 *
 * @since JavaFX 2.0
 */
public final class SmartAxis extends ValueAxis<Double> {

    private final StringProperty   currentFormatterProperty = new SimpleStringProperty(this, "currentFormatter", "");
    private final DefaultFormatter defaultFormatter         = new DefaultFormatter(this);
    private final LogFormatter     logFormatter             = new LogFormatter();
    private       Object           currentAnimationID;
    private       int              numTicks                 = 10;
    private       String           label                    = "";
    private       String           labelSuffix              = "";
    private       double           minValue                 = 1.0;
    private       double           maxValue                 = 1.0;
    private       Mode             mode                     = Mode.LINEAR;
    private       double           range                    = Double.POSITIVE_INFINITY;
    private       boolean          empty                    = true;
    /**
     * When true zero is always included in the visible range. This only has effect if auto-ranging is on.
     */
    private       BooleanProperty  forceZeroInRange         = new BooleanPropertyBase(true) {
        @Override
        protected void invalidated() {
            // This will effect layout if we are auto ranging
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

    // -------------- PUBLIC PROPERTIES --------------------------------------------------------------------------------
    /**
     * The value between each major tick mark in data units. This is automatically set if we are auto-ranging.
     */


    /**
     * Create a auto-ranging SmartAxis
     */
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

                double logUpperBound = Math.log10(getUpperBound());
                double logLowerBound = Math.log10(getLowerBound());

                double delta = logUpperBound - logLowerBound;
                double deltaV = Math.log10(value.doubleValue()) - logLowerBound;

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

    // -------------- CONSTRUCTORS -------------------------------------------------------------------------------------

    public final double getTickUnit() {
        return 10;
    }

    public final void setTickUnit(double value) {

    }

    public final DoubleProperty tickUnitProperty() {
        return null;
    }

    // -------------- PROTECTED METHODS --------------------------------------------------------------------------------

    /**
     * Get the string label name for a tick mark with the given value
     *
     * @param value The value to format into a tick label string
     *
     * @return A formatted string for the given value
     */
    @Override
    protected String getTickMarkLabel(Double value) {
        StringConverter<Double> formatter = getTickLabelFormatter();
        if (formatter == null) {
            formatter = defaultFormatter;
        }
        return formatter.toString(value);
    }

    /**
     * Called to get the current axis range.
     *
     * @return A range object that can be passed to setRange() and calculateTickValues()
     */
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

    /**
     * Called to set the current axis range to the given range. If isAnimating() is true then this method should
     * animate the range to the new range.
     *
     * @param range   A range object returned from autoRange()
     * @param animate If true animate the change in range
     */
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


        int mag = 0;
        if (!data.isEmpty()) {

            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;

            for (Number n : data) {
                min = Math.min(min, n.doubleValue());
                max = Math.max(max, n.doubleValue());
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
            mag = 0;

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

    /**
     * Calculate a list of all the data values for each tick mark in range
     *
     * @param length The length of the axis in display units
     * @param range  A range object returned from autoRange()
     *
     * @return A list of tick marks that fit along the axis if it was the given length
     */
    @Override
    protected List<Double> calculateTickValues(double length, Object range) {

        final Object[]     rangeProps = (Object[]) range;
        final double       lowerBound = (Double) rangeProps[0];
        final double       upperBound = (Double) rangeProps[1];
        final double       tickUnit   = Util.oneSigFigFloor((upperBound - lowerBound) / numTicks);
        final List<Double> tickValues = new ArrayList<>();

        switch (mode) {

            case LINEAR:

                double linStart = Util.oneSigFigFloor(lowerBound);
                double linStop = Util.oneSigFigCeil(upperBound);

                double distance = Double.POSITIVE_INFINITY;

                for (double v = linStart; v <= linStop; v += tickUnit) {
                    tickValues.add(v);

                    if (Math.abs(v - minValue) < distance) {
                        distance = v - minValue;
                    }

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

        return tickValues;

    }

    /**
     * Calculate a list of the data values for every minor tick mark
     *
     * @return List of data values where to draw minor tick marks
     */
    protected List<Double> calculateMinorTickMarks() {

        final double       lowerBound = getLowerBound();
        final double       upperBound = getUpperBound();
        final double       tickUnit   = Util.oneSigFigFloor((upperBound - lowerBound) / numTicks);
        final double       minUnit    = tickUnit / 5;
        final List<Double> ticks      = new ArrayList<>();

        switch (mode) {

            case LINEAR:

                double linStart = Util.oneSigFigFloor(lowerBound);
                double linStop = Util.oneSigFigCeil(upperBound);

                for (double v = linStart; v <= linStop; v += minUnit) {
                    ticks.add(v);
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

    /**
     * Called to set the upper and lower bound and anything else that needs to be auto-ranged
     *
     * @param minValue  The min data value that needs to be plotted on this axis
     * @param maxValue  The max data value that needs to be plotted on this axis
     * @param length    The length of the axis in display coordinates
     * @param labelSize The approximate average size a label takes along the axis
     *
     * @return The calculated range
     */
    @Override
    protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {

        if (isAutoRanging()) {

            switch (mode) {

                case LOGARITHMIC:

                    if (empty) {
                        minValue = 1;
                        maxValue = 100;
                    }

                    return new Object[]{
                            minValue - Math.pow(10, Math.floor(Math.log10(minValue / 5))),
                            maxValue + Math.pow(10, Math.floor(Math.log10(maxValue / 5))),
                            getTickUnit(),
                            getScale(),
                            currentFormatterProperty.get()
                    };


                default:
                case LINEAR:

                    if (empty) {
                        minValue = 0;
                        maxValue = 100;
                    }

                    double range = maxValue - minValue;
                    if (range == 0) {
                        range = maxValue;
                    }

                    return new Object[]{
                            Math.max(maxValue - this.range, minValue - (range / numTicks)),
                            maxValue + (range / numTicks),
                            getTickUnit(),
                            getScale(),
                            currentFormatterProperty.get()
                    };


            }

        } else {
            return getRange();
        }

    }

    // -------------- STYLESHEET HANDLING ------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * @since JavaFX 8.0
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    public enum Mode {
        LINEAR,
        LOGARITHMIC
    }


    // -------------- INNER CLASSES ------------------------------------------------------------------------------------

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

    /**
     * Default number formatter for SmartAxis, this stays in sync with auto-ranging and formats values appropriately.
     * You can wrap this formatter to add prefixes or suffixes;
     *
     * @since JavaFX 2.0
     */
    public static class DefaultFormatter extends StringConverter<Double> {
        private String    prefix    = null;
        private String    suffix    = null;
        private double    magnitude = 1;
        private SmartAxis axis;

        /**
         * Construct a DefaultFormatter for the given SmartAxis
         *
         * @param axis The axis to format tick marks for
         */
        public DefaultFormatter(final SmartAxis axis) {
            this.axis = axis;
        }

        /**
         * Construct a DefaultFormatter for the given SmartAxis with a prefix and/or suffix.
         *
         * @param axis   The axis to format tick marks for
         * @param prefix The prefix to append to the start of formatted number, can be null if not needed
         * @param suffix The suffix to append to the end of formatted number, can be null if not needed
         */
        public DefaultFormatter(SmartAxis axis, String prefix, String suffix) {
            this(axis);
            this.prefix = prefix;
            this.suffix = suffix;
        }

        /**
         * Converts the object provided into its string form.
         * Format of the returned string is defined by this converter.
         *
         * @return a string representation of the object passed in.
         *
         * @see StringConverter#toString
         */
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

        /**
         * Converts the string provided into a Number defined by the this converter.
         * Format of the string and type of the resulting object is defined by this converter.
         *
         * @return a Number representation of the string passed in.
         *
         * @see StringConverter#toString
         */
        @Override
        public Double fromString(String string) {
            return Double.valueOf(string) * magnitude;
        }
    }

}