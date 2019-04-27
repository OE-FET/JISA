package JISA.GUI.SVG;

import javafx.scene.paint.Color;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SVGElement {

    private final String              tag;
    private final Map<String, String> attributes = new LinkedHashMap<>();
    private final Map<String, String> style      = new LinkedHashMap<>();
    private final List<SVGElement>    elements   = new LinkedList<>();

    public SVGElement(String tag) {
        this.tag = tag;
    }

    public void setAttribute(String key, double value){
        setAttribute(key, String.valueOf(value));
    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public void setStyle(String key, String value) {
        style.put(key, value);
    }

    public void add(SVGElement element) {
        elements.add(element);
    }

    public SVGElement setStrokeColour(Color colour) {

        setStyle(
                "stroke",
                String.format("rgb(%s,%s,%s)", colour.getRed() * 255D, colour.getGreen() * 255D, colour.getBlue() * 255D)
        );

        return this;

    }

    public SVGElement setStrokeWidth(double width) {

        setStyle(
                "stroke-width",
                String.format("%spx", width)
        );

        return this;

    }

    public SVGElement setFillColour(Color colour) {

        setStyle(
                "fill",
                String.format("rgb(%s,%s,%s)", colour.getRed() * 255D, colour.getGreen() * 255D, colour.getBlue() * 255D)
        );

        return this;

    }

    public void output(final PrintStream stream) {

        stream.printf("<%s", tag);

        attributes.forEach((k, v) -> {

            if (k.equals("style")) {
                return;
            }

            stream.printf(" %s=\"%s\"", k, v);

        });

        if (!style.isEmpty()) {

            stream.print(" style=\"");

            style.forEach((k, v) -> stream.printf(" %s: %s;", k, v));

            stream.print("\"");

        }

        stream.print(">");

        for (SVGElement element : elements) {
            element.output(stream);
        }

        stream.printf("</%s>", tag);

    }

}
