package jisa.gui;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * Base class to represent all GUI elements.
 */
public interface Element {

    /**
     * Returns the JavaFX node of the element.
     *
     * @return
     */
    Node getNode();

    default Node getBorderedNode() {
        return new ElementBorder(titleProperty(), getNode());
    }

    /**
     * Returns the title of the element.
     *
     * @return Title of element
     */
    String getTitle();

    /**
     * Sets the title of the element
     *
     * @param title Title of element
     */
    void setTitle(String title);

    ObjectProperty<String> titleProperty();

    /**
     * Returns any icon being used by this element.
     *
     * @return Icon
     */
    Image getIcon();

    /**
     * Returns whether this element is currently visible or not.
     *
     * @return Is it visible?
     */
    default boolean isVisible() {
        return getNode().isVisible();
    }

    /**
     * Sets whether this element is currently visible or not.
     *
     * @param visible Should it be visible?
     */
    default void setVisible(boolean visible) {
        getNode().setVisible(visible);
        getNode().setManaged(visible);
    }

    class ElementBorder extends TitledPane {

        private final BorderPane container = new BorderPane();

        public ElementBorder(ObjectProperty<String> title, Node content) {

            setMaxHeight(Double.MAX_VALUE);
            setMaxWidth(Double.MAX_VALUE);
            setCollapsible(false);
            setContent(container);
            textProperty().bind(title);
            container.setCenter(content);
            container.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
            container.setPadding(new Insets(-10));

        }

    }

}
