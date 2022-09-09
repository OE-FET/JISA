package jisa.gui;

import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.Connection;
import jisa.control.SRunnable;
import jisa.devices.Configuration;
import jisa.devices.interfaces.Instrument;
import jisa.devices.interfaces.Instrument.AutoQuantity;
import jisa.devices.interfaces.Instrument.OptionalQuantity;
import jisa.devices.interfaces.Instrument.TableQuantity;

import java.util.LinkedList;
import java.util.List;

public class Configurator<I extends Instrument> extends Fields {

    private final Configuration<I> configuration;
    private       Connection<?>    connection = null;
    private       boolean          sepLast    = false;


    public Configurator(Configuration<I> configuration) {

        super(configuration.getName());
        this.configuration = configuration;
        this.connection    = Connection.findConnectionFor(configuration.getInputInstrument());
        update();

        Connection.addListener(() -> {
            configuration.setInputInstrument(connection == null ? null : connection.getInstrument());
            update();
        });

    }

    public Configurator(String name, Class<I> target) {
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

    }

    public void writeToConfig(ConfigBlock block) {

        block.intValue("Instrument").set(connection == null ? 0 : Connection.getConnectionsByTarget(configuration.getTarget()).indexOf(connection) + 1);
        configuration.writeToConfig(block.subBlock("Configuration"));

    }

    public void linkToConfig(ConfigBlock block) {
        loadFromConfig(block);
        Util.addShutdownHook(() -> writeToConfig(block));
    }

    private synchronized void update() {

        clear();

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

        Field<Integer> instruments = addChoice("Instrument", connections.indexOf(connection), names);

        instruments.setOnChange(() -> {

            connection = connections.get(instruments.get());
            configuration.setInputInstrument(connection != null ? connection.getInstrument() : null);
            update();

        });

        if (configuration.isChoice()) {

            Field<Integer> choice = addChoice(configuration.getChoiceName(), configuration.getChoice(), configuration.getChoices().toArray(String[]::new));

            choice.setOnChange(() -> {
                configuration.selectChoice(choice.get());
                update();
            });

        }

        for (Configuration.Parameter parameter : configuration.getParameters()) {
            makeField(parameter);
        }

    }

    private Field makeField(Configuration.Parameter parameter) {

        if (AutoQuantity.class.isAssignableFrom(parameter.getType())) {

            Object  value = ((AutoQuantity) parameter.getValue()).getValue();
            boolean auto  = ((AutoQuantity) parameter.getValue()).isAuto();

            Configuration.Parameter quantity = new Configuration.Parameter(new Instrument.Parameter(parameter.getName(), value, i -> {}, parameter.getChoices().toArray()));

            Separator s1 = addSeparator();
            if (sepLast) { s1.remove(); }
            Field          qField    = makeField(quantity);
            Field<Boolean> autoCheck = addCheckBox("Auto", auto);
            Separator      s2        = addSeparator();


            autoCheck.setOnChange(() -> {
                qField.setDisabled(autoCheck.get());
                parameter.setValue(new AutoQuantity<>(autoCheck.get(), qField.get()));
            });

            qField.setOnChange(() -> parameter.setValue(new AutoQuantity<>(autoCheck.get(), qField.get())));

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
                public void setOnChange(SRunnable onChange) {

                }

                @Override
                public void editValues(String... values) {

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

            Configuration.Parameter quantity = new Configuration.Parameter(new Instrument.Parameter(parameter.getName(), value, i -> {}, parameter.getChoices().toArray()));


            Separator s1 = addSeparator();
            if (sepLast) { s1.remove(); }
            Field          qField    = makeField(quantity);
            Field<Boolean> autoCheck = addCheckBox("Use", auto);
            Separator      s2        = addSeparator();


            autoCheck.setOnChange(() -> {
                qField.setDisabled(!autoCheck.get());
                parameter.setValue(new OptionalQuantity<>(autoCheck.get(), qField.get()));
            });

            qField.setOnChange(() -> parameter.setValue(new OptionalQuantity<>(autoCheck.get(), qField.get())));

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
                public void setOnChange(SRunnable onChange) {

                }

                @Override
                public void editValues(String... values) {

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
            Field<Integer> field   = addChoice(parameter.getName(), parameter.getChoices().indexOf(parameter.getValue()), options);
            field.setOnChange(() -> parameter.setValue(parameter.getChoices().get(field.get())));

            sepLast = false;

            return field;

        } else if (parameter.getType() == Double.class) {
            Field<Double> field = addDoubleField(parameter.getName(), (Double) parameter.getValue());
            field.setOnChange(() -> parameter.setValue(field.get()));
            sepLast = false;
            return field;
        } else if (parameter.getType() == Integer.class) {
            Field<Integer> field = addIntegerField(parameter.getName(), (Integer) parameter.getValue());
            field.setOnChange(() -> parameter.setValue(field.get()));
            sepLast = false;
            return field;
        } else if (parameter.getType() == Boolean.class) {
            Field<Boolean> field = addCheckBox(parameter.getName(), (Boolean) parameter.getValue());
            field.setOnChange(() -> parameter.setValue(field.get()));
            sepLast = false;
            return field;
        } else if (parameter.getType() == TableQuantity.class) {
            Field<List<List<Double>>> field = addTable(parameter.getName(), ((TableQuantity) parameter.getValue()).getColumns());
            field.set(((TableQuantity) parameter.getValue()).getValue());
            field.setOnChange(() -> parameter.setValue(new TableQuantity(((TableQuantity) parameter.getValue()).getColumns(), field.get())));
            sepLast = false;
            return field;
        } else {
            Field<String> field = addTextField(parameter.getName(), parameter.getValue().toString());
            field.setOnChange(() -> parameter.setValue(field.get()));
            sepLast = false;
            return field;
        }

    }

    public Configuration<I> getConfiguration() {
        return configuration;
    }

}
