package JISA.GUI;

import JISA.Control.ConfigStore;
import JISA.Control.SetGettable;
import JISA.VISA.VISADevice;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class ConfigGrid extends Grid {

    public  Pane                        pane;
    private String                      title;
    private ArrayList<InstrumentConfig> configs = new ArrayList<>();
    private ConfigStore                 config  = null;

    public ConfigGrid(String title) {
        super(title);
        this.title = title;
        addToolbarButton("Connect All", this::connectAll);
    }

    public ConfigGrid(String title, ConfigStore c) {
        this(title);
        setConfigStore(c);
    }

    public InstrumentConfig addInstrument(String name, Class<? extends VISADevice> type) {

        try {
            String key = String.format("instrument%d", configs.size());
            InstrumentConfig<? extends VISADevice> conf = new InstrumentConfig<>(name, key, type, config);
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
            (new Thread(()-> config.connect(false))).start();
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
