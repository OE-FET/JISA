package jisa.gui.queue;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import jisa.experiment.queue.Action;
import jisa.experiment.queue.ActionQueue;
import jisa.gui.JFXElement;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActionQueueDisplay extends JFXElement {

    private final ActionQueue actionQueue;

    private final VBox                      list                 = new VBox();
    private final ScrollPane                scrollPane           = new ScrollPane(list);
    private final List<Action>              selected             = new LinkedList<>();
    private final List<DoubleClickListener> doubleClickListeners = new LinkedList<>();
    private final ExecutorService           executor             = Executors.newSingleThreadExecutor();

    public ActionQueueDisplay(String title, ActionQueue actionQueue) {

        super(title);

        setWindowSize(800, 600);

        setCentreNode(scrollPane);

        scrollPane.setFitToWidth(true);
        BorderPane.setMargin(scrollPane, Insets.EMPTY);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setBorder(Border.EMPTY);
        scrollPane.setBackground(Background.EMPTY);
        scrollPane.getChildrenUnmodifiable().addListener((InvalidationListener) a -> scrollPane.getChildrenUnmodifiable().stream().filter(node -> node.getStyleClass().contains("viewport")).forEach(node -> ((Pane) node).setBackground(Background.EMPTY)));

        this.actionQueue = actionQueue;

        list.setSpacing(10.0);

        actionQueue.getActions().stream().map(ActionDisplay::new).peek(ad -> ad.setOnMouseClicked(event -> executor.submit((() -> handleClick(ad, event))))).forEach(list.getChildren()::add);

        actionQueue.addActionListener(actions -> Platform.runLater(() -> {
            list.getChildren().clear();
            actions.stream().map(ActionDisplay::new).peek(ad -> ad.setOnMouseClicked(event -> executor.submit((() -> handleClick(ad, event))))).forEach(list.getChildren()::add);
        }));

    }

    private void handleClick(ActionDisplay ad, MouseEvent event) {

        if (event.getButton() == MouseButton.PRIMARY) {

            if (event.getClickCount() == 2) {

                doubleClickListeners.forEach(l -> l.onDoubleClick(ad.getAction()));

            } else if (event.getClickCount() == 1) {

                if (!event.isShiftDown()) {
                    selected.clear();
                }

                if (ad.isSelected()) {
                    selected.remove(ad.getAction());
                } else {
                    selected.add(ad.getAction());
                }

                list.getChildren().stream().filter(n -> n instanceof ActionDisplay).forEach(n -> {
                    ((ActionDisplay) n).setSelected(selected.contains(((ActionDisplay) n).getAction()));
                });

            }

        }

    }

    public DoubleClickListener addDoubleClickListener(DoubleClickListener listener) {
        doubleClickListeners.add(listener);
        return listener;
    }

    public void removeDoubleClickListener(DoubleClickListener listener) {
        doubleClickListeners.remove(listener);
    }

    public interface DoubleClickListener {
        void onDoubleClick(Action action);
    }

}
