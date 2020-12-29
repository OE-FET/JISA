package jisa.gui;

import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.Connection;
import jisa.devices.Instrument;
import jisa.experiment.ActionQueue;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class ConnectorGrid extends Grid {

    private final List<Connector<?>> connectors = new LinkedList<>();

    public ConnectorGrid(String title, int numCols) {

        super(title, numCols);
        setGrowth(true, false);

        MenuButton button = addToolbarMenuButton("Add...");
        button.addItem("Driver Interface:", () -> {}).setDisabled(true);
        button.addSeparator();

        (new Reflections("jisa.devices"))
            .getSubTypesOf(Instrument.class).stream()
            .filter(Class::isInterface)
            .sorted(Comparator.comparing(Class::getSimpleName))
            .forEach(c -> {
                String name;
                try {
                    name = String.format("%s (%s)", c.getMethod("getDescription").invoke(null), c.getSimpleName());
                } catch (Exception e){
                    name = c.getSimpleName();
                }
                button.addItem(name, () -> addConnector(c));
            });

        addToolbarButton("Connect All", () -> {
            Element list = connectAllWithList();
            Util.sleep(1000);
            list.close();
        });

    }

    public <T extends Instrument> Connector<T> addConnector(Connector<T> connector) {

        connector.setRemoveButton(() -> removeConnector(connector));
        connectors.add(connector);
        add(connector);

        return connector;

    }

    public Connector<?> addConnector(Class type) {

        String[] input = GUI.inputWindow("Add Instrument", "Add Instrument", "Please enter a name for the connection...", "Name");

        if (input != null) {
            return addConnector(input[0], type);
        } else {
            return null;
        }

    }

    public void removeConnectors() {

        Fields                         input     = new Fields("Remove Connectors");
        Map<Connector, Field<Boolean>> selection = new HashMap<>();

        for (Connector connector : getConnectors()) {
            Field<Boolean> selected = input.addCheckBox(connector.getConnection().getName(), false);
            selection.put(connector, selected);
        }

        if (input.showAsConfirmation()) {

            selection.forEach((c, r) -> {
                if (r.get()) {
                    removeConnector(c);
                }
            });

        }

    }

    public void removeConnector(Connector connector) {
        connectors.remove(connector);
        remove(connector);
        connector.getConnection().delete();
    }

    public void loadFromConfig(ConfigBlock block) {

        for (ConfigBlock sub : block.getSubBlocks().entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).collect(Collectors.toUnmodifiableList())) {

            try {
                Connector connector = addConnector(sub.stringValue("Name").get(), (Class<? extends Instrument>) Class.forName(sub.stringValue("Target").get()));
                connector.loadFromConfig(sub.subBlock("Configuration"));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void writeToConfig(ConfigBlock block) {

        block.clear();

        int i = 0;
        for (Connector connector : getConnectors()) {

            ConfigBlock sub = block.subBlock(String.format("Connection %d", i++));
            sub.stringValue("Name").set(connector.connection.getName());
            sub.stringValue("Target").set(connector.getConnection().getType().getName());
            connector.writeToConfig(sub.subBlock("Configuration"));

        }

        block.save();

    }

    public void linkToConfig(ConfigBlock block) {
        loadFromConfig(block);
        Util.addShutdownHook(() -> writeToConfig(block));
    }

    public <T extends Instrument> Connector<T> addConnector(Connection<T> connection) {
        return addConnector(new Connector<>(connection));
    }


    public <T extends Instrument> Connector<T> addConnector(String name, Class<T> target) {
        return addConnector(new Connection<>(name, target));
    }

    public List<Connector<?>> getConnectors() {
        return List.copyOf(connectors);
    }

    public List<Connection<?>> getConnections() {
        return connectors.stream().map(Connector::getConnection).collect(Collectors.toUnmodifiableList());
    }

    public void connectAll() {
        getConnectors().forEach(Connector::connect);
    }

    public Element connectAllWithList() {

        ListDisplay<Connector> list = new ListDisplay<>("Connections");

        list.setWindowWidth(800.0);
        list.setWindowHeight(500.0);

        for (Connector connector : getConnectors()) {

            ListDisplay.Item<Connector> item = list.add(
                connector,
                String.format("Connect to \"%s\" (%s)", connector.getTitle(), connector.getConnection().getDriver() != null ? connector.getConnection().getDriver().getSimpleName() : "None"),
                connector.getConnection().getDriver() != null ? "Waiting..." : "Not Configured",
                connector.getConnection().getDriver() != null ? ActionQueue.Status.NOT_STARTED.getImage() : ActionQueue.Status.INTERRUPTED.getImage()
            );

            connector.getConnection().addChangeListener(() -> {

                switch (connector.getConnection().getStatus()) {

                    case CONNECTING:
                        item.setImage(ActionQueue.Status.RUNNING.getImage());
                        item.setSubTitle("Connecting...");
                        break;

                    case CONNECTED:
                        item.setImage(ActionQueue.Status.COMPLETED.getImage());
                        item.setSubTitle("Connection Successful");
                        break;

                    case ERROR:
                        item.setImage(ActionQueue.Status.ERROR.getImage());
                        item.setSubTitle("Connection Error");
                        break;

                    default:
                        item.setImage(ActionQueue.Status.NOT_STARTED.getImage());
                        item.setSubTitle("Waiting...");
                        break;

                }

            });

        }

        list.show();

        getConnectors().parallelStream().forEach(Connector::connect);

        return list;

    }

}
