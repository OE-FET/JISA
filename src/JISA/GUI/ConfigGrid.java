package JISA.GUI;

import JISA.Control.SetGettable;
import JISA.VISA.VISADevice;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class ConfigGrid extends Grid {

    public  Pane                        pane;
    private String                      title;
    private ArrayList<InstrumentConfig> configs = new ArrayList<>();

    public ConfigGrid(String title) {
        super(title);
        this.title = title;
        addToolbarButton("Connect All", this::connectAll);
    }

    public SetGettable addInstrument(String name, Class<? extends VISADevice> type) {

        try {
            InstrumentConfig<? extends VISADevice> config = new InstrumentConfig<>(name, type);
            addPane(config.getPane());
            configs.add(config);
            return config.getHandle();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

    }

    public void connectAll() {

        for (InstrumentConfig config : configs) {
            (new Thread(config::connect)).start();
        }

    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
