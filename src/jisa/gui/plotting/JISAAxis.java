package jisa.gui.plotting;

import de.gsi.chart.axes.spi.AxisRange;

import java.util.List;

public class JISAAxis extends JISADefaultAxis {

    public static final List<String> KNOWN_UNITS = List.of("m", "s", "K", "g", "N", "P", "V", "A", "W", "Ohm", "Ω", "Hz", "V/K", "F", "H", "J", "eV");

    public JISAAxis(String axisLabel, String unit) {
        super(axisLabel, unit);
    }

    protected AxisRange autoRange(double minValue, double maxValue, double length, double labelSize) {

        final double min          = isLogAxis ? (Double.isNaN(logMin) ? 1  : logMin) : (minValue > 0 && isForceZeroInRange() ? 0 : minValue);
        final double max          = isLogAxis ? (Double.isNaN(logMax) ? 10 : logMax) : (maxValue < 0 && isForceZeroInRange() ? 0 : maxValue);
        final double padding      = getEffectiveRange(min, max) * this.getAutoRangePadding();
        final double paddingScale = 1.0 + this.getAutoRangePadding();
        final double paddedMin    = isLogAxis ? min / paddingScale : min - padding;
        final double paddedMax    = isLogAxis ? max * paddingScale : max + padding;

        return this.computeRange(paddedMin, paddedMax, length, labelSize);

    }

    public void setUnit(String unit) {

        super.setUnit(unit);
        setAutoUnitScaling(unit != null && KNOWN_UNITS.contains(unit));

    }

    public void setWidth(double width) {
        super.setWidth(width);
    }

    public void setHeight(double height) {
        super.setHeight(height);
    }

}
