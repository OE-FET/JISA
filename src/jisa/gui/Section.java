package jisa.gui;

import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;

/**
 * A collapsible section box that surrounds one GUI element.
 */
public class Section extends JFXWindow implements NotBordered {

    public Pane       pane;
    public TitledPane titled;

    public Section(String title, Element element) {
        super(title, Section.class.getResource("fxml/Section.fxml"));
        setElement(element);
        setTitle(title);
    }

    public Section(String title) {
        this(title, null);
    }

    public void setTitle(String title) {
        GUI.runNow(() -> titled.setText(title));
    }

    public String getTitle() {
        return titled.getText();
    }

    /**
     * Sets which GUI element is displayed inside this section.
     *
     * @param element Element to show
     */
    public void setElement(Element element) {

        if (element == null) {
            clear();
        } else {
            titled.setContent(element instanceof NotBordered ? ((NotBordered) element).getNoBorderPane(false) : element.getPane());

        }

    }

    public void setExpanded(boolean expanded) {
        GUI.runNow(() -> titled.setExpanded(expanded));
    }

    public boolean isExpanded() {
        return titled.isExpanded();
    }

    /**
     * Removes the contents of this section.
     */
    public void clear() {
        titled.setContent(null);
    }

    @Override
    public Pane getNoBorderPane(boolean strip) {
        return getPane();
    }

}
