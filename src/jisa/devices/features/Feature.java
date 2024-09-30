package jisa.devices.features;

import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import org.apache.commons.lang3.ClassUtils;

import java.util.Collections;
import java.util.List;

public interface Feature {

    static ParameterList getFeatureParameters(Instrument instrument, Class<?> target) {

        ParameterList parameters = new ParameterList();

        if (instrument instanceof Feature) {

            List<Class<?>> interfaces = ClassUtils.getAllInterfaces(instrument.getClass());

            Collections.reverse(interfaces);

            interfaces.stream()
                      .filter(Feature.class::isAssignableFrom)
                      .forEach(feature -> {

                          try {

                              feature.getMethod(
                                  "addParameters",
                                  feature,
                                  Class.class,
                                  ParameterList.class
                              ).invoke(null, instrument, target, parameters);

                          } catch (Throwable ignored) { }

                      });

        }

        return parameters;

    }

}
