package jisa.gui;

import java.util.*;

public interface SeriesGroup extends Series {

    Collection<Series> getSeries();

    Series getSeriesFor(double value);

}
