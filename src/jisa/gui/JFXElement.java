package jisa.gui;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jisa.Util;
import jisa.control.SRunnable;
import jisa.enums.Icon;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class JFXElement implements Element {

    private final BorderPane             borderPane;
    private final ObjectProperty<Image>  icon      = new SimpleObjectProperty<>(null);
    private final ObjectProperty<String> title     = new SimpleObjectProperty<>("");
    private final DoubleProperty         width     = new SimpleDoubleProperty(-1);
    private final DoubleProperty         height    = new SimpleDoubleProperty(-1);
    private final DoubleProperty         maxWidth  = new SimpleDoubleProperty(Double.MAX_VALUE);
    private final DoubleProperty         maxHeight = new SimpleDoubleProperty(Double.MAX_VALUE);
    private final ToolBar                toolBar;
    private final ButtonBar              buttonBar;
    private final Scene                  scene;
    private       Stage                  stage     = null;

    static {
        GUI.touch();
    }

    public JFXElement(String title, Node centre) {

        // Make sure the GUI thread has started
        GUI.touch();
        this.title.set(title);

        borderPane = new BorderPane();
        scene      = new Scene(borderPane);
        scene.setFill(Colour.string("#f4f4f4"));
        borderPane.setBackground(Background.EMPTY);
        borderPane.setCenter(centre);

        toolBar   = new ToolBar();
        buttonBar = new ButtonBar();

        setUpBars();


    }

    /**
     * Creates a GUI window using the specified FXML file.
     *
     * @param title    Title to display in window title-bar
     * @param fxmlPath Path to FXML file to use
     *
     * @throws IOException Upon error reading from FXML file
     */
    public JFXElement(String title, String fxmlPath) throws IOException {

        // Make sure the GUI thread has started
        GUI.touch();
        this.title.set(title);

        borderPane = new BorderPane();
        scene      = new Scene(borderPane);
        scene.setFill(Colour.string("#f4f4f4"));
        borderPane.setBackground(Background.EMPTY);

        // Create a loader for our FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        // Tell the loader to link the FXML file to this object
        loader.setController(this);

        // Load our layout from our FXML file into centre panel
        borderPane.setCenter(loader.load());

        toolBar   = new ToolBar();
        buttonBar = new ButtonBar();

        setUpBars();

    }

    public JFXElement(String title, URL resource) {
        this(title, null, resource);
    }

    protected JFXElement(String title, Icon icon, URL resource) {

        // Make sure the GUI thread has started
        GUI.touch();
        this.title.set(title);

        // Create a loader for our FXML file
        FXMLLoader loader = new FXMLLoader(resource);

        // Tell the loader to link the FXML file to this object
        loader.setController(this);

        borderPane = new BorderPane();
        scene      = new Scene(borderPane);
        scene.setFill(Colour.string("#f4f4f4"));
        borderPane.setBackground(Background.EMPTY);

        try {
            borderPane.setCenter(loader.load());
        } catch (IOException ignored) {
        }

        toolBar   = new ToolBar();
        buttonBar = new ButtonBar();

        setUpBars();
    }

    private void setUpBars() {

        toolBar.setVisible(false);
        toolBar.setManaged(false);
        buttonBar.setVisible(false);
        buttonBar.setManaged(false);

        toolBar.getItems().addListener((InvalidationListener) observable -> {

            boolean visible = !toolBar.getItems().isEmpty();
            toolBar.setVisible(visible);
            toolBar.setManaged(visible);

        });

        buttonBar.getButtons().addListener((InvalidationListener) observable -> {

            boolean visible = !buttonBar.getButtons().isEmpty();
            buttonBar.setVisible(visible);
            buttonBar.setManaged(visible);

        });

        toolBar.setBackground(new Background(new BackgroundFill(Color.color(0, 0, 0, 0.1), null, null)));
        toolBar.setPadding(new Insets(5, GUI.SPACING, 5, GUI.SPACING));
        toolBar.setBorder(new Border(new BorderStroke(Color.SILVER, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, 1, 0))));
        BorderPane.setMargin(toolBar, new Insets(0));
        BorderPane.setMargin(borderPane.getCenter(), new Insets(GUI.SPACING));
        buttonBar.setPadding(new Insets(GUI.SPACING));
        buttonBar.setBackground(Background.EMPTY);

        borderPane.setTop(toolBar);
        borderPane.setBottom(buttonBar);

    }

    /**
     * Adds a button to the a toolbar at the top of this element.
     *
     * @param text    Test to display on the button
     * @param onClick Action to perform when clicked
     *
     * @return Button handle
     */
    public Button addToolbarButton(String text, SRunnable onClick) {

        javafx.scene.control.Button button = new javafx.scene.control.Button(text);
        button.setOnAction(event -> onClick.start());

        GUI.runNow(() -> toolBar.getItems().add(button));

        return new Button.ButtonWrapper(button) {

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(button));
            }

        };

    }

    /**
     * Adds a menu button to the toolbar at the top of this element. This button displays a menu with options when clicked.
     *
     * @param text Test to display on the button.
     *
     * @return MenuButton handle
     */
    public MenuButton addToolbarMenuButton(String text) {

        javafx.scene.control.MenuButton button = new javafx.scene.control.MenuButton(text);

        GUI.runNow(() -> toolBar.getItems().add(button));

        return new MenuButton.MenuButtonWrapper(button) {

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(button));
            }

        };

    }

    public double getMaxWidth() {
        return borderPane.getMaxWidth();
    }

    public void setMaxWidth(double width) { GUI.runNow(() -> borderPane.setMaxWidth(width)); }

    public void setMaxWindowWidth(double maxWidth) {
        GUI.runNow(() -> this.maxWidth.set(maxWidth));
    }

    public double getMaxWindowWidth() {
        return maxWidth.get();
    }

    public double getMaxHeight() {
        return borderPane.getMaxHeight();
    }

    public void setMaxHeight(double height) { GUI.runNow(() -> borderPane.setMaxHeight(height)); }

    public void setMaxWindowHeight(double maxHeight) {
        GUI.runNow(() -> this.maxHeight.set(maxHeight));
    }

    public double getMaxWindowHeight() {
        return maxHeight.get();
    }

    public void setMinWidth(double width) {
        GUI.runNow(() -> borderPane.setMinWidth(width));
    }

    public double getMinWidth() {
        return borderPane.getMinWidth();
    }

    public void setMinHeight(double height) {
        GUI.runNow(() -> borderPane.setMinHeight(height));
    }

    public double getMinHeight() {
        return borderPane.getMinHeight();
    }

    /**
     * Adds a separator to the toolbar at the top of this element.
     *
     * @return Separator handle
     */
    public Separator addToolbarSeparator() {

        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();

        GUI.runNow(() -> toolBar.getItems().add(separator));

        return new Separator.SeparatorWrapper(separator) {

            @Override
            public void remove() {
                GUI.runNow(() -> toolBar.getItems().remove(separator));
            }

        };

    }

    /**
     * Removes all items from the toolbar at the top of this element.
     */
    public void clearToolbar() {

        GUI.runNow(() -> toolBar.getItems().clear());

    }

    /**
     * Adds a button to the bottom-right of the element.
     *
     * @param text    Text to display in button
     * @param onClick
     *
     * @return
     */
    public Button addDialogButton(String text, SRunnable onClick) {

        javafx.scene.control.Button button = new javafx.scene.control.Button(text);
        button.setOnAction(event -> onClick.start());

        GUI.runNow(() -> buttonBar.getButtons().add(button));

        return new Button.ButtonWrapper(button) {

            @Override
            public void remove() {
                GUI.runNow(() -> buttonBar.getButtons().remove(button));
            }

        };

    }

    public MenuButton addDialogMenuButton(String text) {

        javafx.scene.control.MenuButton button = new javafx.scene.control.MenuButton(text);

        GUI.runNow(() -> buttonBar.getButtons().add(button));

        return new MenuButton.MenuButtonWrapper(button) {

            @Override
            public void remove() {
                GUI.runNow(() -> buttonBar.getButtons().remove(button));
            }

        };

    }

    public void clearDialogButtons() {

        GUI.runNow(() -> buttonBar.getButtons().clear());

    }

    public void showAsAlert() {
        showAsDialog("OK");
    }

    public int showAsDialog(String... buttons) {

        Semaphore     semaphore = new Semaphore(0);
        AtomicInteger result    = new AtomicInteger(-1);
        Button[]      added     = new Button[buttons.length];

        for (int i = 0; i < buttons.length; i++) {

            int finalI = i;
            added[i] = addDialogButton(buttons[i], () -> {
                result.set(finalI);
                semaphore.release();
            });

        }

        setOnClose(semaphore::release);

        show();

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        close();

        for (Button button : added) { button.remove(); }

        return result.get();

    }

    public boolean showAsConfirmation() {
        return showAsDialog("Cancel", "OK") == 1;
    }

    public void setWindowSize(double width, double height) {

        GUI.runNow(() -> {
            this.width.set(width);
            this.height.set(height);
        });

    }

    public double getWindowWidth() {
        return isShowing() ? getStage().getWidth() : width.get();
    }

    public void setWindowWidth(double width) {
        GUI.runNow(() -> this.width.set(width));
    }

    public double getWindowHeight() {
        return isShowing() ? getStage().getHeight() : height.get();
    }

    public void setWindowHeight(double height) {
        GUI.runNow(() -> this.height.set(height));
    }

    public void autoSizeWindow() {

        GUI.runNow(() -> {

            this.width.set(-1);
            this.height.set(-1);

            if (isShowing()) { stage.sizeToScene(); }

        });
    }

    public Stage getStage() {

        if (stage == null) {

            GUI.runNow(() -> {

                stage = new Stage();
                stage.setScene(scene);
                stage.titleProperty().bind(this.title);

                width.addListener(observable -> updateWidth());
                height.addListener(observable -> updateHeight());
                maxWidth.addListener(observable -> updateWidth());
                maxHeight.addListener(observable -> updateHeight());
                icon.addListener(observable -> updateIcon());

                updateWidth();
                updateHeight();
                updateIcon();

            });

        }

        return stage;

    }

    private void updateWidth() {
        if (stage != null && width.get() >= 0) { stage.setWidth(Math.min(maxWidth.get(), width.get())); }
    }

    private void updateHeight() {
        if (stage != null && height.get() >= 0) { stage.setHeight(Math.min(maxHeight.get(), height.get())); }
    }

    private void updateIcon() {

        if (icon.get() == null) {
            stage.getIcons().clear();
        } else {
            stage.getIcons().setAll(icon.get());
        }

    }

    /**
     * Shows the window.
     */
    public void show() {

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Stage       stage  = getStage();

        if (stage.isMaximized()) {
            GUI.runNow(stage::show);
            return;
        }

        GUI.runNow(() -> {

            stage.setMaxWidth(Math.min(bounds.getWidth(), maxWidth.get()));
            stage.setMaxHeight(Math.min(bounds.getHeight(), maxHeight.get()));

            if (width.get() > -1) {
                stage.setWidth(Math.min(maxWidth.get(), width.get()));
            }

            if (height.get() > -1) {
                stage.setHeight(Math.min(maxHeight.get(), height.get()));
            }

            stage.show();

            double height = Math.min(maxHeight.get(), stage.getHeight());
            double width  = Math.min(maxWidth.get(), stage.getWidth());

            stage.setHeight(height);
            stage.setWidth(width);

            stage.setX((bounds.getMinX() + bounds.getMaxX()) / 2.0 - (width / 2.0));
            stage.setY(Math.min(bounds.getMinY() + (bounds.getHeight() / 4.0), (bounds.getMinY() + bounds.getMaxY()) / 2.0 - (height / 2.0)));

            stage.setMaxWidth(Double.MAX_VALUE);
            stage.setMaxHeight(Double.MAX_VALUE);

        });

    }

    /**
     * Hides the window
     */
    public void hide() {
        GUI.runNow(getStage()::hide);
    }

    /**
     * Closes the window
     */
    public void close() {
        GUI.runNow(getStage()::close);
    }

    /**
     * Returns whether the element is currently open in its own window.
     *
     * @return Open?
     */
    public boolean isShowing() {
        return stage != null && stage.isShowing();
    }

    public void autoAdjustSize() {

        GUI.runNow(() -> {

            Stage stage = getStage();

            double minHeight = stage.getMinHeight();
            double minWidth  = stage.getMinWidth();

            stage.setMinHeight(stage.getHeight());
            stage.setMinWidth(stage.getWidth());

            stage.sizeToScene();

            stage.setMinHeight(minHeight);
            stage.setMinWidth(minWidth);

        });

    }

    public boolean isMaximised() {
        return stage != null && stage.isMaximized();
    }

    /**
     * Sets whether the window is maximised or not.
     *
     * @param flag Maximised?
     */
    public void setMaximised(boolean flag) {
        GUI.runNow(() -> getStage().setMaximized(flag));
    }

    /**
     * Returns whether the element is set to be shown with window decorations (title bar + frame) when shown as window.
     *
     * @return Show window decorations?
     */
    public boolean isDecorated() {
        return stage == null || stage.getStyle() == StageStyle.DECORATED;
    }

    /**
     * Sets whether a title bar + frame should be drawn around this element when shown as a window or not. Can only be
     * called before the window is shown for the first time.
     *
     * @param decorated Show window decorations?
     */
    public void setDecorated(boolean decorated) {
        GUI.runNow((() -> getStage().initStyle(decorated ? StageStyle.DECORATED : StageStyle.UNDECORATED)));
    }

    @Override
    public BorderPane getNode() {
        return borderPane;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        GUI.runNow(() -> this.title.set(title));
    }

    @Override
    public ObjectProperty<String> titleProperty() {
        return title;
    }

    public void setExitOnClose(boolean close) {

        if (close) {

            getStage().setOnCloseRequest(a -> {
                GUI.stopGUI();
                System.exit(0);
            });

        } else {
            stage.setOnCloseRequest(a -> {
            });
        }

    }

    public void setOnClose(SRunnable toRun) {

        getStage().setOnCloseRequest(a -> {

            try {
                toRun.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

    }

    public Image getIcon() {
        return icon.get();
    }

    public void setIcon(Icon icon) {
        GUI.runNow(() -> this.icon.set(icon != null ? icon.getWhiteImage() : null));
    }

    public void setIcon(URL icon) {
        GUI.runNow(() -> this.icon.set(icon != null ? new Image(icon.toExternalForm()) : null));
    }

    public ObjectProperty<Image> iconProperty() {
        return icon;
    }

}
