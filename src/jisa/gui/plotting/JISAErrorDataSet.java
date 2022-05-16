package jisa.gui.plotting;

import de.gsi.chart.XYChartCss;
import de.gsi.dataset.event.UpdateEvent;
import de.gsi.dataset.spi.DoubleDataSet;
import de.gsi.dataset.spi.DoubleErrorDataSet;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import jisa.gui.Colour;
import jisa.gui.Plot;
import jisa.gui.Series;
import jisa.gui.Series.Dash;
import jisa.gui.Series.Ordering;
import jisa.gui.Series.SeriesFitter;
import jisa.gui.Series.Shape;
import jisa.maths.Range;
import jisa.maths.fits.Fit;
import jisa.maths.functions.Function;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class JISAErrorDataSet extends TwoErrorDataSet {

    private final Plot                         plot;
    private final ObjectProperty<Color>        colour         = new SimpleObjectProperty<>(Colour.RED);
    private final ObjectProperty<Color>        errorColour    = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Shape>        shape          = new SimpleObjectProperty<>(Shape.CIRCLE);
    private final ObjectProperty<Dash>         dash           = new SimpleObjectProperty<>(Dash.SOLID);
    private final ObjectProperty<Double>       thickness      = new SimpleObjectProperty<>(3.0);
    private final ObjectProperty<Double>       errorThickness = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Double>       size           = new SimpleObjectProperty<>(6.0);
    private final ObjectProperty<Boolean>      lineVisible    = new SimpleObjectProperty<>(true);
    private final ObjectProperty<Boolean>      markerVisible  = new SimpleObjectProperty<>(true);
    private final ObjectProperty<SeriesFitter> fitter         = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Fit>          fit            = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Ordering>     ordering       = new SimpleObjectProperty<>(Ordering.NONE);

    protected static Map<String, String> mapOf(String... values) {

        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Uneven number of arguments!");
        }

        Map<String, String> map = new LinkedHashMap<>(values.length / 2);

        for (int i = 0; i < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }

        return map;

    }

    public JISAErrorDataSet(String name, Plot plot) {

        super(name, 100);

        this.plot = plot;

        colour.addListener(o -> setStyle(generateStyle()));
        shape.addListener(o -> setStyle(generateStyle()));
        dash.addListener(o -> setStyle(generateStyle()));
        thickness.addListener(o -> setStyle(generateStyle()));
        size.addListener(o -> setStyle(generateStyle()));
        lineVisible.addListener(o -> setStyle(generateStyle()));
        markerVisible.addListener(o -> setStyle(generateStyle()));
        ordering.addListener(o -> setStyle(generateStyle()));

        fitter.addListener(o -> {

            if (isFitted()) {

                Fit fit;

                try {
                    fit = fitter.get().fit(xValues.toArray(new double[0]), yValues.toArray(new double[0]));
                } catch (Throwable e) {
                    fit = null;
                }

                this.fit.set(fit);

                plot.forceRedraw();

            }

        });

        addListener(e -> {

            if (isFitted()) {

                Fit fit;

                try {
                    fit = fitter.get().fit(xValues.toArray(new double[0]), yValues.toArray(new double[0]));
                } catch (Throwable ignored) {
                    fit = null;
                }

                this.fit.set(fit);

            }

        });

        fit.addListener(i -> fireInvalidated(new UpdateEvent(this, "add")));

        setStyle(generateStyle());

    }

    public JISAErrorDataSet setName(String name) {
        super.setName(name);
        plot.updateLegend();
        return this;
    }

    public void copyStyleOf(JISAErrorDataSet other) {
        colour.set(other.colour.get());
        shape.set(other.shape.get());
        dash.set(other.dash.get());
        thickness.set(other.thickness.get());
        size.set(other.size.get());
        lineVisible.set(other.lineVisible.get());
        markerVisible.set(other.markerVisible.get());
    }

    public JISAErrorDataSet setStyle(String style) {
        super.setStyle(style);
        fireInvalidated(new UpdateEvent(this, "add"));
        plot.updateLegend();
        return this;
    }

    public String generateStyle() {

        Map<String, String> style = mapOf(
            XYChartCss.STROKE_COLOR,           Colour.toRGBA(colour.get()),
            XYChartCss.MARKER_COLOR,           Colour.toRGBA(colour.get()),
            XYChartCss.STROKE_WIDTH,           thickness.get().toString(),
            XYChartCss.STROKE_DASH_PATTERN,    Arrays.stream(dash.get().getArray()).map(Object::toString).collect(Collectors.joining(",")),
            XYChartCss.MARKER_SIZE,            size.get().toString(),
            XYChartCss.DATASET_SHOW_IN_LEGEND, Boolean.toString(true),
            XYChartCss.FILL_COLOR,             "none",
            XYChartCss.MARKER_TYPE,            shape.get().name(),
            "lineVisible",                     lineVisible.get().toString(),
            "markerVisible",                   markerVisible.get().toString(),
            "ordering",                        ordering.get().toString()
        );

        return style.entrySet().stream().map(e -> String.format("%s:%s", e.getKey(), e.getValue())).collect(Collectors.joining(";"));

    }

    public Dash getDash() {
        return dash.get();
    }

    public ObjectProperty<Dash> dashProperty() {
        return dash;
    }

    public void setDash(Dash dash) {
        this.dash.set(dash);
    }

    public Color getColour() {
        return colour.get();
    }

    public ObjectProperty<Color> colourProperty() {
        return colour;
    }

    public Shape getShape() {
        return shape.get();
    }

    public ObjectProperty<Shape> shapeProperty() {
        return shape;
    }

    public Double getThickness() {
        return thickness.get();
    }

    public ObjectProperty<Double> thicknessProperty() {
        return thickness;
    }

    public Boolean isLineVisible() {
        return lineVisible.get();
    }

    public ObjectProperty<Boolean> lineVisibleProperty() {
        return lineVisible;
    }

    public Boolean isMarkerVisible() {
        return markerVisible.get();
    }

    public ObjectProperty<Boolean> markerVisibleProperty() {
        return markerVisible;
    }

    public void setColour(Color colour) {
        this.colour.set(colour);
    }

    public void setShape(Shape shape) {
        this.shape.set(shape);
    }

    public void setThickness(Double thickness) {
        this.thickness.set(thickness);
    }

    public void setLineVisible(Boolean lineVisible) {
        this.lineVisible.set(lineVisible);
    }

    public void setMarkerVisible(Boolean markerVisible) {
        this.markerVisible.set(markerVisible);
    }

    public Double getSize() {
        return size.get();
    }

    public ObjectProperty<Double> sizeProperty() {
        return size;
    }

    public void setSize(Double size) {
        this.size.set(size);
    }

    public SeriesFitter getFitter() {
        return fitter.get();
    }

    public ObjectProperty<SeriesFitter> fitterProperty() {
        return fitter;
    }

    public void setFitter(SeriesFitter fitter) {
        this.fitter.set(fitter);
    }

    public boolean isFitted() {
        return fitter.isNotNull().get();
    }

    public Fit getFit() {
        return fit.get();
    }

    public DoubleDataSet getFittedPoints(double minX, double maxX) {

        if (Double.isNaN(minX) || Double.isNaN(maxX)) {
            return null;
        }

        double[] xValues = Range.linear(minX, maxX, 100).doubleArray();
        Fit      fit     = getFit();

        if (fit == null) {
            return null;
        }

        Function      func = fit.getFunction();
        DoubleDataSet set  = new DoubleDataSet(getName(), 100);

        set.add(xValues, Arrays.stream(xValues).map(func::value).toArray());

        return set;

    }

    public Color getErrorColour() {
        return (errorColour.isNotNull().get()) ? errorColour.get() : getColour();
    }

    public ObjectProperty<Color> errorColourProperty() {
        return errorColour;
    }

    public void setErrorColour(Color errorColour) {
        this.errorColour.set(errorColour);
    }

    public Double getErrorThickness() {
        return (errorThickness.isNotNull().get()) ? errorThickness.get() : getThickness();
    }

    public ObjectProperty<Double> errorThicknessProperty() {
        return errorThickness;
    }

    public void setErrorThickness(Double errorThickness) {
        this.errorThickness.set(errorThickness);
    }

    public Ordering getOrdering() {
        return ordering.get();
    }

    public ObjectProperty<Ordering> orderingProperty() {
        return ordering;
    }

    public void setOrdering(Ordering ordering) {
        this.ordering.set(ordering);
    }

}
