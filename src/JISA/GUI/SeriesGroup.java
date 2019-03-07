package JISA.GUI;

import JISA.Experiment.ResultTable;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;

import java.util.*;

public interface SeriesGroup extends Series {

    Collection<Series> getSeries();

    Series getSeriesFor(double value);

}
