package jisa.gui.svg;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class SVG {

    private final double           width;
    private final double           height;
    private final List<SVGElement> elements = new LinkedList<>();

    public SVG(double width, double height) {
        this.width  = width;
        this.height = height;
    }

    public void add(SVGElement element) {
        elements.add(element);
    }

    public void output(PrintStream stream) {

        stream.printf("<svg width='%s' height='%s' xmlns=\"http://www.w3.org/2000/svg\">", width, height);

        for (SVGElement element : elements) {
            element.output(stream);
        }

        stream.print("</svg>");

    }

    public void output(String fileName) throws IOException {

        FileOutputStream writer = new FileOutputStream(fileName);
        PrintStream      stream = new PrintStream(writer);

        output(stream);

        stream.close();
        writer.close();

    }

    public String toString() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        output(new PrintStream(stream));

        return stream.toString();

    }

}
