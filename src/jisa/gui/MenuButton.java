package jisa.gui;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import jisa.control.SRunnable;

public interface MenuButton extends SubElement {

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
     * Adds a menu item to the menu button's menu.
     *
     * @param text    Text to display
     * @param onClick Action to perform when clicked
     *
     * @return Button object representing the menu item
     */
    Button addItem(String text, SRunnable onClick);

    /**
     * Removes all items from the menu button's menu.
     */
    void removeAllItems();

    /**
     * Adds a separator as the next item in the menu.
     *
     * @return Separator object representing the separator
     */
    Separator addSeparator();

    /**
     * Adds a separator, with heading text, as the next item in the menu.
     *
     * @param heading Heading text
     *
     * @return Separator object representing the separator
     */
    Separator addSeparator(String heading);

    abstract class MenuButtonWrapper implements MenuButton {

        private final javafx.scene.control.MenuButton button;

        protected MenuButtonWrapper(javafx.scene.control.MenuButton button) {
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
        public Button addItem(String text, SRunnable onClick) {

            MenuItem item = new MenuItem(text);
            item.setOnAction(event -> SRunnable.start(onClick));
            GUI.runNow(() -> button.getItems().add(item));

            return new Button.MenuItemWrapper(item) {

                @Override
                public void remove() {
                    button.getItems().remove(item);
                }

            };

        }

        @Override
        public void removeAllItems() {
            button.getItems().clear();
        }

        public Separator addSeparator() {

            SeparatorMenuItem separator = new SeparatorMenuItem();
            GUI.runNow(() -> button.getItems().add(separator));

            return new Separator.MenuSeparatorWrapper(separator) {

                @Override
                public void remove() {
                    GUI.runNow(() -> button.getItems().remove(separator));
                }

            };

        }

        public Separator addSeparator(String heading) {

            Label text = new Label(heading);

            MenuItem          headingItem = new CustomMenuItem(text, false);
            headingItem.setDisable(true);

            SeparatorMenuItem separator   = new SeparatorMenuItem();
            GUI.runNow(() -> button.getItems().addAll(headingItem, separator));

            return new Separator.MenuSeparatorWrapper(separator) {

                @Override
                public void remove() {
                    GUI.runNow(() -> button.getItems().removeAll(headingItem, separator));
                }

            };

        }

    }

}
