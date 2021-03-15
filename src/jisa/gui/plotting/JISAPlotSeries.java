package jisa.gui.plotting;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import jisa.experiment.Result;
import jisa.experiment.ResultTable;
import jisa.experiment.ResultTable.Evaluable;
import jisa.gui.Clearable;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class JISAPlotSeries implements Clearable {

    private final JISAPlot                          plot;
    private final ObservableList<JISAPlotPoint>     used                 = FXCollections.observableArrayList();
    private final ObservableList<JISAPlotPoint>     reserved             = FXCollections.observableArrayList();
    private final ObservableList<Predicate<Result>> filters              = FXCollections.observableArrayList();
    private final ObservableList<PathElement>       lineElements         = FXCollections.observableArrayList();
    private final ObservableList<PathElement>       reservedLineElements = FXCollections.observableArrayList();
    private final ResultTable                       dataTable;
    private final Evaluable                         xValue;
    private final Evaluable                         yValue;
    private final Evaluable                         exValue;
    private final Evaluable                         eyValue;
    private final ResultTable.OnUpdate              updater;
    private final Path                              line                 = new Path();
    private       double                            lineWidth            = 1.0;
    private       double                            markerSize           = 5.0;
    private       Double                            minX                 = null;
    private       Double                            maxX                 = null;
    private       Double                            minY                 = null;
    private       Double                            maxY                 = null;
    private       boolean                           lineVisible          = true;
    private       boolean                           markerVisible        = true;
    private       JISAPlotPoint                     templateElement;
    private       String                            name                 = "";
    private       PlotOrder                         order                = PlotOrder.ORDER_ADDED;


    public JISAPlotSeries(JISAPlot plot, ResultTable dataTable, Evaluable xValue, Evaluable yValue, Evaluable exValue, Evaluable eyValue) {

        this.plot      = plot;
        this.dataTable = dataTable;
        this.xValue    = xValue;
        this.yValue    = yValue;
        this.exValue   = exValue;
        this.eyValue   = eyValue;

        dataTable.addClearable(this);

        updater = dataTable.addOnUpdate(row -> {
            if (test(row)) plot.pointAdded(this, addPoint(row));
        });

        for (Result row : dataTable) {
            if (test(row)) addPoint(row);
        }

    }

    public JISAPlotSeries(JISAPlot plot, ResultTable dataTable, Evaluable xValue, Evaluable yValue, Evaluable eyValue) {
        this(plot, dataTable, xValue, yValue, null, eyValue);
    }

    public JISAPlotSeries(JISAPlot plot, ResultTable dataTable, Evaluable xValue, Evaluable yValue) {
        this(plot, dataTable, xValue, yValue, null, null);
    }

    private JISAPlotPoint addPoint(Result row) {

        JISAPlotPoint element = newElement();
        element.setX(xValue.evaluate(row));
        element.setY(yValue.evaluate(row));

        if (exValue != null) {
            element.setErrorX(exValue.evaluate(row));
        }

        if (eyValue != null) {
            element.setErrorX(eyValue.evaluate(row));
        }

        if (maxX == null || element.getX() > maxX) {
            maxX = element.getX();
        }

        if (minX == null || element.getX() < minX) {
            minX = element.getX();
        }

        if (maxY == null || element.getY() > maxY) {
            maxY = element.getY();
        }

        if (minY == null || element.getY() < minY) {
            minY = element.getY();
        }

        used.add(element);

        return element;

    }

    public synchronized void updateLine() {

        List<PathElement>       path  = new LinkedList<>();
        AtomicReference<MoveTo> mtRef = new AtomicReference<>(null);

        MoveTo moveTo = (MoveTo) line.getElements().stream()
                                     .filter(e -> e instanceof MoveTo)
                                     .findFirst()
                                     .orElseGet(MoveTo::new);

        line.getElements().removeIf(e -> !(e instanceof LineTo));

        switch (order) {

            case X_AXIS:
                used.sort(Comparator.comparingDouble(JISAPlotPoint::getX));
                break;

            case Y_AXIS:
                used.sort(Comparator.comparingDouble(JISAPlotPoint::getY));
                break;

        }

        JISAPlotPoint firstPoint = used.get(0);
        moveTo.setX(plot.getXPosition(firstPoint.getX()));
        moveTo.setY(plot.getYPosition(firstPoint.getY()));

        path.add(moveTo);

        for (JISAPlotPoint point : used) {

            LineTo lineTo;

            if (line.getElements().isEmpty()) {
                lineTo = new LineTo();
            } else {
                lineTo = (LineTo) line.getElements().remove(0);
            }

            lineTo.setX(plot.getXPosition(point.getX()));
            lineTo.setY(plot.getYPosition(point.getY()));

            path.add(lineTo);

        }

        line.getElements().clear();
        line.getElements().addAll(path);

    }

    public synchronized void updateBinningSimple() {
        updateBinningSimple(true);
    }

    public synchronized void updateBinningSimple(boolean update) {

        int width = (int) plot.getAreaWidth();

        List<JISAPlotPoint> binned = new LinkedList<>();
        Double              lastY  = null;

        for (int x = 0; x <= width; x++) {

            final int     finalX = x;
            final Double  finalY = lastY;
            JISAPlotPoint found;

            if (lastY == null) {

                found = used
                        .stream()
                        .filter(p -> ((int) plot.getXPosition(p.getX())) == finalX)
                        .findFirst().orElse(null);

            } else {

                found = used.stream()
                            .filter(p -> ((int) plot.getXPosition(p.getX())) == finalX)
                            .max(Comparator.comparingDouble(p -> Math.abs(p.getY() - finalY)))
                            .orElse(null);

            }

            if (found != null) {
                lastY = found.getY();
                binned.add(found);
            }

        }

        List<JISAPlotPoint> removed = used.filtered(p -> !binned.contains(p));
        reserved.addAll(removed);
        used.removeAll(removed);

        if (update) {
            plot.pointsRemoved(this, removed);
        }

    }

    public synchronized void updateBinningFull() {

        List<JISAPlotPoint> removed = used.filtered(p -> plot.isInRange(p.getX(), p.getY()));
        reserved.addAll(removed);
        used.removeAll(removed);
        plot.pointsRemoved(this, removed);

        for (Result row : dataTable) {
            if (plot.isInRange(xValue.evaluate(row), yValue.evaluate(row)) && test(row)) {
                addPoint(row);
            }
        }

        updateBinningSimple(false);

        plot.pointsAdded(this, used.filtered(p -> plot.isInRange(p.getX(), p.getY())));

    }

    public boolean isLineVisible() {
        return lineVisible;
    }

    public void setLineVisible(boolean lineVisible) {
        this.lineVisible = lineVisible;
    }

    public boolean isMarkerVisible() {
        return markerVisible;
    }

    public void setMarkerVisible(boolean markerVisible) {
        this.markerVisible = markerVisible;
    }

    private synchronized JISAPlotPoint newElement() {

        if (reserved.isEmpty()) {
            return templateElement.copy();
        } else {
            JISAPlotPoint element = reserved.remove(0);
            element.reset();
            return element;
        }

    }

    public ObservableList<JISAPlotPoint> getPoints() {
        return used;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public synchronized void redraw() {

        clear();

        for (Result row : dataTable) {
            updater.run(row);
        }

    }

    public synchronized void fullRedraw() {

        used.clear();
        reserved.clear();
        System.gc();

        for (Result row : dataTable) {
            updater.run(row);
        }

    }

    public boolean test(Result row) {

        for (Predicate<Result> filter : filters) {
            if (!filter.test(row)) return false;
        }

        return true;

    }

    public synchronized Predicate<Result> addFilter(Predicate<Result> filter) {

        filters.add(filter);
        redraw();
        return filter;

    }

    public synchronized void removeFilter(Predicate<Result> filter) {

        filters.remove(filter);
        redraw();

    }

    @Override
    public void clear() {
        reserved.addAll(used);
        used.clear();
        minX = null;
        maxX = null;
        minY = null;
        maxY = null;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public double getMarkerSize() {
        return markerSize;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public Path getLine() {
        return line;
    }
}
