package jisa.control;

import java.util.Map;

public interface ConfigBlock {

    ConfigFile.Value<String> stringValue(String name);

    ConfigFile.Value<Integer> intValue(String name);

    ConfigFile.Value<Double> doubleValue(String name);

    ConfigFile.Value<Boolean> booleanValue(String name);

    ConfigFile.Value<Object> value(String name);

    boolean hasValue(String name);

    boolean hasBlock(String name);

    ConfigBlock subBlock(String name);

    Map<String, ConfigBlock> getSubBlocks();

    void save();

    void clear();

    interface Value<T> {

        void set(Object value);

        T get();

        T getOrDefault(T defaultValue);

    }
}
