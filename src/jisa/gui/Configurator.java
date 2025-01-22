package jisa.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.Connection;
import jisa.control.SRunnable;
import jisa.devices.Configuration;
import jisa.devices.Instrument;
import jisa.devices.Instrument.AutoQuantity;
import jisa.devices.Instrument.OptionalQuantity;
import jisa.gui.form.Field;
import jisa.gui.form.TableField;
import jisa.results.ResultTable;
import kotlin.reflect.KClass;

import java.util.LinkedList;
import java.util.List;

public class Configurator<I extends Instrument> extends JFXElement {

    private final Configuration<I> configuration;
    private       Connection<?>    connection = null;
    private       boolean          sepLast    = false;
    private final Form             main       = new Form("Instrument");
    private final Form             config     = new Form("Configuration");
    private final TitledPane       titled     = new TitledPane("Configuration", config.getNode());
    private final Accordion        accordion  = new Accordion(titled);

    public Configurator(Configuration<I> configuration) {

        super(configuration.getName(), new VBox());
        VBox vBox = (VBox) getNode().getCenter();
        vBox.getChildren().addAll(main.getNode(), accordion);
        VBox.setMargin(main.getNode(), new Insets(-GUI.SPACING));
        this.configuration = configuration;
        this.connection    = Connection.findConnectionFor(configuration.getInputInstrument());
        update();

        Connection.addListener(() -> {
            configuration.setInputInstrument(connection == null ? null : connection.getInstrument());
            update();
        });

        accordion.heightProperty().addListener((observable, oldValue, newValue) -> {

            if (isShowing()) {
                autoAdjustSize();
            }

        });

        accordion.expandedPaneProperty().addListener((observable, oldValue, newValue) -> {

            if (isShowing()) {
                Util.runAsync(() -> {
                    Util.sleep(500);
                    getStage().getScene().getWindow().setWidth(getStage().getScene().getWidth() + 0.001);
                });
            }

        });

    }

    public Configurator(String name, Class<I> target) {
        this(new Configuration<>(name, target));
    }

    public Configurator(String name, KClass<I> target) {
        this(new Configuration<>(name, target));
    }

    public void loadFromConfig(ConfigBlock block) {

        if (!block.hasValue("Instrument")) {
            return;
        }

        ConfigBlock.Value<Integer> instrument  = block.intValue("Instrument");
        int                        iValue      = instrument.get();
        List<Connection<?>>        connections = Connection.getConnectionsByTarget(configuration.getTarget());

        if (iValue < 1 || iValue >= connections.size() + 1) {
            connection = null;
        } else {
            connection = connections.get(iValue - 1);
        }

        configuration.setInputInstrument(connection != null ? connection.getInstrument() : null);
        configuration.loadFromConfig(block.subBlock("Configuration"));

        update();
        GUI.runNow(() -> accordion.setExpandedPane(null));

    }

    public void writeToConfig(ConfigBlock block) {

        block.intValue("Instrument").set(connection == null ? 0 : Connection.getConnectionsByTarget(configuration.getTarget()).indexOf(connection) + 1);
        configuration.writeToConfig(block.subBlock("Configuration"));

    }

    public void linkToConfig(ConfigBlock block) {
        loadFromConfig(block);
        Util.addShutdownHook(() -> writeToConfig(block));
    }

    public void setConnection(Connection connection) {

        if (Connection.getConnectionsByTarget(configuration.getTarget()).contains(connection)) {
            this.connection = connection;
            update();
        }

    }

    private synchronized void update() {

        main.clear();
        config.clear();

        List<Connection<?>> connections = new LinkedList<>();
        connections.add(null);
        connections.addAll(Connection.getConnectionsByTarget(configuration.getTarget()));

        String[] names = connections.stream().map(c -> {

            if (c == null) {
                return "None";
            } else {
                return String.format("%s (%s)", c.getName(), c.isConnected() ? c.getInstrument().getClass().getSimpleName() : "Disconnected");
            }

        }).toArray(String[]::new);

        Field<Integer> instruments = main.addChoice("Instrument", connections.indexOf(connection), names);

        instruments.addChangeListener(nv -> {

            try {

                connection = connections.get(nv);
                configuration.setInputInstrument(connection != null ? connection.getInstrument() : null);
                update();

            } catch (Throwable t) {
                t.printStackTrace();
            }

        });

        if (configuration.isChoice()) {

            Field<Integer> choice = main.addChoice(configuration.getChoiceName(), configuration.getChoice(), configuration.getChoices().toArray(String[]::new));

            choice.addChangeListener(nv -> {
                configuration.selectChoice(choice.get());
                update();
            });

        }

        boolean flag = false;
        for (Configuration.Parameter parameter : configuration.getParameters()) {
            makeField(parameter);
            flag = true;
        }

        final boolean made = flag;

        GUI.runNow(() -> {

            boolean old = accordion.isVisible();

            accordion.setVisible(made);
            accordion.setManaged(made);

            if (made) {
                accordion.setExpandedPane(titled);
            } else {
                accordion.setExpandedPane(null);
            }

            if (isShowing()) {
                autoAdjustSize();
            }

        });

    }

    public static <I extends Instrument> Form createConfigurationForm(String title, I instrument) {

        List<SRunnable> onAccept = new LinkedList<>();
        List<SRunnable> onReturn = new LinkedList<>();

        Form form = new Form(title);

        boolean sep = false;

        for (Instrument.Parameter parameter : instrument.getAllParameters()) {

            Object value = parameter.getDefaultValue();

            if (value instanceof AutoQuantity) {

                if (!sep) {
                    form.addSeparator();
                }

                AutoQuantity   cast  = (AutoQuantity) value;
                Field<Boolean> check = form.addCheckBox("Auto", cast.isAuto());
                Field          field = makeBasicField(form, parameter.getName(), cast.getValue());

                if (field == null) {
                    check.remove();
                    continue;
                }

                check.addChangeListener(field::setDisabled);
                field.setDisabled(cast.isAuto());

                onAccept.add(() -> parameter.set(new AutoQuantity<>(check.get(), field.get())));

                if (parameter.hasGetter()) {
                    onReturn.add(() -> {
                        AutoQuantity quantity = (AutoQuantity) parameter.getCurrentValue();
                        field.set(quantity.getValue());
                        check.set(quantity.isAuto());
                    });
                }

                form.addSeparator();
                sep = true;

            } else if (value instanceof OptionalQuantity) {

                if (!sep) {
                    form.addSeparator();
                }

                OptionalQuantity cast  = (OptionalQuantity) value;
                Field<Boolean> check   = form.addCheckBox("Use", cast.isUsed());
                Field          field   = makeBasicField(form, parameter.getName(), cast.getValue());

                if (field == null) {
                    check.remove();
                    continue;
                }

                check.addChangeListener(b -> field.setDisabled(!b));
                field.setDisabled(!cast.isUsed());

                onAccept.add(() -> parameter.set(new OptionalQuantity<>(check.get(), field.get())));

                if (parameter.hasGetter()) {
                    onReturn.add(() -> {
                        OptionalQuantity quantity = (OptionalQuantity) parameter.getCurrentValue();
                        field.set(quantity.getValue());
                        check.set(quantity.isUsed());
                    });
                }

                form.addSeparator();
                sep = true;

            } else if (parameter.isChoice()) {

                List choices = parameter.getChoices();
                Field<Integer> field = form.addChoice(parameter.getName(), choices.indexOf(value), (String[]) choices.stream().map(Object::toString).toArray(String[]::new));

                onAccept.add(() -> parameter.set(choices.get(field.get())));

                if (parameter.hasGetter()) {
                    onReturn.add(() -> field.set(choices.indexOf(parameter.getCurrentValue())));
                }

                sep = false;

            } else {

                Field field = makeBasicField(form, parameter.getName(), value);

                if (field == null) {
                    continue;
                }

                onAccept.add(() -> parameter.set(field.get()));

                if (parameter.hasGetter()) {
                    onReturn.add(() -> field.set(parameter.getCurrentValue()));
                }

                sep = false;

            }

        }

        form.addButton("Apply", () -> {

            onAccept.forEach(Util::runRegardless);
            onAccept.forEach(Util::runRegardless);
            onAccept.forEach(Util::runRegardless);

            onReturn.forEach(Util::runRegardless);

        });

        return form;


    }

    private static <T> Field<T> makeBasicField(Form form, String name, T defaultValue) {

        if (defaultValue instanceof Double) {
            return (Field<T>) form.addDoubleField(name, (double) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return (Field<T>) form.addIntegerField(name, (int) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return (Field<T>) form.addCheckBox(name, (boolean) defaultValue);
        } else if (defaultValue instanceof ResultTable) {
            return (Field<T>) form.addTable(name, (ResultTable) defaultValue);
        } else if (defaultValue instanceof String) {
            return (Field<T>) form.addTextField(name, (String) defaultValue);
        } else {
            return null;
        }

    }

    private Field makeField(Configuration.Parameter parameter) {

        if (AutoQuantity.class.isAssignableFrom(parameter.getType())) {

            Object  value = ((AutoQuantity) parameter.getValue()).getValue();
            boolean auto  = ((AutoQuantity) parameter.getValue()).isAuto();

            Configuration.Parameter quantity = new Configuration.Parameter(new Instrument.Parameter(parameter.getName(), value, i -> { }, parameter.getChoices().toArray()));

            Separator s1 = config.addSeparator();
            if (sepLast) { s1.remove(); }
            Field          qField    = makeField(quantity);
            Field<Boolean> autoCheck = config.addCheckBox("Auto", auto);
            Separator      s2        = config.addSeparator();


            autoCheck.addChangeListener(nv -> {
                qField.setDisabled(autoCheck.get());
                parameter.setValue(new AutoQuantity<>(autoCheck.get(), qField.get()));
            });

            qField.addChangeListener(nv -> parameter.setValue(new AutoQuantity<>(autoCheck.get(), qField.get())));

            qField.setDisabled(autoCheck.get());

            sepLast = true;

            return new Field<AutoQuantity>() {

                @Override
                public void set(AutoQuantity value) {
                    autoCheck.set(value.isAuto());
                    qField.set(value.getValue());
                }

                @Override
                public AutoQuantity get() {
                    return new AutoQuantity(autoCheck.get(), qField.get());
                }

                @Override
                public Listener<AutoQuantity> addChangeListener(Listener<AutoQuantity> onChange) {

                    autoCheck.addChangeListener(ac -> onChange.valueChanged(get()));
                    qField.addChangeListener(ac -> onChange.valueChanged(get()));

                    return onChange;

                }

                @Override
                public void removeChangeListener(Listener<AutoQuantity> onChange) {

                }

                @Override
                public boolean isDisabled() {
                    return autoCheck.isDisabled();
                }

                @Override
                public boolean isVisible() {
                    return autoCheck.isVisible();
                }

                @Override
                public void remove() {
                    qField.remove();
                    autoCheck.remove();
                    s1.remove();
                    s2.remove();
                }

                @Override
                public String getText() {
                    return parameter.getName();
                }

                @Override
                public void setDisabled(boolean disabled) {
                    autoCheck.setDisabled(disabled);
                    qField.setDisabled(autoCheck.get() || disabled);
                }


                @Override
                public void setVisible(boolean visible) {
                    qField.setVisible(visible);
                    autoCheck.setVisible(visible);
                    s1.setVisible(visible);
                    s2.setVisible(visible);
                }

                @Override
                public void setText(String text) {

                }

            };

        } else if (OptionalQuantity.class.isAssignableFrom(parameter.getType())) {

            Object  value = ((OptionalQuantity) parameter.getValue()).getValue();
            boolean auto  = ((OptionalQuantity) parameter.getValue()).isUsed();

            Configuration.Parameter quantity = new Configuration.Parameter(new Instrument.Parameter(parameter.getName(), value, i -> { }, parameter.getChoices().toArray()));


            Separator s1 = config.addSeparator();
            if (sepLast) { s1.remove(); }
            Field          qField    = makeField(quantity);
            Field<Boolean> autoCheck = config.addCheckBox("Use", auto);
            Separator      s2        = config.addSeparator();


            autoCheck.addChangeListener(nv -> {
                qField.setDisabled(!autoCheck.get());
                parameter.setValue(new OptionalQuantity<>(autoCheck.get(), qField.get()));
            });

            qField.addChangeListener(nv -> parameter.setValue(new OptionalQuantity<>(autoCheck.get(), qField.get())));

            qField.setDisabled(!autoCheck.get());

            sepLast = true;

            return new Field<OptionalQuantity>() {

                @Override
                public void set(OptionalQuantity value) {
                    autoCheck.set(value.isUsed());
                    qField.set(value.getValue());
                }

                @Override
                public OptionalQuantity get() {
                    return new OptionalQuantity(autoCheck.get(), qField.get());
                }

                @Override
                public Listener<OptionalQuantity> addChangeListener(Listener<OptionalQuantity> onChange) {

                    autoCheck.addChangeListener(ac -> onChange.valueChanged(get()));
                    qField.addChangeListener(ac -> onChange.valueChanged(get()));

                    return onChange;

                }

                @Override
                public void removeChangeListener(Listener<OptionalQuantity> onChange) {

                }

                @Override
                public boolean isDisabled() {
                    return autoCheck.isDisabled();
                }

                @Override
                public boolean isVisible() {
                    return autoCheck.isVisible();
                }

                @Override
                public void remove() {
                    qField.remove();
                    autoCheck.remove();
                    s1.remove();
                    s2.remove();
                }

                @Override
                public String getText() {
                    return qField.getText();
                }

                @Override
                public void setDisabled(boolean disabled) {
                    autoCheck.setDisabled(disabled);
                    qField.setDisabled(!autoCheck.get() || disabled);
                }

                @Override
                public void setVisible(boolean visible) {
                    qField.setVisible(visible);
                    autoCheck.setVisible(visible);
                    s1.setVisible(visible);
                    s2.setVisible(visible);
                }

                @Override
                public void setText(String text) {

                }

            };

        }

        if (parameter.isChoice()) {

            String[]       options = (String[]) parameter.getChoices().stream().map(Object::toString).toArray(String[]::new);
            Field<Integer> field   = config.addChoice(parameter.getName(), parameter.getChoices().indexOf(parameter.getValue()), options);
            field.addChangeListener(nv -> parameter.setValue(parameter.getChoices().get(field.get())));

            sepLast = false;

            return field;

        } else if (parameter.getType() == Double.class) {
            Field<Double> field = config.addDoubleField(parameter.getName(), (Double) parameter.getValue());
            field.addChangeListener(parameter::setValue);
            sepLast = false;
            return field;
        } else if (parameter.getType() == Integer.class) {
            Field<Integer> field = config.addIntegerField(parameter.getName(), (Integer) parameter.getValue());
            field.addChangeListener(parameter::setValue);
            sepLast = false;
            return field;
        } else if (parameter.getType() == Boolean.class) {
            Field<Boolean> field = config.addCheckBox(parameter.getName(), (Boolean) parameter.getValue());
            field.addChangeListener(parameter::setValue);
            sepLast = false;
            return field;
        } else if (ResultTable.class.isAssignableFrom(parameter.getType())) {
            TableField field = config.addTable(parameter.getName(), ((ResultTable) parameter.getValue()).getColumnsAsArray());
            field.set((ResultTable) parameter.getValue());
            field.addChangeListener(parameter::setValue);
            sepLast = false;
            return field;
        } else {
            Field<String> field = config.addTextField(parameter.getName(), parameter.getValue().toString());
            field.addChangeListener(parameter::setValue);
            sepLast = false;
            return field;
        }

    }

    public Configuration<I> getConfiguration() {
        return configuration;
    }

}
