package jisa.gui.svg;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

public class SVG {

    private double width;
    private double height;

    private List<SVGElement> elements = new LinkedList<>();

    public SVG(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public void add(SVGElement element) {
        elements.add(element);
    }

    public void output(String fileName) throws IOException {

        FileOutputStream writer = new FileOutputStream(fileName);
        PrintStream      stream = new PrintStream(writer);

        stream.printf("<svg width='%s' height='%s' xmlns=\"http://www.w3.org/2000/svg\">", width, height);

        for (SVGElement element : elements) {
            element.output(stream);
        }

        stream.print("</svg>");

        writer.close();
        stream.close();

    }

}
