package jisa.gui.queue;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jisa.experiment.queue.Action;
import jisa.experiment.queue.SweepAction;

import java.util.List;
import java.util.stream.Collectors;

public class ActionDisplay extends VBox {

    public static final  Background BG_SELECTED    = new Background(new BackgroundFill(Color.web("#0096C9"), null, null));
    public static final  Background BG_NORMAL      = new Background(new BackgroundFill(Color.WHITE, null, null));
    public static final  Background BG_HOVER       = new Background(new BackgroundFill(Color.gray(0.98), null, null));
    public static final  Background BG_LABEL       = new Background(new BackgroundFill(Color.gray(0.80), null, null));
    private static final Border     BORDER         = new Border(new BorderStroke(Color.SILVER, BorderStrokeStyle.SOLID, null, new BorderWidths(1)));
    private static final Border     BORDER_PARTIAL = new Border(new BorderStroke(Color.SILVER, BorderStrokeStyle.SOLID, null, new BorderWidths(1.0, 0.0, 0.0, 0.0)));

    private final ImageView statusImage  = new ImageView();
    private final Label     titleLabel   = new Label();
    private final Label     statusLabel  = new Label();
    private final Label     messageLabel = new Label();
    private final VBox      titleLines   = new VBox(new HBox(titleLabel, statusLabel), messageLabel);
    private final HBox      topLine      = new HBox(statusImage, titleLines);
    private final Pane      childLabel   = new Pane();
    private final VBox      childList    = new VBox();
    private final HBox      childBox     = new HBox(childLabel, childList);

    private boolean selected = false;

    public ActionDisplay(Action action) {

        HBox.setHgrow(titleLines, Priority.ALWAYS);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        HBox.setHgrow(statusLabel, Priority.NEVER);

        titleLabel.setMaxWidth(Double.MAX_VALUE);

        setPadding(new Insets(10.0));
        setSpacing(10.0);
        setBackground(BG_NORMAL);
        setBorder(BORDER);

        childLabel.setMaxWidth(Double.MAX_VALUE);
        childLabel.setPadding(new Insets(10.0));
        childLabel.setBackground(BG_LABEL);

        setOnMouseEntered(event -> setBackground(selected ? BG_SELECTED : BG_HOVER));
        setOnMouseExited(event -> setBackground(selected ? BG_SELECTED : BG_NORMAL));

        topLine.setSpacing(10.0);
        titleLines.setSpacing(50.0);

        getChildren().addAll(topLine);

        action.addMessageListener(message -> {

            if (message.getActionPath().get(message.getActionPath().size() - 1).getAction() == action) {

                switch (message.getType()) {

                    case INFO:
                        Platform.runLater(() -> messageLabel.setText(message.getMessage()));
                        break;

                    case WARNING:
                        Platform.runLater(() -> messageLabel.setText("WARNING: " + message.getMessage()));
                        break;

                    case ERROR:
                        Platform.runLater(() -> messageLabel.setText("ERROR: " + message.getMessage()));
                        break;

                }

            }

        });

        action.addStatusListener(status -> Platform.runLater(() -> {
            statusImage.setImage(status.getImage());
            statusLabel.setText(status.getText());
        }));

        titleLabel.setText(action.getName());
        titleLabel.setFont(Font.font(titleLabel.getFont().getFamily(), FontWeight.BOLD, 16));
        titleLines.setSpacing(10);
        statusImage.setFitWidth(35);
        statusImage.setFitHeight(35);
        statusImage.setImage(action.getStatus().getImage());
        statusLabel.setText(action.getStatus().getText());
        statusLabel.setTextFill(Color.GRAY);

        if (action instanceof SweepAction) {

            SweepAction sweepAction = (SweepAction) action;
            childList.setSpacing(-2.0);
            VBox.setMargin(childBox, new Insets(0.0, -10.0, -10.0, -10.0));
            childList.getChildren().addAll((List<ActionDisplay>) sweepAction.generateActions(sweepAction.getCurrentSweepValue()).stream().map(a -> new ActionDisplay((Action) a)).peek(ad -> ((ActionDisplay) ad).setBorder(BORDER_PARTIAL)).collect(Collectors.toList()));

            sweepAction.addSweepActionListener(actions -> Platform.runLater(() -> {
                childList.getChildren().clear();
                childList.getChildren().addAll(actions.stream().map(ActionDisplay::new).peek(ad -> ad.setBorder(BORDER_PARTIAL)).collect(Collectors.toList()));
            }));

            HBox.setHgrow(childLabel, Priority.NEVER);
            HBox.setHgrow(childList, Priority.ALWAYS);

            getChildren().add(childBox);

        }

    }

}