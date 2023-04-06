package jisa.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import jisa.Util;

import java.net.URL;

public class Doc extends JFXElement {

    static {
        GUI.touch();
    }

    private final ObjectProperty<Region>     viewport   = new SimpleObjectProperty<>(null);
    private final ObjectProperty<Background> background = new SimpleObjectProperty<>(new Background(new BackgroundFill(Colour.WHITE, null, null)));
    @FXML
    protected     ScrollPane                 scroll;
    @FXML
    protected     VBox                       list;

    public Doc(String title) {

        super(title, JFXElement.class.getResource("fxml/DocWindow.fxml"));
        BorderPane.setMargin(getNode().getCenter(), Insets.EMPTY);
        viewport.addListener(o -> updateBG());
        background.addListener(o -> updateBG());

        scroll.widthProperty().addListener(o -> {
            if (viewport.get() == null) viewport.set((Region) scroll.lookup(".viewport"));
        });

    }

    private void updateBG() {

        if (viewport.get() != null) {
            GUI.runNow(() -> viewport.get().setBackground(background.get()));
        }

    }

    public void setBackgroundColour(Color colour) {
        background.set(new Background(new BackgroundFill(colour, null, null)));
    }

    public Color getBackgroundColour() {
        return (Color) background.get().getFills().get(0).getFill();
    }

    public Heading addHeading(String text) {

        Label label = new Label(text);
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle("-fx-font-size: 2em; -fx-font-weight: bold;");
        label.setWrapText(true);
        label.setTextFill(Colour.BLACK);

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
        label.setTextFill(Colour.BLACK);

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
        label.setTextFill(Colour.BLACK);

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
        grid.setPadding(new Insets(0, 0, 0, 15));

        GUI.runNow(() -> list.getChildren().add(grid));

        return new List() {

            private int count = 0;
            private final SimpleObjectProperty<Color> colour = new SimpleObjectProperty<>(Colour.BLACK);

            @Override
            public List addItem(String item) {

                Node bullet;

                if (numbered) {
                    bullet = new Label(String.format("%d.", count + 1));
                    ((Label) bullet).textFillProperty().bind(colour);
                    ((Label) bullet).setMinWidth(Region.USE_PREF_SIZE);
                    ((Label) bullet).setMinHeight(Region.USE_PREF_SIZE);
                } else {
                    bullet = new Circle(2, colour.get());
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

    public Image addImage(URL url) {
        try {
            return addImage(new javafx.scene.image.Image(url.openStream()));
        } catch (Exception e) {
            return addImage(Doc.class.getResource("images/jisa.png"));
        }
    }

    public Image addImage(String url) {
        return addImage(new javafx.scene.image.Image(url));
    }

    public Image addImage(javafx.scene.image.Image image) {

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        BorderPane pane = new BorderPane(imageView);
        list.widthProperty().addListener(o -> pane.maxWidth(scroll.getWidth() - 35));
        list.heightProperty().addListener(o -> pane.maxHeight(scroll.getHeight() - 35));
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

    public Value addValue(String name, String initial) {

        Label title = new Label(name);
        title.setStyle("-fx-font-weight: bold;");
        title.setMinHeight(Region.USE_PREF_SIZE);
        title.minWidth(Region.USE_PREF_SIZE);
        Label value = new Label(initial);
        value.setMinHeight(Region.USE_PREF_SIZE);
        value.setMaxWidth(Double.MAX_VALUE);
        HBox container = new HBox(title, value);
        container.setSpacing(15);
        HBox.setHgrow(title, Priority.NEVER);
        HBox.setHgrow(value, Priority.ALWAYS);

        title.setTextFill(Colour.BLACK);
        value.setTextFill(Colour.BLACK);

        GUI.runNow(() -> list.getChildren().add(container));

        return new Value() {
            @Override
            public Value setValue(String text) {
                GUI.runNow(() -> value.setText(text));
                return this;
            }

            @Override
            public Value setText(String text) {
                GUI.runNow(() -> title.setText(text));
                return this;
            }

            @Override
            public Value setAlignment(Align alignment) {

                GUI.runNow(() -> {
                    value.setAlignment(alignment.getPos());
                    value.setTextAlignment(alignment.getAlignment());
                });

                return this;

            }

            @Override
            public Value setColour(Color colour) {

                GUI.runNow(() -> {
                    title.setTextFill(colour);
                    value.setTextFill(colour);
                });

                return this;

            }

            @Override
            public Value clear() {
                GUI.runNow(() -> value.setText(""));
                return this;
            }

            @Override
            public boolean isVisible() {
                return container.isVisible();
            }

            @Override
            public void remove() {
                GUI.runNow(() -> list.getChildren().remove(container));
            }

            @Override
            public void setVisible(boolean visible) {
                GUI.runNow(() -> {
                    container.setVisible(visible);
                    container.setManaged(visible);
                });
            }


        };

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

    public interface Value extends SubElement {

        Value setValue(String text);

        Value setText(String text);

        Value setAlignment(Align alignment);

        Value setColour(Color colour);

        Value clear();

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

    public interface Table extends SubElement {

        TableRow addRow();

    }

    public interface TableRow extends SubElement {

        TableCell addCell();

    }

    public interface TableCell extends SubElement {

        TableCell setText(String text);

        TableCell setAlignment(Align alignment);

        TableCell setColour(Color colour);

    }

}
