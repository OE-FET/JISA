package jisa.gui;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import jisa.control.SRunnable;

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

    /**
     * Shows the element to the user within its own window.
     */
    void show();

    /**
     * Shows the element in its own window to the user and waits until the window is closed.
     */
    void showAndWait();

    /**
     * Shows the element to the user in its own window with an OK button at the bottom, waits until the user closes the window
     * (by pressing OK or otherwise) before returning.
     */
    void showAsAlert();

    /**
     * Shows the element to the user in its own window with the specified buttons at the bottom, waits until the user
     * clicks one of the buttons (or otherwise closes the window) before returning the index of the button chosen.
     *
     * @param buttons Text of buttons to show
     *
     * @return Index of button clicked
     */
    int showAsDialog(String... buttons);

    /**
     * Shows the element to the user in its own window with "OK" and "Cancel" buttons. Returns true when the user
     * clicks "OK", false when they click "Cancel". Waits until a button is clicked (which also closes the window).
     *
     * @return OK or Cancel?
     */
    boolean showAsConfirmation();

    /**
     * Hide the window, without freeing up the graphical resources used to show it, allowing it to be shown again
     * quickly.
     */
    void hide();

    /**
     * Closes the window fully.
     */
    void close();

    boolean isShowing();

    SRunnable addCloseListener(SRunnable onClose);

    void removeCloseListener(SRunnable onClose);

}
