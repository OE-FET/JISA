package jisa.gui;

import jisa.control.ConfigBlock;
import jisa.experiment.Measurement;
import jisa.experiment.Measurement.Parameter;

import java.util.*;
import java.util.stream.Collectors;

public class MeasurementConfigurator extends Tabs {

    private final Measurement           measurement;
    private final Map<String, Fields>   sections          = new LinkedHashMap<>();
    private final Map<Parameter, Field> parameterFieldMap = new HashMap<>();
    private final Grid                  parameterGrid     = new Grid("Parameters", 1);
    private final Grid                  instrumentGrid    = new Grid("Instruments", 2);
    private       ConfigBlock           config            = null;
    private       int                   numColumns        = 2;
    private       boolean               colSpanning       = true;

    public MeasurementConfigurator(String title, Measurement measurement) {

        super(title);

        parameterGrid.setGrowth(true, false);
        instrumentGrid.setGrowth(true, false);

        add(parameterGrid);
        add(instrumentGrid);
        select(parameterGrid);

        this.measurement = measurement;

        for (Parameter<?> parameter : measurement.getParameters()) {

            if (!sections.containsKey(parameter.getSection())) {
                sections.put(
                        parameter.getSection(),
                        new Fields(parameter.getSection())
                );
            }

            parameterFieldMap.put(parameter, parameter.createField(sections.get(parameter.getSection())));

        }

        instrumentGrid.addAll(measurement.getInstruments().stream().map(Configurator::new).collect(Collectors.toList()));

        updateLayout();

    }

    public MeasurementConfigurator(Measurement measurement) {
        this(measurement.getName(), measurement);
    }

    public void linkToConfig(ConfigBlock config) {

        this.config = config;

        ConfigBlock block = config.subBlock(measurement.getClass().getName());

        for (Fields fields : sections.values()) {
            fields.loadFromConfig(block.subBlock(fields.getTitle()));
        }

        instrumentGrid.getElements().forEach(c -> ((Configurator) c).loadFromConfig(block.subBlock("Instrument Configs").subBlock(c.getTitle())));

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

        parameterGrid.clear();

        int          remainder = colSpanning ? sections.size() % numColumns : 0;
        List<Fields> fields    = new ArrayList<>(sections.values());
        Grid         firstRow  = new Grid(remainder);
        Grid         otherRows = new Grid(numColumns);

        for (int i = 0; i < remainder; i++) { firstRow.add(fields.get(i)); }
        for (int i = remainder; i < fields.size(); i++) { otherRows.add(fields.get(i)); }

        if (!firstRow.getElements().isEmpty()) { parameterGrid.add(firstRow); }
        parameterGrid.add(otherRows);

    }

    public boolean showInput() {

        if (showAsConfirmation()) {
            update();
            return true;
        } else {
            return false;
        }

    }

    public void update() {

        measurement.getParameters().forEach(Parameter::update);

        if (config != null) {
            sections.values().forEach(f -> f.writeToConfig(config.subBlock(measurement.getClass().getName()).subBlock(f.getTitle())));
            instrumentGrid.getElements().forEach(c -> ((Configurator) c).writeToConfig(config.subBlock(measurement.getClass().getName()).subBlock("Instrument Configs").subBlock(c.getTitle())));
        }

    }

}
