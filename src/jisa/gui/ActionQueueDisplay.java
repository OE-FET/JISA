package jisa.gui;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.*;
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
import java.util.Map;

public class ActionQueueDisplay extends JFXWindow {

    @FXML
    protected BorderPane        pane;
    @FXML
    protected ListView<HBox>    list;
    @FXML
    protected ToolBar           toolBar;
    private   ActionQueue       queue;
    private   Map<Action, HBox> listItems     = new HashMap<>();
    private   int               scrollTo      = 0;
    private   ActionRunnable    onClick       = null;
    private   ActionRunnable    onDoubleClick = null;

    public ActionQueueDisplay(String title, ActionQueue queue) {

        super(title, ActionQueueDisplay.class.getResource("fxml/ActionQueueWindow.fxml"));

        this.queue = queue;

        toolBar.getItems().addListener((ListChangeListener<? super Node>) change -> {
            boolean show = !toolBar.getItems().isEmpty();
            toolBar.setVisible(show);
            toolBar.setManaged(show);
        });

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

    public jisa.gui.Button addToolbarButton(String name, ClickHandler onClick) {

        Button button = new Button(name);
        button.setOnMouseClicked(event -> onClick.start());
        GUI.runNow(() -> toolBar.getItems().add(button));

        return new jisa.gui.Button() {
            @Override
            public boolean isDisabled() {
                return button.isDisabled();
            }

            @Override
            public void setDisabled(boolean disabled) {
                GUI.runNow(() -> button.setDisable(disabled));
            }

            @Override
            public boolean isVisible() {
                return button.isVisible();
            }

            @Override
            public void setVisible(boolean visible) {

                GUI.runNow(() -> {
                    button.setVisible(visible);
                    button.setManaged(visible);
                });

            }

            @Override
            public String getText() {
                return button.getText();
            }

            @Override
            public void setText(String text) {
                GUI.runNow(() -> button.setText(text));
            }

            @Override
            public void setOnClick(SRunnable onClick) {
                button.setOnMouseClicked(event -> onClick.start());
            }

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(button));
            }

        };

    }

    public MenuButton addToolbarMenuButton(String text) {

        javafx.scene.control.MenuButton button = new javafx.scene.control.MenuButton(text);
        GUI.runNow(() -> toolBar.getItems().add(button));

        return new MenuButton.MenuButtonWrapper(button) {

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(button));
            }

        };

    }

    public Separator addToolbarSeparator() {

        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        GUI.runNow(() -> toolBar.getItems().add(separator));

        return new Separator.SeparatorWrapper(separator) {

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(separator));
            }

        };

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
