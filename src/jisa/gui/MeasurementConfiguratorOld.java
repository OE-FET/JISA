package jisa.gui;

import jisa.control.ConfigBlock;
import jisa.experiment.MeasurementOld;
import jisa.experiment.MeasurementOld.CustomParameter;
import jisa.experiment.MeasurementOld.Parameter;
import jisa.gui.form.Field;

import java.util.*;
import java.util.stream.Collectors;

public class MeasurementConfiguratorOld extends Tabs {

    private final MeasurementOld    measurement;
    private final Map<String, Form> sections          = new LinkedHashMap<>();
    private final Map<Parameter, Field> parameterFieldMap = new HashMap<>();
    private final List<CustomParameter> customs           = new LinkedList<>();
    private final Grid                  parameterGrid     = new Grid("Parameters", 1);
    private final Grid                  instrumentGrid    = new Grid("Instruments", 1);
    private       ConfigBlock           config            = null;
    private       int                   numColumns        = 2;
    private       boolean               colSpanning       = true;

    public MeasurementConfiguratorOld(String title, MeasurementOld measurement) {

        super(title);

        parameterGrid.setGrowth(true, false);
        instrumentGrid.setGrowth(true, false);

        add(parameterGrid);
        add(instrumentGrid);
        select(parameterGrid);

        this.measurement = measurement;

        for (Parameter<?> parameter : measurement.getParameters()) {

            if (parameter instanceof CustomParameter) {
                customs.add((CustomParameter) parameter);
            } else {

                if (!sections.containsKey(parameter.getSection())) {
                    sections.put(parameter.getSection(), new Form(parameter.getSection()));
                }

                parameterFieldMap.put(parameter, parameter.createField(sections.get(parameter.getSection())));

            }

        }

        instrumentGrid.addAll(measurement.getInstruments().stream().map(Configurator::new).collect(Collectors.toList()));

        updateLayout();

    }

    public MeasurementConfiguratorOld(MeasurementOld measurement) {
        this(measurement.getName(), measurement);
    }

    public void linkToConfig(ConfigBlock config) {

        this.config = config;

        ConfigBlock block = config.subBlock(measurement.getClass().getName());

        for (Form fields : sections.values()) {
            fields.loadFromConfig(block.subBlock("Standard Fields").subBlock(fields.getTitle()));
        }

        customs.forEach(c -> c.loadFromConfig(block.subBlock("Custom Fields")));
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

        List<Element> combined = new ArrayList<>();
        combined.addAll(sections.values());
        combined.addAll(customs.stream().map(CustomParameter::getElement).distinct().collect(Collectors.toList()));

        int  remainder = colSpanning ? combined.size() % numColumns : 0;
        Grid firstRow  = new Grid(remainder);
        Grid otherRows = new Grid(numColumns);

        for (int i = 0; i < remainder; i++) {firstRow.add(combined.get(i));}
        for (int i = remainder; i < combined.size(); i++) {otherRows.add(combined.get(i));}

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
            sections.values().forEach(f -> f.writeToConfig(config.subBlock(measurement.getClass().getName()).subBlock("Standard Fields").subBlock(f.getTitle())));
            customs.forEach(f -> f.writeToConfig(config.subBlock(measurement.getClass().getName()).subBlock("Custom Fields")));
            instrumentGrid.getElements().forEach(c -> ((Configurator) c).writeToConfig(config.subBlock(measurement.getClass().getName()).subBlock("Instrument Configs").subBlock(c.getTitle())));
            config.save();
        }

    }

}
