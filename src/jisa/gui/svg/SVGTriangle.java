package jisa.gui.svg;

public class SVGTriangle extends SVGPath {
    public SVGTriangle(double x, double y, double size) {
        super(String.format("M%s %s L%s %s L%s %s Z", x - size, y + size, x, y - size, x + size, y + size));
    }

}
