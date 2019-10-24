package jisa.gui.svg;

import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import jisa.Util;

public class SVGCross extends SVGPath {

    public SVGCross(double x, double y, double size) {

        super("");

        Path cross = new Path();
        cross.getElements().add(new MoveTo(1, 0));
        cross.getElements().add(new LineTo(0, 0));
        cross.getElements().add(new LineTo(0, 1));
        cross.getElements().add(new LineTo(size - 1, size));
        cross.getElements().add(new LineTo(0, 2 * size - 1));
        cross.getElements().add(new LineTo(0, 2 * size));
        cross.getElements().add(new LineTo(1, 2 * size));
        cross.getElements().add(new LineTo(size, size + 1));
        cross.getElements().add(new LineTo(2 * size - 1, 2 * size));
        cross.getElements().add(new LineTo(2 * size, 2 * size));
        cross.getElements().add(new LineTo(2 * size, 2 * size - 1));
        cross.getElements().add(new LineTo(size + 1, size));
        cross.getElements().add(new LineTo(2 * size, 1));
        cross.getElements().add(new LineTo(2 * size, 0));
        cross.getElements().add(new LineTo(2 * size - 1, 0));
        cross.getElements().add(new LineTo(size, size - 1));
        cross.getElements().add(new LineTo(1, 0));

        cross.getElements().replaceAll(e -> {

            if (e instanceof MoveTo) {
                MoveTo mt = (MoveTo) e;
                return new MoveTo(mt.getX() - size + x, mt.getY() - size + y);
            } else if (e instanceof LineTo) {
                LineTo mt = (LineTo) e;
                return new LineTo(mt.getX() - size + x, mt.getY() - size + y);
            } else {
                return e;
            }

        });

        setAttribute("d", Util.pathToSVG(cross));
    }


}
