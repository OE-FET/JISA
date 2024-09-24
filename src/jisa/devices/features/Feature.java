package jisa.devices.features;

import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import org.apache.commons.lang3.ClassUtils;
import org.reflections.Reflections;

public interface Feature {

    static ParameterList getFeatureParameters(Instrument instrument, Class<?> target) {

        ParameterList parameters = new ParameterList();

        if (instrument instanceof Feature) {

            Reflections reflections = new Reflections("jisa.devices.features");
            Feature     inst        = (Feature) instrument;

            ClassUtils.getAllInterfaces(inst.getClass())
                      .stream()
                      .filter(Feature.class::isAssignableFrom)
                      .forEach(feature -> {

                          try {

                              feature.getMethod(
                                  "addParameters",
                                  feature,
                                  Class.class,
                                  ParameterList.class
                              ).invoke(null, inst, target, parameters);

                          } catch (Throwable ignored) { }

                      });

        }

        return parameters;

    }

}
