package jisa.gui.plotting;

import de.gsi.dataset.event.UpdateEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import jisa.gui.Plot;
import jisa.gui.Series;
import jisa.maths.fits.Fit;
import jisa.results.Column;
import jisa.results.ResultTable;
import jisa.results.Row;
import jisa.results.RowEvaluable;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

public class JISASeries implements Series {

    private final ObservableList<JISAErrorDataSet> dataSets       = FXCollections.observableArrayList();
    private final Map<Object, JISAErrorDataSet>    mapping        = new TreeMap<>();
    private final Plot                             plot;
    private       int                              limit          = 0;
    private       ResultTable                      watched        = null;
    private       ResultTable.RowListener          rowListener    = null;
    private       ResultTable.ClearListener        clearListener  = null;
    private       DataSetSelector                  selector       = r -> dataSets.get(0);
    private       Predicate<Row>                   filter         = r -> true;
    private       RowEvaluable<? extends Number>   xEval;
    private       RowEvaluable<? extends Number>   yEval;
    private       RowEvaluable<? extends Number>   eXEval;
    private       RowEvaluable<? extends Number>   eYEval;
    private       Color[]                          colourSequence = Series.defaultColours;
    private       SeriesFitter                     fitter         = null;

    public JISASeries(String name, Plot plot) {

        this.plot = plot;

        JISAErrorDataSet set = new JISAErrorDataSet(name, plot);

        set.addListener(e -> {

            if (limit > 0 && set.getDataCount() > limit) {

                set.autoNotification().set(false);
                set.remove(0, set.getDataCount() - limit);
                set.autoNotification().set(true);
                set.fireInvalidated(new UpdateEvent(set));

            }

        });

        dataSets.add(set);

    }

    @Override
    public Series watch(ResultTable table, RowEvaluable<? extends Number> x, RowEvaluable<? extends Number> y, RowEvaluable<? extends Number> eX, RowEvaluable<? extends Number> eY) {

        if (watched != null) {
            throw new IllegalStateException("This Series is already watching another ResultTable.");
        }

        watched       = table;
        clearListener = table.addClearListener(this::clear);
        rowListener   = table.addRowListener(

            r -> {

                try {

                    if (filter.test(r)) {

                        double errorX = Math.abs(eX.evaluate(r).doubleValue());
                        double errorY = Math.abs(eY.evaluate(r).doubleValue());

                        selector.select(r).add(x.evaluate(r).doubleValue(), y.evaluate(r).doubleValue(), errorY, errorY, errorX, errorX);

                    }

                } catch (RuntimeException e) {
                    e.printStackTrace();
                }

            }

        );


        xEval  = x;
        yEval  = y;
        eXEval = eX;
        eYEval = eY;

        regeneratePoints();

        return this;

    }

    public Series watch(ResultTable table, Column<? extends Number> xData, Column<? extends Number> yData) {

        watch(table, r -> r.get(xData), r -> r.get(yData), r -> 0.0);

        if (getName().startsWith("Series")) {
            setName(yData.getName());
        }

        if (plot.getXLabel().trim().isBlank()) {
            plot.setXLabel(xData.getName(), xData.getUnits());
        }

        if (plot.getYLabel().trim().isBlank()) {
            plot.setYLabel(yData.getName(), yData.getUnits());
        }

        return this;

    }

    @Override
    public Series setLimit(int count) {

        this.limit = count;
        dataSets.forEach(s -> s.fireInvalidated(new UpdateEvent(s)));
        return this;

    }

    @Override
    public int getLimit() {
        return limit;
    }

    public Series watch(ResultTable table, Column<? extends Number> xData, Column<? extends Number> yData, Column<? extends Number> eData) {

        watch(table, r -> r.get(xData), r -> r.get(yData), r -> r.get(eData));

        if (getName().startsWith("Series")) {
            setName(yData.getName());
        }

        if (plot.getXLabel().trim().isBlank()) {
            plot.setXLabel(xData.getName(), xData.getUnits());
        }

        if (plot.getYLabel().trim().isBlank()) {
            plot.setYLabel(yData.getName(), yData.getUnits());
        }

        return this;

    }

    protected void regeneratePoints() {

        clear();

        dataSets.forEach(d -> d.autoNotification().set(false));

        for (Row row : watched) {
            rowListener.added(row);
        }

        dataSets.forEach(d -> d.autoNotification().set(true));
        dataSets.forEach(d -> d.fireInvalidated(new UpdateEvent(d, "add")));

    }

    @Override
    public Series split(RowEvaluable<?> splitBy, SeriesFormatter pattern) {

        selector = r -> {

            Object value = splitBy.evaluate(r);

            if (mapping.containsKey(value)) {

                return mapping.get(value);

            } else {

                JISAErrorDataSet set;

                if (mapping.isEmpty()) {
                    set = dataSets.get(0);
                    set.setName(pattern.getName(r));
                } else {

                    set = new JISAErrorDataSet(pattern.getName(r), plot);

                    set.addListener(e -> {

                        if (limit > 0 && set.getDataCount() > limit) {

                            set.autoNotification().set(false);
                            set.remove(0, set.getDataCount() - limit);
                            set.autoNotification().set(true);
                            set.fireInvalidated(new UpdateEvent(set));

                        }

                    });

                }

                set.copyStyleOf(dataSets.get(0));
                set.setColour(colourSequence[mapping.size() % colourSequence.length]);
                set.setFitter(fitter);

                dataSets.add(set);
                mapping.put(value, set);
                return set;

            }

        };

        regeneratePoints();

        return this;

    }

    @Override
    public ResultTable getWatched() {
        return watched;
    }

    @Override
    public Series filter(Predicate<Row> filter) {

        this.filter = filter;
        regeneratePoints();

        return this;

    }

    @Override
    public Series addPoint(double x, double y, double errorX, double errorY) {

        if (watched != null) {
            throw new IllegalStateException("Cannot manually edit a series that is watching a ResultTable.");
        }

        double eXHalf = Math.abs(errorX);
        double eYHalf = Math.abs(errorY);
        dataSets.get(0).add(x, y, eYHalf, eYHalf, eXHalf, eXHalf);
        return this;

    }

    @Override
    public Series addPoints(Iterable<? extends Number> x, Iterable<? extends Number> y, Iterable<? extends Number> eY) {

        if (watched != null) {
            throw new IllegalStateException("Cannot manually edit a series that is watching a ResultTable.");
        }

        dataSets.get(0).autoNotification().set(false);

        Iterator<? extends Number> xI = x.iterator();
        Iterator<? extends Number> yI = y.iterator();
        Iterator<? extends Number> eI = eY.iterator();

        while (xI.hasNext() && yI.hasNext() && eI.hasNext()) {
            addPoint(xI.next().doubleValue(), yI.next().doubleValue(), eI.next().doubleValue());
        }

        dataSets.get(0).autoNotification().set(true);
        dataSets.forEach(d -> d.fireInvalidated(new UpdateEvent(d, "add")));

        return this;

    }

    public Series removePoint(int index) {

        if (watched != null) {
            throw new IllegalStateException("Cannot manually edit a series that is watching a ResultTable.");
        }

        dataSets.get(0).remove(index);

        return this;

    }

    public Series removePoints(int from, int to) {

        if (watched != null) {
            throw new IllegalStateException("Cannot manually edit a series that is watching a ResultTable.");
        }

        dataSets.get(0).remove(from, to);

        return this;

    }

    @Override
    public Series clear() {
        dataSets.forEach(JISAErrorDataSet::clearData);
        dataSets.retainAll(dataSets.get(0));
        mapping.clear();
        return this;
    }

    @Override
    public Series setMarkerVisible(boolean show) {
        dataSets.forEach(d -> d.setMarkerVisible(show));
        return this;
    }

    @Override
    public boolean isMarkerVisible() {
        return dataSets.get(0).isMarkerVisible();
    }

    @Override
    public Shape getMarkerShape() {
        return null;
    }

    @Override
    public Series setMarkerShape(Shape shape) {
        dataSets.forEach(d -> d.setShape(shape));
        return this;
    }

    @Override
    public double getMarkerSize() {
        return dataSets.get(0).getSize();
    }

    @Override
    public Series setMarkerSize(double size) {
        dataSets.forEach(d -> d.setSize(size));
        return this;
    }

    @Override
    public String getName() {
        return dataSets.get(0).getName();
    }

    @Override
    public Series setName(String name) {
        dataSets.get(0).setName(name);
        return this;
    }

    @Override
    public Color getColour() {
        return dataSets.get(0).getColour();
    }

    @Override
    public Series setColour(Color colour) {

        if (dataSets.size() > 1) {
            setColourSequence(colour);
        } else {
            dataSets.get(0).setColour(colour);
        }

        return this;

    }

    @Override
    public Color getErrorColour() {
        return dataSets.get(0).getErrorColour();
    }

    @Override
    public Series setErrorColour(Color colour) {
        dataSets.forEach(d -> d.setErrorColour(colour));
        return this;
    }

    @Override
    public Series setColourSequence(Color... colours) {

        colourSequence = colours;

        for (int i = 0; i < dataSets.size(); i++) {
            dataSets.get(i).setColour(colourSequence[i % colourSequence.length]);
        }

        return this;

    }

    @Override
    public double getLineWidth() {
        return dataSets.get(0).getThickness();
    }

    @Override
    public Series setLineWidth(double width) {
        dataSets.forEach(d -> d.setThickness(width));
        return this;
    }

    @Override
    public double getErrorLineWidth() {
        return dataSets.get(0).getErrorThickness();
    }

    @Override
    public Series setErrorLineWidth(double width) {
        dataSets.forEach(d -> d.setErrorThickness(width));
        return this;
    }

    @Override
    public Dash getLineDash() {
        return dataSets.get(0).getDash();
    }

    @Override
    public Series setLineDash(Dash dash) {
        dataSets.forEach(d -> d.setDash(dash));
        return this;
    }

    @Override
    public Series setLineVisible(boolean show) {
        dataSets.forEach(d -> d.setLineVisible(show));
        return this;
    }

    @Override
    public boolean isLineVisible() {
        return dataSets.get(0).isLineVisible();
    }

    @Override
    public Series setAutoReduction(int reduceTo, int limit) {
        return this;
    }

    @Override
    public Series reduceNow() {
        return this;
    }

    @Override
    public Series setXAutoRemove(double range) {
        return null;
    }

    @Override
    public Series setYAutoRemove(double range) {
        return null;
    }

    @Override
    public Series remove() {
        plot.removeSeries(this);
        return this;
    }

    @Override
    public Fit getFit() {
        return dataSets.get(0).getFit();
    }

    public Series fit(SeriesFitter fitter) {
        this.fitter = fitter;
        dataSets.forEach(d -> d.setFitter(fitter));
        return this;
    }

    @Override
    public boolean isFitted() {
        return fitter != null;
    }

    @Override
    public Series setPointOrder(Ordering order) {
        dataSets.forEach(d -> d.setOrdering(order));
        return this;
    }

    @Override
    public Ordering getPointOrdering() {
        return dataSets.get(0).getOrdering();
    }

    public Series removeFit() {
        fitter = null;
        return this;
    }

    @Override
    public ObservableList<JISAErrorDataSet> getDatasets() {
        return dataSets;
    }

    public interface DataSetSelector {

        JISAErrorDataSet select(Row row);

    }
}
