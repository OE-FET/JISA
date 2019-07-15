package jisa.control;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class DeviceCommand {

    private ArrayList<DeviceArgument> arguments = new ArrayList<>();
    private String                    name;
    private String                    description;

    public DeviceCommand(String name, String description, DeviceArgument... arguments) {
        this.name = name;
        this.description = description;
        this.arguments.addAll(Arrays.asList(arguments));
    }

    public void reset() {
        for (DeviceArgument arg : arguments) {
            arg.reset();
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public abstract double execute() throws IOException;

    public DeviceArgument[] getArguments() {
        return arguments.toArray(new DeviceArgument[0]);
    }

    public DeviceArgument getArgument(int index) {
        return arguments.get(index);
    }

    public Object getArgumentValue(int index) {
        return getArgument(index).getValue();
    }

    public DeviceCommand clone() {

        DeviceArgument[] args = new DeviceArgument[arguments.size()];

        for (int i = 0; i < args.length; i ++) {
            args[i] = arguments.get(i).clone();
        }

        try {
            DeviceCommand clone = this.getClass().getConstructor(String.class, String.class, DeviceArgument[].class).newInstance(name, description, args);
            return clone;
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class DeviceArgument<T> {

        private String name;
        private Class  type;
        private T      value = null;

        public DeviceArgument(String name, Class type) {
            this.name = name;
            this.type = type;
        }

        public void reset() {
            this.value = null;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public Class getType() {
            return type;
        }

        public boolean isEnum() {
            return type.isEnum();
        }

        public DeviceArgument<T> clone() {
            return new DeviceArgument<>(name, type);
        }

    }

}
