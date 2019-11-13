package jisa.gui;

import jisa.control.SRunnable;

public interface Field<T> extends SubElement {

    /**
     * Sets the value displayed in the field.
     *
     * @param value Value to display
     */
    void set(T value);

    /**
     * Returns the value currently displayed in the field.
     *
     * @return Value displayed
     */
    T get();

    /**
     * Set what action should be taken when the value displayed in the field is changed.
     *
     * @param onChange Action on change
     */
    void setOnChange(SRunnable onChange);

    /**
     * For fields with discrete options this will change what those values are.
     *
     * @param values New values to use as options
     */
    void editValues(String... values);

    /**
     * Returns whether the field is disabled and thus currently refusing input.
     *
     * @return Is the field disabled?
     */
    boolean isDisabled();

    /**
     * Sets whether the field is disabled and thus should refuse input.
     *
     * @param disabled Should it be disabled?
     */
    void setDisabled(boolean disabled);

    /**
     * Returns whether the field is currently visible or not.
     *
     * @return Is it visible?
     */
    boolean isVisible();

    /**
     * Sets whether the field is visible or not.
     *
     * @param visible Show?
     */
    void setVisible(boolean visible);

    /**
     * Removes the field from its respective Fields element.
     */
    void remove();

    /**
     * Returns the text displayed next to the input field.
     *
     * @return Text being displayed
     */
    String getText();

    /**
     * Sets the text to be displayed next to the input field.
     *
     * @param text Text to display
     */
    void setText(String text);

}
