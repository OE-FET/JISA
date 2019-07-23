package jisa.gui;

import jisa.control.ConfigStore;
import jisa.control.SRunnable;
import jisa.devices.*;
import jisa.Util;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ConnectionGrid extends Grid {

    public  Pane                  pane;
    private String                title;
    private ArrayList<Connection> configs = new ArrayList<>();
    private ConfigStore           config  = null;

    public ConnectionGrid(String title) {
        super(title);
        this.title = title;
        addToolbarButton("Connect All", this::connectAll);
        setGrowth(true, false);
    }

    public ConnectionGrid(String title, ConfigStore c) {
        this(title);
        setConfigStore(c);
    }

    public <T extends Instrument> Connection<T> addInstrument(String name, Class<T> type) {

        try {
            String        key  = String.format("instrument%d", configs.size());
            Connection<T> conf = new Connection<>(name, key, type, config);
            addPane(conf.getPane());
            configs.add(conf);
            return conf;
        } catch (Exception e) {
            e.printStackTrace();
            Util.errLog.println(e.getMessage());
            return null;
        }

    }

    public Connection<SMU> addSMU(String name) {
        return addInstrument(name, SMU.class);
    }

    public Connection<TC> addTC(String name) {
        return addInstrument(name, TC.class);
    }

    public Connection<LockIn> addLockIn(String name) {
        return addInstrument(name, LockIn.class);
    }

    public Connection<DPLockIn> addDPLockIn(String name) {
        return addInstrument(name, DPLockIn.class);
    }

    public Connection<DCPower> addDCPower(String name) {
        return addInstrument(name, DCPower.class);
    }

    public Connection<VPreAmp> addVPreAmp(String name) {
        return addInstrument(name, VPreAmp.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends Instrument> Connection<T>[] getInstrumentsByType(Class<T> type) {

        LinkedList<Connection<T>> list = new LinkedList<>();

        for (Connection c : configs) {

            if (type.isAssignableFrom(c.getDeviceType())) {
                list.add(c);
            }

        }

        return list.toArray(new Connection[0]);

    }

    public void connectAll() {
        for (Connection config : configs) {
            (new Thread(() -> config.connect(false))).start();
        }
    }

    public void connectAll(SRunnable onComplete) {

        Semaphore s = new Semaphore(0);

        for (Connection config : configs) {
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

    public void setConfigStore(ConfigStore c) {
        config = c;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
