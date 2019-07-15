package jisa.gui.svg;

public class SVGLine extends SVGElement {

    public SVGLine(double x1, double y1, double x2, double y2) {
        super("line");
        setAttribute("x1", x1);
        setAttribute("y1", y1);
        setAttribute("x2", x2);
        setAttribute("y2", y2);
    }

}
