package JISA.GUI;

import JISA.Experiment.Result;
import JISA.Experiment.ResultList;
import JISA.GUI.FXML.TableWindow;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;

import java.util.Arrays;

public class Table implements Gridable {

    private TableWindow window;
    private String      title;

    public Table(String title) {
        window = TableWindow.create(title);
        this.title = title;
    }

    public Table(String title, ResultList list) {
        window = TableWindow.create(title, list);
        this.title = title;
    }

    public void watchList(ResultList list) {
        window.watchList(list);
    }

    public void update(ResultList list) {
        window.update(list);
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
