package jisa.gui;

import jisa.control.SRunnable;

public interface Button extends SubElement {

    /**
     * Returns whether the button is disabled (greyed-out and un-clickable).
     *
     * @return Disabled?
     */
    boolean isDisabled();

    /**
     * Sets whether the button is disabled or not (greyed-out and un-clickable).
     *
     * @param disabled Disabled?
     */
    void setDisabled(boolean disabled);

    /**
     * Returns whether the button is visible or not.
     *
     * @return Visible?
     */
    boolean isVisible();

    /**
     * Sets whether the button is visible or not.
     *
     * @param visible Visible?
     */
    void setVisible(boolean visible);

    /**
     * Returns the text displayed in the button.
     *
     * @return Text in button
     */
    String getText();

    /**
     * Changes the text displayed in the button.
     *
     * @param text New text to display
     */
    void setText(String text);

    /**
     * Sets what should happen when the button is clicked.
     *
     * @param onClick Lambda
     */
    void setOnClick(SRunnable onClick);


    /**
     * Removes the button from the GUI.
     */
    void remove();

    abstract class ButtonWrapper implements Button {

        private final javafx.scene.control.Button button;

        public ButtonWrapper(javafx.scene.control.Button button) {
            this.button = button;
        }

        @Override
        public boolean isDisabled() {
            return button.isDisabled();
        }

        @Override
        public void setDisabled(boolean disabled) {
            GUI.runNow(() -> button.setDisable(disabled));
        }

        @Override
        public boolean isVisible() {
            return button.isVisible();
        }

        @Override
        public void setVisible(boolean visible) {

            GUI.runNow(() -> {
                button.setVisible(visible);
                button.setManaged(visible);
            });

        }

        @Override
        public String getText() {
            return button.getText();
        }

        @Override
        public void setText(String text) {
            GUI.runNow(() -> button.setText(text));
        }

        @Override
        public void setOnClick(SRunnable onClick) {
            button.setOnAction(event -> SRunnable.start(onClick));
        }

    }

    abstract class MenuItemWrapper implements Button {

        private final javafx.scene.control.MenuItem button;

        public MenuItemWrapper(javafx.scene.control.MenuItem button) {
            this.button = button;
        }

        @Override
        public boolean isDisabled() {
            return button.isDisable();
        }

        @Override
        public void setDisabled(boolean disabled) {
            GUI.runNow(() -> button.setDisable(disabled));
        }

        @Override
        public boolean isVisible() {
            return button.isVisible();
        }

        @Override
        public void setVisible(boolean visible) {
            GUI.runNow(() -> button.setVisible(visible));
        }

        @Override
        public String getText() {
            return button.getText();
        }

        @Override
        public void setText(String text) {
            GUI.runNow(() -> button.setText(text));
        }

        @Override
        public void setOnClick(SRunnable onClick) {
            button.setOnAction(event -> SRunnable.start(onClick));
        }

    }

}
