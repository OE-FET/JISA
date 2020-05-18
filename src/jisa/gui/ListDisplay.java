package jisa.gui;

import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jisa.control.SRunnable;

import java.util.LinkedList;
import java.util.List;

public class ListDisplay<T> extends JFXElement {

    private final List<DefaultMenuItem<T>> defaultMenuItems = new LinkedList<>();
    @FXML
    protected     ListView<ListItem>       list;
    private       SRunnable                onChange         = null;

    public ListDisplay(String title) {

        super(title, ListDisplay.class.getResource("fxml/ActionQueueWindow.fxml"));
        list.getSelectionModel().selectedIndexProperty().addListener((a, b, c) -> {
            triggerOnChange();
            ((ListItem) getSelected()).triggerOnSelected();
        });

    }

    /**
     * Adds an object to the ListDisplay, displaying it with the given title, sub-title and image, returning a handle
     * in the form of an Item object.
     *
     * @param object   Object to add
     * @param title    Title to show
     * @param subTitle Sub-title to show
     * @param image    Image to show
     *
     * @return Item handle
     */
    public Item<T> add(T object, String title, String subTitle, Image image) {

        ListItem item = new ListItem(object, title, subTitle, image);

        for (DefaultMenuItem<T> menuItem : defaultMenuItems) {
            item.addMenuItem(menuItem.text, () -> menuItem.action.run(item));
        }

        GUI.runNow(() -> list.getItems().add(item));
        return item;
    }

    /**
     * Removes the specified item from the list (if it contains it in the first place).
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public void remove(Item<T> toRemove) {

        if (list.getItems().contains(toRemove)) {
            GUI.runNow(() -> list.getItems().remove(toRemove));
        }

    }

    public void clear() {
        GUI.runNow(() -> list.getItems().clear());
    }

    /**
     * Add a context menu item to all current and future items in the DisplayList.
     *
     * @param text   Text to show on menu option
     * @param action Action to perform when clicked
     */
    public void addDefaultMenuItem(String text, ItemRunnable<T> action) {

        defaultMenuItems.add(new DefaultMenuItem<>(text, action));

        for (ListItem item : list.getItems()) {
            item.addMenuItem(text, () -> action.run(item));
        }

    }

    /**
     * Returns the currently selected item in the list.
     *
     * @return Currently selected item
     */
    public Item<T> getSelected() {
        return list.getSelectionModel().getSelectedItem();
    }

    /**
     * Returns the index of the currently selected item in the list.
     *
     * @return Index of currently selected item
     */
    public int getSelectedIndex() {
        return list.getSelectionModel().getSelectedIndex();
    }

    /**
     * Selects the specified item in the list.
     *
     * @param item Item to select
     */
    public void select(Item<T> item) {
        GUI.runNow(() -> list.getSelectionModel().select((ListItem) item));
    }

    /**
     * Selects the item with the specified index in the list.
     *
     * @param index Index of item to select
     */
    public void select(int index) {
        GUI.runNow(() -> list.getSelectionModel().select(index));
    }

    /**
     * Sets what is done when the currently selected item in the list is changed.
     *
     * @param onChange Action to perform
     */
    public void setOnChange(SRunnable onChange) {
        this.onChange = onChange;
    }

    protected void triggerOnChange() {
        if (onChange != null) onChange.start();
    }

    public interface Item<T> extends SubElement {

        Button addMenuItem(String text, SRunnable action);

        void select();

        void setOnSelected(SRunnable onSelected);

        T getObject();

    }

    public interface ItemRunnable<T> {

        void run(Item<T> item) throws Exception;

        default void runRegardless(Item<T> item) {
            try {
                run(item);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        default void start(Item<T> item) {
            (new Thread(() -> runRegardless(item))).start();
        }

    }

    private static class DefaultMenuItem<T> {

        public final String          text;
        public final ItemRunnable<T> action;

        public DefaultMenuItem(String text, ItemRunnable<T> action) {
            this.text   = text;
            this.action = action;
        }

    }

    private class ListItem extends HBox implements Item<T> {

        private final Label       title       = new Label();
        private final Label       subTitle    = new Label();
        private final ImageView   imageView   = new ImageView();
        private final T           object;
        private final ContextMenu contextMenu = new ContextMenu();
        private       SRunnable   onSelected  = null;

        public ListItem(T object, String titleText, String subTitleText, Image image) {

            this.object = object;

            setSpacing(5.0);
            setAlignment(Pos.CENTER_LEFT);

            imageView.setImage(image);
            title.setText(titleText);
            subTitle.setText(subTitleText);

            title.setFont(Font.font(title.getFont().getName(), FontWeight.BOLD, 16));

            imageView.setFitHeight(32);
            imageView.setFitWidth(32);
            imageView.setSmooth(true);

            VBox vBox = new VBox(title, subTitle);
            vBox.setSpacing(1);
            VBox.setVgrow(title, Priority.NEVER);
            VBox.setVgrow(subTitle, Priority.NEVER);

            getChildren().addAll(imageView, vBox);

            HBox.setHgrow(imageView, Priority.NEVER);
            HBox.setHgrow(vBox, Priority.ALWAYS);

            setOnContextMenuRequested(event -> contextMenu.show(this, event.getScreenX(), event.getScreenY()));

        }

        public Button addMenuItem(String text, SRunnable action) {

            MenuItem menuItem = new MenuItem(text);
            menuItem.setOnAction(event -> action.start());
            contextMenu.getItems().add(menuItem);

            return new Button.MenuItemWrapper(menuItem) {

                @Override
                public void remove() {
                    GUI.runNow(() -> contextMenu.getItems().remove(menuItem));
                }

            };

        }

        @Override
        public void remove() {
            GUI.runNow(() -> list.getItems().remove(this));
        }

        public void select() {
            ListDisplay.this.select(this);
        }

        public void setOnSelected(SRunnable onSelected) {
            this.onSelected = onSelected;
        }

        @Override
        public T getObject() {
            return object;
        }

        protected void triggerOnSelected() {
            if (onSelected != null) onSelected.start();
        }

    }

}
