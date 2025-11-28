package jisa.gui;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import jisa.devices.camera.frame.Frame;
import jisa.maths.matrices.Matrix;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;

public class ImageDisplay extends JFXElement implements FrameAcceptor {

    private final CanvasPane             canvasPane;
    private final Canvas                 canvas;
    private       PixelBuffer<IntBuffer> pixels;
    private       WritableImage          image;
    private final List<OverlayGenerator> overlays = new LinkedList<>();

    private ImageDisplay(String title, CanvasPane centre) {

        super(title, centre);

        this.canvasPane = centre;
        this.canvas     = centre.canvas;

        setMinHeight(500);
        setMinWidth(500);
        setWindowSize(600, 525);

        canvas.widthProperty().addListener((observable, oldValue, newValue) -> resized());
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> resized());

        canvas.getGraphicsContext2D().setImageSmoothing(false);

        BorderPane.setMargin(centre, Insets.EMPTY);

    }

    public interface OverlayGenerator {
        Image getOverlay(int width, int height);
    }

    public ImageDisplay(String title) {
        this(title, new CanvasPane(500, 500));
    }

    public void render() {

        if (image != null) {

            double width   = image.getWidth();
            double height  = image.getHeight();
            double cWidth  = canvas.getWidth();
            double cHeight = canvas.getHeight();
            double ratio   = width / height;
            double rWidth  = Math.min(cWidth, cHeight * ratio);
            double rHeight = Math.min(cHeight, cWidth / ratio);
            double x       = (cWidth - rWidth) / 2;
            double y       = (cHeight - rHeight) / 2;

            GUI.runNow(() -> {

                pixels.updateBuffer(b -> null);

                canvas.getGraphicsContext2D().setFill(Color.BLACK);
                canvas.getGraphicsContext2D().fillRect(0, 0, cWidth, cHeight);
                canvas.getGraphicsContext2D().drawImage(image, x, y, rWidth, rHeight);

                for (OverlayGenerator overlay : overlays) {
                    canvas.getGraphicsContext2D().drawImage(overlay.getOverlay((int) width, (int) height), x, y, rWidth, rHeight);
                }

            });

        }

    }

    public OverlayGenerator addOverlay(OverlayGenerator overlay) {
        overlays.add(overlay);
        render();
        return overlay;
    }

    public OverlayGenerator addOverlay(Image overlay) {
        return addOverlay((w, h) -> overlay);
    }

    public OverlayGenerator addCrosshairs(int thickness, Color color) {

        OverlayGenerator generator = (width, height) -> {

            WritableImage image       = new WritableImage(width, height);
            PixelWriter   pixelWriter = image.getPixelWriter();

            int startX = (width / 2) - (thickness / 2);
            int startY = (height / 2) - (thickness / 2);

            for (int y = startY; y < startY + thickness; y++) {

                for (int x = 0; x < width; x++) {
                    pixelWriter.setColor(x, y, color);
                }

            }

            for (int x = startX; x < startX + thickness; x++) {

                for (int y = 0; y < height; y++) {
                    pixelWriter.setColor(x, y, color);
                }

            }

            return image;

        };

        return addOverlay(generator);

    }

    public void removeOverlay(OverlayGenerator generator) {
        overlays.remove(generator);
        render();
    }

    public void clearOverlays() {
        overlays.clear();
        render();
    }

    public void drawMono(short[][] data, short max) {

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int value = (int) (255 * ((double) data[y][x] / max));
                raw[width * y + x] = 255 << 24 | value << 16 | value << 8 | value;

            }

        }

        render();

    }

    public <N extends Number> void drawMono(Matrix<N> data, N max) {

        int height = data.rows();
        int width  = data.cols();

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int value = (int) (255 * (data.get(y, x).doubleValue() / max.doubleValue()));
                raw[width * y + x] = 255 << 24 | value << 16 | value << 8 | value;

            }

        }

        render();

    }

    public <N extends Number> void drawMono(Iterable<Iterable<N>> data, N max) {

        int height = (int) StreamSupport.stream(data.spliterator(), false).count();
        int width  = height > 0 ? (int) StreamSupport.stream(data.iterator().next().spliterator(), false).count() : 0;

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        int i = 0;
        for (Iterable<N> row : data) {

            for (N datum : row) {

                int value = (int) (255 * (datum.doubleValue() / max.doubleValue()));
                raw[i++] = 255 << 24 | value << 16 | value << 8 | value;

            }

        }

        render();

    }

    public void drawMono(int[][] data, int max) {

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int value = (int) (255 * ((double) data[y][x] / max));
                raw[width * y + x] = 255 << 24 | value << 16 | value << 8 | value;

            }

        }

        render();

    }

    public void drawMono(double[][] data, double max) {

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                int value = (int) (255 * ((double) data[y][x] / max));
                raw[width * y + x] = 255 << 24 | value << 16 | value << 8 | value;

            }

        }

        render();

    }

    public void drawARGB(int[] data, int width, int height) {

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();
        System.arraycopy(data, 0, raw, 0, width * height);

        render();

    }

    public void acceptFrame(Frame frame) {
        drawFrame(frame);
    }

    public void drawFrame(Frame data) {

        int width  = data.getWidth();
        int height = data.getHeight();

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        data.readARGBData(pixels.getBuffer().array());

        render();

    }

    public void drawARGB(int[][] data) {

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        for (int y = 0; y < height; y++) {
            System.arraycopy(data[y], 0, raw, width * y, width);
        }

        render();

    }

    public void drawRGB(int[][][] data) {

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        if (height == 0 || width == 0) {
            return;
        }

        if (data[0][0].length != 3) {
            throw new IllegalArgumentException("Data must have three colour components!");
        }

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                raw[width * y + x] = (255 << 24) | (data[y][x][0] << 16) | (data[y][x][1] << 8) | data[y][x][2];

            }

        }

        render();

    }

    public void drawRGB(int[][] red, int[][] green, int[][] blue) {

        int redHeight   = red.length;
        int greenHeight = green.length;
        int blueHeight  = blue.length;
        int redWidth    = redHeight > 0 ? red[0].length : 0;
        int greenWidth  = greenHeight > 0 ? green[0].length : 0;
        int blueWidth   = blueHeight > 0 ? blue[0].length : 0;

        if (redHeight != greenHeight || redHeight != blueHeight || redWidth != greenWidth || redWidth != blueWidth) {
            throw new IllegalArgumentException("Sizes of red, green, and blue arrays do not match.");
        }

        if (pixels == null || pixels.getHeight() != redHeight || pixels.getWidth() != redWidth) {
            pixels = new PixelBuffer<>(redWidth, redHeight, IntBuffer.allocate(redWidth * redHeight), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        for (int y = 0; y < redHeight; y++) {

            for (int x = 0; x < redWidth; x++) {

                raw[redWidth * y + x] = (255 << 24) | (red[y][x] << 16) | (green[y][x] << 8) | blue[y][x];

            }

        }

        render();


    }


    public void resized() {
        render();
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

}
