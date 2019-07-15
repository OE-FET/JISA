package jisa.gui;

import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;

public class Section extends JFXWindow implements NotBordered {

    public Pane       pane;
    public TitledPane titled;

    public Section(String title, Element element) {
        super(title, Section.class.getResource("fxml/Section.fxml"));
        setElement(element);
        setTitle(title);
    }

    public void setTitle(String title) {
        titled.setText(title);
    }

    public String getTitle() {
        return titled.getText();
    }

    public Section(String title) {
        this(title, null);
    }

    public void setElement(Element e) {

        if (e == null) {
            clear();
        } else {
            titled.setContent(e instanceof NotBordered ? ((NotBordered) e).getNoBorderPane(false) : e.getPane());

        }

    }

    public void clear() {
        titled.setContent(null);
    }

    @Override
    public Pane getNoBorderPane(boolean strip) {
        return getPane();
    }
}
