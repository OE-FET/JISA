package jisa.devices.features;

import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import org.reflections.Reflections;

public interface Feature {

    static ParameterList getFeatureParameters(Instrument instrument, Class<?> target) {

        ParameterList parameters = new ParameterList();

        if (instrument instanceof Feature) {

            Reflections reflections = new Reflections("jisa.devices.features");
            Feature     inst        = (Feature) instrument;

            reflections.getSubTypesOf(Feature.class)
                       .stream()
                       .filter(Class::isInterface)
                       .forEach(feature -> {

                           if (feature.isAssignableFrom(instrument.getClass())) {

                               try {

                                   feature.getMethod(
                                       "addParameters",
                                       feature,
                                       Class.class,
                                       ParameterList.class
                                   ).invoke(null, inst, target, parameters);

                               } catch (Throwable ignored) { }

                           }

                       });

        }

        return parameters;

    }

}
