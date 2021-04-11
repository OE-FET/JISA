package jisa.gui.queue;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jisa.Util;
import jisa.experiment.queue.Action;
import jisa.experiment.queue.ActionQueue;
import jisa.gui.GUI;
import jisa.gui.JFXElement;
import jisa.maths.Range;

import java.util.*;
import java.util.stream.Collectors;

public class ActionQueueDisplay extends JFXElement {

    static {
        GUI.touch();
    }

    private final ScrollPane         scrollPane;
    private final VBox               list     = new VBox();
    private final ActionQueue        queue;
    private final Set<ActionDisplay> selected = new HashSet<>();

    public ActionQueueDisplay(String title, ActionQueue queue) {

        super(title, new ScrollPane());

        this.scrollPane = (ScrollPane) getNode().getCenter();
        list.setMaxWidth(Double.MAX_VALUE);
        list.setMaxHeight(Double.MAX_VALUE);
        scrollPane.setMinWidth(500.0);
        scrollPane.setMinHeight(300.0);
        scrollPane.setContent(list);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(Background.EMPTY);
        scrollPane.widthProperty().addListener(l -> ((Region) scrollPane.lookup(".viewport")).setBackground(Background.EMPTY));
        BorderPane.setMargin(scrollPane, Insets.EMPTY);
        scrollPane.setPadding(new Insets(GUI.SPACING));
        list.setBackground(Background.EMPTY);
        this.queue = queue;

        this.list.setSpacing(10.0);

        queue.addListener((added, removed, moved) -> GUI.runNow(() -> {
            addActions(added);
            removeActions(removed);
            moveActions(moved);
        }));

        addActions(queue.getActions());

    }

    public List<Action> getSelectedActions() {
        return selected.stream().map(ActionDisplay::getAction).collect(Collectors.toUnmodifiableList());
    }

    public List<Integer> getSelectedIndices() {
        List<Action<?>> actions = this.queue.getActions();
        return selected.stream().map(it -> actions.indexOf(it.getAction())).filter(it -> it > -1).collect(Collectors.toList());
    }

    public void setSelectedActions(Action... actions) {
        setSelectedActions(List.of(actions));
    }

    public void setSelectedActions(Collection<Action> actions) {
        selected.clear();
        selected.addAll(list.getChildren().stream().filter(it -> it instanceof ActionDisplay).map(it -> (ActionDisplay) it).filter(it -> actions.contains(it.getAction())).collect(Collectors.toList()));
        updateSelected();
    }

    public void setSelectedIndices(Collection<Integer> indices) {
        List<Action<?>> actions = this.queue.getActions();
        setSelectedActions(indices.stream().filter(it -> it > -1 && it < actions.size()).map(actions::get).collect(Collectors.toList()));
    }

    protected void updateSelected() {
        GUI.runNow(() -> {
            list.getChildren().forEach(it -> ((ActionDisplay) it).setSelected(false));
            selected.forEach(it -> it.setSelected(true));
        });
    }

    protected void addActions(Collection<Action<?>> actions) {

        list.getChildren().addAll(
            actions.stream()
                   .map(Action::getDisplay)
                   .peek(a -> a.setOnMouseClicked(e -> Util.runAsync(() -> onActionClicked(a, e))))
                   .peek(a -> a.addRunningListener(this::scrollToNode))
                   .collect(Collectors.toList())
        );

    }

    protected void onActionClicked(ActionDisplay<?> actionDisplay, MouseEvent mouseEvent) {

        if (!queue.isRunning() && mouseEvent.getClickCount() >= 2) {

            actionDisplay.getAction().userEdit();

        } else if (mouseEvent.isControlDown() && !actionDisplay.isSelected()) {

            selected.add(actionDisplay);
            updateSelected();

        } else if (mouseEvent.isControlDown() && actionDisplay.isSelected()) {

            selected.remove(actionDisplay);
            updateSelected();

        } else if (mouseEvent.isShiftDown() && !selected.isEmpty()) {

            int index = list.getChildren().indexOf(actionDisplay);
            int closest = selected.stream()
                                  .map(list.getChildren()::indexOf)
                                  .min(Comparator.comparingInt(i -> Math.abs(i - index)))
                                  .orElse(index);

            for (int i : Range.count(closest, index)) {
                selected.add((ActionDisplay) list.getChildren().get(i));
            }

            updateSelected();

        } else if (actionDisplay.isSelected() && selected.size() == 1) {

            selected.clear();
            updateSelected();

        } else {

            selected.clear();
            selected.add(actionDisplay);
            updateSelected();

        }

    }

    public void scrollToAction(Action<?> action) {

        scrollToNode(
            list.getChildren()
                .stream()
                .filter(it -> it instanceof ActionDisplay)
                .filter(it -> ((ActionDisplay) it).getAction() == action)
                .findFirst()
                .orElse(null)
        );

    }

    public synchronized void scrollToNode(Node node) {

        if (node != null) {

            Bounds scroll  = scrollPane.getViewportBounds();
            Bounds content = scrollPane.getContent().getBoundsInLocal();
            Bounds item    = scrollPane.getContent().sceneToLocal(node.localToScene(node.getBoundsInLocal()));

            if (!scrollPane.localToScene(scrollPane.getBoundsInLocal()).contains(node.localToScene(node.getBoundsInLocal()))) {

                GUI.runNow(() -> {

                    double h = content.getHeight();
                    double y = item.getMinY();
                    double v = scroll.getHeight();

                    Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(scrollPane.vvalueProperty(), scrollPane.getVvalue())),
                        new KeyFrame(Duration.millis(250), new KeyValue(scrollPane.vvalueProperty(), scrollPane.getVmax() * (y / (h - v))))
                    );

                    timeline.playFromStart();

                });

            }

        }

    }

    protected void removeActions(Collection<Action<?>> actions) {
        list.getChildren().removeIf(it -> actions.contains(((ActionDisplay) it).getAction()));
    }

    protected void moveActions(Map<Integer, Action<?>> map) {

        Map<Integer, ActionDisplay<?>> displayMap = new LinkedHashMap<>();

        map.forEach((i, a) -> {

            ActionDisplay<?> display = list.getChildren()
                                           .stream()
                                           .filter(it -> it instanceof ActionDisplay)
                                           .filter(it -> ((ActionDisplay) it).getAction() == a)
                                           .map(it -> (ActionDisplay<?>) it)
                                           .findFirst()
                                           .orElse(null);

            displayMap.put(i, display);

        });

        List<Node> children = new ArrayList<>(list.getChildren());

        displayMap.forEach(children::set);
        list.getChildren().setAll(children);

    }

    public void setExpanded(boolean show) {

        getAllNodes().stream()
                     .filter(it -> it instanceof SweepActionDisplay)
                     .map(it -> (SweepActionDisplay<?>) it)
                     .forEach(it -> it.setShowAll(show));

        getAllNodes().stream()
                     .filter(it -> it instanceof SimpleActionDisplay)
                     .map(it -> (SimpleActionDisplay) it)
                     .forEach(it -> it.setShowAll(show));

    }

    protected List<Node> getAllNodes() {
        List<Node> nodes = new LinkedList<>();
        addAllDescendents(this.list, nodes);
        return nodes;
    }

    private static void addAllDescendents(Parent parent, List<Node> nodes) {

        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent) { addAllDescendents((Parent) node, nodes); }
        }

    }

}
