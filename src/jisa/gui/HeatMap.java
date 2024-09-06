package jisa.gui;

import com.google.common.primitives.Doubles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jisa.maths.Range;
import jisa.maths.functions.GFunction;

import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class HeatMap extends JFXElement {

    private final BorderPane               pane;
    private final Label                    tLabel;
    private final Canvas                   canvas;
    private final Canvas                   colourbar;
    private final GraphicsContext          gc;
    private final GraphicsContext          cgc;
    private       double[][]               lastData  = new double[0][0];
    private       GFunction<Color, Double> colourMap = vv -> Color.hsb(288 - 234 * vv, 0.8, 0.3 + 0.68 * vv, 1.0);

    public HeatMap(String title) {

        super(title, new BorderPane(new CanvasPane(500, 500), new Label(title), new CanvasPane(125, 510), null, null));

        pane      = (BorderPane) getNode().getCenter();
        tLabel    = (Label) pane.getTop();
        canvas    = ((CanvasPane) pane.getCenter()).canvas;
        colourbar = ((CanvasPane) pane.getRight()).canvas;
        gc        = canvas.getGraphicsContext2D();
        cgc       = colourbar.getGraphicsContext2D();

        tLabel.setFont(Font.font(tLabel.getFont().getFamily(), FontWeight.BOLD, 22));
        tLabel.setAlignment(Pos.CENTER);
        tLabel.setMaxWidth(Double.MAX_VALUE);

        GUI.runNow(() -> getStage().getScene().setFill(Color.WHITE));

        pane.setPadding(new Insets(0.0, 20.0, 20.0, 20.0));

        BorderPane.setMargin(pane.getCenter(), new Insets(5.0, 10.0, 5.0, 0.0));
        BorderPane.setMargin(tLabel, new Insets(10.0));

        ((CanvasPane) pane.getRight()).setMaxWidth(125);
        ((CanvasPane) pane.getRight()).setMinWidth(125);

        this.canvas.widthProperty().addListener((observable, oldValue, newValue) -> draw(lastData));
        this.canvas.heightProperty().addListener((observable, oldValue, newValue) -> draw(lastData));

        draw(new double[0][0]);

    }

    public void draw(List<List<Double>> data) {

        double[][] converted = new double[data.size()][];

        for (int i = 0; i < data.size(); i++) {
            converted[i] = Doubles.toArray(data.get(i));
        }

        draw(converted);

    }

    public void draw(double[][] data) {

        lastData = data;

        double height = canvas.getHeight();
        double width  = canvas.getWidth();
        double min    = Stream.of(data).flatMapToDouble(DoubleStream::of).min().orElse(-1.0);
        double max    = Stream.of(data).flatMapToDouble(DoubleStream::of).max().orElse(1.0);

        gc.setFill(colourMap.value(0.0));
        gc.setStroke(Color.BLACK);
        gc.fillRect(0, 0, width, height);

        int ny = data.length;
        int nx = ny > 0 ? data[0].length : 0;

        for (int y = 0; y < ny; y++) {

            double yv = Math.floor(((double) y / ny) * height);

            for (int x = 0; x < nx; x++) {

                double xv = Math.floor(((double) x / nx) * width);
                double vv = (data[y][x] - min) / (max - min);

                gc.setFill(colourMap.value(vv));
                gc.fillRect(xv, yv, Math.ceil(width / nx), Math.ceil(height / ny));

            }

        }

        gc.strokeRect(1, 1, width-2, height-2);

        height = colourbar.getHeight();

        cgc.clearRect(0, 0, 150, height);

        for (int y = 5; y <= height - 5; y++) {

            double vv = 1.0 - (y - 5) / (height - 10);
            cgc.setFill(colourMap.value(vv));
            cgc.fillRect(2, y, 48, 1);

        }

        cgc.setFill(Color.BLACK);
        cgc.setStroke(Color.BLACK);
        cgc.strokeRect(2, 5, 48, height - 10);
        cgc.setTextBaseline(VPos.CENTER);

        double i = 10;
        for (double y : Range.linear(5.0, height - 5.0, 11)) {
            cgc.strokeLine(50, y, 55, y);
            cgc.fillText(String.format("%.02e", min + (i-- / 10.0) * (max - min)), 60, y);
        }

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
