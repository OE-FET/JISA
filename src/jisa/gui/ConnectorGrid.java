package jisa.gui;

import javafx.scene.layout.Pane;
import jisa.Util;
import jisa.control.ConfigBlock;
import jisa.control.ConfigStore;
import jisa.control.SRunnable;
import jisa.devices.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ConnectorGrid extends Grid {

    public  Pane                 pane;
    private String               title;
    private ArrayList<Connector> configs = new ArrayList<>();
    private ConfigBlock          config  = null;
    private String               key     = "";

    public ConnectorGrid(String title) {
        super(title);
        this.title = title;
        addToolbarButton("Connect All", this::connectAll);
        setGrowth(true, false);
    }

    public ConnectorGrid(String title, ConfigBlock c) {
        this(title, "ConGrid", c);
    }

    public ConnectorGrid(String title, String key, ConfigBlock c) {
        this(title);
        this.key = key;
        setConfigStore(c);
    }

    public <T extends Instrument> Connector<T> addInstrument(String name, Class<T> type) {

        try {
            Connector<T> conf = new Connector<>(name, type, config.subBlock(name));
            addPane(conf.getPane());
            configs.add(conf);
            return conf;
        } catch (Exception e) {
            e.printStackTrace();
            Util.errLog.println(e.getMessage());
            return null;
        }

    }

    public Connector<SMU> addSMU(String name) {
        return addInstrument(name, SMU.class);
    }

    public Connector<TC> addTC(String name) {
        return addInstrument(name, TC.class);
    }

    public Connector<LockIn> addLockIn(String name) {
        return addInstrument(name, LockIn.class);
    }

    public Connector<DPLockIn> addDPLockIn(String name) {
        return addInstrument(name, DPLockIn.class);
    }

    public Connector<DCPower> addDCPower(String name) {
        return addInstrument(name, DCPower.class);
    }

    public Connector<VPreAmp> addVPreAmp(String name) {
        return addInstrument(name, VPreAmp.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends Instrument> Connector<T>[] getInstrumentsByType(Class<T> type) {

        LinkedList<Connector<T>> list = new LinkedList<>();

        for (Connector c : configs) {

            if (type.isAssignableFrom(c.getDeviceType())) {
                list.add(c);
            }

        }

        return list.toArray(new Connector[0]);

    }

    public void connectAll() {
        for (Connector config : configs) {
            (new Thread(() -> config.connect(false))).start();
        }
    }

    public void connectAll(SRunnable onComplete) {

        Semaphore s = new Semaphore(0);

        for (Connector config : configs) {
            (new Thread(() -> {
                config.connect(false);
                s.release();
            })).start();
        }

        try {
            s.acquire(configs.size());
            onComplete.run();
        } catch (Exception e) {
            Util.exceptionHandler(e);
        }

    }

    public void setConfigStore(ConfigBlock c) {
        config = c;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
