package jisa.gui;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jisa.experiment.ActionQueue;
import jisa.experiment.ResultTable;

import java.util.HashMap;
import java.util.Map;

public class ActionQueueDisplay extends JFXWindow {

    @FXML
    protected BorderPane                            pane;
    @FXML
    protected ListView<HBox>                        list;
    @FXML
    protected ToolBar                               toolBar;
    private   ActionQueue                           queue;
    private   Map<ActionQueue.Action, HBox>         listItems = new HashMap<>();
    private   Map<ActionQueue.Action, ClickHandler> onClick   = new HashMap<>();
    private   int                                   scrollTo  = 0;

    public ActionQueueDisplay(String title, ActionQueue queue) {

        super(title, ActionQueueDisplay.class.getResource("fxml/ActionQueueWindow.fxml"));

        this.queue = queue;

        toolBar.getItems().addListener((ListChangeListener<? super Node>) change -> {
            boolean show = !toolBar.getItems().isEmpty();
            toolBar.setVisible(show);
            toolBar.setManaged(show);
        });

        for (ActionQueue.Action action : queue) add(action);

        queue.addQueueListener((added, removed) -> GUI.runNow(() -> {

            GUI.runNow(() -> {

                for (ActionQueue.Action add : added) add(add);
                for (ActionQueue.Action rem : removed) remove(rem);

            });

        }));

    }

    private static Image imageFromStatus(ActionQueue.Status status) {

        switch (status) {

            default:
            case NOT_STARTED:
                return new Image(ActionQueueDisplay.class.getResourceAsStream("images/queued.png"));

            case RUNNING:
                return new Image(ActionQueueDisplay.class.getResourceAsStream("images/progress.png"));

            case INTERRUPTED:
                return new Image(ActionQueueDisplay.class.getResourceAsStream("images/cancelled.png"));

            case COMPLETED:
                return new Image(ActionQueueDisplay.class.getResourceAsStream("images/complete.png"));

            case ERROR:
                return new Image(ActionQueueDisplay.class.getResourceAsStream("images/error.png"));

        }

    }

    private static String stringFromStatus(ActionQueue.Status status) {

        switch (status) {

            default:
            case NOT_STARTED:
                return "Not Started";

            case RUNNING:
                return "Running";

            case INTERRUPTED:
                return "Interrupted";

            case COMPLETED:
                return "Completed";

            case ERROR:
                return "Error Encountered";

        }

    }

    private synchronized void add(ActionQueue.Action action) {
        HBox entry = makeItem(action);
        list.getItems().add(entry);
        list.scrollTo(entry);
    }

    private synchronized void remove(ActionQueue.Action action) {
        if (listItems.containsKey(action)) list.getItems().remove(listItems.get(action));
    }

    private synchronized HBox makeItem(ActionQueue.Action action) {

        HBox container = new HBox();
        container.setSpacing(15);
        container.setAlignment(Pos.CENTER_LEFT);

        MenuItem remItem = new MenuItem("Remove");
        ContextMenu menu = new ContextMenu(remItem);

        remItem.setOnAction(event -> queue.removeAction(action));
        container.setOnContextMenuRequested(event -> menu.show(container, event.getScreenX(), event.getScreenY()));

        ImageView image  = new ImageView(imageFromStatus(action.getStatus()));
        Label     name   = new Label(action.getName());
        Label     status = new Label(stringFromStatus(action.getStatus()));
        name.setFont(Font.font(name.getFont().getName(), FontWeight.BOLD, 16));

        action.addStatusListener((old, value) -> GUI.runNow(() -> {

            image.setImage(imageFromStatus(value));
            status.setText(stringFromStatus(value) + (value == ActionQueue.Status.ERROR ? ": " + action.getException().getMessage() : ""));

            if (value == ActionQueue.Status.RUNNING) {

                GUI.runNow(() -> {

                    Bounds box  = list.localToScene(list.getBoundsInLocal());
                    Bounds item = container.localToScene(container.getBoundsInLocal());

                    if (!box.contains(item)) list.scrollTo(container);

                });

            }

        }));

        image.setImage(imageFromStatus(action.getStatus()));
        status.setText(stringFromStatus(action.getStatus()) + (action.getStatus() == ActionQueue.Status.ERROR ? ": " + action.getException().getMessage() : ""));

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

                Grid window = new Grid(action.getName(), 1);
                Doc  doc    = new Doc(action.getName());

                doc.addImage(imageFromStatus(action.getStatus()))
                   .setAlignment(Doc.Align.CENTRE);

                doc.addHeading(action.getName())
                   .setAlignment(Doc.Align.CENTRE);

                doc.addValue("Status", stringFromStatus(action.getStatus()));

                if (action.getStatus() == ActionQueue.Status.ERROR) {
                    doc.addText(action.getException().getMessage())
                       .setColour(Colour.RED);
                }

                ResultTable data = action.getData();

                window.add(doc);

                if (data != null) {
                    Table table = new Table("Data");
                    window.add(new Table("Data", data));
                }

                (new Thread(window::showAndWait)).start();


            } else if (onClick.containsKey(action)) {

                onClick.get(action).start();

            }

        });

        listItems.put(action, container);

        return container;

    }

    public void setOnClick(ActionQueue.Action action, ClickHandler onClick) {
        this.onClick.put(action, onClick);
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
            public void setOnClick(ClickHandler onClick) {
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

    public interface ActionClick {

        void click(ActionQueue.Action action) throws Exception;

        default void runRegardless(ActionQueue.Action action) {
            try {
                click(action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        default void start(ActionQueue.Action action) {
            (new Thread(() -> runRegardless(action))).start();
        }

    }

}
