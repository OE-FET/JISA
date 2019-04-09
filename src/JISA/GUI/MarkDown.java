package JISA.GUI;

import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MarkDown extends JFXWindow {

    public WebView web;
    public HBox    pane;

    public MarkDown(String title) {
        super(title, MarkDown.class.getResource("FXML/MDWindow.fxml"));
    }

    public MarkDown(String title, String content) {
        this(title);
        setContent(content);
    }

    public void setContent(String text) {

        Parser       parser   = Parser.builder().build();
        Node         node     = parser.parse(text);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String       output   = renderer.render(node);
        WebEngine    engine   = web.getEngine();
        GUI.runNow(() -> engine.loadContent("<style> img { max-width: 100%; } </style><html style='width: 90%; font-family: sans-serif;'>" + output + "</html>"));

    }

    public void loadFile(String path, Class clazz) throws IOException {

        InputStream stream = clazz.getResourceAsStream(path);
        assert stream != null;
        BufferedReader reader  = new BufferedReader(new InputStreamReader(stream));
        StringBuilder  builder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
            builder.append("\n");
        }

        setContent(builder.toString());

    }

}
