package jisa.gui.plotting;

import de.gsi.chart.marker.Marker;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import jisa.gui.Colour;


public interface JISAMarker {

    JISAMarker CIRCLE = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setFill(stroke);
        gc.fillOval(x - size, y - size, 2.0 * size, 2.0 * size);
        gc.setFill(fill);
        double sz = size - strokeSize;
        gc.fillOval(x - sz, y - sz, 2.0 * sz, 2.0 * sz);
    };

    JISAMarker DOT = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setFill(stroke);
        gc.fillOval(x - size, y - size, 2.0 * size, 2.0 * size);
    };

    JISAMarker RECTANGLE = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setFill(stroke);
        gc.fillRect(x - size, y - size, 2.0 * size, 2.0 * size);
        gc.setFill(fill);
        double sz = size - strokeSize;
        gc.fillRect(x - sz, y - sz, 2.0 * sz, 2.0 * sz);
    };

    JISAMarker DIAMOND = (gc, x, y, size, strokeSize, stroke, fill) -> {

        gc.setFill(stroke);

        double[] xPoints = {x + size, x, x - size, x, x + size};
        double[] yPoints = {y, y + size, y, y - size, y};
        gc.fillPolygon(xPoints, yPoints, xPoints.length);

        gc.setFill(fill);
        double sz = size - strokeSize;

        xPoints = new double[]{x + sz, x, x - sz, x, x + sz};
        yPoints = new double[]{y, y + sz, y, y - sz, y};
        gc.fillPolygon(xPoints, yPoints, xPoints.length);

    };

    JISAMarker CROSS = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setStroke(stroke);
        gc.setLineWidth(strokeSize);
        gc.strokeLine(x - size, y - size, x + size, y + size);
        gc.strokeLine(x - size, y + size, x + size, y - size);
    };

    void draw(GraphicsContext gc, final double x, final double y, final double size, final double strokeSize, final Color stroke, final Color fill);

}
