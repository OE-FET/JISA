package jisa.gui;

public interface Button {

    /**
     * Sets whether the button is disabled or not (greyed-out and un-clickable).
     *
     * @param disabled Disabled?
     */
    void setDisabled(boolean disabled);

    /**
     * Returns whether the button is disabled (greyed-out and un-clickable).
     *
     * @return Disabled?
     */
    boolean isDisabled();

    /**
     * Sets whether the button is visible or not.
     *
     * @param visible Visible?
     */
    void setVisible(boolean visible);

    /**
     * Returns whether the button is visible or not.
     *
     * @return Visible?
     */
    boolean isVisible();

    /**
     * Changes the text displayed in the button.
     *
     * @param text New text to display
     */
    void setText(String text);

    /**
     * Returns the text displayed in the button.
     *
     * @return Text in button
     */
    String getText();

    /**
     * Sets what should happen when the button is clicked.
     *
     * @param onClick Lambda
     */
    void setOnClick(ClickHandler onClick);


    /**
     * Removes the button from the GUI.
     */
    void remove();

}
