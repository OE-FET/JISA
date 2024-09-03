package jisa.devices;

import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.SRunnable;
import jisa.results.ResultList;
import jisa.results.ResultTable;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Configuration<T extends Instrument> {

    private final Class<T>           target;
    private final String             name;
    private final List<T>            choices        = new LinkedList<>();
    private final List<String>       names          = new LinkedList<>();
    private final List<Parameter<?>> parameters     = new LinkedList<>();
    private final List<Parameter<?>> pastParameters = new LinkedList<>();
    private final List<SRunnable>    listeners      = new LinkedList<>();
    private       String             choiceName     = null;
    private       Instrument         input          = null;
    private       T                  choice         = null;

    public Configuration(String name, Class<T> target) {
        this.name   = name;
        this.target = target;
    }

    public Configuration(String name, KClass<T> target) {
        this(name, JvmClassMappingKt.getJavaClass(target));
    }

    public String getName() {
        return name;
    }

    public void loadFromConfig(ConfigBlock block) {

        if (block.hasValue("Choice")) {
            selectChoice(block.intValue("Choice").get());
        }

        for (Parameter parameter : parameters) {

            if (parameter.getType() == Instrument.AutoQuantity.class && block.hasBlock(parameter.getName())) {

                ConfigBlock subBlock = block.subBlock(parameter.getName());

                if (!subBlock.hasValue("Auto") || !subBlock.hasValue("Value")) {
                    continue;
                }

                ConfigBlock.Value<Boolean> auto  = subBlock.booleanValue("Auto");
                ConfigBlock.Value          value = makeValue(subBlock, ((Instrument.AutoQuantity) parameter.getValue()).getValue().getClass(), "Value");

                if (value == null) {
                    continue;
                }

                parameter.setValue(new Instrument.AutoQuantity<>(auto.get(), value.get()));

            } else if (parameter.getType() == Instrument.OptionalQuantity.class && block.hasBlock(parameter.getName())) {

                ConfigBlock subBlock = block.subBlock(parameter.getName());

                if (!subBlock.hasValue("Use") || !subBlock.hasValue("Value")) {
                    continue;
                }

                ConfigBlock.Value<Boolean> use   = subBlock.booleanValue("Use");
                ConfigBlock.Value          value = makeValue(subBlock, ((Instrument.OptionalQuantity) parameter.getValue()).getValue().getClass(), "Value");

                if (value == null) {
                    continue;
                }

                parameter.setValue(new Instrument.OptionalQuantity<>(use.get(), value.get()));

            } else if (ResultTable.class.isAssignableFrom(parameter.getType()) && block.hasBlock(parameter.getName())) {

                ConfigBlock subBlock = block.subBlock(parameter.getName());

                try {
                    parameter.setValue(ResultList.fromCSVString(subBlock.stringValue("contents").getOrDefault("")));
                } catch (Throwable ignored) { }


            } else if (block.hasValue(parameter.getName())) {

                ConfigBlock.Value value = makeValue(block, parameter.getType(), parameter.getName());

                if (parameter.getType().isEnum()) {
                    ConfigBlock.Value<String> value2 = block.stringValue(parameter.getName());
                    parameter.setValue(Arrays.stream(parameter.getType().getEnumConstants()).filter(e -> e.toString().equals(value2.get())).findFirst().orElse(parameter.getType().getEnumConstants()[0]));
                } else if (value != null) {
                    parameter.setValue(value.get());
                }


            }


        }

    }

    public SRunnable addChangeListener(SRunnable listener) {
        listeners.add(listener);
        return listener;
    }

    public void removeChangeListener(SRunnable listener) {
        listeners.remove(listener);
    }

    public void writeToConfig(ConfigBlock block) {

        if (isChoice()) {
            block.intValue("Choice").set(getChoice());
        }

        for (Parameter parameter : parameters) {

            if (parameter.getType() == Instrument.AutoQuantity.class) {

                ConfigBlock                subBlock = block.subBlock(parameter.getName());
                ConfigBlock.Value<Boolean> auto     = subBlock.booleanValue("Auto");
                ConfigBlock.Value          value    = makeValue(subBlock, ((Instrument.AutoQuantity) parameter.getValue()).getValue().getClass(), "Value");

                if (value == null) {
                    continue;
                }

                auto.set(((Instrument.AutoQuantity<?>) parameter.getValue()).isAuto());
                value.set(((Instrument.AutoQuantity<?>) parameter.getValue()).getValue());

            } else if (parameter.getType() == Instrument.OptionalQuantity.class) {

                ConfigBlock                subBlock = block.subBlock(parameter.getName());
                ConfigBlock.Value<Boolean> use      = subBlock.booleanValue("Use");
                ConfigBlock.Value          value    = makeValue(subBlock, ((Instrument.OptionalQuantity) parameter.getValue()).getValue().getClass(), "Value");

                if (value == null) {
                    continue;
                }

                use.set(((Instrument.OptionalQuantity<?>) parameter.getValue()).isUsed());
                value.set(((Instrument.OptionalQuantity<?>) parameter.getValue()).getValue());

            } else if (ResultTable.class.isAssignableFrom(parameter.getType())) {

                ConfigBlock subBlock = block.subBlock(parameter.getName());
                subBlock.clear();
                ResultTable           quantity = (ResultTable) parameter.getValue();
                subBlock.stringValue("contents").set(quantity.getCSV());

            } else {

                ConfigBlock.Value value = makeValue(block, parameter.getType(), parameter.getName());

                if (parameter.getType().isEnum()) {
                    value = block.stringValue(parameter.getName());
                    value.set(parameter.getValue().toString());
                } else if (value != null) {
                    value.set(parameter.getValue());
                }

            }


        }

        block.save();

    }

    private ConfigBlock.Value makeValue(ConfigBlock block, Class<?> type, String name) {

        ConfigBlock.Value value = null;

        if (type == String.class) {
            value = block.stringValue(name);
        } else if (type == Integer.class) {
            value = block.intValue(name);
        } else if (type == Double.class) {
            value = block.doubleValue(name);
        } else if (type == Boolean.class) {
            value = block.booleanValue(name);
        }

        return value;

    }

    public Class<T> getTarget() {
        return target;
    }

    public void selectChoice(int index) {

        if (index < 0 || index >= choices.size()) {
            return;
        }

        choice = choices.get(index);

        List<Parameter<?>> newParameters = choice.getAllParameters(target).stream().map(Parameter::new).collect(Collectors.toList());

        pastParameters.addAll(parameters);

        List<Parameter<?>> toRemove = new LinkedList<>();

        for (Parameter p : newParameters) {

            pastParameters.stream()
                          .filter(p2 -> p.getType().equals(p2.getType()) && p.getName().equals(p2.getName()))
                          .findFirst()
                          .ifPresent(found -> {
                              p.setValue(found.getValue());
                              toRemove.add(found);
                          });

        }

        pastParameters.removeAll(toRemove);

        parameters.clear();
        parameters.addAll(newParameters);

    }

    public boolean isChoice() {
        return choiceName != null;
    }

    public String getChoiceName() {
        return choiceName;
    }

    public List<String> getChoices() {
        return List.copyOf(names);
    }

    public int getChoice() {
        return choices.indexOf(choice);
    }

    public List<Parameter<?>> getParameters() {
        return List.copyOf(parameters);
    }

    public T configure() throws IOException, DeviceException {

        if (input == null || choice == null) {
            return null;
        }

        for (Parameter<?> parameter : parameters) {
            parameter.write();
        }

        return choice;

    }

    public Instrument getInputInstrument() {
        return input;
    }

    public void setInputInstrument(Instrument instrument) {

        choiceName = null;

        choices.clear();
        names.clear();

        this.input = instrument;

        if (instrument == null) {
            parameters.clear();
            return;
        }

        if (instrument instanceof MultiInstrument && ((MultiInstrument) instrument).contains(target)) {

            choiceName = "Sub Instrument";

            choices.addAll(((MultiInstrument) instrument).get(target));

            for (int i = 0; i < choices.size(); i++) {
                names.add(String.format("%d: %s", i, choices.get(i).getName()));
            }

        } else if (target.isAssignableFrom(instrument.getClass())) {
            choices.add((T) instrument);
        }

        selectChoice(0);

    }

    public T getInstrument() throws IOException, DeviceException {
        return configure();
    }

    public T get() throws IOException, DeviceException {
        return configure();
    }

    public static class Parameter<S> {

        private final Instrument.Parameter<S> parameter;
        private       S                       value;
        private final Configuration<?>        configuration;

        public Parameter(Instrument.Parameter<S> parameter, Configuration<?> configuration) {
            this.parameter     = parameter;
            this.value         = parameter.getDefaultValue();
            this.configuration = configuration;
        }

        public Parameter(Instrument.Parameter<S> parameter) {
            this(parameter, null);
        }

        public String getName() {
            return parameter.getName();
        }

        public boolean isChoice() {
            return parameter.isChoice();
        }

        public List<S> getChoices() {
            return parameter.getChoices();
        }

        public S getValue() {
            return value;
        }

        public void setValue(S value) {

            this.value = value;

            if (configuration != null) {
                configuration.listeners.forEach(Util::runRegardless);
            }

        }

        public Class<S> getType() {
            return (Class<S>) parameter.getDefaultValue().getClass();
        }

        public void write() throws IOException, DeviceException {
            parameter.set(value);
        }

    }

}
