package jisa.gui;

import de.gsi.chart.axes.spi.TickMark;
import de.gsi.chart.axes.spi.format.DefaultTimeFormatter;
import de.gsi.dataset.DataSet;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import jisa.Util;
import jisa.gui.plotting.*;
import jisa.gui.svg.*;
import jisa.maths.fits.Fit;
import jisa.maths.functions.Function;
import jisa.results.ResultTable;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.gsi.dataset.DataSet.DIM_X;
import static de.gsi.dataset.DataSet.DIM_Y;

public class Plot extends JFXElement implements Element, Clearable {

    public static final Pattern UNIT_PATTERN = Pattern.compile("^(.*)[(\\[](.*?)[)\\]]$");


    private       JISAXYChart                     chart;
    private       JISAAxis                        xAxis;
    private       JISAAxis                        yAxis;
    private final ObservableList<Series>          series         = FXCollections.observableArrayList();
    private final Map<Series, ListChangeListener> listeners      = new HashMap<>();
    private final JISAZoomer                      zoomer         = new JISAZoomer();
    private final List<ClickListener>             clickListeners = new LinkedList<>();

    public Plot(String title, String xLabel, String xUnits, String yLabel, String yUnits) {

        super(title);
        setMinHeight(500);
        setMinWidth(500);
        setWindowSize(600, 525);

        GUI.runNow(() -> {

            xAxis = new JISAAxis(xLabel, xUnits);
            yAxis = new JISAAxis(yLabel, yUnits);

            xAxis.setAutoRangePadding(0.05);
            yAxis.setAutoRangePadding(0.05);

            xAxis.setForceZeroInRange(false);
            yAxis.setForceZeroInRange(false);

            chart = new JISAXYChart(xAxis, yAxis);
            chart.setTitle(title);
            setCentreNode(chart);

            getStage().getScene().setFill(Colour.WHITE);

        });

        series.addListener((ListChangeListener<? super Series>) c -> GUI.runNow(() -> {

            while (c.next()) {

                for (Series added : c.getAddedSubList()) {

                    ListChangeListener<DataSet> listener = x -> GUI.runNow(() -> {

                        while (x.next()) {
                            chart.getDatasets().addAll(x.getAddedSubList());
                            chart.getDatasets().removeAll(x.getRemoved());
                        }

                        chart.getLegend().updateLegend(chart.getDatasets(), chart.getRenderers());

                    });

                    added.getDatasets().addListener(listener);
                    listeners.put(added, listener);

                    chart.getDatasets().addAll(added.getDatasets());

                }

                for (Series removed : c.getRemoved()) {

                    ListChangeListener<DataSet> listener = listeners.get(removed);
                    removed.getDatasets().removeListener(listener);
                    chart.getDatasets().removeAll(removed.getDatasets());

                }

                chart.getLegend().updateLegend(chart.getDatasets(), chart.getRenderers());

            }

        }));

        chart.getCanvas().setOnMouseClicked(event -> {

            double x = xAxis.getValueForDisplay(event.getX());
            double y = yAxis.getValueForDisplay(event.getY());

            Util.runAsync(() -> clickListeners.forEach(l -> l.runRegardless(event, x, y)));

        });


    }

    public JISAXYChart getChart() {
        return chart;
    }

    public Plot(String title, String xLabel, String yLabel) {
        this(title, "", "", "", "");
        setXLabel(xLabel);
        setYLabel(yLabel);
    }

    public Plot(String title) {
        this(title, "", "", "", "");
    }

    public Plot() {
        this("", "", "", "", "");
    }

    public Plot(String title, ResultTable table) {
        this(title);
        createSeries().watch(table, table.getNthNumericColumn(0), table.getNthNumericColumn(1));
    }

    public void updateLegend() {
        GUI.runNow(() -> chart.getLegend().updateLegend(chart.getDatasets(), chart.getRenderers(), true));
    }

    public void forceRedraw() {
        GUI.runNow(() -> chart.forceRedraw());
    }

    public boolean isLegendVisible() {
        return chart.isLegendVisible();
    }

    public void setLegendVisible(boolean visible) {
        GUI.runNow(() -> chart.setLegendVisible(visible));
    }

    public void setXLabel(String name, String units) {

        GUI.runNow(() -> {
            xAxis.setName(name);
            xAxis.setUnit(units);
        });

    }

    public void setXLabel(String name) {

        Matcher matcher = UNIT_PATTERN.matcher(name);

        if (matcher.matches()) {
            setXLabel(matcher.group(1), matcher.group(2));
        } else {
            setXLabel(name, null);
        }

    }

    public void setXUnit(String unit) {
        GUI.runNow(() -> xAxis.setUnit(unit));
    }

    public void setYUnit(String unit) {
        GUI.runNow(() -> yAxis.setUnit(unit));
    }

    public void setTitle(String title) {
        super.setTitle(title);
        GUI.runNow(() -> chart.setTitle(title));
    }

    public String getXUnit() {
        return xAxis.getUnit();
    }

    public String getYUnit() {
        return yAxis.getUnit();
    }

    public String getXLabel() {
        return xAxis.getName();
    }

    public String getYLabel() {
        return yAxis.getName();
    }

    public void setYLabel(String name, String units) {

        GUI.runNow(() -> {
            yAxis.setName(name);
            yAxis.setUnit(units);
        });

    }

    public void setYLabel(String name) {

        Matcher matcher = UNIT_PATTERN.matcher(name);

        if (matcher.matches()) {
            setYLabel(matcher.group(1), matcher.group(2));
        } else {
            setYLabel(name, null);
        }

    }

    public void setXLimits(double min, double max) {

        GUI.runNow(() -> {
            xAxis.setAutoRanging(false);
            xAxis.setMin(min);
            xAxis.setMax(max);
        });

    }

    public void setXMin(double min) {
        GUI.runNow(() -> {
            xAxis.setAutoRanging(false);
            xAxis.setMin(min);
        });
    }

    public void setXMax(double min) {
        GUI.runNow(() -> {
            xAxis.setAutoRanging(false);
            xAxis.setMax(min);
        });
    }

    public double getXMin() {
        return xAxis.getMin();
    }

    public double getXMax() {
        return xAxis.getMax();
    }

    public void autoRangeX() {
        GUI.runNow(() -> xAxis.setAutoRanging(true));
    }

    public boolean isXAutoRanging() {
        return xAxis.isAutoRanging();
    }

    public void setYLimits(double min, double may) {

        GUI.runNow(() -> {
            yAxis.setAutoRanging(false);
            yAxis.setMin(min);
            yAxis.setMax(may);
        });

    }

    public void setYMin(double min) {
        GUI.runNow(() -> {
            yAxis.setAutoRanging(false);
            yAxis.setMin(min);
        });
    }

    public void setYMax(double min) {
        GUI.runNow(() -> {
            yAxis.setAutoRanging(false);
            yAxis.setMax(min);
        });
    }

    public double getYMin() {
        return yAxis.getMin();
    }

    public double getYMax() {
        return yAxis.getMax();
    }

    public void autoRangeY() {
        GUI.runNow(() -> yAxis.setAutoRanging(true));
    }

    public boolean isYAutoRanging() {
        return yAxis.isAutoRanging();
    }

    public List<Series> getSeries() {
        return List.copyOf(series);
    }

    public Series createSeries() {

        Series series = new JISASeries("Series " + (this.series.size() + 1), this);

        series.setColour(Series.defaultColours[this.series.size() % Series.defaultColours.length]);

        GUI.runNow(() -> this.series.add(series));
        return series;

    }

    public void removeSeries(Series toRemove) {
        GUI.runNow(() -> this.series.remove(toRemove));
    }

    @Override
    public void clear() {
        GUI.runNow(series::clear);
    }

    public void setXAxisType(AxisType type) {

        switch (type) {

            case LINEAR:

                GUI.runNow(() -> {
                    xAxis.setLogAxis(false);
                    xAxis.setTimeAxis(false);
                    xAxis.setAutoUnitScaling(JISAAxis.KNOWN_UNITS.contains(yAxis.getUnit()));
                });

                break;

            case LOGARITHMIC:

                GUI.runNow(() -> {
                    xAxis.setLogAxis(true);
                    xAxis.setTimeAxis(false);
                    xAxis.setAutoUnitScaling(JISAAxis.KNOWN_UNITS.contains(yAxis.getUnit()));
                });

                break;

            case TIME:

                GUI.runNow(() -> {
                    xAxis.setLogAxis(false);
                    xAxis.setTimeAxis(true);
                    xAxis.setAutoUnitScaling(false);
                    ((DefaultTimeFormatter) xAxis.getAxisLabelFormatter()).setTimeZoneOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
                });

                break;

        }

    }

    public void setYAxisType(AxisType type) {

        switch (type) {

            case LINEAR:

                GUI.runNow(() -> {
                    yAxis.setLogAxis(false);
                    yAxis.setTimeAxis(false);
                    yAxis.setAutoUnitScaling(JISAAxis.KNOWN_UNITS.contains(yAxis.getUnit()));
                });

                break;

            case LOGARITHMIC:

                GUI.runNow(() -> {
                    yAxis.setLogAxis(true);
                    yAxis.setTimeAxis(false);
                    yAxis.setAutoUnitScaling(JISAAxis.KNOWN_UNITS.contains(yAxis.getUnit()));
                });

                break;

            case TIME:

                GUI.runNow(() -> {
                    yAxis.setLogAxis(false);
                    yAxis.setTimeAxis(true);
                    yAxis.setAutoUnitScaling(false);
                    ((DefaultTimeFormatter) yAxis.getAxisLabelFormatter()).setTimeZoneOffset(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
                });

                break;

        }

    }

    public AxisType getXAxisType() {

        if (xAxis.isLogAxis()) {
            return AxisType.LOGARITHMIC;
        } else if (xAxis.isTimeAxis()) {
            return AxisType.TIME;
        } else {
            return AxisType.LINEAR;
        }

    }

    public AxisType getYAxisType() {

        if (yAxis.isLogAxis()) {
            return AxisType.LOGARITHMIC;
        } else if (yAxis.isTimeAxis()) {
            return AxisType.TIME;
        } else {
            return AxisType.LINEAR;
        }

    }

    public void setXAxisLogarithmBase(double base) {
        GUI.runNow(() -> xAxis.setLogarithmBase(base));
    }

    public void setYAxisLogarithmBase(double base) {
        GUI.runNow(() -> yAxis.setLogarithmBase(base));
    }

    public double getXAxisLogarithmBase() {
        return xAxis.getLogarithmBase();
    }

    public double getYAxisLogarithmBase() {
        return yAxis.getLogarithmBase();
    }

    /**
     * Adds toolbar button that opens the plot save dialog when clicked.
     *
     * @param text Text to show
     */
    public jisa.gui.Button addSaveButton(String text) {
        return addToolbarButton(text, this::showSaveDialog);
    }

    /**
     * Shows a dialogue allowing the user to save this plot as an image, with user-alterable parameters of format, width
     * and height.
     */
    public void showSaveDialog() {

        Fields         save   = new Fields("Save Plot");
        Field<Integer> format = save.addChoice("Format", 1, "svg", "png", "tex");
        Field<Integer> width  = save.addIntegerField("Width", 600);
        Field<Integer> height = save.addIntegerField("Height", 400);
        Field<String>  file   = save.addFileSave("Path");

        save.addDialogButton("Cancel", save::close);

        save.addDialogButton("Save", () -> {

            try {

                if (!file.get().trim().equals("")) {

                    switch (format.get()) {

                        case 0:

                            if (!file.get().endsWith(".svg")) {
                                file.set(file.get() + ".svg");
                            }

                            saveSVG(file.get(), width.get(), height.get());
                            break;

                        case 1:

                            if (!file.get().endsWith(".png")) {
                                file.set(file.get() + ".png");
                            }

                            savePNG(file.get(), width.get(), height.get());
                            break;

                        case 2:

                            if (!file.get().endsWith(".tex")) {
                                file.set(file.get() + ".tex");
                            }

                            saveTex(file.get());
                            break;

                    }

                    save.close();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        save.show();

    }

    public void setMouseEnabled(boolean flag) {

        if (flag) {

            if (!chart.getPlugins().contains(zoomer)) {
                GUI.runNow(() -> chart.getPlugins().add(zoomer));
            }

        } else {
            GUI.runNow(() -> chart.getPlugins().remove(zoomer));
        }

    }

    public boolean isMouseEnabled() {
        return chart.getPlugins().contains(zoomer);
    }

    public Plot copy() {

        Plot plot = new Plot(getTitle(), getXLabel(), getXUnit(), getYLabel(), getYUnit());

        plot.series.addAll(this.series);
        plot.setLegendVisible(isLegendVisible());
        plot.setMouseEnabled(isMouseEnabled());
        plot.setXAxisType(getXAxisType());
        plot.setYAxisType(getYAxisType());
        plot.setWindowSize(getWindowWidth(), getWindowHeight());
        plot.setMinHeight(getMinHeight());
        plot.setMaxHeight(getMaxHeight());
        plot.setMinWidth(getMinWidth());
        plot.setMaxWidth(getMaxWidth());
        plot.updateLegend();

        return plot;

    }

    public void savePNG(String path, double w, double h) {

        Plot plot = copy();

        plot.setWindowSize(w, h);
        plot.show();

        Util.sleep(250);

        double diffH = h - plot.chart.getHeight();
        double diffW = w - plot.chart.getWidth();

        plot.setWindowSize(w + diffW, h + diffH);

        Util.sleep(250);

        GUI.runNow(() -> {

            WritableImage image = plot.chart.snapshot(null, null);

            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File(path));
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        plot.close();

    }

    public SVG getSVG(double width, double height) {

        SVGElement main = new SVGElement("g");

        main.setAttribute("font-family", "sans-serif")
            .setAttribute("font-size", 12);

        double aStartX = 100.0;
        double aStartY = height + 65.0;
        double aEndX   = 100.0 + width;
        double aEndY   = 65.0;

        SVGElement axisBox = new SVGElement("rect");

        axisBox.setAttribute("x", 100.0)
               .setAttribute("y", 65.0)
               .setAttribute("width", width)
               .setAttribute("height", height)
               .setStrokeWidth(1)
               .setFillColour("none")
               .setStrokeColour(Color.BLACK);

        SVGElement clip = new SVGElement("clipPath");
        clip.setAttribute("id", "lineClip");

        SVGElement clipPath = new SVGElement("rect");

        clipPath.setAttribute("x", aStartX)
                .setAttribute("y", aEndY)
                .setAttribute("width", width)
                .setAttribute("height", height);

        clipPath.setStrokeColour("none");
        clipPath.setFillColour("none");

        clip.add(clipPath);
        main.add(clip);

        SVGText title = new SVGText((aStartX + aEndX) / 2, 50.0, "middle", chart.getTitle());
        title.setAttribute("font-size", "20px");
        main.add(title);

        List<TickMark> xTicks = this.xAxis.getTickMarks();
        List<TickMark> yTicks = this.yAxis.getTickMarks();

        double xScale = (aEndX - aStartX) / this.xAxis.getWidth();
        double yScale = (aEndY - aStartY) / this.yAxis.getHeight();

        for (TickMark x : xTicks) {

            double pos = xScale * x.getPosition() + aStartX;

            if (!Util.isBetween(pos, aStartX, aEndX)) {
                continue;
            }

            SVGLine tick = new SVGLine(pos, aStartY, pos, aStartY + 10);

            tick.setStrokeWidth(1)
                .setStrokeColour(Colour.BLACK);

            SVGLine grid = new SVGLine(pos, aStartY, pos, aEndY);

            grid.setStrokeWidth(0.5)
                .setStrokeColour(Colour.SILVER)
                .setDash("2", "2");

            main.add(tick);
            main.add(grid);

            SVGText label = new SVGText(pos, aStartY + 26.0, "middle", x.getText());
            main.add(label);

        }

        SVGText xLabel = new SVGText((aEndX + aStartX) / 2, aStartY + 75.0, "middle", xAxis.getAxisLabel().getText());
        xLabel.setAttribute("font-size", "16px");
        main.add(xLabel);

        for (TickMark y : yTicks) {

            double pos = aEndY - yScale * y.getPosition();

            if (!Util.isBetween(pos, aEndY, aStartY)) {
                continue;
            }

            SVGLine tick = new SVGLine(aStartX, pos, aStartX - 10, pos);

            tick.setStrokeWidth(1)
                .setStrokeColour(Colour.BLACK);
            SVGLine grid = new SVGLine(aStartX, pos, aEndX, pos);

            grid.setStrokeWidth(0.5)
                .setStrokeColour(Colour.SILVER)
                .setDash("2", "2");
            main.add(tick);
            main.add(grid);

            SVGText label = new SVGText(aStartX - 12.0, pos + 4.0, "end", y.getText());
            main.add(label);

        }

        SVGText yLabel = new SVGText(aStartX - 75.0, (aEndY + aStartY) / 2, "middle", yAxis.getAxisLabel().getText());
        yLabel.setAttribute("transform", String.format("rotate(-90 %s %s)", aStartX - 75.0, (aEndY + aStartY) / 2))
              .setAttribute("font-size", "16px");
        main.add(yLabel);
        main.add(axisBox);

        SVGElement legend = new SVGElement("rect");

        legend.setStrokeWidth(1.0)
              .setStrokeColour(Color.BLACK)
              .setFillColour("none");


        double legendH = (chart.getDatasets().filtered(DataSet::isVisible).size() * 25) + 5.0;
        double legendX = aEndX + 25.0;
        double legendY = aEndY;

        double legendW = 0.0;

        for (DataSet s : chart.getDatasets().filtered(DataSet::isVisible)) {
            legendW = Math.max(legendW, (10.0 * s.getName().length()) + 15.0 + 5 + 3 + 20.0);
        }

        legend.setAttribute("x", legendX)
              .setAttribute("y", legendY)
              .setAttribute("width", legendW)
              .setAttribute("height", legendH);

        if (chart.isLegendVisible()) {
            main.add(legend);
        } else {
            legendW = 0;
        }

        int i = 0;
        for (DataSet set : chart.getDatasets().filtered(DataSet::isVisible)) {

            JISAErrorDataSet s = (JISAErrorDataSet) set;

            Color        c = s.getColour();
            double       w = s.getThickness();
            double       m = s.getSize();
            Series.Shape p = s.getShape();

            List<String> terms = new LinkedList<>();

            SVGElement legendCircle = makeMarker(s.isMarkerVisible() ? p : Series.Shape.DASH, c, legendX + 15.0, legendY + (25 * i) + 15.0, 5.0);
            SVGText legendText = new SVGText(
                legendX + 15.0 + 5 + 3 + 10,
                legendY + (25 * i) + 15.0 + 5,
                "beginning",
                s.getName()
            );

            legendText.setAttribute("font-size", "16px");

            if (chart.isLegendVisible()) {
                main.add(legendCircle);
                main.add(legendText);
            }

            boolean first = true;

            List<SVGElement> list = new LinkedList<>();

            double lastX = -1;
            double lastY = -1;

            for (int j = 0; j < s.getDataCount(); j++) {

                double x = aStartX + xScale * this.xAxis.getDisplayPosition(s.get(DIM_X, j));
                double y = aEndY - yScale * this.yAxis.getDisplayPosition(s.get(DIM_Y, j));

                if (!Util.isBetween(x, aStartX, aEndX) || !Util.isBetween(y, aEndY, aStartY)) {
                    continue;
                }

                terms.add(String.format("%s%s %s", first ? "M" : "L", x, y));

                if (s.isMarkerVisible()) {

                    if (s.getErrorPositive(DIM_Y, j) + s.getErrorNegative(DIM_Y, j) > 0) {

                        double yp = aEndY - yScale * this.yAxis.getDisplayPosition(s.get(DIM_Y, j) + s.getErrorPositive(DIM_Y, j));
                        double yn = aEndY - yScale * this.yAxis.getDisplayPosition(s.get(DIM_Y, j) - s.getErrorNegative(DIM_Y, j));
                        double xn = x - 5;
                        double xp = x + 5;

                        String  erPath   = String.format("M%s %s L%s %s M%s %s L%s %s M%s %s L%s %s", xn, yp, xp, yp, x, yp, x, yn, xn, yn, xp, yn);
                        SVGPath errorBar = new SVGPath(erPath);

                        errorBar.setStrokeColour(c)
                                .setStrokeWidth(w)
                                .setStyle("fill", "none");

                        list.add(errorBar);

                    }

                    list.add(makeMarker(p, c, x, y, m));

                }

                first = false;

            }

            SVGPath path;
            if (!s.isFitted()) {
                path = new SVGPath(String.join(" ", terms));
                path.setAttribute("clip-path", "url(#lineClip)");
            } else {

                Fit          fit      = s.getFit();
                Function     func     = fit.getFunction();
                List<String> elements = new LinkedList<>();
                boolean      firstEl  = true;

                for (double xc = aStartX; xc <= aEndX; xc++) {

                    double x  = this.xAxis.getValueForDisplay((xc - aStartX) / xScale);
                    double y  = func.value(x);
                    double yc = aEndY - yScale * this.yAxis.getDisplayPosition(y);

                    if (Util.isBetween(yc, aEndY, aStartY)) {

                        elements.add(String.format("%s%s %s", firstEl ? "M" : "L", xc, yc));
                        firstEl = false;

                    }

                }

                path = new SVGPath(String.join(" ", elements));

            }

            path.setStrokeColour(c)
                .setStrokeWidth(w)
                .setDash(s.getDash().getArray())
                .setStyle("fill", "none");

            if (s.isLineVisible()) {
                main.add(path);
            }

            list.forEach(main::add);

            i++;

        }


        SVG svg = new SVG(width + legendW + 50.0 + 100.0, height + 60.0 + 100.0);
        svg.add(main);

        return svg;

    }

    public void saveSVG(String fileName, double width, double height) throws IOException {
        getSVG(width, height).output(fileName);
    }

    public void outputSVG(PrintStream stream, double width, double height) {
        getSVG(width, height).output(stream);
    }

    public void outputSVG(double width, double height) {
        getSVG(width, height).output(System.out);
    }

    private SVGElement makeMarker(Series.Shape p, Color c, double x, double y, double m) {

        SVGElement marker;

        switch (p) {

            case TRIANGLE:

                marker = new SVGTriangle(x, y, m)
                    .setStrokeColour(c)
                    .setFillColour(Color.WHITE)
                    .setStrokeWidth(2);
                break;

            case DASH:

                marker = new SVGLine(x - m, y, x + m, y)
                    .setStrokeColour(c)
                    .setStrokeWidth(2);
                break;

            default:
            case CIRCLE:
            case DOT:

                marker = new SVGCircle(x, y, m)
                    .setStrokeColour(c)
                    .setFillColour(p == Series.Shape.CIRCLE ? Color.WHITE : c)
                    .setStrokeWidth(2);
                break;

            case SQUARE:
            case DIAMOND:

                marker = new SVGSquare(x, y, m)
                    .setStrokeColour(c)
                    .setFillColour(Color.WHITE)
                    .setStrokeWidth(2);

                if (p == Series.Shape.DIAMOND) {
                    marker.setAttribute("transform", "rotate(45 " + x + " " + y + ")");
                }

                break;

            case CROSS:

                marker = new SVGCross(x, y, m)
                    .setStrokeColour(c)
                    .setFillColour(c)
                    .setStrokeWidth(1);

                break;


        }

        return marker;

    }

    public void saveTex(String path) throws IOException {

        StringBuilder builder = new StringBuilder();
        builder.append("\\begin{tikzpicture}\n");


        builder.append("\\begin{axis}[\n").append("\txmode                   = ").append(xAxis.isLogAxis() ? "log" : "normal").append(",\n").append("\tymode                   = ").append(yAxis.isLogAxis() ? "log" : "normal").append(",\n");

        if (!xAxis.isAutoRanging()) {
            builder.append("\txmin                    = ").append(xAxis.getMin()).append(",\n").append("\txmax                    = ").append(xAxis.getMax()).append(",\n");
        }
        if (!yAxis.isAutoRanging()) {
            builder.append("\tymin                    = ").append(yAxis.getMin()).append(",\n").append("\tymax                    = ").append(yAxis.getMax()).append(",\n");
        }

        builder.append("\tgrid,\n").append("\tgrid style              = {dotted},\n").append("\tlegend pos              = outer north east,\n").append("\twidth                   = 0.7 * \\linewidth,\n").append("\ttitle                   = {\\textbf{").append(getTitle().replace("^", "\\^{}")).append("}},\n").append("\txlabel                  = {").append(xAxis.getAxisLabel().getText().replace("^", "\\^{}")).append("},\n").append("\tylabel                  = {").append(yAxis.getAxisLabel().getText().replace("^", "\\^{}")).append("},\n").append("\tlegend cell align       = left,\n").append("\tevery axis title/.style = {at={(0.5, 1.2)}}\n").append("]\n");

        List<String> legend = new LinkedList<>();

        for (int i = 0; i < chart.getDatasets().size(); i++) {

            JISAErrorDataSet series = (JISAErrorDataSet) chart.getDatasets().get(i);

            String symbol = "*";
            String fill   = "white";

            if (!series.isMarkerVisible()) {symbol = "none";}

            String onlyMarks = series.isLineVisible() ? "" : "only marks,\n";

            String lineType;

            switch (series.getDash()) {

                case DOTTED:
                    lineType = ",\n\tdotted";
                    break;

                case DASHED:
                case TWO_DASH:
                case DOT_DASH:
                case LONG_DASH:
                    lineType = ",\n\tdashed";
                    break;

                default:
                case SOLID:
                    lineType = "";
                    break;

            }


            int red   = (int) (series.getColour().getRed() * 255);
            int green = (int) (series.getColour().getGreen() * 255);
            int blue  = (int) (series.getColour().getBlue() * 255);

            builder.append("\\addplot[\n");

            if (!series.isLineVisible()) {builder.append("\tonly marks,\n");}

            builder.append("\tmark               = ").append(symbol).append(",\n").append("\tmark options       = { fill = ").append(fill).append(", scale=1.25 },\n").append("\tcolor              = {rgb,255:red,").append(red).append(";green,").append(green).append(";blue,").append(blue).append("},\n").append("\tline width         = ").append(series.getThickness() / 2.0).append(lineType).append(",\n").append("\terror bars/.cd,\n").append("\ty dir              = both,\n").append("\ty explicit,\n").append("\terror bar style    = { line width = ").append(series.getThickness() / 2.0).append(" },\n").append("\terror mark options = { rotate = 90, mark size = 3, line width = ").append(series.getThickness() / 2.0).append(" },\n").append("]\n").append("table[x index = 0, y index = 1, y error index = 2] {\n");


            for (int j = 0; j < series.getDataCount(); j++) {
                builder.append(String.format("\t%.04e\t%.04e\t%.04e\n", series.getX(j), series.getY(j), series.getErrorPositive(1, j) > 0 ? series.getErrorPositive(1, j) * 2.0 : Double.NaN));
            }

            builder.append("\n};\n");

            legend.add("\t{" + series.getName().replace("^", "\\^{}") + "}");

//            if (series.isFitted()) {
//
//                Function fit = series.getFit().getFunction();
//
//                builder.append("\\addplot[\n")
//                       .append("\tmark               = none,\n")
//                       .append("\tcolor              = {rgb,255:red,").append(red).append(";green,").append(green).append(";blue,").append(blue).append("},\n")
//                       .append("\tline width         = ").append(series.getLineWidth() / 2.0)
//                       .append(lineType).append(",\n")
//                       .append("\tforget plot\n")
//                       .append("]\n")
//                       .append("table[x index = 0, y index = 1] {\n");
//
//                for (double x : Range.linear(getXLowerLimit(), getXUpperLimit(), 100)) {
//                    builder.append(String.format("\t%.04e\t%.04e\n", x, fit.value(x)));
//                }
//
//                builder.append("\n};\n");
//
//            }

        }

        if (chart.isLegendVisible()) {
            builder.append(String.format("\\legend{\n%s\n}\n", String.join(",\n", legend)));
        }

        builder.append("\\end{axis}\n");

        builder.append("\\end{tikzpicture}");

        FileOutputStream writer = new FileOutputStream(path);
        PrintStream      stream = new PrintStream(writer);

        stream.print(builder.toString());

        stream.close();

    }

    public ClickListener addClickListener(ClickListener listener) {
        clickListeners.add(listener);
        return listener;
    }

    public void removeClickListener(ClickListener listener) {
        clickListeners.remove(listener);
    }

    public enum AxisType {
        LINEAR,
        LOGARITHMIC,
        TIME
    }

    public interface ClickListener {

        void clicked(MouseEvent event, double x, double y) throws Exception;

        default void runRegardless(MouseEvent event, double x, double y) {
            Util.runRegardless(() -> clicked(event, x, y));
        }

    }

}
