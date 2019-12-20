package jisa.gui;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;
import jisa.Util;
import jisa.maths.Function;
import jisa.experiment.Result;
import jisa.experiment.ResultTable;
import jisa.maths.fits.Fit;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class JISAChart extends XYChart<Double, Double> {

    private Map<Series, ChartNode> nodeTemplates = new HashMap<>();
    private Map<Series, Fitter>    fitters       = new HashMap<>();
    private List<JISASeries>       series        = new LinkedList<>();
    private Plot.Sort              sorting       = Plot.Sort.X_AXIS;
    private Legend                 legend        = new Legend();
    private double                 minX          = Double.POSITIVE_INFINITY;
    private double                 maxX          = Double.NEGATIVE_INFINITY;
    private double                 minY          = Double.POSITIVE_INFINITY;
    private double                 maxY          = Double.NEGATIVE_INFINITY;
    private List<Node>             newlyAdded    = new ArrayList<>();
    private ParallelTransition     animation     = new ParallelTransition();

    public JISAChart() {

        super(new SmartAxis(), new SmartAxis());
        ((SmartAxis) getXAxis()).setChart(this);
        ((SmartAxis) getYAxis()).setChart(this);
        getXAxis().setAnimated(false);
        getYAxis().setAnimated(false);

        setLegend(legend);
        setLegendSide(Side.RIGHT);

        setData(FXCollections.observableArrayList());

        getData().addListener((ListChangeListener<? super Series<Double, Double>>) c -> {

            while (c.next()) {

                if (c.wasAdded()) {

                    for (Series<Double, Double> added : c.getAddedSubList()) {
                        seriesAdded(added, getData().size());
                    }

                }

                if (c.wasRemoved()) {

                    for (Series<Double, Double> added : c.getRemoved()) {
                        seriesRemoved(added);
                    }

                }

            }

        });

    }

    private static List<XYChart.Data<Double, Double>> reducePoints(List<XYChart.Data<Double, Double>> points, double epsilon) {

        double dmax  = 0;
        int    index = 0;
        int    end   = points.size() - 1;
        Line   line  = new Line(points.get(0), points.get(end));

        for (int i = 1; i < end; i++) {
            double d = line.getDistance(points.get(i));
            if (d > dmax) {
                dmax  = d;
                index = i;
            }
        }

        List<XYChart.Data<Double, Double>> results = new LinkedList<>();

        if (dmax > epsilon) {
            List<XYChart.Data<Double, Double>> list1 = reducePoints(points.subList(0, index + 1), epsilon);
            List<XYChart.Data<Double, Double>> list2 = reducePoints(points.subList(index, points.size()), epsilon);
            results.addAll(list1);
            results.addAll(list2.subList(1, list2.size()));
        } else {
            results.add(points.get(0));
            results.add(points.get(end));
        }

        return results;

    }

    private Node createSymbol(Series<Double, Double> series, Data<Double, Double> item) {

        ChartNode symbol;
        if (nodeTemplates.containsKey(series)) {
            symbol = nodeTemplates.get(series).clone();
        } else {
            symbol = new ChartNode();
            nodeTemplates.put(series, symbol);
        }

        symbol.setOpacity(0);

        return symbol;

    }

    @Override
    protected void dataItemAdded(Series<Double, Double> series, int itemIndex, Data<Double, Double> item) {

        Node symbol = createSymbol(series, item);
        item.setNode(symbol);
        newlyAdded.add(symbol);
        GUI.runNow(() -> getPlotChildren().add(symbol));

    }

    @Override
    protected void dataItemRemoved(Data<Double, Double> item, Series<Double, Double> series) {

        Node symbol = item.getNode();

        if (symbol != null) {
            GUI.runNow(() -> getPlotChildren().remove(symbol));
        }

    }

    @Override
    protected void dataItemChanged(Data<Double, Double> item) {

    }

    @Override
    protected void seriesAdded(Series<Double, Double> series, int seriesIndex) {

        Path line = new ChartLine();
        series.setNode(line);
        getPlotChildren().add(line);

        int i = 0;
        for (Data<Double, Double> data : series.getData()) {
            dataItemAdded(series, i++, data);
        }

    }

    @Override
    protected void updateLegend() {

        GUI.runNow(() -> {

            legend.getItems().clear();

            if (getData() != null) {

                for (int seriesIndex = 0; seriesIndex < getData().size(); seriesIndex++) {

                    Series series = getData().get(seriesIndex);

                    if (nodeTemplates.containsKey(series)) {
                        Legend.LegendItem legendItem = new Legend.LegendItem(series.getName(), nodeTemplates.get(series).clone(true));
                        legend.getItems().add(legendItem);
                    }

                }
            }

            if (legend.getItems().size() > 0) {

                if (getLegend() == null) {
                    setLegend(legend);
                }

            } else {

                setLegend(null);

            }

        });
    }

    @Override
    protected void seriesRemoved(Series<Double, Double> series) {

        GUI.runNow(() -> {

            for (Data<Double, Double> data : series.getData()) {

                Node symbol = data.getNode();

                if (symbol != null) {
                    getPlotChildren().remove(symbol);
                }

            }

            Node line = series.getNode();
            if (line != null) {
                getPlotChildren().remove(line);
            }

            nodeTemplates.remove(series);
            fitters.remove(series);

            updateLegend();

        });

    }

    @Override
    protected synchronized void layoutPlotChildren() {

        GUI.runNow(() -> {

            this.animation.jumpTo(Duration.millis(250));
            ParallelTransition animation = new ParallelTransition();

            List<JISALineTo> constructedPath = new ArrayList<>();

            for (int i = 0; i < getData().size(); i++) {

                Series<Double, Double> series = getData().get(i);

                if (series.getNode() instanceof Path) {

                    final ObservableList<PathElement> seriesLine = ((Path) series.getNode()).getElements();
                    constructedPath.clear();
                    seriesLine.clear();

                    Fit      fit    = fitters.containsKey(series) ? fitters.get(series).getFit(series.getData()) : null;
                    Function fitted = fit == null ? null : fit.getFunction();

                    double minX = Double.POSITIVE_INFINITY;
                    double maxX = Double.NEGATIVE_INFINITY;

                    List<LineTo> toAnimate = new ArrayList<>();

                    for (int j = 0; j < series.getData().size(); j++) {

                        Data<Double, Double> data = series.getData().get(j);

                        double x = getXAxis().getDisplayPosition(data.getXValue());
                        double y = getYAxis().getDisplayPosition(data.getYValue());

                        double e = data.getExtraValue() == null ? 0.0 : getYAxis().getDisplayPosition((Double) data.getExtraValue()) - getYAxis().getDisplayPosition(0.0);
                        if (Double.isNaN(x) || Double.isNaN(y)) {
                            continue;
                        }

                        minX = Math.min(x, minX);
                        maxX = Math.max(x, maxX);

                        Node symbol = data.getNode();

                        if (fitted == null) {

                            JISALineTo element = new JISALineTo(x, y);

                            if (symbol != null && newlyAdded.contains(symbol)) {
                                toAnimate.add(element);
                            }

                            constructedPath.add(element);

                        }

                        if (symbol != null) {

                            ((ChartNode) symbol).setErrorBar(2.0 * e);

                            final double w = symbol.prefWidth(-1);
                            final double h = symbol.prefHeight(-1);
                            symbol.resizeRelocate(x - w / 2, y - h / 2, w, h);

                        }

                    }

                    if (fitted != null) {

                        double start = 0;
                        double stop  = getWidth();

                        int pixels = (int) (stop - start);

                        for (double x : Util.makeLinearArray(start, stop, pixels + 1)) {

                            constructedPath.add(new JISALineTo(
                                x,
                                getYAxis().getDisplayPosition(fitted.value(getXAxis().getValueForDisplay(x)))
                            ));

                        }

                    }

                    List<Data<Double, Double>> sorted = new ArrayList<>(series.getData());

                    switch (getAxisSortingPolicy()) {

                        case X_AXIS:
                            constructedPath.sort(Comparator.comparingDouble(LineTo::getX));
                            sorted.sort(Comparator.comparingDouble(Data<Double, Double>::getXValue));
                            break;

                        case Y_AXIS:
                            constructedPath.sort(Comparator.comparingDouble(LineTo::getY));
                            sorted.sort(Comparator.comparingDouble(Data<Double, Double>::getYValue));
                            break;

                    }

                    for (int j = 0; j < sorted.size(); j++) {

                        ChartNode symbol = (ChartNode) sorted.get(j).getNode();

                        if (newlyAdded.contains(symbol)) {
                            newlyAdded.remove(symbol);
                            Animation anim = symbol.animate(j > 0 ? sorted.get(j - 1).getNode() : null, j + 1 < sorted.size() ? sorted.get(j + 1).getNode() : null);
                            animation.getChildren().add(anim);
                        }

                    }

                    for (int n = 0; n < constructedPath.size(); n++) {

                        JISALineTo element = constructedPath.get(n);

                        if (toAnimate.contains(element)) {
                            Timeline trans = element.animate(n > 0 ? constructedPath.get(n - 1) : null, (n + 1) < constructedPath.size() ? constructedPath.get(n + 1) : null);
                            if (trans != null) animation.getChildren().add(trans);
                        }

                    }

                    if (!constructedPath.isEmpty()) {

                        LineTo first = constructedPath.get(0);
                        seriesLine.add(new MoveTo(first.getX(), first.getY()));
                        seriesLine.addAll(constructedPath);

                    }

                }

            }
            this.animation = animation;
            animation.play();

        });

    }

    public Legend getChartLegend() {
        return legend;
    }

    public List<JISASeries> getSeries() {
        return new ArrayList<>(series);
    }

    public JISASeries createSeries() {

        Series<Double, Double>      series  = new Series<>(FXCollections.observableArrayList());
        AtomicReference<JISASeries> created = new AtomicReference<>();

        GUI.runNow(() -> {

            getData().add(series);
            JISASeries toReturn = new JISASeries(series);
            int        count    = getData().size();
            toReturn.setColour(jisa.gui.Series.defaultColours[(count - 1) % jisa.gui.Series.defaultColours.length]);
            toReturn.setName(String.format("Series %d", count));
            this.series.add(toReturn);
            created.set(toReturn);

        });

        updateLegend();
        return created.get();

    }

    private JISASeries createSubSeries() {

        Series<Double, Double>      series  = new Series<>(FXCollections.observableArrayList());
        AtomicReference<JISASeries> created = new AtomicReference<>();

        GUI.runNow(() -> {

            getData().add(series);
            JISASeries toReturn = new JISASeries(series);
            int        count    = getData().size();
            toReturn.setColour(jisa.gui.Series.defaultColours[(count - 1) % jisa.gui.Series.defaultColours.length]);
            toReturn.setName(String.format("Series %d", count));

            created.set(toReturn);

        });

        updateLegend();
        return created.get();

    }

    public Plot.Sort getAxisSortingPolicy() {
        return sorting;
    }

    public void setAxisSortingPolicy(Plot.Sort sort) {
        sorting = sort;
        requestChartLayout();
    }

    public interface ResultHandler {

        void handle(Result row, double x, double y, double e);

    }

    public interface Fitter {
        Fit getFit(List<Data<Double, Double>> data);

    }

    public interface DataHandler {

        void click(double x, double y, double e);

    }

    public static class JISALineTo extends LineTo {

        public JISALineTo(double x, double y) {
            super(x, y);
        }

        public Timeline animate(LineTo last, LineTo next) {
            return animate(last, next, 250);
        }

        public Timeline animate(LineTo last, LineTo next, double millis) {

            double fromX;
            double fromY;

            if (last != null && next != null) {
                fromX = (last.getX() + next.getX()) / 2;
                fromY = (last.getY() + next.getY()) / 2;
            } else if (last != null) {
                fromX = last.getX();
                fromY = last.getY();
            } else if (next != null) {
                fromX = next.getX();
                fromY = next.getY();
            } else {
                return null;
            }

            Timeline timeline = new Timeline();
            timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(0), new KeyValue(xProperty(), fromX)),
                new KeyFrame(Duration.millis(0), new KeyValue(yProperty(), fromY)),
                new KeyFrame(Duration.millis(millis), new KeyValue(xProperty(), getX())),
                new KeyFrame(Duration.millis(millis), new KeyValue(yProperty(), getY()))
            );

            setX(fromX);
            setY(fromY);

            return timeline;

        }

    }

    public static class ChartTag extends StackPane {

        public ChartTag() {

            setShape(new Path(
                new MoveTo(0, 0),
                new LineTo(5, 10),
                new LineTo(50, 10),
                new LineTo(50, -10),
                new LineTo(5, -10),
                new LineTo(0, 0),
                new ClosePath()
            ));

            setHeight(20);
            setWidth(50);

            setEffect(new DropShadow(5.0, Color.BLACK));

        }

    }

    public static class ChartNode extends StackPane {

        private Map<String, String>   style     = new HashMap<>();
        private jisa.gui.Series.Shape shape     = jisa.gui.Series.Shape.CIRCLE;
        private Color                 colour    = Color.ORANGERED;
        private double                size      = 5.0;
        private boolean               visible   = true;
        private boolean               isLegend  = false;
        private List<ChartNode>       clones    = new LinkedList<>();
        private StackPane             symbol    = new StackPane();
        private Path                  errorBar  = new Path();
        private Animation             animation = null;

        public ChartNode() {
            this(false);
        }

        public ChartNode(boolean legend) {

            isLegend = legend;
            getChildren().addAll(errorBar, symbol);
            setStyle(shape, colour, size);
            setLineWidth(2.5);
            setErrorBar(0.0);

        }

        public ChartNode clone() {
            return clone(false);
        }

        public ChartNode clone(boolean legend) {

            ChartNode node = new ChartNode(legend);
            node.setStyle(shape, colour, size);
            node.setMarkerVisible(visible);
            clones.add(node);
            return node;

        }

        public void setErrorBar(double height) {

            height = Math.abs(height);

            List<PathElement> elements = errorBar.getElements();
            elements.clear();

            if (height > 0 && !isLegend) {

                errorBar.setVisible(true);

                elements.add(new MoveTo(0, 0));
                elements.add(new LineTo(size * 2, 0));
                elements.add(new MoveTo(size, 0));
                elements.add(new LineTo(size, height));
                elements.add(new MoveTo(0, height));
                elements.add(new LineTo(size * 2, height));

                errorBar.resizeRelocate(-size, -height / 2, errorBar.prefWidth(-1), errorBar.prefHeight(-1));

            } else {
                errorBar.setVisible(false);
            }

        }

        public Animation animate(Node lastNode, Node nextNode) {

            double x;
            double y;

            if (lastNode != null && nextNode != null) {
                x = (lastNode.getLayoutX() + nextNode.getLayoutX()) / 2;
                y = (lastNode.getLayoutY() + nextNode.getLayoutY()) / 2;
            } else if (lastNode != null) {
                x = lastNode.getLayoutX();
                y = lastNode.getLayoutY();
            } else if (nextNode != null) {
                x = nextNode.getLayoutX();
                y = nextNode.getLayoutY();
            } else {
                x = getLayoutX();
                y = getLayoutY();
            }

            Timeline movement = new Timeline();

            movement.getKeyFrames().addAll(
                new KeyFrame(Duration.millis(0), new KeyValue(layoutXProperty(), x)),
                new KeyFrame(Duration.millis(0), new KeyValue(layoutYProperty(), y)),
                new KeyFrame(Duration.millis(250), new KeyValue(layoutXProperty(), getLayoutX())),
                new KeyFrame(Duration.millis(250), new KeyValue(layoutYProperty(), getLayoutY()))
            );

            FadeTransition fade = new FadeTransition();
            fade.setNode(this);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.setDuration(Duration.millis(500));

            animation = new ParallelTransition(fade, movement);

            return animation;

        }

        public synchronized void updateStyle() {

            List<String> list = new LinkedList<>();
            style.forEach((k, v) -> list.add(String.format("%s: %s;", k, v)));
            symbol.setStyle(String.join(" ", list));

        }

        public void setStyle(jisa.gui.Series.Shape shape, Color colour, double size) {

            style.clear();

            switch (shape) {

                case CIRCLE:

                    style.put("-fx-background-radius", size + "px");
                    style.put("-fx-max-width", 2 * size + "px");
                    style.put("-fx-max-height", 2 * size + "px");
                    style.put("-fx-padding", size + "px");
                    style.put("-fx-background-insets", "0, 2px");
                    style.put("-fx-background-color", Util.colourToCSS(colour) + ", white");
                    break;

                case DOT:

                    style.put("-fx-background-radius", size + "px");
                    style.put("-fx-padding", size + "px");
                    style.put("-fx-background-color", Util.colourToCSS(colour));
                    break;

                case SQUARE:

                    style.put("-fx-padding", size + "px");
                    style.put("-fx-background-insets", "0, 2px");
                    style.put("-fx-background-color", Util.colourToCSS(colour) + ", white");
                    break;

                case DIAMOND:

                    style.put("-fx-padding", size + "px");
                    style.put("-fx-background-insets", "0, 2px");
                    style.put("-fx-background-color", Util.colourToCSS(colour) + ", white");
                    style.put("-fx-rotate", "45");
                    break;

                case CROSS:

                    Path cross = new Path();
                    cross.getElements().add(new MoveTo(1, 0));
                    cross.getElements().add(new LineTo(0, 0));
                    cross.getElements().add(new LineTo(0, 1));
                    cross.getElements().add(new LineTo(size - 1, size));
                    cross.getElements().add(new LineTo(0, 2 * size - 1));
                    cross.getElements().add(new LineTo(0, 2 * size));
                    cross.getElements().add(new LineTo(1, 2 * size));
                    cross.getElements().add(new LineTo(size, size + 1));
                    cross.getElements().add(new LineTo(2 * size - 1, 2 * size));
                    cross.getElements().add(new LineTo(2 * size, 2 * size));
                    cross.getElements().add(new LineTo(2 * size, 2 * size - 1));
                    cross.getElements().add(new LineTo(size + 1, size));
                    cross.getElements().add(new LineTo(2 * size, 1));
                    cross.getElements().add(new LineTo(2 * size, 0));
                    cross.getElements().add(new LineTo(2 * size - 1, 0));
                    cross.getElements().add(new LineTo(size, size - 1));
                    cross.getElements().add(new LineTo(1, 0));

                    style.put("-fx-padding", size + "px");
                    style.put("-fx-shape", "\"" + Util.pathToSVG(cross) + "\"");
                    style.put("-fx-background-color", Util.colourToCSS(colour));
                    break;

                case TRIANGLE:

                    style.put("-fx-padding", size + "px");
                    style.put("-fx-shape", "\"" + String.format("M%s %s L%s %s L%s %s Z", 0 - size, 0 + size, 0, 0- size, 0 + size, 0 + size) + "\"");
                    style.put("-fx-background-insets", "0, 2px");
                    style.put("-fx-background-color", Util.colourToCSS(colour) + ", white");
                    break;

                case STAR:
                    break;

                case DASH:

                    style.put("-fx-padding", "1 " + size + " 1 " + size);
                    style.put("-fx-background-color", Util.colourToCSS(colour));
                    break;

            }

            style.put("visibility", isMarkerVisible() ? "visible" : "hidden");

            if (isLegend && !isMarkerVisible()) {
                style.clear();
                style.put("-fx-background-color", Util.colourToCSS(colour));
                style.put("-fx-padding", "1px 5px 1px 5px");
                style.put("visibility", "visible");
            }

            updateStyle();
            this.shape  = shape;
            this.colour = colour;
            this.size   = size;

            errorBar.setStroke(colour);

            clones.forEach(n -> n.setStyle(shape, colour, size));

        }

        public JISASeries.Shape getMarkerShape() {
            return shape;
        }

        public void setMarkerShape(jisa.gui.Series.Shape shape) {

            setStyle(shape, colour, size);

        }

        public Color getMarkerColour() {
            return colour;
        }

        public void setMarkerColour(Color colour) {

            setStyle(shape, colour, size);

        }

        public double getMarkerSize() {
            return size;
        }

        public void setMarkerSize(double size) {

            setStyle(shape, colour, size);

        }

        public void setLineWidth(double width) {

            errorBar.setStrokeWidth(width);
            clones.forEach(n -> n.setLineWidth(width));

        }

        public boolean isMarkerVisible() {
            return visible;
        }

        public void setMarkerVisible(boolean flag) {

            style.put("visibility", flag ? "visible" : "hidden");
            this.visible = flag;

            // If this is the legend item and were hiding the markers, change the legend symbol to a line
            if (isLegend && !isMarkerVisible()) {
                style.clear();
                style.put("-fx-background-color", Util.colourToCSS(colour));
                style.put("-fx-padding", "1px 5px 1px 5px");
                style.put("visibility", "visible");
            }

            updateStyle();

            clones.forEach(n -> n.setMarkerVisible(flag));

        }

    }

    private static class Line {

        private double x1;
        private double x2;
        private double y1;
        private double y2;
        private double dx;
        private double dy;
        private double x1y2;
        private double x2y1;
        private double length;

        public Line(XYChart.Data<Double, Double> start, XYChart.Data<Double, Double> end) {

            x1     = start.getXValue();
            x2     = end.getXValue();
            y1     = start.getYValue();
            y2     = end.getYValue();
            dx     = x1 - x2;
            dy     = y1 - y2;
            x1y2   = x1 * y2;
            x2y1   = x2 * y1;
            length = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
        }

        public double getDistance(XYChart.Data<Double, Double> p) {

            if (length == 0) {
                return 0;
            }

            return Math.abs(dy * p.getXValue() - dx * p.getYValue() + x1y2 - x2y1) / length;
        }

    }

    public class ChartLine extends Path {

        private Map<String, String> style   = new HashMap<>();
        private double              width   = 2.5;
        private Color               colour  = Colour.ORANGERED;
        private boolean             visible = true;
        private List<ChartLine>     clones  = new LinkedList<>();

        public ChartLine() {

            setStrokeLineJoin(StrokeLineJoin.BEVEL);
            style.put("-fx-stroke-width", width + "px");
            style.put("-fx-stroke", Util.colourToCSS(colour));
            style.put("visibility", visible ? "visible" : "hidden");
            updateStyle();

        }

        public ChartLine clone() {

            ChartLine node = new ChartLine();
            node.style = this.style;
            node.setDashes(getDashes());
            node.updateStyle();
            clones.add(node);

            return node;

        }

        public synchronized void updateStyle() {

            List<String> list = new LinkedList<>();
            style.forEach((k, v) -> list.add(String.format("%s: %s;", k, v)));
            setStyle(String.join(" ", list));
            applyCss();

            clones.forEach(ChartLine::updateStyle);

        }

        public double getLineWidth() {
            return width;
        }

        public void setLineWidth(double width) {

            style.put("-fx-stroke-width", width + "px");
            this.width = width;
            updateStyle();

        }

        public List<Double> getDashes() {
            return new ArrayList<>(getStrokeDashArray());
        }

        public void setDashes(List<Double> dashes) {

            getStrokeDashArray().setAll(dashes);
            clones.forEach(c -> c.getStrokeDashArray().setAll(dashes));
        }

        public void setDashes(Double... dashes) {

            getStrokeDashArray().setAll(dashes);
            clones.forEach(c -> c.getStrokeDashArray().setAll(dashes));
        }

        public Color getLineColour() {
            return colour;
        }

        public void setLineColour(Color colour) {

            style.put("-fx-stroke", Util.colourToCSS(colour));
            this.colour = colour;
            updateStyle();

        }

        public boolean isLineVisible() {
            return visible;
        }

        public void setLineVisible(boolean flag) {

            style.put("visibility", flag ? "visible" : "hidden");
            this.visible = flag;
            updateStyle();

        }

    }

    public class JISASeries implements jisa.gui.Series {

        private Predicate<Result>          filter         = r -> true;
        private Series<Double, Double>     series;
        private double                     xTrack         = Double.POSITIVE_INFINITY;
        private double                     yTrack         = Double.POSITIVE_INFINITY;
        private double                     maxX           = Double.NEGATIVE_INFINITY;
        private double                     maxY           = Double.NEGATIVE_INFINITY;
        private List<Data<Double, Double>> data;
        private ChartNode                  template;
        private ChartLine                  line;
        private Dash                       dash           = Dash.SOLID;
        private ResultHandler              handler;
        private List<jisa.gui.Series>      subSeries      = new ArrayList<>();
        private ResultTable.Evaluable      xData          = null;
        private ResultTable.Evaluable      yData          = null;
        private ResultTable.Evaluable      eData          = null;
        private ResultTable                watching       = null;
        private ResultTable.OnUpdate       rtListener     = null;
        private Color[]                    defaultColours = jisa.gui.Series.defaultColours;
        private int                        maxPoints      = Integer.MAX_VALUE;
        private int                        redPoints      = Integer.MAX_VALUE;
        private DataHandler                click          = (x, y, e) -> {
        };

        public JISASeries(Series<Double, Double> series) {

            this.series = series;

            if (!nodeTemplates.containsKey(series)) {
                nodeTemplates.put(series, new ChartNode());
            }

            this.template = nodeTemplates.get(series);
            this.line     = (ChartLine) series.getNode();
            this.data     = series.getData();

        }

        private boolean isInRange(double x, double y) {
            return Util.isBetween(x, maxX - xTrack, maxX) && Util.isBetween(y, maxY - yTrack, maxY);
        }

        @Override
        public JISASeries watch(ResultTable list, ResultTable.Evaluable xData, ResultTable.Evaluable yData, ResultTable.Evaluable eData) {

            if (watching != null) {
                watching.removeOnUpdate(rtListener);
            }

            watching = list;

            this.xData = xData;
            this.yData = yData;
            this.eData = eData;
            handler    = (r, x, y, e) -> addPoint(x, y, e);
            rtListener = list.addOnUpdate(r -> {

                if (filter.test(r)) {
                    GUI.runNow(() -> handler.handle(r, xData.evaluate(r), yData.evaluate(r), eData.evaluate(r)));
                }

            });

            for (Result row : list) {

                if (filter.test(row)) {
                    GUI.runNow(() -> handler.handle(row, xData.evaluate(row), yData.evaluate(row), eData.evaluate(row)));
                }

            }

            return this;

        }

        public JISASeries watch(ResultTable list, int xData, int yData, int eData) {

            GUI.runNow(() -> {

                if (((SmartAxis) getXAxis()).getLabelText().trim().equals("")) {
                    ((SmartAxis) getXAxis()).setLabelText(list.getTitle(xData));
                }

                if (((SmartAxis) getYAxis()).getLabelText().trim().equals("")) {
                    ((SmartAxis) getYAxis()).setLabelText(list.getTitle(yData));
                }

            });

            return watch(list, r -> r.get(xData), r -> r.get(yData), r -> r.get(eData));

        }

        public JISASeries watch(ResultTable list, int xData, int yData) {

            GUI.runNow(() -> {

                if (((SmartAxis) getXAxis()).getLabelText().trim().equals("")) {
                    ((SmartAxis) getXAxis()).setLabelText(list.getTitle(xData));
                }

                if (((SmartAxis) getYAxis()).getLabelText().trim().equals("")) {
                    ((SmartAxis) getYAxis()).setLabelText(list.getTitle(yData));
                }

            });

            return watch(list, r -> r.get(xData), r -> r.get(yData), r -> 0.0);

        }


        @Override
        public JISASeries split(ResultTable.Evaluable splitBy, SeriesFormatter formatter) {

            if (watching == null) {
                throw new IllegalStateException("A series must be watching a ResultTable before being able to be split.");
            }

            // Remove any previously added points
            clear();
            getData().remove(series);

            // Create a map of splitting value to series
            final Map<Double, jisa.gui.Series> map = new HashMap<>();

            handler = (r, x, y, e) -> {

                double value = splitBy.evaluate(r);

                if (!map.containsKey(value)) {

                    jisa.gui.Series series = createSeries()
                        .setName(formatter.getName(r))
                        .setColour(defaultColours[subSeries.size() % defaultColours.length])
                        .showLine(isShowingLine())
                        .showMarkers(isShowingMarkers())
                        .setMarkerShape(getMarkerShape())
                        .setMarkerSize(getMarkerSize());

                    if (isFitted()) {
                        series.fit(getFitter());
                    }

                    map.put(value, series);
                    subSeries.add(series);

                }

                map.get(value).addPoint(x, y, e);

            };

            for (Result row : watching) {
                if (filter.test(row)) {
                    handler.handle(row, xData.evaluate(row), yData.evaluate(row), eData.evaluate(row));
                }
            }

            return this;

        }

        @Override
        public JISASeries watchAll(ResultTable list, int xData) {

            if (watching != null) {
                watching.removeOnUpdate(rtListener);
            }

            clear();
            getData().remove(series);
            subSeries.clear();

            final Map<Integer, jisa.gui.Series> map = new HashMap<>();

            for (int i = 0; i < list.getNumCols(); i++) {

                if (i == xData) {
                    continue;
                }

                jisa.gui.Series series = createSeries()
                    .setName(list.getTitle(i))
                    .setColour(defaultColours[subSeries.size() % defaultColours.length]);

                if (isFitted()) {
                    series.fit(getFitter());
                }

                subSeries.add(series);
                map.put(i, series);

            }

            handler = (r, x, y, e) -> {

                double[] data = r.getData();

                for (int i = 0; i < data.length; i++) {

                    if (i == xData) {
                        continue;
                    }

                    map.get(i).addPoint(data[xData], data[i]);

                }

            };

            watching   = list;
            this.xData = r -> r.get(xData);
            this.yData = r -> r.get(xData);

            rtListener = list.addOnUpdate(r -> {
                if (filter.test(r)) {
                    handler.handle(r, 0, 0, 0);
                }
            });

            for (Result row : list) {
                if (filter.test(row)) {
                    handler.handle(row, 0, 0, 0);
                }
            }

            return this;

        }

        @Override
        public jisa.gui.Series setOnClick(DataHandler onClick) {

            if (onClick == null) {
                click = (x, y, e) -> {
                };
            } else {
                click = onClick;
            }

            return this;

        }

        @Override
        public ResultTable getWatched() {
            return watching;
        }

        @Override
        public JISASeries filter(Predicate<Result> filter) {

            if (watching == null) {
                throw new IllegalStateException("You cannot filter a series that isn't watching a ResultTable.");
            }

            this.filter = filter;

            clear();

            for (Result row : watching) {
                if (filter.test(row)) {
                    handler.handle(row, xData.evaluate(row), yData.evaluate(row), eData.evaluate(row));
                }
            }

            return this;

        }

        @Override
        public JISASeries addPoint(double x, double y, double error) {

            maxX = Math.max(x, maxX);
            maxY = Math.max(y, maxY);

            if (isInRange(x, y)) {
                Data<Double, Double> data = new Data<>(x, y, error);
                GUI.runNow(() -> series.getData().add(data));
                data.getNode().setOnMouseClicked(e -> {
                    if (e.getClickCount() == 1) {
                        click.click(x, y, error);
                    }
                });
            }

            series.getData().removeIf(data -> !isInRange(data.getXValue(), data.getYValue()));

            if (data.size() > maxPoints) {
                reduceNow();
            }

            return this;
        }

        @Override
        public List<Data<Double, Double>> getPoints() {
            return series.getData();
        }

        @Override
        public JISASeries clear() {
            GUI.runNow(() -> series.getData().clear());
            subSeries.forEach(jisa.gui.Series::clear);
            return this;
        }

        @Override
        public JISASeries showMarkers(boolean show) {

            GUI.runNow(() -> {
                template.setMarkerVisible(show);
            });

            subSeries.forEach(s -> s.showMarkers(show));

            return this;

        }

        @Override
        public boolean isShowingMarkers() {
            return template.isMarkerVisible();
        }

        @Override
        public JISASeries setMarkerShape(Shape shape) {

            GUI.runNow(() -> {
                template.setMarkerShape(shape);
                subSeries.forEach(s -> s.setMarkerShape(shape));
            });

            return this;

        }

        public JISASeries setMarkerSize(double size) {

            GUI.runNow(() -> {
                template.setMarkerSize(size);
                subSeries.forEach(s -> s.setMarkerSize(size));
            });

            return this;

        }

        @Override
        public Shape getMarkerShape() {
            return template.getMarkerShape();
        }

        @Override
        public double getMarkerSize() {
            return template.getMarkerSize();
        }

        @Override
        public String getName() {
            return series.getName();
        }

        @Override
        public JISASeries setName(String name) {
            GUI.runNow(() -> series.setName(name));
            return this;
        }

        @Override
        public Color getColour() {
            return template.getMarkerColour();
        }

        @Override
        public JISASeries setColour(Color colour) {

            GUI.runNow(() -> {
                template.setMarkerColour(colour);
                line.setLineColour(colour);

            });
            return this;

        }

        @Override
        public JISASeries setColourSequence(Color... colours) {

            defaultColours = colours;

            for (int i = 0; i < subSeries.size(); i++) {
                subSeries.get(i).setColour(defaultColours[i % defaultColours.length]);
            }

            return this;

        }

        @Override
        public double getLineWidth() {
            return line.getLineWidth();
        }

        @Override
        public JISASeries setLineWidth(double width) {

            GUI.runNow(() -> {
                line.setLineWidth(width);
                template.setLineWidth(width);
            });
            subSeries.forEach(s -> s.setLineWidth(width));
            return this;

        }

        @Override
        public Dash getLineDash() {
            return dash;
        }

        @Override
        public JISASeries setLineDash(Dash dash) {

            GUI.runNow(() -> line.setDashes(dash.getArray()));
            subSeries.forEach(s -> s.setLineDash(dash));
            this.dash = dash;
            return this;

        }

        @Override
        public JISASeries showLine(boolean show) {

            GUI.runNow(() -> line.setLineVisible(show));

            subSeries.forEach(s -> s.showLine(show));
            return this;

        }

        @Override
        public boolean isShowingLine() {
            return line.isLineVisible();
        }

        @Override
        public JISASeries setAutoReduction(int reduceTo, int limit) {

            maxPoints = limit;
            redPoints = reduceTo;

            if (data.size() > maxPoints) {
                reduceNow(false);
            }

            subSeries.forEach(s -> setAutoReduction(reduceTo, limit));

            return this;
        }

        @Override
        public JISASeries reduceNow() {
            return reduceNow(true);
        }

        public JISASeries reduceNow(boolean subs) {

            List<Data<Double, Double>> list = new ArrayList<>(data);
            list.sort(Comparator.comparing(XYChart.Data::getXValue));

            double[] deviations = new double[Math.max(0, list.size() - 2)];
            for (int i = 2; i < list.size(); i++) {
                XYChart.Data<Double, Double> p1  = list.get(i - 2);
                XYChart.Data<Double, Double> p2  = list.get(i - 1);
                XYChart.Data<Double, Double> p3  = list.get(i);
                double                       dev = new Line(p1, p3).getDistance(p2);
                deviations[i - 2] = dev;
            }

            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;

            for (double d : deviations) {
                max = Math.max(d, max);
                min = Math.min(d, min);
            }

            double epsilon = min;
            double step    = (max - min) / 10.0;

            while (list.size() > redPoints) {
                List<XYChart.Data<Double, Double>> toKeep = reducePoints(list, epsilon);
                list.removeIf(d -> !toKeep.contains(d));
                epsilon += step;
            }

            GUI.runNow(() -> data.removeIf(d -> !list.contains(d)));

            if (subs) {
                subSeries.forEach(jisa.gui.Series::reduceNow);
            }

            return this;

        }

        @Override
        public JISASeries setXAutoRemove(double range) {

            xTrack = range;
            subSeries.forEach(s -> s.setXAutoRemove(range));
            series.getData().removeIf(data -> !isInRange(data.getXValue(), data.getYValue()));

            return this;

        }

        @Override
        public JISASeries setYAutoRemove(double range) {

            yTrack = range;
            subSeries.forEach(s -> s.setYAutoRemove(range));
            series.getData().removeIf(data -> !isInRange(data.getXValue(), data.getYValue()));

            return this;

        }

        @Override
        public JISASeries remove() {

            getData().remove(series);
            subSeries.forEach(jisa.gui.Series::remove);
            JISAChart.this.series.remove(this);
            return this;
        }

        @Override
        public JISASeries fit(Fitter fitter) {

            fitters.put(series, fitter);
            subSeries.forEach(s -> s.fit(fitter));
            requestChartLayout();

            return this;
        }

        @Override
        public Fit getFit() {

            if (isFitted()) {
                return fitters.get(series).getFit(data);
            } else {
                return null;
            }

        }

        @Override
        public Fitter getFitter() {
            return fitters.getOrDefault(series, null);
        }

        @Override
        public boolean isFitted() {
            return fitters.containsKey(series);
        }


        @Override
        public Series<Double, Double> getXYChartSeries() {
            return series;
        }

        @Override
        public Iterator<Data<Double, Double>> iterator() {
            return series.getData().iterator();
        }

    }

}
