package jisa.gui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;


public class Logger extends JFXElement {

    public  BorderPane     pane;
    public        ListView<Node> list;
    private final String         title;


    public Logger(String title) {
        super(title, Logger.class.getResource("fxml/LoggerWindow.fxml"));
        this.title = title;
    }

    public interface Line {

        void edit(String newText);

        void delete();

    }

    public interface PLine {

        void edit(String newText);

        void setProgress(double pct);

        default void setProgress(int count, int max) {
            setProgress(((double) count / (double) max) * 100D);
        }

        void delete();

    }

    public synchronized Line addLine(String text, Object... args) {

        final Label l = new Label(String.format(text, args));
        GUI.runNow(() -> {
            l.setFont(Font.font("Monospace", 12));
            int index = list.getItems().size();
            list.getItems().add(l);
            list.scrollTo(index);
        });

        return new Line() {
            @Override
            public void edit(String newText) {
                GUI.runNow(() -> l.setText(newText));
            }

            @Override
            public void delete() {
                GUI.runNow(() -> list.getItems().remove(l));
            }

        };

    }

    public synchronized void clear() {
        GUI.runNow(list.getItems()::clear);
    }

}
