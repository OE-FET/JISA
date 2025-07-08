package jisa.devices.features;

import jisa.devices.Instrument;
import jisa.devices.ParameterList;
import org.apache.commons.lang3.ClassUtils;

import java.util.List;

public interface Feature {

    static void addFeatureParameters(Instrument instrument, Class<?> target, ParameterList parameters) {

        if (instrument instanceof Feature) {

            List<Class<?>> interfaces = ClassUtils.getAllInterfaces(instrument.getClass());

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

    }

}
