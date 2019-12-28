package jisa.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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
import jisa.maths.functions.Function;
import jisa.experiment.ResultTable;
import jisa.gui.svg.*;
import jisa.maths.fits.Fit;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Plot extends JFXWindow implements Element, Clearable {

    public  BorderPane pane;
    public  ToolBar    toolbar;
    public  Pane       stack;
    public  JISAChart  chart;
    public  SmartAxis  xAxis;
    public  SmartAxis  yAxis;
    public  Button     autoButton;
    public  HBox       sliderBox;
    public  Slider     rangeSliderX;
    private Rectangle  rect;
    private Series     autoSeries = null;

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

            toolbar.getItems().addListener((ListChangeListener<? super Node>) change -> {
                boolean show = !toolbar.getItems().isEmpty();
                toolbar.setVisible(show);
                toolbar.setManaged(show);
            });

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

    /**
     * Sets the scaling type to use for the y-axis.
     *
     * @param type LINEAR or LOGARITHMIC
     */
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

    /**
     * Sets the scaling type to use for the x-axis.
     *
     * @param type LINEAR or LOGARITHMIC
     */
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

    /**
     * Sets how line-segments connecting plotted points should be ordered.
     *
     * @param ordering X_AXIS, Y_AXIS or ORDER_ADDED
     */
    public void setPointOrdering(Sort ordering) {
        chart.setAxisSortingPolicy(ordering);
    }

    /**
     * Creates a new data series to display on the plot.
     *
     * @return Newly created Series object
     */
    public Series createSeries() {
        return chart.createSeries();
    }

    /**
     * Returns the series object of any automatically generated series from the Plot constructor.
     *
     * @return Automatically generated series, null if there is none
     */
    public Series getAutoSeries() {

        return autoSeries;
    }

    /**
     * Adds toolbar button that opens the plot save dialog when clicked.
     *
     * @param text Text to show
     */
    public jisa.gui.Button addSaveButton(String text) {
        return addToolbarButton(text, this::showSaveDialog);
    }

    public jisa.gui.Button addToolbarButton(String text, ClickHandler onClick) {

        Button button = new Button(text);
        button.setOnMouseClicked(mouseEvent -> new Thread(() -> {
            try {
                onClick.click();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start());

        GUI.runNow(() -> toolbar.getItems().add(button));

        return new jisa.gui.Button() {
            @Override
            public void setDisabled(boolean disabled) { GUI.runNow(() -> button.setDisable(disabled)); }

            @Override
            public boolean isDisabled() { return button.isDisabled(); }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    button.setVisible(visible);
                    button.setManaged(visible);
                });

            }

            @Override
            public boolean isVisible() { return button.isVisible(); }

            @Override
            public void setText(String text) { GUI.runNow(() -> button.setText(text)); }

            @Override
            public String getText() { return button.getText(); }

            @Override
            public void setOnClick(ClickHandler onClick) {
                button.setOnMouseClicked(mouseEvent -> new Thread(() -> {
                    try {
                        onClick.click();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start());
            }

            @Override
            public void remove() {
                GUI.runNow(() -> toolbar.getItems().remove(button));
            }

        };

    }

    public jisa.gui.Separator addToolbarSeparator() {

        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();

        GUI.runNow(() -> toolbar.getItems().add(separator));

        return new Separator() {
            @Override
            public void remove() {
                GUI.runNow(() -> toolbar.getItems().remove(separator));
            }

            @Override
            public boolean isVisible() {
                return separator.isVisible();
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> {
                    separator.setVisible(visible);
                    separator.setManaged(visible);
                });
            }
        };

    }

    public void showSaveDialog() {

        Fields         save   = new Fields("Save Plot");
        Field<Integer> format = save.addChoice("Format", "svg", "png");
        Field<Integer> width  = save.addIntegerField("Width", 600);
        Field<Integer> height = save.addIntegerField("Height", 400);
        Field<String>  file   = save.addFileSave("Path");

        save.addButton("Cancel", save::close);

        save.addButton("Save", () -> {

            if (!file.get().trim().equals("")) {

                switch (format.get()) {

                    case 0:
                        saveSVG(file.get(), width.get(), height.get());
                        break;

                    case 1:
                        savePNG(file.get(), width.get(), height.get());
                        break;

                }

                save.close();

            }

        });

        save.show();

    }

    /**
     * Sets whether the x-axis range slider is visible or not.
     *
     * @param flag Visible?
     */
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

    /**
     * Returns the text used for the x-axis label.
     *
     * @return Current x-axis label text
     */
    public String getXLabel() {
        return xAxis.getLabelText();
    }

    /**
     * Sets the text used for the x-axis label.
     *
     * @param label New x-axis label text
     */
    public void setXLabel(String label) {
        xAxis.setLabelText(label);
    }

    /**
     * Returns the text used for the y-axis label.
     *
     * @return Current y-axis label text
     */
    public String getYLabel() {
        return yAxis.getLabelText();
    }

    /**
     * Sets the text used for the y-axis label.
     *
     * @param label New y-axis label text
     */
    public void setYLabel(String label) {
        yAxis.setLabelText(label);
    }


    public void setZoomMode() {

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

    /**
     * Sets whether
     *
     * @param flag
     */
    public void useMouseCommands(boolean flag) {

        if (flag) {

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

        SVGLine xAxisBox = new SVGLine(aStartX - 0.5, aEndY, aEndX, aEndY);
        SVGLine yAxisBox = new SVGLine(aEndX, aStartY + 0.5, aEndX, aEndY);

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

        xAxisBox.setStrokeWidth(1)
             .setStrokeColour(Color.GREY);

        yAxisBox.setStrokeWidth(1)
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
                .setDash("2", "2");

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
                .setDash("2", "2");
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

        main.add(xAxisBox);
        main.add(yAxisBox);

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

            if (!chart.getData().contains(s.getXYChartSeries())) {
                continue;
            }

            Color        c = s.getColour();
            double       w = s.getLineWidth();
            double       m = s.getMarkerSize();
            Series.Shape p = s.getMarkerShape();

            List<String> terms = new LinkedList<>();

            SVGElement legendCircle = makeMarker(s.isShowingMarkers() ? p : Series.Shape.DASH, c, legendX + 15.0, legendY + (25 * i) + 15.0, 5.0);
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

                    if (point.getExtraValue() != null && (double) point.getExtraValue() > 0) {

                        double error = (double) point.getExtraValue();
                        double yp    = aEndY - yScale * this.yAxis.getDisplayPosition(point.getYValue() + error);
                        double yn    = aEndY - yScale * this.yAxis.getDisplayPosition(point.getYValue() - error);
                        double xn    = x - 5;
                        double xp    = x + 5;

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
                .setDash(s.getLineDash().getArray())
                .setStyle("fill", "none");

            if (s.isShowingLine()) {
                main.add(path);
            }

            list.forEach(main::add);

            i++;

        }


        SVG svg = new SVG(width + legendW + 50.0 + 100.0, height + 60.0 + 100.0);
        svg.add(main);

        svg.output(fileName);


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
