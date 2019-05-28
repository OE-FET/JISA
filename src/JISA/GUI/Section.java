package JISA.GUI;

import javafx.scene.control.TitledPane;

public class Section extends JFXWindow {

    public TitledPane pane;

    public Section(String title, Element element) {
        super(title, Section.class.getResource("FXML/Section.fxml"));
        setElement(element);
    }

    public Section(String title) {
        this(title, null);
    }

    public void setElement(Element e) {
        pane.setContent(e.getPane());
    }

    public void clear() {
        pane.setContent(null);
    }

}
