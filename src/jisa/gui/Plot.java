package jisa.gui;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.dataset.DataSet;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import jisa.Util;
import jisa.gui.plotting.*;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Plot extends JFXElement implements Element, Clearable {

    public static final Pattern UNIT_PATTERN = Pattern.compile("^(.*)[(\\[](.*?)[)\\]]$");


    private       JISAXYChart                     chart;
    private       DefaultNumericAxis              xAxis;
    private       DefaultNumericAxis              yAxis;
    private final ObservableList<Series>          series    = FXCollections.observableArrayList();
    private final Map<Series, ListChangeListener> listeners = new HashMap<>();
    private final JISAZoomer                      zoomer    = new JISAZoomer();

    public Plot(String title, String xLabel, String xUnits, String yLabel, String yUnits) {

        super(title);
        setMinHeight(500);
        setMinWidth(500);

        GUI.runNow(() -> {
            xAxis = new JISAAxis(xLabel, xUnits);
            yAxis = new JISAAxis(yLabel, yUnits);
            xAxis.setAutoRangePadding(0.05);
            yAxis.setAutoRangePadding(0.05);
            xAxis.setForceZeroInRange(false);
            yAxis.setForceZeroInRange(false);
            chart = new JISAXYChart(xAxis, yAxis);
            chart.setTitle(title);
            BorderPane.setMargin(chart, new Insets(0));
            setCentreNode(chart);
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

    }

    public Plot(String title, String xLabel, String yLabel) {
        this(title, "", "", "", "");
        setXLabel(xLabel);
        setYLabel(yLabel);
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
        xAxis.setName(name);
        xAxis.setUnit(units);
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
        xAxis.setUnit(unit);
    }

    public void setYUnit(String unit) {
        yAxis.setUnit(unit);
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
        yAxis.setName(name);
        yAxis.setUnit(units);
    }

    public void setYLabel(String name) {

        Matcher matcher = UNIT_PATTERN.matcher(name);

        if (matcher.matches()) {
            setYLabel(matcher.group(1), matcher.group(2));
        } else {
            setYLabel(name, null);
        }

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

    public void setXAxisLogarithmic(boolean flag, double base) {
        GUI.runNow(() -> {
            xAxis.setLogAxis(true);
            xAxis.setLogarithmBase(base);
        });
    }

    public void setXAxisLogarithmic(boolean flag) {
        setXAxisLogarithmic(flag, 10);
    }

    public boolean isXAxisLogarithmic() {
        return xAxis.isLogAxis();
    }

    public void setYAxisLogarithmic(boolean flag, double base) {
        GUI.runNow(() -> {
            yAxis.setLogAxis(true);
            yAxis.setLogarithmBase(base);
        });
    }

    public void setYAxisLogarithmic(boolean flag) {
        setYAxisLogarithmic(flag, 10);
    }

    public boolean isYAxisLogarithmic() {
        return yAxis.isLogAxis();
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

            if (!file.get().trim().equals("")) {

                switch (format.get()) {

                    case 0:

                        if (!file.get().endsWith(".svg")) {
                            file.set(file.get() + ".svg");
                        }

//                        saveSVG(file.get(), width.get(), height.get());
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

    public void savePNG(String path, double w, double h) {

        Plot plot = new Plot(getTitle(), getXLabel(), getXUnit(), getYLabel(), getYUnit());

        plot.chart.getDatasets().addAll(chart.getDatasets());
        plot.updateLegend();

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

}
