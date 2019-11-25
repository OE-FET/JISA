package jisa.gui;

/**
 * Represents handles returned by GUI elements when adding sub-elements such as buttons, input fields and separators etc.
 */
public interface SubElement {

    /**
     * Returns whether this sub-element is visible or not.
     *
     * @return Shown?
     */
    boolean isVisible();

    /**
     * Sets whether this sub-element is visible or not.
     *
     * @param visible Show?
     */
    void setVisible(boolean visible);

    /**
     * Removes the sub-element from its parent.
     */
    void remove();

}
