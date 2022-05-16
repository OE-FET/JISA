package jisa.gui.plotting;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.ui.geometry.Side;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import jisa.gui.Colour;
import jnr.ffi.annotations.In;

public class JISAXYChart extends XYChart {

    public JISAXYChart(Axis... axes) {
        super(axes);
        titleLabel.setPadding(new Insets(15));
        getPlotArea().setBackground(new Background(new BackgroundFill(Colour.string("#f4f4f4"), null, null)));
        getAxesAndCanvasPane().setBackground(new Background(new BackgroundFill(Colour.WHITE, null, null)));
        getRenderers().clear();
        getRenderers().add(new JISARenderer());
        setLegendSide(Side.RIGHT);
        getLegend().setVertical(true);
        ((FlowPane) getLegend().getNode()).setBackground(Background.EMPTY);
        ((FlowPane) getLegend().getNode()).setAlignment(Pos.TOP_LEFT);
        GridPane.setMargin(getAxesPane(Side.LEFT), new Insets(0, 0, 0, 15));
        GridPane.setMargin(getAxesPane(Side.BOTTOM), new Insets(0, 0, 15, 0));
        GridPane.setMargin(getAxesPane(Side.RIGHT), new Insets(0, 15, 0, 0));
        ((FlowPane) getLegend().getNode()).setPadding(new Insets(0, 15, 0, 0));

    }

    public void forceRedraw() {
        redrawCanvas();
    }

}
