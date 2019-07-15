package jisa.gui;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;

public class MarkDown extends JFXWindow {

    public WebView   web;
    public VBox      pane;
    public ButtonBar buttonBar;
    String content = "";

    public MarkDown(String title) {
        super(title, MarkDown.class.getResource("fxml/MDWindow.fxml"));

        buttonBar.setVisible(false);
        buttonBar.setManaged(false);

        buttonBar.getButtons().addListener((ListChangeListener<? super javafx.scene.Node>) l -> {

            buttonBar.setVisible(!buttonBar.getButtons().isEmpty());
            buttonBar.setManaged(!buttonBar.getButtons().isEmpty());

        });
    }

    public MarkDown(String title, String content) {
        this(title);
        setContent(content);
    }

    public void setContent(String text) {

        content = text;

        Parser       parser   = Parser.builder().build();
        Node         node     = parser.parse(content);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String       output   = renderer.render(node);
        WebEngine    engine   = web.getEngine();
        GUI.runNow(() -> engine.loadContent("<style> img { max-width: 100%; } </style><html style='width: 95%; font-family: sans-serif;'>" + output + "</html>"));

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

    public void addLine(String text) {
        add(text + "\n");
    }

    public void add(String text) {
        setContent(content + text);
    }

    public void showAndWait() {

        final Semaphore s = new Semaphore(0);

        stage.setOnCloseRequest(we -> s.release());

        Button okay = new Button("OK");
        okay.setOnAction(ae -> s.release());

        GUI.runNow(() -> buttonBar.getButtons().add(okay));

        show();

        try {
            s.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        close();

        GUI.runNow(() -> buttonBar.getButtons().remove(okay));

    }

}
