package jisa.gui;

import java.util.Collection;
import java.util.List;

/**
 * Represents objects that can "contain" GUI elements. These are almost always GUI elements themselves such as Grid.
 */
public interface Container {

    /**
     * Add an element to this container.
     *
     * @param element Element to add.
     */
    void add(Element element);

    /**
     * Add multiple elements to this container.
     *
     * @param elements Elements to add.
     */
    default void addAll(Element... elements) {
        for (Element e : elements) {
            add(e);
        }
    }

    /**
     * Add all the specified elements to this container.
     *
     * @param elements Elements to add.
     */
    default void addAll(Collection<Element> elements) {
        for (Element e : elements) {
            add(e);
        }
    }

    /**
     * Removes the specified elements from this container.
     *
     * @param element Elements to remove
     */
    void remove(Element element);

    /**
     * Removes all the specified elements from this container.
     *
     * @param elements Elements to remove
     */
    default void removeAll(Element... elements) {
        for (Element e : elements) {
            remove(e);
        }
    }

    /**
     * Removes all the specified elements from this container.
     *
     * @param elements Elements to remove
     */
    default void removeAll(Collection<Element> elements) {
        for (Element e : elements) {
            remove(e);
        }
    }

    /**
     * Removes all elements from this container.
     */
    void clear();

    /**
     * Returns a list of all elements currently inside this container.
     *
     * @return List of elements.
     */
    List<Element> getElements();

}
