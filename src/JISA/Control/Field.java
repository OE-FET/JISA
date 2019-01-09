package JISA.Control;

public interface Field<T> {

    void set(T value);

    T get();

    void setOnChange(SRunnable onChange);

    void editValues(String... values);

}
