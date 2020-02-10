package jisa.gui;

import javafx.scene.Node;
import javafx.scene.control.SeparatorMenuItem;

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

    abstract class SeparatorWrapper implements Separator {

        private final javafx.scene.control.Separator separator;

        protected SeparatorWrapper(javafx.scene.control.Separator separator) {
            this.separator = separator;
        }

        @Override
        public boolean isVisible() {
            return separator.isVisible();
        }

        @Override
        public void setVisible(boolean visible) {

            GUI.runNow(() -> {
                separator.setVisible(visible);
                separator.setManaged(visible);
            });

        }
    }

    abstract class MenuSeparatorWrapper implements Separator {

        private final SeparatorMenuItem separator;

        protected MenuSeparatorWrapper(SeparatorMenuItem separator) {
            this.separator = separator;
        }

        @Override
        public boolean isVisible() {
            return separator.isVisible();
        }

        @Override
        public void setVisible(boolean visible) {
            GUI.runNow(() -> separator.setVisible(visible));
        }
    }

}
