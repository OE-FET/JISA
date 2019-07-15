package jisa.gui;

import java.util.Collection;
import java.util.List;

public interface Container {

    void add(Element element);

    default void addAll(Element... elements) {
        for (Element e : elements) {
            add(e);
        }
    }

    default void addAll(Collection<Element> elements) {
        for (Element e : elements) {
            add(e);
        }
    }

    void remove(Element element);

    default void removeAll(Element... elements) {
        for (Element e : elements) {
            remove(e);
        }
    }

    default void removeAll(Collection<Element> elements) {
        for (Element e : elements) {
            remove(e);
        }
    }

    void clear();

    List<Element> getElements();

}
