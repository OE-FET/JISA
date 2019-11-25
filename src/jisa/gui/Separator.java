package jisa.gui;

public interface Separator extends SubElement {

    /**
     * Removes this separator from its GUI element.
     */
    void remove();

    /**
     * Returns whether this separator is visible.
     *
     * @return Is it visible?
     */
    boolean isVisible();

    /**
     * Sets whether this separator is visible or not.
     */
    void setVisible(boolean visible);

}
