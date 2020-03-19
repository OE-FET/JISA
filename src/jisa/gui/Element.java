package jisa.gui;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 * Base class to represent all GUI elements.
 */
public interface Element {

    /**
     * Returns the base pane of the element, to be used when adding to visual containers such as Grid elements.
     *
     * @return Base pane
     */
    Pane getPane();

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

    /**
     * Returns whether this element is currently visible or not.
     *
     * @return Is it visible?
     */
    default boolean isVisible() {
        return getPane().isVisible();
    }

    /**
     * Sets whether this element is currently visible or not.
     *
     * @param visible Should it be visible?
     */
    default void setVisible(boolean visible) {
        getPane().setVisible(visible);
        getPane().setManaged(visible);
    }

    /**
     * Returns any icon being used by this element.
     *
     * @return Icon
     */
    Image getIcon();

}
