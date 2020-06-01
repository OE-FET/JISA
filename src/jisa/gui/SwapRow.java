package jisa.gui;

import javafx.animation.*;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import jisa.Util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class SwapRow extends JFXElement {

    private final HBox       grid;
    private final ImageView  imageView     = new ImageView();
    private final List<Item> items         = new LinkedList<>();
    private       int        currentConfig = 0;

    public SwapRow(String title) {
        this(title, new HBox());
    }

    private SwapRow(String title, HBox grid) {
        super(title, grid);
        this.grid = grid;
    }

    public void add(Element element, int... configs) {

        Node       node = element.getBorderedNode();
        ScrollPane pane = new ScrollPane(node);
        pane.setBackground(Background.EMPTY);
        pane.setFitToWidth(true);
        pane.setFitToHeight(true);
        pane.setMinHeight(element.getNode().minHeight(-1) + 35);
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Item item = new Item(element, pane, configs);
        items.add(item);

        if (item.isInConfig(currentConfig)) {
            item.showImmediately();
        } else {
            item.hideImmediately();
        }

        GUI.runNow(() -> {
            grid.getChildren().add(pane);
            HBox.setHgrow(pane, Priority.ALWAYS);
            HBox.setMargin(pane, new Insets(GUI.SPACING / 2));
            pane.setMaxWidth(Double.MAX_VALUE);
        });
    }

    public int getConfiguration() {
        return currentConfig;
    }

    public void setConfiguration(int config) {

        GUI.runNow(() -> {
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Colour.TRANSPARENT);
            Bounds      bounds = getNode().getCenter().getBoundsInLocal();
            Rectangle2D rect   = new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
            parameters.setViewport(rect);
            imageView.setImage(getNode().getCenter().snapshot(null, null));
            getNode().setCenter(imageView);
        });

        ParallelTransition transition = new ParallelTransition();

        for (Item item : items) {

            if (item.isInConfig(currentConfig) && !item.isInConfig(config)) {
                item.hideImmediately();
            }

            if (!item.isInConfig(currentConfig) && item.isInConfig(config)) {
                item.showImmediately();
            }

        }

        GUI.runNow(grid::layout);

        Map<Item, Double>  width   = items.stream().collect(Collectors.toMap(i -> i, i -> i.getPane().isManaged() ? i.getPane().getWidth() : 0.0));
        Map<Item, Double>  opacity = items.stream().collect(Collectors.toMap(i -> i, i -> i.getPane().getOpacity()));
        Map<Item, Boolean> managed = items.stream().collect(Collectors.toMap(i -> i, i -> i.getPane().isManaged()));

        for (Item item : items) {

            if (item.isInConfig(currentConfig)) {
                item.showImmediately();
            } else {
                item.hideImmediately();
            }

        }

        transition.getChildren().setAll(items.stream().map(i -> i.animate(width.get(i), opacity.get(i), managed.get(i))).collect(Collectors.toList()));

        Semaphore semaphore = new Semaphore(0);

        transition.setOnFinished(event -> semaphore.release());

        GUI.runNow(() -> getNode().setCenter(grid));
        GUI.runNow(transition::playFromStart);
        currentConfig = config;

        try {
            semaphore.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void remove(Element element) {

        List<Item>   matches = items.stream().filter(v -> v.getElement() == element).collect(Collectors.toList());
        List<Region> panes   = matches.stream().map(Item::getPane).collect(Collectors.toList());

        items.removeAll(matches);

        GUI.runNow(() -> grid.getChildren().removeAll(panes));

    }

    public void clear() {
        GUI.runNow(() -> grid.getChildren().clear());
        items.clear();
    }

    public Node getBorderedNode() {

        BorderPane border = new BorderPane();
        border.setCenter(getNode());
        border.setPadding(new Insets(-1.5 * GUI.SPACING));

        return border;

    }

    private static class Item {

        private final Element       element;
        private final Region        pane;
        private final List<Integer> configs = new LinkedList<>();

        public Item(Element element, Region pane, int... configs) {
            this.element = element;
            this.pane    = pane;
            for (int i : configs) this.configs.add(i);
        }

        public Element getElement() {
            return element;
        }

        public Region getPane() {
            return pane;
        }

        public List<Integer> getConfigs() {
            return new ArrayList<>(configs);
        }

        public boolean isInConfig(int config) {
            return configs.contains(config);
        }

        public void hideImmediately() {

            GUI.runNow(() -> {
                pane.setManaged(false);
                pane.setMaxWidth(0.0);
                pane.setOpacity(0.0);
            });

        }

        public void showImmediately() {

            GUI.runNow(() -> {
                pane.setManaged(true);
                pane.setMaxWidth(Double.MAX_VALUE);
                pane.setOpacity(1.0);
            });

        }

        public Animation animate(double width, double opacity, boolean managed) {

            Timeline animation = new Timeline();

            animation.getKeyFrames().setAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(pane.maxWidthProperty(), pane.isManaged() ? pane.getWidth() : 0.0)),
                    new KeyFrame(Duration.ZERO, new KeyValue(pane.opacityProperty(), pane.getOpacity())),
                    new KeyFrame(Duration.millis(250), new KeyValue(pane.maxWidthProperty(), width)),
                    new KeyFrame(Duration.millis(250), new KeyValue(pane.opacityProperty(), opacity))
            );

            if (managed) {
                GUI.runNow(() -> pane.setManaged(true));
                animation.setOnFinished(event -> pane.setMaxWidth(Double.MAX_VALUE));
            } else {
                animation.setOnFinished(event -> pane.setManaged(false));
            }

            return animation;

        }

    }

}
