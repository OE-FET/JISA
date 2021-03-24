package jisa.gui;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jisa.control.SRunnable;
import jisa.experiment.ActionQueue;
import jisa.experiment.ActionQueue.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActionQueueDisplay extends JFXElement {

    @FXML
    protected BorderPane     pane;
    @FXML
    protected ListView<HBox> list;

    private final ActionQueue                 queue;
    private final Map<Action, HBox>           listItems     = new HashMap<>();
    private final Map<HBox, Action>           reverseMap    = new HashMap<>();
    private final Map<Action, SRunnable>      clickers      = new HashMap<>();
    private final Map<Action, SRunnable>      dblClickers   = new HashMap<>();
    private final int                         scrollTo      = 0;
    private       ActionRunnable              onClick       = null;
    private       ActionRunnable              onDoubleClick = null;

    public ActionQueueDisplay(String title, ActionQueue queue) {

        super(title, ActionQueueDisplay.class.getResource("fxml/ActionQueueWindow.fxml"));

        this.queue = queue;

        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        for (Action action : queue) { add(action); }

        queue.addQueueListener((added, removed, changed) -> GUI.runNow(() -> {

            List<HBox> oldSelected = List.copyOf(list.getSelectionModel().getSelectedItems());

            for (Action add : added) { add(add); }
            for (Action rem : removed) { remove(rem); }

            changed.forEach((index, action) -> {

                HBox item;

                if (listItems.containsKey(action)) {
                     item = listItems.get(action);
                } else {
                    item = makeItem(action);
                }

                list.getItems().set(index, item);

            });

            if (!oldSelected.isEmpty()) {
                list.getSelectionModel().clearSelection();
                oldSelected.forEach(list.getSelectionModel()::select);
            }

        }));

    }

    private synchronized void add(Action action) {
        HBox entry = makeItem(action);
        list.getItems().add(entry);
        list.scrollTo(entry);
    }

    private synchronized void remove(Action action) {
        if (listItems.containsKey(action)) { list.getItems().remove(listItems.get(action)); }
    }

    private synchronized HBox makeItem(Action action) {

        HBox container = new HBox();
        container.setSpacing(15);
        container.setAlignment(Pos.CENTER_LEFT);

        ImageView image  = new ImageView(action.getStatus().getImage());
        Label     name   = new Label(action.getName());
        Label     status = new Label(action.getStatus().getText());
        name.setFont(Font.font(name.getFont().getName(), FontWeight.BOLD, 16));

        action.nameProperty().addListener((o) -> GUI.runNow(() -> name.setText(action.getName())));

        action.addStatusListener((old, value) -> GUI.runNow(() -> {

            image.setImage(value.getImage());
            status.setText(value.getText() + (value == ActionQueue.Status.ERROR ? ": " + action.getException().getMessage() : ""));

            if (value == ActionQueue.Status.RUNNING) {

                GUI.runNow(() -> {

                    Bounds box  = list.localToScene(list.getBoundsInLocal());
                    Bounds item = container.localToScene(container.getBoundsInLocal());

                    if (!box.contains(item)) { list.scrollTo(container); }

                });

            }

        }));

        image.setImage(action.getStatus().getImage());
        status.setText(action.getStatus().getText() + (action.getStatus() == ActionQueue.Status.ERROR ? ": " + action.getException().getMessage() : ""));

        image.setFitHeight(32);
        image.setFitWidth(32);
        image.setSmooth(true);

        VBox title = new VBox(name, status);
        title.setSpacing(1);
        VBox.setVgrow(name, Priority.NEVER);
        VBox.setVgrow(status, Priority.NEVER);

        container.getChildren().addAll(image, title);

        HBox.setHgrow(image, Priority.NEVER);
        HBox.setHgrow(title, Priority.ALWAYS);

        container.setOnMouseClicked(event -> {

            if (event.getClickCount() >= 2) {

                if (dblClickers.containsKey(action) && dblClickers.get(action) != null) {
                    dblClickers.get(action).start();
                } else if (onDoubleClick != null) {
                    onDoubleClick.start(action);
                }

            } else {

                if (clickers.containsKey(action) && clickers.get(action) != null) {
                    clickers.get(action).start();
                } else if (onClick != null) {
                    onClick.start(action);
                }

            }

        });

        listItems.put(action, container);
        reverseMap.put(container, action);

        return container;

    }

    public int getSelectedIndex() {
        return list.getSelectionModel().getSelectedIndex();
    }

    public Action getSelectedAction() {
        return reverseMap.getOrDefault(list.getSelectionModel().getSelectedItem(), null);
    }

    public List<Action> getSelectedActions() {
        return list.getSelectionModel().getSelectedIndices().stream().map(list.getItems()::get).map(reverseMap::get).collect(Collectors.toList());
    }

    public List<Integer> getSelectedIndices() {
        return list.getSelectionModel().getSelectedIndices();
    }

    public void select(int index) {

        GUI.runNow(() -> {
            list.getSelectionModel().clearSelection();
            list.getSelectionModel().select(index);
        });

    }

    public void selectIndices(List<Integer> indices) {

        GUI.runNow(() -> {
            list.getSelectionModel().clearSelection();
            indices.forEach(list.getSelectionModel()::select);
        });

    }

    public void selectActions(List<Action> actions) {

        GUI.runNow(() -> {
            list.getSelectionModel().clearSelection();
            actions.forEach(a -> list.getSelectionModel().select(listItems.get(a)));
        });

    }

    public void select(Action a) {
        if (listItems.containsKey(a) && list.getItems().contains(listItems.get(a))) {
            GUI.runNow(() -> {
                list.getSelectionModel().clearSelection();
                list.getSelectionModel().select(listItems.get(a));
            });
        }
    }

    public void setOnClick(ActionRunnable onClick) {
        this.onClick = onClick;
    }

    public void setOnClick(Action action, SRunnable onClick) {
        this.clickers.put(action, onClick);
    }

    public void setOnDoubleClick(ActionRunnable onDoubleClick) {
        this.onDoubleClick = onDoubleClick;
    }

    public void setOnDoubleClick(Action action, SRunnable onClick) {
        this.dblClickers.put(action, onClick);
    }

    public interface ActionRunnable {

        void run(Action action) throws Exception;

        default void runRegardless(Action action) {
            try {
                run(action);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        default void start(Action action) {
            (new Thread(() -> runRegardless(action))).start();
        }

    }

    public interface ActionClick {

        void click(Action action) throws Exception;

        default void runRegardless(Action action) {

            try {
                click(action);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        default void start(Action action) {
            (new Thread(() -> runRegardless(action))).start();
        }

    }

}
