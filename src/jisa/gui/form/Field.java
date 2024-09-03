package jisa.gui.form;

import jisa.control.ConfigBlock;
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
    default T getValue() {
        return get();
    }

    /**
     * Alias for set(T value) for Kotlin property access.
     *
     * @param value Value to display
     */
    default void setValue(T value) {
        set(value);
    }

    /**
     * Sets an action to perform when the value of this input field is changed.
     *
     * @param onChange What to do on change
     */
    Listener<T> addChangeListener(Listener<T> onChange);

    default Listener<T> addChangeListener(Runnable onChange) {
        return addChangeListener(e -> onChange.run());
    }

    /**
     * Removes the given listener from the field, causing it to no-longer be called when the value is changed.
     *
     * @param onChange The listener to remove
     */
    void removeChangeListener(Listener<T> onChange);

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

    default void writeOtherDefaults(ConfigBlock block) {
    }

    default void loadOtherDefaults(ConfigBlock block) {
    }

    interface Listener<T> {
        void valueChanged(T value);
    }

}
