package JISA.GUI;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;

public class Tabs extends JFXWindow implements Gridable {

    public  BorderPane          pane;
    public  VBox                sidebar;
    public  ScrollPane          scrollPane;
    private String              title;
    private ArrayList<HBox>     tabs      = new ArrayList<>();
    private ArrayList<Runnable> switchers = new ArrayList<>();

    public Tabs(String title) throws IOException {
        super(title, "FXML/TabWindow.fxml");
        this.title = title;
    }

    public void addTab(Gridable element) {

        HBox tab = new HBox();
        tab.setPadding(new Insets(15, 15, 15, 15));

        Label name = new Label(element.getTitle());
        name.setTextFill(Color.WHITE);

        tab.getChildren().add(name);

        Runnable onClick = () -> {

            for (HBox other : tabs) {
                other.setStyle("-fx-background-color: transparent;");
                other.getChildren().get(0).setStyle("-fx-text-fill: white;");
            }

            tab.setStyle("-fx-background-color: white;");
            name.setStyle("-fx-text-fill: #4c4c4c;");

            scrollPane.setContent(element.getPane());
        };

        tab.setOnMouseClicked((ae) -> onClick.run());

        sidebar.getChildren().add(tab);
        tabs.add(tab);
        switchers.add(onClick);

        if (tabs.size() == 1) {
            onClick.run();
        }

    }

    public void changeTab(int pane) {

        GUI.runNow(() -> {
            switchers.get(pane).run();
        });

    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
