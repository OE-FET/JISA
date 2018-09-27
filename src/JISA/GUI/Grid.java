package JISA.GUI;

import JISA.GUI.FXML.GridWindow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class Grid implements Gridable {

    private GridWindow window;
    private String     title;

    public Grid(String title, Gridable... items) {
        window = GridWindow.create(title);
        for (Gridable item : items) {
            add(item);
        }
        this.title = title;
    }

    public void add(Gridable toAdd) {

        BorderPane pane  = new BorderPane();
        StackPane  stack = new StackPane();
        Label      t     = new Label();

        stack.setPadding(new Insets(10, 10, 10, 10));
        stack.setAlignment(Pos.CENTER_LEFT);
        stack.setStyle("-fx-background-color: #4c4c4c; -fx-background-radius: 5px 5px 0 0;");
        pane.setStyle("-fx-background-color: white; -fx-background-radius: 5px;");
        pane.setEffect(new DropShadow(10, new Color(0, 0, 0, 0.25)));
        t.setFont(new Font("System Bold", 14));
        t.setTextFill(Color.WHITE);
        t.setText(toAdd.getTitle());
        stack.getChildren().add(t);
        pane.setTop(stack);
        pane.setCenter(toAdd.getPane());

        window.addPane(pane);
    }

    public void addToolbarButton(String text, ClickHandler onClick) {
        window.addToolbarButton(text, onClick);
    }

    public void show() {
        window.show();
    }

    public void hide() {
        window.hide();
    }

    public void close() {
        window.close();
    }

    @Override
    public Pane getPane() {
        return window.getPane();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
