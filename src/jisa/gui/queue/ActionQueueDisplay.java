package jisa.gui.queue;

import javafx.application.Platform;
import javafx.scene.layout.VBox;
import jisa.experiment.queue.ActionQueue;
import jisa.gui.JFXElement;

public class ActionQueueDisplay extends JFXElement {

    private final ActionQueue actionQueue;
    private final VBox        list = new VBox();

    public ActionQueueDisplay(String title, ActionQueue actionQueue) {

        super(title);

        setWindowSize(800, 600);

        setCentreNode(list);

        this.actionQueue = actionQueue;

        list.setSpacing(10.0);

        actionQueue.getActions().stream().map(ActionDisplay::new).forEach(list.getChildren()::add);

        actionQueue.addActionListener(actions -> Platform.runLater(() -> {
            list.getChildren().clear();
            actions.stream().map(ActionDisplay::new).forEach(list.getChildren()::add);
        }));

    }


}
