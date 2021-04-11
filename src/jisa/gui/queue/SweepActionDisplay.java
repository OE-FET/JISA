package jisa.gui.queue;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;
import jisa.experiment.queue.Action;
import jisa.experiment.queue.Listener;
import jisa.experiment.queue.SweepAction;
import jisa.gui.GUI;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SweepActionDisplay<T> extends ActionDisplay {

    private static final Border FULL_BORDER = new Border(new BorderStroke(Color.SILVER, BorderStrokeStyle.SOLID, null, new BorderWidths(1)));
    private static final Border SUB_BORDER  = new Border(new BorderStroke(Color.SILVER, BorderStrokeStyle.SOLID, null, new BorderWidths(1, 0, 0, 1)));

    private static boolean showAll = false;

    protected final SweepAction<T>                   action;
    protected final ImageView                        imageView      = new ImageView();
    protected final Label                            titleLabel     = new Label();
    protected final Label                            statusLabel    = new Label();
    protected final ChoiceBox<T>                     values         = new ChoiceBox<>();
    protected final VBox                             titleBox       = new VBox(titleLabel, statusLabel);
    protected final VBox                             tags           = new VBox();
    protected final HBox                             mainBox        = new HBox(imageView, titleBox, tags);
    protected final Label                            childLabel     = new Label("Sweep Actions");
    protected final HBox                             childHeader    = new HBox(childLabel, values);
    protected final VBox                             children       = new VBox();
    protected final VBox                             container      = new VBox();
    private final   List<Listener<ActionDisplay<?>>> childListeners = new LinkedList<>();


    public SweepActionDisplay(SweepAction<T> action) {

        super(action);

        getChildren().add(container);

        this.action = action;

        VBox.setVgrow(this, Priority.NEVER);

        mainBox.setSpacing(10);
        mainBox.setPadding(new Insets(10));
        mainBox.setAlignment(Pos.CENTER_LEFT);

        HBox.setHgrow(imageView, Priority.NEVER);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        HBox.setHgrow(values, Priority.NEVER);
        HBox.setHgrow(childLabel, Priority.ALWAYS);

        VBox.setVgrow(titleLabel, Priority.NEVER);
        VBox.setVgrow(statusLabel, Priority.NEVER);

        titleLabel.setFont(Font.font(titleLabel.getFont().getFamily(), FontWeight.BOLD, 16));
        childLabel.setFont(Font.font(titleLabel.getFont().getFamily(), FontWeight.BOLD, 16));
        childLabel.setTextAlignment(TextAlignment.CENTER);
        childLabel.setMaxWidth(Double.MAX_VALUE);
        childHeader.setBackground(new Background(new BackgroundFill(Color.gray(0.98), null, null)));
        childHeader.setAlignment(Pos.CENTER_LEFT);
        childHeader.setPadding(new Insets(5, 10, 5, 10));
        childHeader.setBorder(new Border(new BorderStroke(Color.SILVER, BorderStrokeStyle.SOLID, null, new BorderWidths(1, 1, 0, 1))));

        imageView.setFitWidth(35);
        imageView.setFitHeight(35);

        tags.setAlignment(Pos.CENTER_LEFT);

        titleBox.setSpacing(0.0);

        imageView.setImage(action.getStatus().getImage());
        titleLabel.setText(action.getName());
        statusLabel.setText(action.getStatus().getText());

        action.addNameListener(it -> GUI.runNow(() -> titleLabel.setText(it.getName())));

        action.addStatusListener(it -> GUI.runNow(() -> {

            imageView.setImage(it.getStatus().getImage());
            statusLabel.setText(it.getStatus() == Action.Status.ERROR ? String.format("%s: %s", it.getStatus().getText(), it.getError().getMessage()) : it.getStatus().getText());

            if (it.getStatus() == Action.Status.RUNNING) {
                triggerRunningListeners(this);
            }

        }));

        container.getChildren().addAll(mainBox, childHeader, children);

        children.getChildren().addListener((InvalidationListener) l -> {
            boolean visible = children.getChildren().size() > 0;
            children.setVisible(visible);
            children.setManaged(visible);
        });

        children.setBorder(new Border(new BorderStroke(Color.SILVER, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 1, 1, 0))));

        VBox.setMargin(children, new Insets(0, 10, 10, 10));
        VBox.setMargin(childHeader, new Insets(0, 10, 0, 10));

        values.setConverter(new StringConverter<T>() {

            @Override
            public String toString(T value) {

                if (value == null) {
                    return "All";
                }

                return action.format(value);

            }

            @Override
            public T fromString(String string) {

                if (string.equals("All")) {
                    return null;
                }

                return action.getSweepValues().stream().filter(it -> action.format(it).equals(string)).findFirst().orElse(null);

            }

        });


        values.valueProperty().addListener(e -> drawChildren());

        action.addSweepValueListener(it -> GUI.runNow(() -> {
            if (values.getValue() != null) {
                values.setValue(it);
            }
        }));
        action.addChildrenListener(it -> GUI.runNow(() -> {
            values.getItems().clear();
            values.getItems().add(null);
            values.getItems().addAll(action.getSweepValues());
            values.setValue(action.getCurrentSweepValue());
        }));

        action.addAttributeListener(l -> drawTags());

        values.getItems().add(null);
        values.getItems().addAll(action.getSweepValues());
        values.setValue(showAll ? null : action.getCurrentSweepValue());

        drawTags();
        drawChildren();

    }

    public void setSelected(boolean selected) {
        titleLabel.setTextFill(selected ? Color.WHITE : Color.BLACK);
        statusLabel.setTextFill(selected ? Color.WHITE : Color.BLACK);
        tags.getChildren().forEach(l -> ((Label) l).setTextFill(selected ? Color.WHITE : Color.BLACK));
        super.setSelected(selected);
    }

    protected void drawTags() {

        GUI.runNow(() -> {

            tags.getChildren().clear();

            for (String tag : action.getTags()) {

                Label label = new Label(tag);
                tags.getChildren().add(label);
                VBox.setVgrow(label, Priority.NEVER);

            }

        });

    }

    protected void drawChildren() {

        GUI.runNow(() -> {

            children.getChildren().forEach(it -> ((ActionDisplay<?>) it).removeRunningListeners(childListeners));
            childListeners.clear();
            children.getChildren().clear();
            children.getChildren().addAll(
                (values.getValue() == null ? action.getChildren() : action.getChildrenByValue(values.getValue()))
                    .stream()
                    .map(Action::getDisplay)
                    .peek(a -> a.setBorder(SUB_BORDER))
                    .peek(a -> childListeners.add(a.addRunningListener(l -> triggerRunningListeners((ActionDisplay<?>) l))))
                    .collect(Collectors.toList())
            );

        });

    }


    @Override
    public Action<?> getAction() {
        return action;
    }

    public void setShowAll(boolean show) {

        if (show) {
            GUI.runNow(() -> values.setValue(null));
        } else {
            GUI.runNow(() -> values.setValue(action.getCurrentSweepValue()));
        }

        showAll = show;

    }

}