package jisa.gui.queue;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import jisa.experiment.queue.Action;
import jisa.experiment.queue.ActionQueue;
import jisa.gui.JFXElement;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class ActionQueueMessageDisplay extends JFXElement {

    private ObservableList<Action.Message> messageList  = FXCollections.observableList(new LinkedList<>());
    private TableView<Action.Message>      messageTable = new TableView<>(messageList);

    public ActionQueueMessageDisplay(String title, ActionQueue actionQueue) {

        super(title);

        actionQueue.addMessageListener(message -> Platform.runLater(() -> {

            messageList.add(message);

            messageTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
            messageTable.getColumns().forEach((column) -> {

                Text   t   = new Text(column.getText());
                double max = t.getLayoutBounds().getWidth();

                for (int i = 0; i < messageTable.getItems().size(); i++) {

                    if (column.getCellData(i) != null) {

                        t = new Text(column.getCellData(i).toString());

                        double calcwidth = t.getLayoutBounds().getWidth();

                        if (calcwidth > max) {
                            max = calcwidth;
                        }

                    }
                }

                column.setPrefWidth(max + 10.0d);

            });

            messageTable.scrollTo(message);

        }));

        TableColumn<Action.Message, String> timestamp = new TableColumn<>("Timestamp");
        timestamp.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(LocalDateTime.ofEpochSecond(param.getValue().getTimestamp() / 1000, 0, OffsetDateTime.now().getOffset()).format(DateTimeFormatter.ISO_DATE_TIME).replace("T", " ")));

        TableColumn<Action.Message, String> action = new TableColumn<>("Action");
        action.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getActionPath().stream().map(Action.ActionPathPart::toString).collect(Collectors.joining(" > "))));

        TableColumn<Action.Message, String> type = new TableColumn<>("Type");
        type.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getType().name()));

        TableColumn<Action.Message, String> message = new TableColumn<>("Message");
        message.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getMessage()));

        messageTable.getColumns().addAll(timestamp, action, type, message);

        messageTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        setCentreNode(messageTable);

    }
}
