package jisa.gui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.net.URL;


public class SplashScreen extends JFXElement {

    static {
        GUI.touch();
    }

    private final ImageView imageView;

    public SplashScreen(String title, String image) {
        this(title);
        setImage(image);
    }

    public SplashScreen(String title, URL image) {
        this(title);
        setImage(image);
    }

    public SplashScreen(String title, Image image) {
        this(title);
        setImage(image);
    }

    public SplashScreen(String title) {
        this(title, new ImageView());
    }

    private SplashScreen(String title, ImageView imageView) {
        super(title, imageView);
        this.imageView = imageView;
        BorderPane.setMargin(imageView, Insets.EMPTY);
        setDecorated(false);
    }

    public void setImage(Image image) {
        GUI.runNow(() -> imageView.setImage(image));
    }

    public void setImage(URL image) {
        setImage(new Image(image.toExternalForm()));
    }

    public void setImage(String image) {
        setImage(new Image(image));
    }

    public Image getImage() {
        return imageView.getImage();
    }

}
