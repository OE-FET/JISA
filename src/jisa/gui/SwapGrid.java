package jisa.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SwapGrid extends Grid {

    private final List<Item> items                = new LinkedList<>();
    private       int        currentConfiguration = 0;

    public SwapGrid(String title, int numCols) {
        super(title, numCols);
    }

    public SwapGrid(String title) {
        super(title);
    }

    public void add(Element element, int... configurations) {

        Item item = new Item(configurations, element);

        items.add(item);

        if (item.isInConfiguration(currentConfiguration)) {
            super.add(item.getElement());
        }

    }

    public void remove(Element element) {

        items.removeIf(i -> i.getElement() == element);
        super.remove(element);

    }

    public void clear() {
        items.clear();
        super.clear();
    }

    public List<Element> getElements() {
        return items.stream().map(Item::getElement).collect(Collectors.toList());
    }

    public void setConfiguration(int configuration) {

        currentConfiguration = configuration;
        super.clear();
        items.stream().filter(i -> i.isInConfiguration(currentConfiguration)).forEach(i -> super.add(i.getElement()));

    }

    public int getConfiguration() {
        return currentConfiguration;
    }

    private static class Item {

        private final int[]   configurations;
        private final Element element;

        private Item(int[] configurations, Element element) {
            this.configurations = configurations;
            this.element        = element;
        }

        public boolean isInConfiguration(int configuration) {

            for (int value : configurations) {
                if (value == configuration) return true;
            }

            return false;

        }

        public Element getElement() {
            return element;
        }

    }

}
