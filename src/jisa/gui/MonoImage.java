package jisa.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.stream.IntStream;

public class MonoImage extends JFXElement {

    private final CanvasPane canvasPane;
    private final Canvas     canvas;
    private       short[][]  lastData = new short[256][256];
    private       short      lastMax  = 1;

    private MonoImage(String title, CanvasPane centre) {

        super(title, centre);

        this.canvasPane = centre;
        this.canvas     = centre.canvas;

        setMinHeight(500);
        setMinWidth(500);
        setWindowSize(600, 525);

        canvas.widthProperty().addListener((observable, oldValue, newValue) -> draw(lastData, lastMax));
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> draw(lastData, lastMax));

    }

    public MonoImage(String title) {
        this(title, new CanvasPane(500, 500));
    }

    public void draw(short[][] data, short max) {

        lastData = data;
        lastMax  = max;

        int height = data.length;
        int width  = data[0].length;

        double cHeight = canvas.getHeight();
        double cWidth  = canvas.getWidth();

        WritableImage image       = new WritableImage(width, height);
        PixelWriter   pixelWriter = image.getPixelWriter();

        IntStream.range(0, height).parallel().forEach(y -> {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, Color.gray((double) data[y][x] / max));
            }
        });

        GUI.runNow(() -> canvas.getGraphicsContext2D().drawImage(image, 0, 0, cWidth, cHeight));

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
