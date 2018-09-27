package JISA.GUI;

import JISA.Experiment.ResultList;
import JISA.GUI.FXML.PlotWindow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Plot implements Gridable {

    public  PlotWindow window;
    private String     title;

    public Plot(String title, ResultList list, int xColumn, int yColumn) {
        window = PlotWindow.create(title, list, xColumn, yColumn);
        this.title = title;
    }

    public Plot(String title, ResultList list) {
        this(title, list, 0, 1);
    }

    public Plot(String title, String xLabel, String yLabel) {
        window = PlotWindow.create(title, xLabel, yLabel);
    }

    @Override
    public Pane getPane() {
        return window.getPane();
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    public void close() {
        window.close();
    }

    public int createSeries(String name, Color colour) {
        return window.createSeries(name, colour);
    }

    public void addPoint(int series, double x, double y) {
        window.addPoint(series, x, y);
    }

    public void addPoint(double x, double y) {
        window.addPoint(x, y);
    }

    public void watchList(ResultList list, int xData, int yData, String seriesName, Color colour) {
        window.watchList(list, xData, yData, seriesName, colour);
    }

}
