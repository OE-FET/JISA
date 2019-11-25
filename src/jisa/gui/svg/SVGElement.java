package jisa.gui.svg;

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

    public SVGElement setAttribute(String key, double value) {
        return setAttribute(key, String.valueOf(value));
    }

    public SVGElement setAttribute(String key, String value) {
        attributes.put(key, value);
        return this;
    }

    public SVGElement setStyle(String key, String value) {
        style.put(key, value);
        return this;
    }

    public void add(SVGElement element) {
        elements.add(element);
    }

    public SVGElement setStrokeColour(Color colour) {

        return setStrokeColour(String.format("#%02X%02X%02X",
            (int) (colour.getRed() * 255),
            (int) (colour.getGreen() * 255),
            (int) (colour.getBlue() * 255))
        );

    }

    public SVGElement setStrokeColour(String colour) {

        setAttribute("stroke", colour);
        return this;

    }

    public SVGElement setStrokeWidth(double width) {

        setAttribute(
            "stroke-width",
            String.format("%s", width)
        );

        return this;

    }

    public SVGElement setDash(String... dash) {

        setAttribute(
            "stroke-dasharray",
            String.join(",", dash)
        );

        return this;

    }

    public SVGElement setDash(Double... dash) {

        String[] strDash = new String[dash.length];

        for (int i = 0; i < dash.length; i++) {
            strDash[i] = Double.toString(dash[i]);
        }

        return setDash(strDash);

    }

    public SVGElement setFillColour(Color colour) {

        return setFillColour(String.format(
            "#%02X%02X%02X",
            (int) (colour.getRed() * 255),
            (int) (colour.getGreen() * 255),
            (int) (colour.getBlue() * 255)));

    }

    public SVGElement setFillColour(String colour) {
        setAttribute("fill", colour);
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

        elements.forEach(element -> element.output(stream));

        stream.printf("</%s>", tag);

    }

}
