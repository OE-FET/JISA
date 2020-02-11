package jisa.experiment;

import jisa.Util;

import java.util.Arrays;
import java.util.List;

public class Combination implements Comparable<Combination> {

    private final Double[] values;

    public Combination(Double... values) {
        this.values = values;
    }

    public boolean equals(Object other) {

        if (other instanceof Combination) {

            if (values.length != ((Combination) other).values.length) {
                return false;
            }

            int i = 0;
            for (double v : ((Combination) other).values) {
                if (v != values[i++]) return false;
            }

            return true;

        } else {
            return false;
        }

    }

    public String toString() {
        return Util.joinDoubles(", ", Arrays.asList(values));
    }

    @Override
    public int compareTo(Combination combination) {

        if (equals(combination)) {
            return 0;
        } else if (combination.values[0] < values[0]) {
            return -1;
        } else {
            return +1;
        }

    }

}
