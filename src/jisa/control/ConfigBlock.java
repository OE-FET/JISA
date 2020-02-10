package jisa.control;

public interface ConfigBlock {

    ConfigFile.Value<String> stringValue(String name);

    ConfigFile.Value<Integer> intValue(String name);

    ConfigFile.Value<Double> doubleValue(String name);

    ConfigFile.Value<Boolean> booleanValue(String name);

    ConfigBlock subBlock(String name);

    void save();

    interface Value<T> {

        void set(Object value);

        T get();

        T getOrDefault(T defaultValue);

    }
}
