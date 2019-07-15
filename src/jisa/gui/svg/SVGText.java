package jisa.gui.svg;

import java.io.PrintStream;

public class SVGText extends SVGElement {

    public SVGText(double x, double y, String anchor, String text) {
        super("text");

        setAttribute("x", x);
        setAttribute("y", y);
        setAttribute("text-anchor", anchor);

        add(new TNode(text));

    }

    private class TNode extends SVGElement {

        String text;

        public TNode(String text) {
            super("");
            this.text = text;
        }


        public void output(PrintStream stream) {
            stream.print(text);
        }

    }

}
