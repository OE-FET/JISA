package jisa.gui;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Shorts;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import jisa.Util;
import jisa.devices.camera.frame.Frame;
import jisa.maths.Range;
import jisa.maths.functions.Function;
import jisa.maths.functions.XYFunction;
import jisa.maths.interpolation.Interpolation;
import jisa.maths.matrices.Matrix;
import jisa.results.ResultTable;
import jisa.results.RowEvaluable;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.*;

public class HeatMap extends JFXElement {

    private static final double AXIS_SIZE  = 50;
    private static final double TITLE_SIZE = 33;
    private static final double CBAR_SIZE  = 100;
    private static final double CBAR_GAP   = 10;

    private static final List<Color> defaults = Arrays.stream(Series.defaultColours, 0, 6).collect(Collectors.toList());
    private static final Function    r        = Interpolation.interpolate1D(Range.linear(0, 1, defaults.size()), defaults.stream().map(c -> c.getRed() * 255).collect(Collectors.toList()));
    private static final Function    g        = Interpolation.interpolate1D(Range.linear(0, 1, defaults.size()), defaults.stream().map(c -> c.getGreen() * 255).collect(Collectors.toList()));
    private static final Function    b        = Interpolation.interpolate1D(Range.linear(0, 1, defaults.size()), defaults.stream().map(c -> c.getBlue() * 255).collect(Collectors.toList()));

    private final CanvasPane             pane;
    private final Canvas                 canvas;
    private final GraphicsContext        gc;
    private       PixelBuffer<IntBuffer> barBuffer = null;
    private       WritableImage          bar       = new WritableImage(1, 100);
    private       PixelBuffer<IntBuffer> buffer    = null;
    private       WritableImage          image     = null;

    private ColourMap  colourMap = ColourMap.JISA;
    private TickMapper xMapper   = TickMapper.DEFAULT;
    private TickMapper yMapper   = TickMapper.DEFAULT;

    private double height   = 500;
    private double width    = 500;
    private double min      = -1;
    private double max      = 1;
    private double pStartX  = AXIS_SIZE;
    private double pStartY  = 2 * TITLE_SIZE;
    private double pEndX    = 0;
    private double pEndY    = 0;
    private double pWidth   = 0;
    private double pHeight  = 0;
    private double cStartX  = 0;
    private double cStartY  = pStartY;
    private double cEndY    = 0;
    private double stepX    = 0;
    private double stepY    = 0;
    private int    everyX   = 1;
    private int    everyY   = 1;
    private int    ny       = 0;
    private int    nx       = 0;
    private double maxTickX = 1.0;
    private double maxTickY = 1.0;
    private double maxTickW = 1.0;

    public HeatMap(String title) {

        super(title, new CanvasPane(500, 500));

        setMinHeight(500);
        setMinWidth(500);
        setWindowSize(600, 525);

        GUI.runNow(() -> getStage().getScene().setFill(Color.WHITE));

        pane     = (CanvasPane) getNode().getCenter();
        canvas   = pane.canvas;
        gc       = canvas.getGraphicsContext2D();
        ny       = 0;
        nx       = 0;
        maxTickX = IntStream.range(0, nx).mapToObj(i -> new Text(xMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getWidth()).max().orElse(1.0);
        maxTickY = IntStream.range(0, ny).mapToObj(i -> new Text(yMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getHeight()).max().orElse(1.0);
        maxTickW = IntStream.range(0, ny).mapToObj(i -> new Text(yMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getWidth()).max().orElse(1.0);
        height   = canvas.getHeight();
        width    = canvas.getWidth();
        pEndX    = width - CBAR_GAP - CBAR_SIZE;
        pEndY    = height - AXIS_SIZE;
        pWidth   = pEndX - pStartX;
        pHeight  = pEndY - pStartY;
        cStartX  = pEndX + CBAR_GAP;
        cEndY    = pStartY + pHeight;
        stepX    = pWidth / nx;
        stepY    = pHeight / ny;
        everyX   = (int) Math.ceil((maxTickX + 10.0) / stepX);
        everyY   = (int) Math.ceil((maxTickY + 10.0) / stepY);

        canvas.widthProperty().addListener((observable, oldValue, newValue) -> resized());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> resized());

        GUI.runNow(() -> {
            drawTitle();
            drawColourBar();
            drawAxes();
            drawMap();
        });

    }

    public void setTitle(String title) {

        GUI.runNow(() -> {
            super.setTitle(title);
            drawTitle();
        });

    }

    public void setColourMap(ColourMap colourMap) {

        this.colourMap = colourMap;

        GUI.runNow(() -> {
            drawColourBar();
            drawMap();
        });

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
        setXTicks(i -> { try { return String.format("%.02g", x[i]); } catch (Throwable e) { return null; } });
        setYTicks(i -> { try { return String.format("%.02g", y[i]); } catch (Throwable e) { return null; } });

    }

    public void drawMesh(Collection<? extends Number> xValues, Collection<? extends Number> yValues, Collection<? extends Number> values) {

        drawMesh(
            Doubles.toArray(xValues),
            Doubles.toArray(yValues),
            Doubles.toArray(values)
        );

    }

    public void drawInterpolate(Collection<? extends Number> xValues, Collection<? extends Number> yValues, Collection<? extends Number> values) {

        XYFunction function = Interpolation.interpolate2D(xValues, yValues, values);

        double[]   x    = xValues.stream().mapToDouble(Number::doubleValue).distinct().sorted().toArray();
        double[]   y    = yValues.stream().mapToDouble(Number::doubleValue).distinct().sorted().toArray();
        double[][] data = new double[y.length][x.length];

        for (int i = 0; i < y.length; i++) {
            for (int j = 0; j < x.length; j++) {
                data[i][j] = function.value(x[i], y[j]);
            }
        }

        draw(data);
        setXTicks(i -> { try { return String.format("%.02g", x[i]); } catch (Throwable e) { return null; } });
        setYTicks(i -> { try { return String.format("%.02g", y[i]); } catch (Throwable e) { return null; } });

    }

    public void drawInterpolate(double[] xValues, double[] yValues, double[] values) {
        drawInterpolate(Doubles.asList(xValues), Doubles.asList(yValues), Doubles.asList(values));
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

    public void setXTicks(TickMapper tickMapper) {
        xMapper = tickMapper;
        updateAxes();
    }

    public void setYTicks(TickMapper tickMapper) {
        yMapper = tickMapper;
        updateAxes();
    }

    public void setXTicks(String... values) {
        setXTicks(TickMapper.fromArray(values));
    }

    public void setYTicks(String... values) {
        setXTicks(TickMapper.fromArray(values));
    }

    public void setXTicks(Number... values) {
        setXTicks(Stream.of(values).map(n -> String.format("%.02g", n.doubleValue())).toArray(String[]::new));
    }

    public void setYTicks(Number... values) {
        setYTicks(Stream.of(values).map(n -> String.format("%.02g", n.doubleValue())).toArray(String[]::new));
    }

    public void setXTicks(Collection<? extends Number> values) {
        setXTicks(values.stream().map(n -> String.format("%.02g", n.doubleValue())).toArray(String[]::new));
    }

    public void setYTicks(Collection<? extends Number> values) {
        setYTicks(values.stream().map(n -> String.format("%.02g", n.doubleValue())).toArray(String[]::new));
    }

    public void draw(int[][] data) {
        draw(Stream.of(data).map(r -> IntStream.of(r).boxed().map(Integer::doubleValue).toArray(Double[]::new)).toArray(Double[][]::new));
    }

    public void draw(long[][] data) {
        draw(Stream.of(data).map(r -> LongStream.of(r).boxed().map(Long::doubleValue).toArray(Double[]::new)).toArray(Double[][]::new));
    }

    public synchronized void draw(double[][] data) {

        if (ny != data.length || nx != (data.length > 0 ? data[0].length : 0)) {

            ny = data.length;
            nx = ny > 0 ? data[0].length : 0;

            updateAxes();

        }

        min = Stream.of(data).mapToDouble(Doubles::min).min().orElse(-1.0);
        max = Stream.of(data).mapToDouble(Doubles::max).max().orElse(1.0);

        double range = max - min;

        if (image == null || image.getWidth() != nx || image.getHeight() != ny) {
            buffer = new PixelBuffer<>(nx, ny, IntBuffer.allocate(nx * ny), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(buffer);
        }

        int[] pixels = buffer.getBuffer().array();

        // Draw pixels
        for (int y = 0; y < ny; y++) {


            for (int x = 0; x < nx; x++) {

                double vv = ((double) data[y][x] - min) / range;
                pixels[(x % nx) + (y * nx)] = colourMap.value(vv);

            }

        }

        GUI.runNow(() -> {
            buffer.updateBuffer(b -> null);
            drawMap();
            drawColourBar();
        });


    }

    public synchronized void draw(short[][] data) {

        if (ny != data.length || nx != (data.length > 0 ? data[0].length : 0)) {

            ny = data.length;
            nx = ny > 0 ? data[0].length : 0;

            updateAxes();

        }

        min = Stream.of(data).mapToDouble(Shorts::min).min().orElse(-1.0);
        max = Stream.of(data).mapToDouble(Shorts::max).max().orElse(1.0);

        double range = max - min;

        if (image == null || image.getWidth() != nx || image.getHeight() != ny) {
            buffer = new PixelBuffer<>(nx, ny, IntBuffer.allocate(nx * ny), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(buffer);
        }

        int[] pixels = buffer.getBuffer().array();

        // Draw pixels
        for (int y = 0; y < ny; y++) {


            for (int x = 0; x < nx; x++) {

                double vv = ((double) data[y][x] - min) / range;
                pixels[(x % nx) + (y * nx)] = colourMap.value(vv);

            }

        }

        GUI.runNow(() -> {

            gc.clearRect(cStartX, cStartY, 25, pHeight);
            gc.clearRect(cStartX + 25, 0, CBAR_SIZE - 25, height);

            buffer.updateBuffer(b -> null);
            drawMap();
            drawColourBar();

        });

    }

    public synchronized void draw(Frame<? extends Number, ?> frame) {

        if (frame instanceof Frame.IntFrame) {
            drawIntFrame((Frame.IntFrame) frame);
            return;
        }

        if (ny != frame.getHeight() || nx != frame.getHeight()) {

            ny = frame.getHeight();
            nx = frame.getWidth();

            updateAxes();

        }

        double[] data = Arrays.stream(frame.getData()).mapToDouble(Number::doubleValue).toArray();

        min = Arrays.stream(data).min().orElse(-1.0);
        max = Arrays.stream(data).max().orElse(+1.0);

        double range = max - min;

        if (image == null || image.getWidth() != nx || image.getHeight() != ny) {
            buffer = new PixelBuffer<>(nx, ny, IntBuffer.allocate(nx * ny), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(buffer);
        }

        int[] pixels = buffer.getBuffer().array();

        // Draw pixels
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = colourMap.value((data[i] - min) / range);
        }

        GUI.runNow(() -> {

            gc.clearRect(cStartX, cStartY, 25, pHeight);
            gc.clearRect(cStartX + 25, 0, CBAR_SIZE - 25, height);

            buffer.updateBuffer(b -> null);
            drawMap();
            drawColourBar();

        });


    }

    public synchronized void drawIntFrame(Frame.IntFrame frame) {

        if (ny != frame.getHeight() || nx != frame.getHeight()) {

            ny = frame.getHeight();
            nx = frame.getWidth();

            updateAxes();

        }

        int[] data = frame.data();

        min = IntStream.of(data).min().orElse(-1);
        max = IntStream.of(data).max().orElse(+1);

        double range = max - min;

        if (image == null || image.getWidth() != nx || image.getHeight() != ny) {
            buffer = new PixelBuffer<>(nx, ny, IntBuffer.allocate(nx * ny), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(buffer);
        }

        int[] pixels = buffer.getBuffer().array();

        // Draw pixels
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = colourMap.value((data[i] - min) / range);
        }


        GUI.runNow(() -> {

            gc.clearRect(cStartX, cStartY, 25, pHeight);
            gc.clearRect(cStartX + 25, 0, CBAR_SIZE - 25, height);

            buffer.updateBuffer(b -> null);
            drawMap();
            drawColourBar();

        });


    }

    private void updateAxes() {

        maxTickX = IntStream.range(0, nx).mapToObj(i -> new Text(xMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getWidth()).max().orElse(0.0);
        maxTickY = IntStream.range(0, ny).mapToObj(i -> new Text(yMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getHeight()).max().orElse(0.0);
        maxTickW = IntStream.range(0, ny).mapToObj(i -> new Text(yMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getWidth()).max().orElse(0.0);
        stepX    = pWidth / nx;
        stepY    = pHeight / ny;
        everyX   = (int) Math.ceil((maxTickX + 10.0) / stepX);
        everyY   = (int) Math.ceil((maxTickY + 10.0) / stepY);

        GUI.runNow(() -> {

            gc.clearRect(0, 0, width, height);

            drawTitle();
            drawColourBar();
            drawAxes();
            drawMap();

        });

    }

    private void resized() {

        if (maxTickX == 0 || maxTickY == 0 || maxTickW == 0) {
            maxTickX = IntStream.range(0, nx).mapToObj(i -> new Text(xMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getWidth()).max().orElse(1.0);
            maxTickY = IntStream.range(0, ny).mapToObj(i -> new Text(yMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getHeight()).max().orElse(1.0);
            maxTickW = IntStream.range(0, ny).mapToObj(i -> new Text(yMapper.value(i))).mapToDouble(t -> t.getBoundsInLocal().getWidth()).max().orElse(1.0);
        }

        height  = canvas.getHeight();
        width   = canvas.getWidth();
        pEndX   = width - CBAR_GAP - CBAR_SIZE;
        pEndY   = height - AXIS_SIZE;
        pWidth  = pEndX - pStartX;
        pHeight = pEndY - pStartY;
        cStartX = pEndX + CBAR_GAP;
        cEndY   = pStartY + pHeight;
        stepX   = pWidth / nx;
        stepY   = pHeight / ny;
        everyX  = (int) Math.ceil((maxTickX + 10.0) / stepX);
        everyY  = (int) Math.ceil((maxTickY + 10.0) / stepY);

        GUI.runNow(() -> {

            gc.clearRect(0, 0, width, height);

            drawTitle();
            drawColourBar();
            drawAxes();
            drawMap();

        });

    }

    private void drawMap() {
        gc.setImageSmoothing(false);
        gc.drawImage(image, pStartX, pStartY, pWidth, pHeight);
    }

    private void drawTitle() {

        gc.clearRect(0, 0, canvas.getWidth(), 2.0 * TITLE_SIZE);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 22));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(getTitle(), (pStartX + pEndX) / 2.0, TITLE_SIZE);

    }

    private void drawColourBar() {

        gc.clearRect(cStartX, cStartY, CBAR_SIZE, pHeight);
        gc.clearRect(cStartX + 26, 0, CBAR_SIZE, height);

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

        int height = (int) (cEndY - cStartY);

        if (barBuffer == null || barBuffer.getHeight() != height) {
            barBuffer = new PixelBuffer<>(1, height, IntBuffer.allocate(height), PixelFormat.getIntArgbPreInstance());
            bar       = new WritableImage(barBuffer);
        }

        int[] pixels = barBuffer.getBuffer().array();

        for (int j = 0; j < height; j++) {

            double vv = (double) j / (height - 1);
            pixels[j] = colourMap.value(1 - vv);

        }

        barBuffer.updateBuffer(b -> null);

        gc.setImageSmoothing(false);
        gc.drawImage(bar, cStartX, cStartY, 25, pHeight);

    }

    private void drawAxes() {

        gc.setStroke(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRect(pStartX, pStartY, pWidth, pHeight);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setFont(Font.getDefault());

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

    }

    public void clear() {
        draw(new double[0][0]);
    }

    public void savePNG(String path) throws IOException {

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);

        WritableImage image = canvas.snapshot(params, null);

        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File(path));

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

        ColourMap JISA      = vv -> 255 << 24 | ((int) (r.value(1 - vv))) << 16 | ((int) (g.value(1 - vv))) << 8 | ((int) (b.value(1 - vv)));
        ColourMap VIRIDIS   = vv -> Util.HSBtoARGB(288 - 234 * vv, 0.8, 0.3 + 0.68 * vv);
        ColourMap INFERNO   = vv -> Util.HSBtoARGB(288 + 142 * vv, 0.8, Math.sqrt(vv));
        ColourMap GREYSCALE = vv -> (255 << 24) | ((int) (vv * 255) << 16) | ((int) (vv * 255) << 8) | ((int) (vv * 255));
        ColourMap RAINBOW   = vv -> Util.HSBtoARGB(300 * vv, 1.0, 1.0);
        ColourMap RED       = vv -> Util.HSBtoARGB(0, 1.0, vv);
        ColourMap GREEN     = vv -> Util.HSBtoARGB(120, 1.0, vv);
        ColourMap BLUE      = vv -> Util.HSBtoARGB(240, 1.0, vv);
        ColourMap FRENCH    = vv -> 255 << 24 | (int) (255 * Math.min(1.0, 2 * vv)) << 16 | (int) (255 * (1.0 - (2.0 * Math.abs(vv - 0.5)))) << 8 | (int) (255 * Math.min(1.0, 2 * (1 - vv)));

        int value(double value);

    }

    public interface TickMapper {

        TickMapper DEFAULT = i -> String.format("%d", i);

        static TickMapper fromArray(String... values) {

            return i -> {

                try {
                    return values[i];
                } catch (Throwable e) {
                    return null;
                }

            };

        }

        String value(int tick);

    }

}
