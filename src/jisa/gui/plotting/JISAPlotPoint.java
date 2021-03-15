package jisa.gui.plotting;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import jisa.maths.Range;
import jisa.maths.matrices.RealMatrix;

public abstract class JISAPlotPoint {

    private final JISAPlot plot;
    private final Shape    marker;
    private final Path     errorXPath = new Path();
    private final Path     errorYPath = new Path();
    private       double   x          = 0.0;
    private       double   y          = 0.0;
    private       double   errorX     = 0.0;
    private       double   errorY     = 0.0;
    private       double   size;
    private       Color    colour;

    public JISAPlotPoint(JISAPlot plot, Shape marker, double size, Color colour) {

        this.plot   = plot;
        this.marker = marker;
        this.size   = size;
        this.colour = colour;

        drawShape();

    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getErrorX() {
        return errorX;
    }

    public void setErrorX(double errorX) {
        this.errorX = errorX;
    }

    public double getErrorY() {
        return errorY;
    }

    public void setErrorY(double errorY) {
        this.errorY = errorY;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public Color getColour() {
        return colour;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public void reset() {
        x      = 0.0;
        y      = 0.0;
        errorX = 0.0;
        errorY = 0.0;
    }

    public Shape getMarker() {
        return marker;
    }

    public Path getXErrorBar() {
        return errorXPath;
    }

    public Path getYErrorBar() {
        return errorYPath;
    }

    public JISAPlot getPlot() {
        return plot;
    }

    public void reposition() {

        double xPos   = plot.getXPosition(x);
        double yPos   = plot.getYPosition(y);
        double width  = marker.prefWidth(-1);
        double height = marker.prefHeight(-1);

        marker.resizeRelocate(xPos - width / 2.0, yPos - height / 2.0, width, height);

        errorX = Math.abs(errorX);
        errorY = Math.abs(errorY);

        errorXPath.getElements().clear();
        errorYPath.getElements().clear();

        if (errorX > 0) {

            double minX = plot.getXPosition(x - errorX);
            double maxX = plot.getXPosition(x + errorX);

            errorXPath.getElements().addAll(
                    new MoveTo(minX, yPos - size / 2.0),
                    new LineTo(minX, yPos + size / 2.0),
                    new MoveTo(minX, yPos),
                    new LineTo(maxX, yPos),
                    new MoveTo(maxX, yPos - size / 2.0),
                    new LineTo(maxX, yPos + size / 2.0)
            );

        }

        if (errorY > 0) {

            double minY = plot.getYPosition(y - errorY);
            double maxY = plot.getYPosition(y + errorY);

            errorYPath.getElements().addAll(
                    new MoveTo(xPos - size / 2.0, minY),
                    new LineTo(xPos + size / 2.0, minY),
                    new MoveTo(xPos, minY),
                    new LineTo(xPos, maxY),
                    new MoveTo(xPos - size / 2.0, maxY),
                    new LineTo(xPos + size / 2.0, maxY)
            );

        }


    }

    public abstract void drawShape();

    public abstract JISAPlotPoint copy();


    public static class Circle extends JISAPlotPoint {

        public Circle(JISAPlot plot, double size, Color colour) {
            super(plot, new javafx.scene.shape.Circle(), size, colour);
            getMarker().setFill(Color.WHITE);
            getMarker().setStroke(Color.BLACK);
            getMarker().setStrokeWidth(2.0);
        }

        @Override
        public void drawShape() {

            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) getMarker();
            circle.setRadius(getSize() / 2);
            circle.setStroke(getColour());

        }

        @Override
        public Circle copy() {
            return new Circle(getPlot(), getSize(), getColour());
        }

    }

    public static class Dot extends JISAPlotPoint {

        public Dot(JISAPlot plot, double size, Color colour) {
            super(plot, new javafx.scene.shape.Circle(5.0), size, colour);
            getMarker().setFill(Color.BLACK);
            getMarker().setStrokeWidth(0.0);
        }

        @Override
        public void drawShape() {

            javafx.scene.shape.Circle circle = (javafx.scene.shape.Circle) getMarker();
            circle.setRadius(getSize() / 2);
            circle.setFill(getColour());

        }

        @Override
        public Dot copy() {
            return new Dot(getPlot(), getSize(), getColour());
        }

    }

    public static class Square extends JISAPlotPoint {

        public Square(JISAPlot plot, double size, Color colour) {
            super(plot, new Rectangle(5.0, 5.0), size, colour);
            getMarker().setFill(Color.WHITE);
            getMarker().setStroke(Color.BLACK);
            getMarker().setStrokeWidth(2.0);
        }

        @Override
        public void drawShape() {

            Rectangle rectangle = (Rectangle) getMarker();
            rectangle.setWidth(getSize());
            rectangle.setHeight(getSize());
            rectangle.setStroke(getColour());

        }

        @Override
        public Square copy() {
            return new Square(getPlot(), getSize(), getColour());
        }

    }

    public static class Diamond extends Dash {

        public Diamond(JISAPlot plot, double size, Color colour) {
            super(plot, size, colour);
            getMarker().setRotate(45.0);
        }

        @Override
        public Diamond copy() {
            return new Diamond(getPlot(), getSize(), getColour());
        }

    }

    public static class Cross extends JISAPlotPoint {

        public Cross(JISAPlot plot, double size, Color colour) {
            super(plot, new Path(), size, colour);
        }

        @Override
        public void drawShape() {

            Path path = (Path) getMarker();

            path.getElements().clear();
            path.getElements().addAll(
                    new MoveTo(0.0, 0.0),
                    new LineTo(getSize(), getSize()),
                    new MoveTo(0.0, getSize()),
                    new LineTo(getSize(), 0.0)
            );

            path.setStroke(getColour());
            path.setStrokeWidth(2.0);

        }

        @Override
        public Cross copy() {
            return new Cross(getPlot(), getSize(), getColour());
        }

    }

    public static class Triangle extends JISAPlotPoint {

        public Triangle(JISAPlot plot, double size, Color colour) {

            super(plot, new Polygon(0.0, 2.0, 1.0, 0.0, 2.0, 2.0), size, colour);
            getMarker().setFill(Color.WHITE);
            getMarker().setStroke(Color.BLACK);
            getMarker().setStrokeWidth(2.0);

        }

        @Override
        public void drawShape() {

            Polygon triangle = (Polygon) getMarker();

            triangle.getPoints().setAll(
                    0.0, getSize(),
                    getSize() / 2, 0.0,
                    getSize(), getSize()
            );

            triangle.setStroke(getColour());

        }

        @Override
        public Triangle copy() {
            return new Triangle(getPlot(), getSize(), getColour());
        }

    }

    public static class Star extends JISAPlotPoint {

        public Star(JISAPlot plot, double size, Color colour) {

            super(plot, new Polygon(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), size, colour);
            getMarker().setFill(Color.WHITE);
            getMarker().setStroke(Color.BLACK);
            getMarker().setStrokeWidth(2.0);

        }

        @Override
        public void drawShape() {

            Polygon star = (Polygon) getMarker();
            star.getPoints().clear();

            RealMatrix longMatrix  = RealMatrix.asColumn(0.0, getSize() / 2.0);
            RealMatrix shortMatrix = RealMatrix.asColumn(0.0, getSize() / 4.0);

            boolean spike = true;
            for (double t : Range.linear(0.0, 2.0 * Math.PI, 11)) {

                RealMatrix start = spike ? longMatrix : shortMatrix;
                RealMatrix point = start.rotate2D(t);

                star.getPoints().addAll(point.get(0, 0), point.get(1, 0));

                spike = !spike;

            }

            star.setStroke(getColour());

        }

        @Override
        public Star copy() {
            return new Star(getPlot(), getSize(), getColour());
        }

    }

    public static class Dash extends JISAPlotPoint {

        public Dash(JISAPlot plot, double size, Color colour) {

            super(plot, new Line(0.0, 0.0, 5.0, 0.0), size, colour);
            getMarker().setStroke(Color.BLACK);
            getMarker().setStrokeWidth(2.0);

        }

        @Override
        public void drawShape() {

            Line line = (Line) getMarker();
            line.setEndX(getSize());
            line.setStroke(getColour());

        }

        @Override
        public Dash copy() {
            return new Dash(getPlot(), getSize(), getColour());
        }

    }

}
