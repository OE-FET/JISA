package jisa.experiment;

import jisa.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Combination implements Comparable<Combination> {

    private final Object[] values;

    public Combination(Object... values) {
        this.values = values;
    }

    public boolean equals(Object other) {

        if (other instanceof Combination) {

            if (values.length != ((Combination) other).values.length) {
                return false;
            }

            int i = 0;
            for (Object v : ((Combination) other).values) {
                if (!v.toString().equals(values[i++].toString())) return false;
            }

            return true;

        } else {
            return false;
        }

    }

    public String toString() {
        return Arrays.stream(values).map(Objects::toString).collect(Collectors.joining(", "));
    }

    @Override
    public int compareTo(Combination combination) {
        return toString().compareTo(combination.toString());
    }

}
