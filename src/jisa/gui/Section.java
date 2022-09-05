package jisa.gui;

import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import jisa.control.SRunnable;

/**
 * A collapsible section box that surrounds one GUI element.
 */
public class Section extends JFXElement {

    public        Pane       pane;
    public        TitledPane titled;
    private final Label      title      = new Label();
    private final BorderPane borderPane = new BorderPane();
    private final HBox       toolBar    = new HBox();

    public Section(String title, Element element) {

        super(title, Section.class.getResource("fxml/Section.fxml"));
        borderPane.setLeft(this.title);
        Pane centre = new Pane();
        centre.setMaxWidth(Double.MAX_VALUE);
        borderPane.setCenter(centre);
        borderPane.setRight(toolBar);
        borderPane.minWidthProperty().bind(titled.widthProperty().subtract(25));

        toolBar.getChildren().addListener((InvalidationListener) observable -> {
            boolean show = !toolBar.getChildren().isEmpty();
            toolBar.setVisible(show);
            toolBar.setManaged(show);
        });

        toolBar.setVisible(false);
        toolBar.setManaged(false);
        toolBar.setPadding(new Insets(0, 10, 0, 10));

        titled.setGraphic(borderPane);

        setElement(element);
        setTitle(title);

    }

    public Section(String title) {
        this(title, null);
    }

    public String getTitle() {
        return title.getText();
    }

    public void setTitle(String title) {
        GUI.runNow(() -> this.title.setText(title));
    }

    public Node getBorderedNode() {

        BorderPane border = new BorderPane();
        border.setCenter(getNode());
        border.setPadding(new Insets(-GUI.SPACING));

        return border;
    }

    /**
     * Sets which GUI element is displayed inside this section.
     *
     * @param element Element to show
     */
    public void setElement(Element element) {

        if (element == null) {
            clear();
        } else {
            GUI.runNow(() -> titled.setContent(element instanceof NotBordered ? ((NotBordered) element).getNoBorderPane(false) : element.getNode()));
        }

    }

    /**
     * Removes the contents of this section.
     */
    public void clear() {
        GUI.runNow(() -> titled.setContent(null));
    }

    public boolean isExpanded() {
        return titled.isExpanded();
    }

    public void setExpanded(boolean expanded) {
        GUI.runNow(() -> titled.setExpanded(expanded));
    }

    public boolean isExpandable() {
        return titled.isCollapsible();
    }

    public void setExpandable(boolean expanded) {
        GUI.runNow(() -> titled.setCollapsible(expanded));
    }

    public Button addTitleButton(String text, SRunnable onClick) {

        javafx.scene.control.Button button = new javafx.scene.control.Button(text);
        button.setOnAction(event -> onClick.start());
        button.setPadding(new Insets(1, 5, 1, 5));

        GUI.runNow(() -> toolBar.getChildren().add(button));

        return new Button.ButtonWrapper(button) {

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getChildren().remove(button));
            }

        };

    }

}
