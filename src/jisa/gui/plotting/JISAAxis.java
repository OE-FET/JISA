package jisa.gui.plotting;

import de.gsi.chart.axes.spi.AxisRange;
import de.gsi.chart.axes.spi.DefaultNumericAxis;

import java.util.List;

public class JISAAxis extends DefaultNumericAxis {

    public static final List<String> KNOWN_UNITS = List.of("m", "s", "K", "g", "N", "P", "V", "A", "W", "Ohm", "Ω", "Hz", "V/K", "F", "H", "J", "eV");


    public JISAAxis(String axisLabel, String unit) {
        super(axisLabel, unit);
    }

    protected AxisRange autoRange(double minValue, double maxValue, double length, double labelSize) {

        double min = minValue > 0.0 && this.isForceZeroInRange() ? 0.0 : minValue;

        if (this.isLogAxis && minValue <= 0.0) {

            min             = 1.0E-6;
            this.isUpdating = true;
            this.setMin(1.0E-6);
            this.isUpdating = false;

        }

        double max          = maxValue < 0.0 && this.isForceZeroInRange() ? 0.0 : maxValue;
        double padding      = getEffectiveRange(min, max) * this.getAutoRangePadding();
        double paddingScale = 1.0 + this.getAutoRangePadding();
        double paddedMin    = this.isLogAxis ? minValue / paddingScale : min - padding;
        double paddedMax    = this.isLogAxis ? maxValue * paddingScale : max + padding;
        return this.computeRange(paddedMin, paddedMax, length, labelSize);

    }

    public void setUnit(String unit) {

        super.setUnit(unit);
        setAutoUnitScaling(unit != null && KNOWN_UNITS.contains(unit));

    }

}
