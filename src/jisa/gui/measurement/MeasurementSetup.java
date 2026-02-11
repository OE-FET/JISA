package jisa.gui.measurement;

import jisa.Util;
import jisa.control.SRunnable;
import jisa.experiment.Measurement;
import jisa.gui.Configurator;
import jisa.gui.Form;
import jisa.gui.Grid;
import jisa.gui.Tabs;
import jisa.gui.form.ChoiceField;
import jisa.gui.form.Field;
import jisa.maths.Range;
import jisa.results.ResultList;
import jisa.results.ResultStream;
import jisa.results.ResultTable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MeasurementSetup<R extends Measurement<?>> extends Tabs {

    public static final Map<Class, FieldCreator> DEFAULT_TYPES = Util.buildMap(map -> {
        map.put(String.class, (FieldCreator<String>) Form::addTextField);
        map.put(Double.class, (FieldCreator<Double>) Form::addDoubleField);
        map.put(Integer.class, (FieldCreator<Integer>) Form::addIntegerField);
        map.put(Boolean.class, (FieldCreator<Boolean>) Form::addCheckBox);
        map.put(Range.class, (FieldCreator<Range<Double>>) Form::addDoubleRange);
        map.put(ResultTable.class, (FieldCreator<ResultTable>) Form::addTable);
        map.put(ResultList.class, (FieldCreator<ResultTable>) Form::addTable);
        map.put(ResultStream.class, (FieldCreator<ResultTable>) Form::addTable);
    });

    private final R               measurement;
    private final Grid            parameters  = new Grid("Parameters", 2);
    private final Grid            instruments = new Grid("Instruments", 1);
    private final List<SRunnable> setters     = new LinkedList<>();

    public MeasurementSetup(R measurement) {
        this(String.format("%s: %s", measurement.getName(), measurement.getLabel()), measurement);
    }

    public MeasurementSetup(String title, R measurement) {

        super(title);
        this.measurement = measurement;

        measurement.getInstruments().stream().map(i -> new Util.Pair<Measurement.InstrumentValue, Configurator>(i, new Configurator(i.getName(), i.getType()))).forEach(p -> {

            p.a().setConfiguration(p.b().getConfiguration());
            p.b().getConfiguration().setInputInstrument(p.a().get());
            instruments.add(p.b());
            setters.add(() -> p.a().set(p.b().getConfiguration().getInstrument()));

        });

        Map<String, Form> sections = new HashMap<>();

        for (Measurement.ParamValue p : measurement.getParameters()) {

            if (p.getType() == Measurement.Type.CUSTOM) {
                p.set(null);
                setters.add(p::get);
                continue;
            }

            if (!sections.containsKey(p.getSection())) {
                Form section = new Form(p.getSection());
                sections.put(p.getSection(), section);
                parameters.add(section);
            }

            Form form = sections.get(p.getSection());

            Class dataType = p.getDataType();

            if (p.isChoice()) {

                List<String> options = p.getOptions();

                if (dataType == Integer.TYPE || dataType == Integer.class) {
                    Field field = form.addChoice(p.getName(), (Integer) p.get(), options.toArray(String[]::new));
                    setters.add(() -> p.set(field.get()));
                } else if (dataType == String.class) {
                    Field field = form.addTextChoice(p.getName(), (String) p.get(), options.toArray(String[]::new));
                    setters.add(() -> p.set(field.get()));
                }

            } else if (p.getType() == Measurement.Type.AUTO) {

                if (DEFAULT_TYPES.containsKey(dataType)) {

                    Field field = DEFAULT_TYPES.get(dataType).create(form, p.getName(), p.get());
                    setters.add(() -> p.set(field.get()));

                } else if (dataType.isEnum()) {

                    List<Object>         enums = List.of(dataType.getEnumConstants());
                    ChoiceField<Integer> field = form.addChoice(p.getName(), enums.indexOf(p.get()), enums.stream().map(Object::toString).toArray(String[]::new));

                    setters.add(() -> p.set(enums.get(field.get())));

                }

            } else if (p.getType() == Measurement.Type.TIME && (dataType == Integer.TYPE || dataType == Integer.class)) {

                Field<Integer> field = form.addTimeField(p.getName(), (Integer) p.get());
                setters.add(() -> p.set(field.get()));

            } else if (p.getType() == Measurement.Type.FILE_OPEN && dataType == String.class) {

                Field<String> field = form.addFileOpen(p.getName(), (String) p.get());
                setters.add(() -> p.set(field.get()));

            } else if (p.getType() == Measurement.Type.FILE_SAVE && dataType == String.class) {

                Field<String> field = form.addFileSave(p.getName(), (String) p.get());
                setters.add(() -> p.set(field.get()));

            } else if (p.getType() == Measurement.Type.DIRECTORY_SELECT && dataType == String.class) {

                Field<String> field = form.addDirectorySelect(p.getName(), (String) p.get());
                setters.add(() -> p.set(field.get()));

            }

        }

        parameters.addAll(measurement.getCustomGUIElements());

        parameters.setGrowth(true, false);
        instruments.setGrowth(true, false);

        addAll(parameters, instruments);
        autoAdjustSize();

    }

    public R getMeasurement() {
        return measurement;
    }

    public interface FieldCreator<T> {
        Field<T> create(Form form, String name, T def);
    }

    public void apply() {
        setters.forEach(Util::runRegardless);
    }

    public boolean showAndApply() {

        if (showAsConfirmation()) {
            apply();
            return true;
        }

        return false;

    }

}
