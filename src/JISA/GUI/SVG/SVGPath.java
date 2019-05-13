package JISA.GUI.SVG;

public class SVGPath extends SVGElement {

    public SVGPath(String path) {
        super("path");
        setAttribute("d", path);
    }

}
