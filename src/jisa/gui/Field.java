package jisa.gui;

import jisa.control.ConfigBlock;
import jisa.control.SRunnable;
import jisa.gui.SubElement;

public interface Field<T> extends SubElement {

    /**
     * Sets the value displayed in this input field.
     *
     * @param value Value to display
     */
    void set(T value);

    /**
     * Returns the value currently displayed in this input field.
     *
     * @return Value being displayed
     */
    T get();

    /**
     * Alias for get() for Kotlin property access.
     *
     * @return Value being displayed
     */
    default T getValue() { return get(); }

    /**
     * Alias for set(T value) for Kotlin property access.
     *
     * @param value Value to display
     */
    default void setValue(T value) { set(value); }

    /**
     * Sets the action to perform when the value of this input field is changed.
     *
     * @param onChange What to do on change
     */
    void setOnChange(SRunnable onChange);

    /**
     * Edits the available set of discrete options for this field, if applicable.
     *
     * @param values New set of options
     */
    void editValues(String... values);

    /**
     * Returns whether this field is currently disabled.
     *
     * @return Is it disabled
     */
    boolean isDisabled();

    /**
     * Sets whether this field is disabled.
     *
     * @param disabled Should it be disabled?
     */
    void setDisabled(boolean disabled);

    /**
     * Returns whether this field is visible.
     *
     * @return Is it visible?
     */
    boolean isVisible();

    /**
     * Sets whether this field should be visible or not.
     *
     * @param visible Visible?
     */
    void setVisible(boolean visible);

    /**
     * Removes this input field from its Fields container.
     */
    void remove();

    /**
     * Returns the text label next to this input field.
     *
     * @return Text label
     */
    String getText();

    /**
     * Sets the text label displayed next to this input field.
     *
     * @param text Text to display
     */
    void setText(String text);

    default void writeOtherDefaults(ConfigBlock block) {}

    default void loadOtherDefaults(ConfigBlock block) {}

}
