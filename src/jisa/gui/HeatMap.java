package jisa.gui;

import com.google.common.primitives.Doubles;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import jisa.maths.Range;
import jisa.maths.functions.GFunction;
import jisa.maths.matrices.Matrix;
import jisa.results.ResultTable;
import jisa.results.RowEvaluable;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HeatMap extends JFXElement {

    private static final double AXIS_SIZE  = 50;
    private static final double TITLE_SIZE = 33;
    private static final double CBAR_SIZE  = 100;
    private static final double CBAR_GAP   = 10;

    private final CanvasPane      pane;
    private final Canvas          canvas;
    private final GraphicsContext gc;

    private double[][]                 lastData  = new double[0][0];
    private ColourMap                  colourMap = ColourMap.MATPLOTLIB;
    private GFunction<String, Integer> xMapper   = x -> String.format("%d", x);
    private GFunction<String, Integer> yMapper   = y -> String.format("%d", y);

    public HeatMap(String title) {

        super(title, new CanvasPane(500, 500));

        setMinHeight(500);
        setMinWidth(500);
        setWindowSize(600, 525);

        GUI.runNow(() -> getStage().getScene().setFill(Color.WHITE));

        pane   = (CanvasPane) getNode().getCenter();
        canvas = pane.canvas;
        gc     = canvas.getGraphicsContext2D();

        canvas.widthProperty().addListener((observable, oldValue, newValue) -> draw(lastData));
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> draw(lastData));

    }

    public void setTitle(String title) {

        GUI.runNow(() -> {
            super.setTitle(title);
            draw(lastData);
        });

    }

    public void setColourMap(ColourMap colourMap) {
        this.colourMap = colourMap;
        draw(lastData);
    }

    public HeatMap(String title, double[][] data) {
        this(title);
        draw(data);
    }

    public HeatMap(String title, Double[][] data) {
        this(title);
        draw(data);
    }

    public HeatMap(String title, Matrix<Double> data) {
        this(title);
        draw(data);
    }

    public HeatMap(String title, Collection<Collection<Double>> data) {
        this(title);
        draw(data);
    }

    public void draw(Matrix<Double> data) {
        draw(data.getData());
    }

    public void draw(Double[][] data) {
        draw(Stream.of(data).map(v -> Stream.of(v).mapToDouble(Double::doubleValue).toArray()).toArray(double[][]::new));
    }

    public void drawMesh(double[] xValues, double[] yValues, double[] values) {

        double[]   x    = DoubleStream.of(xValues).distinct().sorted().toArray();
        double[]   y    = DoubleStream.of(yValues).distinct().sorted().toArray();
        double[][] data = new double[y.length][x.length];

        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < x.length; j++) {
                data[i][j] = 0.0;
            }
        }

        IntStream.range(0, values.length).parallel().forEach(i -> {

            int ix = Arrays.binarySearch(x, xValues[i]);
            int iy = Arrays.binarySearch(y, yValues[i]);

            data[iy][ix] = values[i];

        });

        draw(data);
        setXTicks(i -> { try { return String.format("%.02g", x[i]); } catch (Throwable e) { return null; } } );
        setYTicks(i -> { try { return String.format("%.02g", y[i]); } catch (Throwable e) { return null; } } );

    }

    public void drawMesh(Collection<? extends Number> xValues, Collection<? extends Number> yValues, Collection<? extends Number> values) {

        drawMesh(
            Doubles.toArray(xValues),
            Doubles.toArray(yValues),
            Doubles.toArray(values)
        );

    }

    public void watch(ResultTable table, RowEvaluable<? extends Number> x, RowEvaluable<? extends Number> y, RowEvaluable<? extends Number> v) {
        drawMesh(table.get(x), table.get(y), table.get(v));
        table.addRowListener(row -> drawMesh(table.get(x), table.get(y), table.get(v)));
        table.addClearListener(this::clear);
    }

    public void draw(Collection<Collection<Double>> data) {

        double[][] converted = new double[data.size()][];

        int i = 0;
        for (Collection<Double> row : data) {
            converted[i++] = Doubles.toArray(row);
        }

        draw(converted);

    }

    public void setXTicks(GFunction<String, Integer> tickMapper) {
        xMapper = tickMapper;
        draw(lastData);
    }

    public void setYTicks(GFunction<String, Integer> tickMapper) {
        yMapper = tickMapper;
        draw(lastData);
    }

    public void setXTicks(String... values) {
        setXTicks(i -> values[i]);
    }

    public void setYTicks(String... values) {
        setYTicks(i -> values[i]);
    }

    public void setXTicks(Number... values) {
        setXTicks(Stream.of(values).map(Number::toString).toArray(String[]::new));
    }

    public void setYTicks(Number... values) {
        setYTicks(Stream.of(values).map(Number::toString).toArray(String[]::new));
    }

    public void draw(double[][] data) {

        lastData = data;

        GUI.runNow(() -> {


            final int ny       = data.length;
            final int nx       = ny > 0 ? data[0].length : 0;
            double    maxTickX = IntStream.range(0, nx).mapToObj(i -> new Text(xMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getWidth()).max().orElse(0.0);
            double    maxTickY = IntStream.range(0, ny).mapToObj(i -> new Text(yMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getHeight()).max().orElse(0.0);
            double    maxTickW = IntStream.range(0, ny).mapToObj(i -> new Text(yMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getWidth()).max().orElse(0.0);

            // Calculate co-ordinates of elements
            final double height  = canvas.getHeight();
            final double width   = canvas.getWidth();
            final double min     = Stream.of(data).flatMapToDouble(DoubleStream::of).min().orElse(-1.0);
            final double max     = Stream.of(data).flatMapToDouble(DoubleStream::of).max().orElse(1.0);
            final double pStartX = Math.max(AXIS_SIZE, maxTickW + 15.0);
            final double pStartY = 2 * TITLE_SIZE;
            final double pEndX   = width - CBAR_GAP - CBAR_SIZE;
            final double pEndY   = height - AXIS_SIZE;
            final double pWidth  = pEndX - pStartX;
            final double pHeight = pEndY - pStartY;
            final double cStartX = pEndX + CBAR_GAP;
            final double cStartY = pStartY;
            final double cEndY   = pStartY + pHeight;

            // Draw the title
            gc.clearRect(0, 0, width, height);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 22));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(getTitle(), (pStartX + pEndX) / 2.0, TITLE_SIZE);

            // Draw empty plotting area
            gc.setStroke(Color.BLACK);
            gc.setFill(colourMap.value(0.0));
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeRect(pStartX, pStartY, pWidth, pHeight);
            gc.fillRect(pStartX, pStartY, pWidth, pHeight);

            double px = Math.ceil(pWidth / nx);
            double py = Math.ceil(pHeight / ny);

            // Draw pixels
            for (int y = 0; y < ny; y++) {

                double yv = Math.floor(pStartY + ((double) y / ny) * pHeight);

                for (int x = 0; x < nx; x++) {

                    double xv = Math.floor(pStartX + ((double) x / nx) * pWidth);
                    double vv = (data[y][x] - min) / (max - min);

                    gc.setFill(colourMap.value(vv));
                    gc.fillRect(xv, yv, px, py);

                }

            }

            gc.setFill(Color.BLACK);
            gc.setStroke(Color.BLACK);
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFont(Font.getDefault());

            double i = 10;
            for (double y : Range.linear(cStartY, cEndY, 11)) {
                gc.strokeLine(cStartX + 26, y, cStartX + 30, y);
                gc.fillText(String.format("%.02e", min + (i-- / 10.0) * (max - min)), cStartX + 40, y);
            }

            // Draw Colour Bar
            gc.setStroke(Color.BLACK);
            gc.strokeRect(cStartX, cStartY, 25, pHeight);

            for (double y = cStartY; y < cEndY; y++) {

                double vv = 1.0 - ((y - cStartY) / pHeight);
                gc.setFill(colourMap.value(vv));
                gc.fillRect(cStartX, y, 25, 1);

            }

            double stepX = pWidth / nx;
            double stepY = pHeight / ny;

            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.TOP);
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.getDefault());

            int everyX = (int) Math.ceil((maxTickX + 10.0) / stepX);
            int everyY = (int) Math.ceil((maxTickY + 10.0) / stepY);

            // Draw x-axis ticks
            int j = 0;
            for (double x = pStartX + 0.5 * stepX; x < pEndX; x += stepX) {

                if ((j % everyX) == 0) {

                    String tick = xMapper.value(j);

                    if (tick != null && !tick.isBlank()) {
                        gc.strokeLine(x, pEndY + 1, x, pEndY + 5);
                        gc.fillText(tick, x, pEndY + 10);
                    }

                }

                j++;

            }

            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setTextBaseline(VPos.CENTER);
            gc.setStroke(Color.BLACK);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.getDefault());

            // Draw y-axis ticks
            j = 0;
            for (double y = pStartY + 0.5 * stepY; y < pEndY; y += stepY) {

                if ((j % everyY) == 0) {

                    String tick = yMapper.value(j);

                    if (tick != null && !tick.isBlank()) {
                        gc.strokeLine(pStartX - 1, y, pStartX - 5, y);
                        gc.fillText(tick, pStartX - 10, y);
                    }

                }

                j++;

            }

        });

    }

    public void clear() {
        draw(new double[0][0]);
    }

    private static class CanvasPane extends Pane {

        final Canvas canvas;

        CanvasPane(double width, double height) {

            setWidth(width);
            setHeight(height);

            canvas = new Canvas(width, height);

            getChildren().add(canvas);

            canvas.widthProperty().bind(this.widthProperty());
            canvas.heightProperty().bind(this.heightProperty());

        }

    }

    public interface ColourMap {

        ColourMap JISA       = vv -> Color.hsb(330 - 234 * vv, 0.8, 0.3 + 0.68 * vv, 1.0);
        ColourMap MATPLOTLIB = vv -> Color.hsb(288 - 234 * vv, 0.8, 0.3 + 0.68 * vv, 1.0);
        ColourMap GREYSCALE  = Color::gray;
        ColourMap GAYSCALE   = vv -> Color.hsb(300 * vv, 1.0, 1.0, 1.0);
        ColourMap RED        = vv -> Color.hsb(0, 1.0, vv, 1.0);
        ColourMap GREEN      = vv -> Color.hsb(120, 1.0, vv, 1.0);
        ColourMap BLUE       = vv -> Color.hsb(240, 1.0, vv, 1.0);
        ColourMap FERAL      = vv -> Color.color(Math.min(1.0, 2 * vv), 1.0 - (2.0 * Math.abs(vv - 0.5)), Math.min(1.0, 2 * (1 - vv)));

        Color value(double value);

    }

}
