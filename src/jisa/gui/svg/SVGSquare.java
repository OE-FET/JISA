package jisa.gui.svg;

import javafx.scene.shape.*;
import jisa.Util;

public class SVGSquare extends SVGElement {

    public SVGSquare(double x, double y, double size) {

        super("polygon");

        Polygon path = new Polygon();

        path.getPoints().setAll(
            x - size, y - size,
            x + size, y - size,
            x + size, y + size,
            x - size, y + size
        );

        setAttribute("points", Util.polygonToSVG(path));

    }

}
