package jisa.gui;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import jisa.devices.camera.frame.Frame;
import jisa.devices.camera.frame.RGBFrame;
import jisa.devices.camera.frame.U16RGBFrame;
import jisa.maths.matrices.Matrix;

import java.nio.IntBuffer;
import java.util.stream.StreamSupport;

public class ImageDisplay extends JFXElement {

    private final CanvasPane             canvasPane;
    private final Canvas                 canvas;
    private       PixelBuffer<IntBuffer> pixels;
    private       WritableImage          image;

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

    public ImageDisplay(String title) {
        this(title, new CanvasPane(500, 500));
    }

    public void drawMono(short[][] data, short max) {

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

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

        GUI.runNow(() -> {
            pixels.updateBuffer(b -> null);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });

    }

    public <N extends Number> void drawMono(Matrix<N> data, N max) {

        int height = data.rows();
        int width  = data.cols();

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

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

        GUI.runNow(() -> {
            pixels.updateBuffer(b -> null);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });

    }

    public <N extends Number> void drawMono(Iterable<Iterable<N>> data, N max) {

        int    height  = (int) StreamSupport.stream(data.spliterator(), false).count();
        int    width   = height > 0 ? (int) StreamSupport.stream(data.iterator().next().spliterator(), false).count() : 0;
        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

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

        GUI.runNow(() -> {
            pixels.updateBuffer(b -> null);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });

    }

    public void drawMono(int[][] data, int max) {

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

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

        GUI.runNow(() -> {
            pixels.updateBuffer(b -> null);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });

    }

    public void drawFrame(Frame.UShortFrame frame) {
        drawMono(frame.image(), Short.MAX_VALUE);
    }

    public void drawFrame(RGBFrame frame) {
        drawARGBInt(frame.getARGBImage());
    }

    public void drawFrame(U16RGBFrame frame) {
        drawARGBLong(frame.getARGBImage());
    }

    public void drawMono(double[][] data, double max) {

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

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

        GUI.runNow(() -> {
            pixels.updateBuffer(b -> null);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });

    }

    public void drawARGBInt(int[][] data) {

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        for (int y = 0; y < height; y++) {
            System.arraycopy(data[y], 0, raw, width * y, width);
        }

        GUI.runNow(() -> {
            pixels.updateBuffer(b -> null);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });

    }

    public void drawARGBLong(long[][] data) {

        final double factor = 255.0 / Character.MAX_VALUE;

        int height = data.length;
        int width  = height > 0 ? data[0].length : 0;

        if (height == 0 || width == 0) {
            return;
        }

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

        if (pixels == null || pixels.getHeight() != height || pixels.getWidth() != width) {
            pixels = new PixelBuffer<>(width, height, IntBuffer.allocate(width * height), PixelFormat.getIntArgbPreInstance());
            image  = new WritableImage(pixels);
        }

        int[] raw = pixels.getBuffer().array();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {

                char red    = (char) ((data[y][x] >> 32) & 0xFFFF);
                char green  = (char) ((data[y][x] >> 16) & 0xFFFF);
                char blue   = (char) ((data[y][x]) & 0xFFFF);
                char redI   = (char) (red * factor);
                char greenI = (char) (green * factor);
                char blueI  = (char) (blue * factor);

                raw[width * y + x] = (255 << 24 | redI << 16 | greenI << 8 | blueI);

            }

        }

        GUI.runNow(() -> {
            pixels.updateBuffer(b -> null);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });


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

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

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

        GUI.runNow(() -> {
            pixels.updateBuffer(b -> null);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });

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

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

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

        GUI.runNow(() -> {
            pixels.updateBuffer(b -> null);
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });


    }


    public void resized() {

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

        GUI.runNow(() -> {
            canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight);
        });

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
