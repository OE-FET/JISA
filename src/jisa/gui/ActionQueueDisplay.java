package jisa.gui;

import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jisa.experiment.ActionQueue;
import jisa.experiment.ActionQueue.Action;

import java.util.HashMap;
import java.util.Map;

public class ActionQueueDisplay extends JFXElement {

    @FXML
    protected BorderPane        pane;
    @FXML
    protected ListView<HBox>    list;
    private   ActionQueue       queue;
    private   Map<Action, HBox> listItems     = new HashMap<>();
    private   int               scrollTo      = 0;
    private   ActionRunnable    onClick       = null;
    private   ActionRunnable    onDoubleClick = null;

    public ActionQueueDisplay(String title, ActionQueue queue) {

        super(title, ActionQueueDisplay.class.getResource("fxml/ActionQueueWindow.fxml"));

        this.queue = queue;

        for (Action action : queue) add(action);

        queue.addQueueListener((added, removed) -> GUI.runNow(() -> {

            for (Action add : added) add(add);
            for (Action rem : removed) remove(rem);

        }));

    }

    private synchronized void add(Action action) {
        HBox entry = makeItem(action);
        list.getItems().add(entry);
        list.scrollTo(entry);
    }

    private synchronized void remove(Action action) {
        if (listItems.containsKey(action)) list.getItems().remove(listItems.get(action));
    }

    private synchronized HBox makeItem(Action action) {

        HBox container = new HBox();
        container.setSpacing(15);
        container.setAlignment(Pos.CENTER_LEFT);

        MenuItem    remItem = new MenuItem("Remove");
        ContextMenu menu    = new ContextMenu(remItem);

        remItem.setOnAction(event -> queue.removeAction(action));
        container.setOnContextMenuRequested(event -> menu.show(container, event.getScreenX(), event.getScreenY()));

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

                    if (!box.contains(item)) list.scrollTo(container);

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

            if (event.getClickCount() >= 2 && onDoubleClick != null) {

                onDoubleClick.start(action);


            } else if (onClick != null) {

                onClick.start(action);

            }

        });

        listItems.put(action, container);

        return container;

    }

    public void setOnClick(ActionRunnable onClick) {
        this.onClick = onClick;
    }

    public void setOnDoubleClick(ActionRunnable onDoubleClick) {
        this.onDoubleClick = onDoubleClick;
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
