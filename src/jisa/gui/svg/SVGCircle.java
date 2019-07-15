package jisa.gui.svg;

public class SVGCircle extends SVGElement {

    public SVGCircle(double x, double y, double r) {
        super("circle");
        setAttribute("cx", x);
        setAttribute("cy", y);
        setAttribute("r", r);
    }

}
