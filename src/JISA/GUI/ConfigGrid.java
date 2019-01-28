package JISA.GUI;

import JISA.Control.ConfigStore;
import JISA.Control.SRunnable;
import JISA.Control.SetGettable;
import JISA.Util;
import JISA.VISA.VISADevice;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class ConfigGrid extends Grid {

    public  Pane                        pane;
    private String                      title;
    private ArrayList<InstrumentConfig> configs = new ArrayList<>();
    private ConfigStore                 config  = null;

    public ConfigGrid(String title) {
        super(title);
        this.title = title;
        addToolbarButton("Connect All", this::connectAll);
        setGrowth(true, false);
    }

    public ConfigGrid(String title, ConfigStore c) {
        this(title);
        setConfigStore(c);
    }

    public <T extends VISADevice> InstrumentConfig<T> addInstrument(String name, Class<T> type) {

        try {
            String              key  = String.format("instrument%d", configs.size());
            InstrumentConfig<T> conf = new InstrumentConfig<>(name, key, type, config);
            addPane(conf.getPane());
            configs.add(conf);
            return conf;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

    }

    public void connectAll() {
        for (InstrumentConfig config : configs) {
            (new Thread(() -> config.connect(false))).start();
        }
    }

    public void connectAll(SRunnable onComplete) {

        Semaphore s = new Semaphore(0);

        for (InstrumentConfig config : configs) {
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
