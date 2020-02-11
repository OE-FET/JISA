package jisa.gui;

import jisa.maths.Range;

public class RangeInput extends Fields {

    private static final int LINEAR      = 0;
    private static final int LOGARITHMIC = 1;
    private static final int STEP_COUNT  = 0;
    private static final int STEP_SIZE   = 1;

    private final Field<Integer> scale;
    private final Field<Integer> mode;
    private final Field<Double>  start;
    private final Field<Double>  stop;
    private final Field<Integer> stepCount;
    private final Field<Double>  stepSize;
    private final Field<Boolean> mirror;

    public RangeInput(String title, String units) {

        super(title);

        scale = addChoice("Scale", "Linear", "Logarithmic");
        mode  = addChoice("Mode", "Step Count", "Step Size");
        addSeparator();
        start     = addDoubleField(String.format("Start [%s]", units));
        stop      = addDoubleField(String.format("Stop [%s]", units));
        stepCount = addIntegerField("No. Steps", 1);
        stepSize  = addDoubleField(String.format("Step Size [%s]", units));
        addSeparator();
        mirror = addCheckBox("Mirrored", false);

        updateGUI();
        mode.setOnChange(this::updateGUI);

        stepCount.setOnChange(this::updateSize);
        stepSize.setOnChange(this::updateCount);

        start.setOnChange(() -> {
            updateSize();
            updateCount();
        });

        stop.setOnChange(() -> {
            updateSize();
            updateCount();
        });

    }

    private void updateGUI() {

        stepCount.setDisabled(mode.get() != STEP_COUNT);
        stepSize.setDisabled(mode.get() != STEP_SIZE);
        updateSize();
        updateCount();

    }

    private void updateSize() {
        if (!stepCount.isDisabled()) stepSize.set((stepCount.get() > 1) ? (stop.get() - start.get()) / (stepCount.get() - 1) : 0.0);
    }

    private void updateCount() {
        if (!stepSize.isDisabled()) stepCount.set((int) Math.floor(Math.abs((stop.get() - start.get()) / stepSize.get())) + 1);
    }

    public Range<Double> getRange() {

        int scale = this.scale.get();
        int mode  = this.mode.get();

        Range<Double> range = null;
        if (scale == LINEAR && mode == STEP_COUNT) {
            range = Range.linear(start.get(), stop.get(), stepCount.get());
        } else if (scale == LINEAR && mode == STEP_SIZE) {
            range = Range.step(start.get(), stop.get(), stepSize.get());
        } else if (scale == LOGARITHMIC && mode == STEP_COUNT) {
            range = Range.exponential(start.get(), stop.get(), stepCount.get());
        } else if (scale == LOGARITHMIC && mode == STEP_SIZE) {
            range = Range.geometric(start.get(), stop.get(), stepSize.get());
        }

        if (range == null) {
            throw new IllegalStateException("Unknown selection");
        }

        return (mirror.get()) ? range.mirror() : range;

    }

}
