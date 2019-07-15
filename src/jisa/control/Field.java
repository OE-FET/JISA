package jisa.control;

public interface Field<T> {

    void set(T value);

    T get();

    void setOnChange(SRunnable onChange);

    void editValues(String... values);

    void setDisabled(boolean disabled);

    boolean isDisabled();

    void setVisible(boolean visible);

    boolean isVisible();

    void remove();

    void setText(String text);

    String getText();

}
