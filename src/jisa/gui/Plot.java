package jisa.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;
import jisa.Util;
import jisa.experiment.ResultTable;
import jisa.gui.svg.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Plot extends JFXWindow implements Element, Clearable {

    public  BorderPane   pane;
    public  ToolBar      toolbar;
    public  Pane         stack;
    public  JISAChart    chart;
    public  SmartAxis    xAxis;
    public  SmartAxis    yAxis;
    public  ToggleButton autoButton;
    public  ToggleButton zoomButton;
    public  ToggleButton dragButton;
    public  HBox         sliderBox;
    public  Slider       rangeSliderX;
    private Rectangle    rect;
    private Series       autoSeries = null;

    /**
     * Creates an empty plot from the given title, x-axis label, and y-axis label.
     *
     * @param title  Title of the plot
     * @param xLabel X-Axis Label
     * @param yLabel Y-Axis Lable
     */
    public Plot(String title, String xLabel, String yLabel) {

        super(title, Plot.class.getResource("fxml/PlotWindow.fxml"));

        chart = new JISAChart();
        chart.setMinHeight(400);
        xAxis = (SmartAxis) chart.getXAxis();
        yAxis = (SmartAxis) chart.getYAxis();

        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);
        BorderPane.setAlignment(chart, Pos.CENTER);

        chart.setLegendSide(Side.RIGHT);
        chart.setAnimated(true);
        chart.setMinHeight(400.0);

        stack.getChildren().add(chart);

        GUI.runNow(() -> {
            chart.setStyle("-fx-background-color: white;");
            xAxis.setLabelText(xLabel);
            yAxis.setLabelText(yLabel);
            chart.setTitle(title);
            xAxis.setForceZeroInRange(false);
            yAxis.setForceZeroInRange(false);
            xAxis.setAnimated(false);
            yAxis.setAnimated(false);
            xAxis.setAutoRanging(true);
            yAxis.setAutoRanging(true);
            rect = new Rectangle();
            rect.setFill(Color.CORNFLOWERBLUE.deriveColor(0, 1, 1, 0.5));

            rect.setVisible(false);
            rect.setManaged(false);

            AnchorPane canvas = new AnchorPane();
            canvas.setStyle(
                "-fx-background-color: transparent; -fx-border-color: black; -fx-border-style: solid; -fx-border-width: 5px;");
            canvas.setMouseTransparent(true);
            canvas.getChildren().add(rect);
            canvas.setManaged(false);
            stack.getChildren().add(canvas);
            toolbar.setVisible(false);
            toolbar.setManaged(false);
            rangeSliderX.setValue(100.0);
            sliderBox.setVisible(false);
            sliderBox.setManaged(false);

        });

        rangeSliderX.valueProperty().addListener((a, b, c) -> {

            double value = rangeSliderX.getValue() / 100.0;

            if (value >= 1.0) {
                autoXLimits();
            } else {

                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;

                for (XYChart.Series<Double, Double> series : chart.getData()) {

                    for (XYChart.Data<Double, Double> point : series.getData()) {

                        min = Math.min(point.getXValue(), min);
                        max = Math.max(point.getXValue(), max);

                    }

                }

                double range = Math.abs(max - min) * value;

                setXAutoTrack(range);

            }

        });

    }

    public Plot(String title, String xLabel) {

        this(title, xLabel, "");
    }

    public Plot(String title) {

        this(title, "", "");
    }

    public Plot(String title, ResultTable toWatch) {

        this(title);
        autoSeries = createSeries().watchAll(toWatch);

    }

    public Plot(String title, ResultTable toWatch, int xData) {

        this(title);
        autoSeries = createSeries().watchAll(toWatch, xData);

    }


    public Plot(String title, ResultTable toWatch, int xData, int yData) {

        this(title);
        autoSeries = createSeries().watch(toWatch, xData, yData);

    }


    public Plot(String title, ResultTable toWatch, int xData, int yData, int sData) {

        this(title);
        autoSeries = createSeries().watch(toWatch, xData, yData).split(sData);

    }

    public void setYAxisType(AxisType type) {

        switch (type) {

            case LINEAR:
                yAxis.setMode(SmartAxis.Mode.LINEAR);
                break;

            case LOGARITHMIC:
                yAxis.setMode(SmartAxis.Mode.LOGARITHMIC);
                break;

        }

    }

    public void setXAxisType(AxisType type) {

        switch (type) {

            case LINEAR:
                xAxis.setMode(SmartAxis.Mode.LINEAR);
                break;

            case LOGARITHMIC:
                xAxis.setMode(SmartAxis.Mode.LOGARITHMIC);
                break;

        }

    }

    public void setPointOrdering(Sort ordering) {
        chart.setAxisSortingPolicy(ordering);
    }

    public Series createSeries() {
        return chart.createSeries();
    }

    /**
     * Returns the series object of any automatically generated series from the Plot constructor
     *
     * @return Automatically generated series, null if there is none
     */
    public Series getAutoSeries() {

        return autoSeries;
    }

    public void showToolbar(boolean flag) {

        GUI.runNow(() -> {

            toolbar.setManaged(flag);
            toolbar.setVisible(flag);
            adjustSize();

        });

    }

    public void showSlider(boolean flag) {

        GUI.runNow(() -> {

            if (!flag) {
                rangeSliderX.setValue(100.0);
            }

            sliderBox.setVisible(flag);
            sliderBox.setManaged(flag);

            adjustSize();

        });

    }

    public String getXLabel() {
        return xAxis.getLabelText();
    }

    public void setXLabel(String label) {
        xAxis.setLabelText(label);
    }

    public String getYLabel() {
        return yAxis.getLabelText();
    }

    public void setYLabel(String label) {
        yAxis.setLabelText(label);
    }

    public void setZoomMode() {

        zoomButton.setSelected(true);
        autoButton.setSelected(false);
        dragButton.setSelected(false);

        XYChart.Series<Double, Double> series = new XYChart.Series<>();

        final Node                          background = chart.lookup(".chart-plot-background");
        final SimpleObjectProperty<Point2D> start      = new SimpleObjectProperty<>();
        final SimpleObjectProperty<Point2D> first      = new SimpleObjectProperty<>();

        chart.setOnMousePressed(event -> {
            Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            start.set(new Point2D(event.getX(), event.getY()));
            first.set(new Point2D(pointX.getX(), pointY.getY()));
            rect.setVisible(true);
            rect.setManaged(true);
        });

        chart.setOnMouseDragged(event -> {

            final double x = Math.max(0, Math.min(stack.getWidth(), event.getX()));
            final double y = Math.max(0, Math.min(stack.getHeight(), event.getY()));
            rect.setX(Math.min(x, start.get().getX()));
            rect.setY(Math.min(y, start.get().getY()));
            rect.setWidth(Math.abs(x - start.get().getX()));
            rect.setHeight(Math.abs(y - start.get().getY()));

        });

        chart.setOnMouseReleased(event -> {

            final Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            final Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());

            final double minX = xAxis.getValueForDisplay(Math.min(first.get().getX(), pointX.getX()));
            final double maxX = xAxis.getValueForDisplay(Math.max(first.get().getX(), pointX.getX()));
            final double minY = yAxis.getValueForDisplay(Math.min(first.get().getY(), pointY.getY()));
            final double maxY = yAxis.getValueForDisplay(Math.max(first.get().getY(), pointY.getY()));

            setXLimits(Math.min(minX, maxX), Math.max(minX, maxX));
            setYLimits(Math.min(minY, maxY), Math.max(minY, maxY));

            rect.setWidth(0);
            rect.setHeight(0);
            rect.setVisible(false);
            rect.setManaged(false);

        });

    }

    public void useMouseCommands(boolean flag) {

        if (flag) {

            showToolbar(false);

            XYChart.Series<Double, Double> series = new XYChart.Series<>();

            final Node                          background = chart.lookup(".chart-plot-background");
            final SimpleObjectProperty<Point2D> startZoom  = new SimpleObjectProperty<>();
            final SimpleObjectProperty<Point2D> firstZoom  = new SimpleObjectProperty<>();
            final SimpleObjectProperty<Point2D> start      = new SimpleObjectProperty<>();
            final SimpleObjectProperty<Point2D> startMax   = new SimpleObjectProperty<>();
            final SimpleObjectProperty<Point2D> startMin   = new SimpleObjectProperty<>();


            chart.setOnMousePressed(event -> {

                if (event.isSecondaryButtonDown()) {

                    pane.getScene().setCursor(Cursor.CROSSHAIR);
                    Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
                    Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
                    startZoom.set(new Point2D(event.getX(), event.getY()));
                    firstZoom.set(new Point2D(pointX.getX(), pointY.getY()));
                    rect.setVisible(true);
                    rect.setManaged(true);
                } else if ((event.isPrimaryButtonDown() && event.isControlDown()) || event.isMiddleButtonDown()) {
                    pane.getScene().setCursor(Cursor.MOVE);
                    Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
                    Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
                    startMin.set(new Point2D(xAxis.getLowerBound(), yAxis.getLowerBound()));
                    startMax.set(new Point2D(xAxis.getUpperBound(), yAxis.getUpperBound()));
                    start.set(new Point2D(pointX.getX(), pointY.getY()));
                } else if (event.getClickCount() >= 2) {
                    autoXLimits();
                    autoYLimits();
                }

            });

            chart.setOnMouseDragged(event -> {

                if (event.isSecondaryButtonDown()) {

                    final double x = Math.max(0, Math.min(stack.getWidth(), event.getX()));
                    final double y = Math.max(0, Math.min(stack.getHeight(), event.getY()));
                    rect.setX(Math.min(x, startZoom.get().getX()));
                    rect.setY(Math.min(y, startZoom.get().getY()));
                    rect.setWidth(Math.abs(x - startZoom.get().getX()));
                    rect.setHeight(Math.abs(y - startZoom.get().getY()));

                } else if ((event.isPrimaryButtonDown() && event.isControlDown()) || event.isMiddleButtonDown()) {

                    Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
                    Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());

                    final double diffX = xAxis.getValueForDisplay(pointX.getX()) - xAxis.getValueForDisplay(start.get().getX());
                    final double diffY = yAxis.getValueForDisplay(pointY.getY()) - yAxis.getValueForDisplay(start.get().getY());

                    final double minX = startMin.get().getX() - diffX;
                    final double minY = startMin.get().getY() - diffY;

                    final double maxX = startMax.get().getX() - diffX;
                    final double maxY = startMax.get().getY() - diffY;

                    setXLimits(Math.min(minX, maxX), Math.max(minX, maxX));
                    setYLimits(Math.min(minY, maxY), Math.max(minY, maxY));

                }

            });

            chart.setOnMouseReleased(event -> {

                pane.getScene().setCursor(Cursor.DEFAULT);

                if (rect.isVisible() && rect.getWidth() > 0 && rect.getHeight() > 0) {

                    final Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
                    final Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());

                    final double minX = xAxis.getValueForDisplay(Math.min(firstZoom.get().getX(), pointX.getX()));
                    final double maxX = xAxis.getValueForDisplay(Math.max(firstZoom.get().getX(), pointX.getX()));
                    final double minY = yAxis.getValueForDisplay(Math.min(firstZoom.get().getY(), pointY.getY()));
                    final double maxY = yAxis.getValueForDisplay(Math.max(firstZoom.get().getY(), pointY.getY()));

                    setXLimits(Math.min(minX, maxX), Math.max(minX, maxX));
                    setYLimits(Math.min(minY, maxY), Math.max(minY, maxY));

                    rect.setWidth(0);
                    rect.setHeight(0);
                    rect.setVisible(false);
                    rect.setManaged(false);

                } else if (rect.isVisible()) {
                    rect.setWidth(0);
                    rect.setHeight(0);
                    rect.setVisible(false);
                    rect.setManaged(false);
                }

            });

        } else {

            chart.setOnMousePressed(null);
            chart.setOnMouseDragged(null);
            chart.setOnMouseReleased(null);

        }


    }

    public void setDragMode() {

        zoomButton.setSelected(false);
        autoButton.setSelected(false);
        dragButton.setSelected(true);

        final Node                          background = chart.lookup(".chart-plot-background");
        final SimpleObjectProperty<Point2D> start      = new SimpleObjectProperty<>();
        final SimpleObjectProperty<Point2D> startMax   = new SimpleObjectProperty<>();
        final SimpleObjectProperty<Point2D> startMin   = new SimpleObjectProperty<>();


        chart.setOnMousePressed(event -> {
            Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            startMin.set(new Point2D(xAxis.getLowerBound(), yAxis.getLowerBound()));
            startMax.set(new Point2D(xAxis.getUpperBound(), yAxis.getUpperBound()));
            start.set(new Point2D(pointX.getX(), pointY.getY()));
        });

        chart.setOnMouseDragged(event -> {

            Point2D pointX = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            Point2D pointY = yAxis.sceneToLocal(event.getSceneX(), event.getSceneY());

            final double diffX = xAxis.getValueForDisplay(pointX.getX()) - xAxis.getValueForDisplay(start.get().getX());
            final double diffY = yAxis.getValueForDisplay(pointY.getY()) - yAxis.getValueForDisplay(start.get().getY());

            final double minX = startMin.get().getX() - diffX;
            final double minY = startMin.get().getY() - diffY;

            final double maxX = startMax.get().getX() - diffX;
            final double maxY = startMax.get().getY() - diffY;

            setXLimits(Math.min(minX, maxX), Math.max(minX, maxX));
            setYLimits(Math.min(minY, maxY), Math.max(minY, maxY));

        });

        chart.setOnMouseReleased(null);

    }

    public void setAutoMode() {

        zoomButton.setSelected(false);
        autoButton.setSelected(true);
        dragButton.setSelected(false);

        chart.setOnMousePressed(null);
        chart.setOnMouseDragged(null);
        chart.setOnMouseReleased(null);

        autoXLimits();
        autoYLimits();

    }

    /**
     * Sets the bounds on the x-axis.
     *
     * @param min Minimum value to show on x-axis
     * @param max Maximum value to show on x-axis
     */
    public void setXLimits(final double min, final double max) {

        GUI.runNow(() -> {
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(min);
            xAxis.setUpperBound(max);
        });

    }

    /**
     * Sets the bounds on the y-axis.
     *
     * @param min Minimum value to show on y-axis
     * @param max Maximum value to show on y-axis
     */
    public void setYLimits(final double min, final double max) {

        GUI.runNow(() -> {
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(min);
            yAxis.setUpperBound(max);
        });
    }

    public void setLegendPosition(Side position) {
        GUI.runNow(() -> chart.setLegendSide(position));
    }

    public void showLegend(boolean show) {

        GUI.runNow(() -> chart.setLegendVisible(show));
    }

    /**
     * Sets the x-axis to automatically choose its bounds.
     */
    public void autoXLimits() {
        GUI.runNow(() -> {
            xAxis.setMaxRange(Double.POSITIVE_INFINITY);
            xAxis.setAutoRanging(true);
        });
    }

    /**
     * Sets the y-axis to automatically choose its bounds.
     */
    public void autoYLimits() {
        GUI.runNow(() -> {
            yAxis.setMaxRange(Double.POSITIVE_INFINITY);
            yAxis.setAutoRanging(true);
        });
    }

    public void setXAutoTrack(double range) {
        GUI.runNow(() -> {
            xAxis.setMaxRange(range);
            xAxis.setAutoRanging(true);
        });
    }

    public void setYAutoTrack(double range) {
        GUI.runNow(() -> {
            yAxis.setMaxRange(range);
            yAxis.setAutoRanging(true);
        });
    }

    public void show() {

        super.show();
        adjustSize();
    }

    public void savePNG(String path, double w, double h) {

        Util.sleep(500);

        double height = stage.getHeight();
        double width  = stage.getWidth();

        GUI.runNow(() -> {

            stage.setWidth(w);
            stage.setHeight(h);

        });

        Util.sleep(100);

        GUI.runNow(() -> {

            WritableImage image = chart.snapshot(new SnapshotParameters(), null);
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File(path));
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        Util.sleep(100);

        GUI.runNow(() -> {
            stage.setWidth(width);
            stage.setHeight(height);
        });

    }

    public void saveSVG(String fileName, double width, double height) throws IOException {

        SVGElement main = new SVGElement("g");

        main.setAttribute("font-family", "sans-serif")
            .setAttribute("font-size", 12);

        double aStartX = 100.0;
        double aStartY = height + 65.0;
        double aEndX   = 100.0 + width;
        double aEndY   = 65.0;

        SVGLine xAxis = new SVGLine(aStartX - 0.5, aStartY, aEndX, aStartY);
        SVGLine yAxis = new SVGLine(aStartX, aStartY + 0.5, aStartX, aEndY);

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

        xAxis.setStrokeWidth(1)
             .setStrokeColour(Color.GREY);

        yAxis.setStrokeWidth(1)
             .setStrokeColour(Color.GREY);

        List<Double> xTicks = this.xAxis.getMajorTicks();
        List<Double> yTicks = this.yAxis.getMajorTicks();

        double xScale = (aEndX - aStartX) / this.xAxis.getWidth();
        double yScale = (aEndY - aStartY) / this.yAxis.getHeight();

        StringConverter<Double> formatterX = this.xAxis.getTickLabelFormatter();
        StringConverter<Double> formatterY = this.yAxis.getTickLabelFormatter();

        for (Double x : xTicks) {

            double pos = xScale * this.xAxis.getDisplayPosition(x) + aStartX;

            if (!Util.isBetween(pos, aStartX, aEndX)) {
                continue;
            }

            SVGLine tick = new SVGLine(pos, aStartY, pos, aStartY + 10);

            tick.setStrokeWidth(1)
                .setStrokeColour(Colour.GREY);

            SVGLine grid = new SVGLine(pos, aStartY, pos, aEndY);

            grid.setStrokeWidth(0.5)
                .setStrokeColour(Colour.SILVER)
                .setDash("5", "5");

            main.add(tick);
            main.add(grid);

            SVGText label = new SVGText(pos, aStartY + 26.0, "middle", formatterX.toString(x));
            main.add(label);

        }

        SVGText xLabel = new SVGText((aEndX + aStartX) / 2, aStartY + 75.0, "middle", this.xAxis.getLabel());
        xLabel.setAttribute("font-size", "16px");
        main.add(xLabel);

        for (Double y : yTicks) {

            double pos = aEndY - yScale * this.yAxis.getDisplayPosition(y);

            if (!Util.isBetween(pos, aEndY, aStartY)) {
                continue;
            }

            SVGLine tick = new SVGLine(aStartX, pos, aStartX - 10, pos);

            tick.setStrokeWidth(1)
                .setStrokeColour(Colour.GREY);
            SVGLine grid = new SVGLine(aStartX, pos, aEndX, pos);

            grid.setStrokeWidth(0.5)
                .setStrokeColour(Colour.SILVER)
                .setDash("5", "5");
            main.add(tick);
            main.add(grid);

            SVGText label = new SVGText(aStartX - 12.0, pos + 4.0, "end", formatterY.toString(y));
            main.add(label);

        }

        SVGText yLabel = new SVGText(aStartX - 75.0, (aEndY + aStartY) / 2, "middle", this.yAxis.getLabel());
        yLabel.setAttribute("transform", String.format("rotate(-90 %s %s)", aStartX - 75.0, (aEndY + aStartY) / 2))
              .setAttribute("font-size", "16px");
        main.add(yLabel);

        main.add(xAxis);
        main.add(yAxis);

        SVGElement legend = new SVGElement("rect");

        legend.setStrokeWidth(1.0)
              .setStrokeColour(Color.SILVER)
              .setFillColour(Color.web("#f5f5f5"));


        double legendH = (chart.getData().size() * 25) + 5.0;
        double legendX = aEndX + 25.0;
        double legendY = ((aEndY + aStartY) / 2) - (legendH / 2);

        double legendW = 0.0;

        for (XYChart.Series s : chart.getData()) {
            legendW = Math.max(legendW, (10.0 * s.getName().length()) + 15.0 + 5 + 3 + 20.0);
        }

        legend.setAttribute("x", legendX)
              .setAttribute("y", legendY)
              .setAttribute("width", legendW)
              .setAttribute("height", legendH)
              .setAttribute("rx", 5)
              .setAttribute("ry", 5);

        if (chart.isLegendVisible()) {
            main.add(legend);
        } else {
            legendW = 0;
        }

        int i = 0;
        for (Series s : chart.getSeries()) {

            Color  c = s.getColour();
            double w = s.getLineWidth();

            List<String> terms = new LinkedList<>();

            SVGCircle legendCircle = new SVGCircle(legendX + 15.0, legendY + (25 * i) + 15.0, 5);

            legendCircle.setStrokeColour(c)
                        .setFillColour(Color.WHITE)
                        .setStrokeWidth(3);


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

            for (XYChart.Data<Double, Double> point : s.getXYChartSeries().getData()) {

                double x = aStartX + xScale * this.xAxis.getDisplayPosition(point.getXValue());
                double y = aEndY - yScale * this.yAxis.getDisplayPosition(point.getYValue());

                if (!Util.isBetween(x, aStartX, aEndX) || !Util.isBetween(y, aEndY, aStartY)) {
                    continue;
                }

                terms.add(String.format("%s%s %s", first ? "M" : "L", x, y));

                if (s.isShowingMarkers()) {

                    SVGCircle circle = new SVGCircle(x, y, 5);

                    circle.setStrokeColour(c)
                          .setFillColour(Color.WHITE)
                          .setStrokeWidth(3);

                    list.add(circle);

                }

                first = false;

            }

            SVGPath path = new SVGPath(String.join(" ", terms));
            path.setAttribute("clip-path", "url(#lineClip)");

            path.setStrokeColour(c)
                .setStrokeWidth(w)
                .setStyle("fill", "none");

            main.add(path);

            list.forEach(main::add);

            i++;

        }


        SVG svg = new SVG(width + legendW + 50.0 + 100.0, height + 60.0 + 100.0);
        svg.add(main);

        svg.output(fileName);


    }

    public void saveSVG(String path) throws IOException {

        saveSVG(path, 600, 500);
    }

    private void adjustSize() {

        GUI.runNow(() -> {

            double width  = stage.getWidth();
            double height = stage.getHeight();

            stage.setMinWidth(0.0);
            stage.setMinHeight(0.0);

            stage.sizeToScene();
            stage.setMinHeight(stage.getHeight());
            stage.setMinWidth(stage.getWidth());
            stage.setHeight(height);
            stage.setWidth(width);

        });

    }

    @Override
    public Pane getPane() {

        return pane;
    }


    @Override
    public synchronized void clear() {
        GUI.runNow(() -> chart.getData().clear());
    }

    public enum AxisType {
        LINEAR,
        LOGARITHMIC
    }

    public enum Sort {
        X_AXIS,
        Y_AXIS,
        ORDER_ADDED
    }

}
