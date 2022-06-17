package jisa.gui.plotting;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import jisa.maths.Range;


public interface JISAMarker {

    JISAMarker CIRCLE = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setStroke(stroke);
        gc.setLineWidth(strokeSize);
        gc.setFill(fill);
        gc.fillOval(x - size, y - size, 2.0 * size, 2.0 * size);
        gc.strokeOval(x - size, y - size, 2.0 * size, 2.0 * size);
    };

    JISAMarker DOT = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setStroke(stroke);
        gc.setLineWidth(strokeSize);
        gc.setFill(stroke);
        gc.fillOval(x - size, y - size, 2.0 * size, 2.0 * size);
        gc.strokeOval(x - size, y - size, 2.0 * size, 2.0 * size);
    };

    JISAMarker RECTANGLE = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setStroke(stroke);
        gc.setLineWidth(strokeSize);
        gc.setFill(fill);
        gc.fillRect(x - size, y - size, 2.0 * size, 2.0 * size);
        gc.strokeRect(x - size, y - size, 2.0 * size, 2.0 * size);
    };

    JISAMarker TRIANGLE = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setStroke(stroke);
        gc.setLineWidth(strokeSize);
        gc.setFill(fill);
        drawRegularPolygon(gc, x, y, 3, size, 0);
    };

    JISAMarker STAR = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setStroke(stroke);
        gc.setLineWidth(strokeSize);
        gc.setFill(fill);
        drawStarPolygon(gc, x, y, 5, size, 0.5 * size, 0);
    };

    JISAMarker DIAMOND = (gc, x, y, size, strokeSize, stroke, fill) -> {

        gc.setFill(stroke);

        double[] xPoints = {x + size, x, x - size, x, x + size};
        double[] yPoints = {y, y + size, y, y - size, y};
        gc.fillPolygon(xPoints, yPoints, xPoints.length);

        gc.setFill(fill);
        final double sz = size - strokeSize;

        xPoints = new double[]{x + sz, x, x - sz, x, x + sz};
        yPoints = new double[]{y, y + sz, y, y - sz, y};
        gc.fillPolygon(xPoints, yPoints, xPoints.length);

    };

    JISAMarker DASH = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setStroke(stroke);
        gc.setLineWidth(strokeSize);
        gc.strokeLine(x - size, y, x + size, y);
    };

    JISAMarker PLUS = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setStroke(stroke);
        gc.setLineWidth(strokeSize);
        gc.strokeLine(x - size, y, x + size, y);
        gc.strokeLine(x, y - size, x, y + size);
    };

    JISAMarker CROSS = (gc, x, y, size, strokeSize, stroke, fill) -> {
        gc.setStroke(stroke);
        gc.setLineWidth(strokeSize);
        gc.strokeLine(x - size, y - size, x + size, y + size);
        gc.strokeLine(x - size, y + size, x + size, y - size);
    };

    static void drawRegularPolygon(GraphicsContext gc, double x, double y, int sides, double radius, double initialAngle) {

        Range<Double> angles = Range.linear(initialAngle, initialAngle + (2 * Math.PI), sides + 1);

        final double[] xP = angles.stream().mapToDouble(t -> x + radius * Math.sin(t)).toArray();
        final double[] yP = angles.stream().mapToDouble(t -> y - radius * Math.cos(t)).toArray();

        gc.fillPolygon(xP, yP, sides);
        gc.strokePolygon(xP, yP, sides);

    }

    static void drawStarPolygon(GraphicsContext gc, double x, double y, int points, double radius1, double radius2, double initialAngle) {

        Range<Double> angles = Range.linear(initialAngle, initialAngle + (2 * Math.PI), (2 * points) + 1);

        double[] xP = new double[points * 2];
        double[] yP = new double[points * 2];

        for (int i = 0; i < (points * 2); i++) {

            xP[i] = x + ((i % 2 == 0) ? radius1 : radius2) * Math.sin(angles.get(i));
            yP[i] = y - ((i % 2 == 0) ? radius1 : radius2) * Math.cos(angles.get(i));

        }

        gc.fillPolygon(xP, yP, points * 2);
        gc.strokePolygon(xP, yP, points * 2);

    }

    void draw(GraphicsContext gc, final double x, final double y, final double size, final double strokeSize, final Color stroke, final Color fill);

}
