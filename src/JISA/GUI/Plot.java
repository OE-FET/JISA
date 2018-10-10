package JISA.GUI;

import JISA.Experiment.Result;
import JISA.Experiment.ResultList;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Plot implements Gridable {

    public  BorderPane                                pane;
    private Stage                                     stage;
    private ArrayList<XYChart.Series<Double, Double>> data   = new ArrayList<>();
    public  LineChart                                 chart;
    public  NumberAxis                                xAxis;
    public  NumberAxis                                yAxis;
    private double                                    xRange = 0;
    private double                                    maxX   = Double.NEGATIVE_INFINITY;
    private double                                    minX   = Double.POSITIVE_INFINITY;
    private double                                    maxY   = Double.NEGATIVE_INFINITY;
    private double                                    minY   = Double.POSITIVE_INFINITY;

    public Plot(String title, String xLabel, String yLabel) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/PlotWindow.fxml"));
            loader.setController(this);
            Parent    root  = loader.load();
            Scene     scene = new Scene(root);
            Semaphore s     = new Semaphore(0);
            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                this.stage = stage;
                chart.setStyle("-fx-background-color: white;");
                xAxis.setLabel(xLabel);
                yAxis.setLabel(yLabel);
                chart.setTitle(title);
                xAxis.setForceZeroInRange(false);
                yAxis.setForceZeroInRange(false);
                s.release();
            });
            s.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Plot(String title, ResultList list, int xData, int yData, String seriesName, Color colour) {
        this(title, list.getTitle(xData), list.getTitle(yData));
        watchList(list, xData, yData, seriesName, colour);
    }

    public Plot(String title, ResultList list, int xData, int yData) {
        this(title, list, xData, yData, "Data", Color.RED);
    }

    public Plot(String title, ResultList list) {
        this(title, list, 0 ,1);
    }

    public void setXLimit(final double min, final double max) {
        xAxis.setAutoRanging(false);
        xAxis.setForceZeroInRange(false);
        xAxis.setLowerBound(min);
        xAxis.setUpperBound(max);
    }

    public void setYLimit(final double min, final double max) {
        yAxis.setAutoRanging(false);
        yAxis.setForceZeroInRange(false);
        yAxis.setLowerBound(min);
        yAxis.setUpperBound(max);
    }

    public void autoXLimit() {
        Platform.runLater(() -> {
            xAxis.setAutoRanging(true);
        });
    }

    public void autoYLimit() {
        Platform.runLater(() -> {
            yAxis.setAutoRanging(true);
        });
    }

    public void autoFollow(double xRange) {
        this.xRange = xRange;
    }

    public void watchList(final ResultList list, final int xData, final int yData, String seriesName, Color colour) {

        final int series = createSeries(seriesName, colour);

        list.setOnUpdate(() -> {
            Result r = list.getLastRow();
            addPoint(series, r.get(xData), r.get(yData));
        });

    }

    public int createSeries(String name, Color colour) {

        XYChart.Series<Double, Double> series = new XYChart.Series<>();

        series.setName(name);

        data.add(series);
        int index = data.size() - 1;
        Platform.runLater(() -> {
            chart.getData().add(series);
            chart.setStyle(chart.getStyle().concat(
                    String.format("CHART_COLOR_%d: rgba(%f, %f, %f);", index + 1, colour.getRed() * 255, colour.getGreen() * 255, colour.getBlue() * 255)
            ));
        });
        return index;

    }

    public void addPoint(double x, double y) {

        if (data.size() == 0) {
            createSeries("Data", Color.RED);
        }

        addPoint(data.size() - 1, x, y);

    }

    public void addPoint(int series, double x, double y) {

        Platform.runLater(() -> {
            data.get(series).getData().add(
                    new XYChart.Data<>(x, y)
            );

            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);

            if (xRange > 0) {
                setXLimit(maxX - xRange, maxX);
                autoXLimit();
            } else {
                xAxis.setAutoRanging(true);
                yAxis.setAutoRanging(true);
            }

        });

    }

    public void show() {
        Platform.runLater(() -> {
                    stage.show();
                }
        );
    }

    public void hide() {
        Platform.runLater(() -> {
                    stage.hide();
                }
        );
    }

    public void close() {
        Platform.runLater(() -> {
                    stage.close();
                }
        );
    }

    public Pane getPane() {
        return pane;
    }

    @Override
    public String getTitle() {
        return stage.getTitle();
    }

}
