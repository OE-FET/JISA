package jisa.devices;

import jisa.devices.Instrument.Parameter;

import java.util.LinkedList;

public class ParameterList extends LinkedList<Instrument.Parameter<?>> {

    public <T> void addValue(String name, T defValue, Instrument.Setter<T> setter) {
        add(new Parameter<>(name, defValue, setter));
    }

    public <T> void addValue(String name, Instrument.Getter<T> defValue, T elseValue, Instrument.Setter<T> setter) {

        T def;

        try {
            def = defValue.get();
        } catch (Throwable e) {
            def = elseValue;
        }

        add(new Parameter<>(name, def, setter, defValue));

    }

    public <T> void addChoice(String name, T defValue, Instrument.Setter<T> setter, T... options) {

        if (defValue instanceof Enum && options.length == 0) {
            options = (T[]) defValue.getClass().getEnumConstants();
        }

        add(new Parameter<>(name, defValue, setter, options));
    }

    public <T> void addChoice(String name, Instrument.Getter<T> defValue, T elseValue, Instrument.Setter<T> setter, T... options) {

        T def;

        try {
            def = defValue.get();
        } catch (Throwable e) {
            def = elseValue;
        }

        add(new Parameter<>(name, def, setter, defValue, options));

    }

    public <T> void addAuto(String name, boolean auto, T defValue, Instrument.Setter<T> autoSetter, Instrument.Setter<T> valueSetter) {
        
        add(new Parameter<>(name, new Instrument.AutoQuantity<>(auto, defValue), q -> {
            
            if (q.isAuto()) {
                autoSetter.set(q.getValue());
            } else {
                valueSetter.set(q.getValue());
            }

        }));

    }

    public <T> void addAuto(String name, Instrument.Getter<Boolean> autoGet, boolean autoDef, Instrument.Getter<T> valueGet, T defValue, Instrument.Setter<T> autoSetter, Instrument.Setter<T> valueSetter) {

        boolean auto;

        try {
            auto = autoGet.get();
        } catch (Throwable e) {
            auto = autoDef;
        }

        T value;

        try {
            value = valueGet.get();
        } catch (Throwable e) {
            value = defValue;
        }

        add(new Parameter<Instrument.AutoQuantity<T>>(name, new Instrument.AutoQuantity<>(auto, value), q -> {

            if (q.isAuto()) {
                autoSetter.set(q.getValue());
            } else {
                valueSetter.set(q.getValue());
            }

        }, () -> new Instrument.AutoQuantity<>(autoGet.get(), valueGet.get())));

    }

    public <T> void addOptional(String name, boolean used, T defValue, Instrument.Setter<T> unusedSetter, Instrument.Setter<T> usedSetter) {

        add(new Parameter<>(name, new Instrument.OptionalQuantity<>(used, defValue), q -> {

            if (q.isUsed()) {
                usedSetter.set(q.getValue());
            } else {
                unusedSetter.set(q.getValue());
            }

        }));

    }

    public <T> void addOptional(String name, Instrument.Getter<Boolean> usedGet, boolean usedDef, Instrument.Getter<T> valueGet, T defValue, Instrument.Setter<T> autoSetter, Instrument.Setter<T> valueSetter) {

        boolean used;

        try {
            used = usedGet.get();
        } catch (Throwable e) {
            used = usedDef;
        }

        T value;

        try {
            value = valueGet.get();
        } catch (Throwable e) {
            value = defValue;
        }

        add(new Parameter<Instrument.OptionalQuantity<T>>(name, new Instrument.OptionalQuantity<>(used, defValue), q -> {

            if (!q.isUsed()) {
                autoSetter.set(q.getValue());
            } else {
                valueSetter.set(q.getValue());
            }

        }, () -> new Instrument.OptionalQuantity<>(usedGet.get(), valueGet.get())));

    }

}
