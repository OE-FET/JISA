package jisa.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import jisa.Util;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Semaphore;

public class Doc extends JFXWindow {

    public BorderPane pane;
    public VBox       list;
    public ButtonBar  buttonBar;

    public Doc(String title) {

        super(title, JFXWindow.class.getResource("fxml/DocWindow.fxml"));

        buttonBar.setVisible(false);
        buttonBar.setManaged(false);

        buttonBar.getButtons().addListener((ListChangeListener<? super Node>) l -> {

            buttonBar.setVisible(!buttonBar.getButtons().isEmpty());
            buttonBar.setManaged(!buttonBar.getButtons().isEmpty());

        });

    }

    public Heading addHeading(String text) {

        Label label = new Label(text);
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: 2em; -fx-font-weight: bold;");
        label.setWrapText(true);

        GUI.runNow(() -> list.getChildren().add(label));

        return new Heading() {
            @Override
            public Heading setText(String text) {
                GUI.runNow(() -> label.setText(text));
                return this;
            }

            @Override
            public Heading setAlignment(Align align) {
                GUI.runNow(() -> {
                    label.setAlignment(align.getPos());
                    label.setTextAlignment(align.getAlignment());
                });
                return this;
            }

            @Override
            public Heading setColour(Color colour) {
                GUI.runNow(() -> label.setTextFill(colour));
                return this;
            }

            @Override
            public boolean isVisible() {
                return label.isVisible();
            }

            @Override
            public void remove() {
                GUI.runNow(() -> list.getChildren().remove(label));
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                });
            }

        };

    }

    public Heading addSubHeading(String text) {

        Label label = new Label(text);
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold");
        label.setWrapText(true);
        GUI.runNow(() -> list.getChildren().add(label));

        return new Heading() {
            @Override
            public Heading setText(String text) {
                GUI.runNow(() -> label.setText(text));
                return this;
            }

            @Override
            public Heading setAlignment(Align align) {
                GUI.runNow(() -> {
                    label.setAlignment(align.getPos());
                    label.setTextAlignment(align.getAlignment());
                });
                return this;
            }

            @Override
            public Heading setColour(Color colour) {
                GUI.runNow(() -> label.setTextFill(colour));
                return this;
            }

            @Override
            public boolean isVisible() {
                return label.isVisible();
            }

            @Override
            public void remove() {
                GUI.runNow(() -> list.getChildren().remove(label));
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                });
            }

        };

    }

    public Paragraph addText(String text) {

        Label label = new Label(text);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
        GUI.runNow(() -> list.getChildren().add(label));

        return new Paragraph() {

            @Override
            public Paragraph addLine(String text) {
                GUI.runNow(() -> label.setText(label.getText().concat("\n").concat(text)));
                return this;
            }

            @Override
            public Paragraph setText(String text) {
                GUI.runNow(() -> label.setText(text));
                return this;
            }

            @Override
            public Paragraph setAlignment(Align align) {
                GUI.runNow(() -> {
                    label.setAlignment(align.getPos());
                    label.setTextAlignment(align.getAlignment());
                });
                return this;
            }

            @Override
            public Paragraph clear() {
                GUI.runNow(() -> label.setText(""));
                return this;
            }

            @Override
            public Paragraph setColour(Color colour) {
                GUI.runNow(() -> label.setTextFill(colour));
                return this;
            }

            @Override
            public boolean isVisible() {
                return label.isVisible();
            }

            @Override
            public void remove() {
                GUI.runNow(() -> list.getChildren().remove(label));
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                });
            }


        };

    }

    public List addList(boolean numbered) {

        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);

        GUI.runNow(() -> list.getChildren().add(grid));

        return new List() {

            private int count = 0;
            private SimpleObjectProperty<Color> colour = new SimpleObjectProperty<>(Colour.BLACK);

            @Override
            public List addItem(String item) {

                Node bullet;

                if (numbered) {
                    bullet = new Label(String.format("%d.", count + 1));
                    ((Label) bullet).textFillProperty().bind(colour);
                    ((Label) bullet).setMinWidth(Region.USE_PREF_SIZE);
                    ((Label) bullet).setMinHeight(Region.USE_PREF_SIZE);
                } else {
                    bullet = new Circle(3, colour.get());
                    ((Circle) bullet).fillProperty().bind(colour);
                }

                Label text = new Label(item);
                text.setWrapText(true);
                text.setMinHeight(Region.USE_PREF_SIZE);
                text.setMaxWidth(Double.MAX_VALUE);
                text.textFillProperty().bind(colour);

                GUI.runNow(() -> {
                    grid.add(bullet, 0, count);
                    grid.add(text, 1, count);
                });

                count++;

                return this;

            }

            @Override
            public List setColour(Color colour) {
                GUI.runNow(() -> this.colour.set(colour));
                return this;
            }

            @Override
            public List clear() {

                GUI.runNow(() -> grid.getChildren().clear());
                count = 0;
                return this;

            }

            @Override
            public boolean isVisible() {
                return grid.isVisible();
            }

            @Override
            public void remove() {
                GUI.runNow(() -> list.getChildren().remove(grid));
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> {
                    grid.setVisible(visible);
                    grid.setManaged(visible);
                });
            }


        };

    }

    public Image addImage(String url) {

        ImageView imageView = new ImageView(new javafx.scene.image.Image(url));
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        stage.widthProperty().addListener((observableValue, number, t1) -> imageView.maxWidth(stage.getWidth() - 35));
        stage.heightProperty().addListener((observableValue, number, t1) -> imageView.maxHeight(stage.getHeight() - 35));
        BorderPane pane = new BorderPane(imageView);
        GUI.runNow(() -> list.getChildren().add(pane));

        return new Image() {

            @Override
            public Image setAlignment(Align alignment) {
                BorderPane.setAlignment(imageView, alignment.getPos());
                return this;
            }

            @Override
            public boolean isVisible() {
                return pane.isVisible();
            }

            @Override
            public void remove() {
                GUI.runNow(() -> list.getChildren().remove(pane));
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> {
                    pane.setVisible(visible);
                    pane.setManaged(visible);
                });
            }


        };

    }

    public Link addLink(String text, String url) {

        Label label = new Label(text);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
        GUI.runNow(() -> list.getChildren().add(label));

        label.setCursor(Cursor.HAND);
        label.setTextFill(Colour.BLUE);
        label.setUnderline(true);

        label.setOnMouseClicked(mouseEvent -> Util.openInBrowser(url));

        return new Link() {

            @Override
            public Link setText(String text) {

                GUI.runNow(() -> label.setText(text));
                return this;

            }

            @Override
            public Link setURL(String url) {

                label.setOnMouseClicked(mouseEvent -> Util.openInBrowser(url));

                return this;

            }

            @Override
            public Link setAlignment(Align align) {
                GUI.runNow(() -> {
                    label.setAlignment(align.getPos());
                    label.setTextAlignment(align.getAlignment());
                });
                return this;
            }


            @Override
            public Link setColour(Color colour) {
                GUI.runNow(() -> label.setTextFill(colour));
                return this;
            }

            @Override
            public boolean isVisible() {
                return label.isVisible();
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> {
                    label.setVisible(visible);
                    label.setManaged(visible);
                });
            }

            @Override
            public void remove() {
                GUI.runNow(() -> list.getChildren().remove(label));
            }
        };

    }

    public void showAndWait() {

        final Semaphore s = new Semaphore(0);

        stage.setOnCloseRequest(we -> s.release());

        javafx.scene.control.Button okay = new Button("OK");
        okay.setOnAction(ae -> s.release());

        GUI.runNow(() -> buttonBar.getButtons().add(okay));

        show();

        try {
            s.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        close();

        GUI.runNow(() -> buttonBar.getButtons().remove(okay));

    }

    public enum Align {

        LEFT(Pos.CENTER_LEFT, TextAlignment.LEFT),
        RIGHT(Pos.CENTER_RIGHT, TextAlignment.RIGHT),
        CENTRE(Pos.CENTER, TextAlignment.CENTER);

        private final Pos           pos;
        private final TextAlignment alignment;

        Align(Pos pos, TextAlignment alignment) {
            this.pos       = pos;
            this.alignment = alignment;
        }

        public Pos getPos() {
            return pos;
        }

        public TextAlignment getAlignment() {
            return alignment;
        }

    }

    public interface Heading extends SubElement {

        Heading setText(String text);

        Heading setAlignment(Align alignment);

        Heading setColour(Color colour);

    }

    public interface Paragraph extends SubElement {

        Paragraph addLine(String text);

        Paragraph setText(String text);

        Paragraph setAlignment(Align alignment);

        Paragraph clear();

        Paragraph setColour(Color colour);

    }

    public interface List extends SubElement {

        List addItem(String item);

        List setColour(Color colour);

        List clear();

    }

    public interface Image extends SubElement {

        Image setAlignment(Align alignment);

    }

    public interface Link extends SubElement {

        Link setText(String text);

        Link setURL(String url);

        Link setAlignment(Align alignment);

        Link setColour(Color colour);

    }

}
