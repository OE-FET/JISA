package jisa.gui;

import jisa.control.ConfigBlock;
import jisa.experiment.Measurement;
import jisa.experiment.Measurement.Parameter;

import java.util.*;

public class MeasurementFields extends Grid {

    private final Measurement           measurement;
    private final Map<String, Fields>   sections          = new LinkedHashMap<>();
    private final Map<Parameter, Field> parameterFieldMap = new HashMap<>();
    private       int                   numColumns        = 2;
    private       boolean               colSpanning       = true;

    public MeasurementFields(String title, Measurement measurement) {

        super(title, 1);

        this.measurement = measurement;

        for (Parameter<?> parameter : measurement.getParameters()) {

            if (!sections.containsKey(parameter.getSection())) sections.put(
                    parameter.getSection(),
                    new Fields(parameter.getSection())
            );

            parameterFieldMap.put(parameter, parameter.createField(sections.get(parameter.getSection())));

        }

        updateLayout();

    }

    public void linkConfig(ConfigBlock config) {

        ConfigBlock block = config.subBlock(measurement.getClass().getName());

        for (Fields fields : sections.values()) {
            fields.linkConfig(block.subBlock(fields.getTitle()));
        }

    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int columns) {
        this.numColumns = columns;
        updateLayout();
    }

    public boolean isColSpanning() {
        return colSpanning;
    }

    public void setColSpanning(boolean flag) {
        colSpanning = flag;
        updateLayout();
    }

    public <T> Field<T> getField(Parameter<T> parameter) {
        return parameterFieldMap.get(parameter);
    }

    private void updateLayout() {

        clear();

        int          remainder = colSpanning ? sections.size() % numColumns : 0;
        List<Fields> fields    = new ArrayList<>(sections.values());
        Grid         firstRow  = new Grid(remainder);
        Grid         otherRows = new Grid(numColumns);

        for (int i = 0; i < remainder; i++) firstRow.add(fields.get(i));
        for (int i = remainder; i < fields.size(); i++) otherRows.add(fields.get(i));

        if (!firstRow.getElements().isEmpty()) add(firstRow);
        add(otherRows);

    }

    public boolean showInput() {

        boolean result;

        if (showAsConfirmation()) {
            measurement.getParameters().forEach(Parameter::update);
            sections.values().forEach(Fields::writeToConfig);
            return true;
        } else {
            return false;
        }

    }

}
