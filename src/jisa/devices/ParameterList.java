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

        addValue(name, def, setter);

    }

    public <T> void addChoice(String name, T defValue, Instrument.Setter<T> setter, T... options) {
        add(new Parameter<>(name, defValue, setter, options));
    }

    public <T> void addChoice(String name, Instrument.Getter<T> defValue, T elseValue, Instrument.Setter<T> setter, T... options) {

        T def;

        try {
            def = defValue.get();
        } catch (Throwable e) {
            def = elseValue;
        }

        addChoice(name, def, setter, options);

    }

    public <T> void addAuto(String name, boolean auto, T defValue, Instrument.Setter<T> autoSetter, Instrument.Setter<T> valueSetter) {
        
        add(new Parameter<>(name, new Instrument.AutoQuantity<T>(auto, defValue), q -> {
            
            if (q.isAuto()) {
                autoSetter.set(q.getValue());
            } else {
                valueSetter.set(q.getValue());
            }

        }));

    }

    public <T> void addOptional(String name, boolean used, T defValue, Instrument.Setter<T> unusedSetter, Instrument.Setter<T> usedSetter) {
        
        add(new Parameter<>(name, new Instrument.OptionalQuantity<T>(used, defValue), q -> {
            
            if (q.isUsed()) {
                usedSetter.set(q.getValue());
            } else {
                unusedSetter.set(q.getValue());
            }

        }));

    }

}
