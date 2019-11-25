package jisa.gui;

import jisa.control.IConf;
import jisa.control.RTask;
import jisa.control.Returnable;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.experiment.Col;
import jisa.experiment.ResultStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Dashboard extends Grid {

    private final Grid           topRow      = new Grid(4);
    private final Grid           plots       = new Grid(3);
    private final List<Category> categories  = new LinkedList<>();
    private       ResultStream   stream      = null;
    private       RTask          logger      = null;
    private       int            interval;
    private final Button         startButton = addToolbarButton("Start", () -> {

        String file = GUI.saveFileSelect();

        if (file != null) {
            start(file);
        }

    });

    private final Button stopButton = addToolbarButton("Stop", this::stop);

    public Dashboard(String title, int interval) {
        super(title, 1);
        setGrowth(true, false);
        addAll(topRow, plots);
        this.interval = interval;
    }

    public Dashboard(String title) {
        this(title, 2000);
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public <T extends Instrument> Category<T> addInstrument(String name, IConf<T> conf) {

        return new Category<>(name, conf);

    }

    public <T extends Instrument> Category<T> addInstrument(String name, T instrument) {
        return addInstrument(name, (IConf<T>) () -> instrument);
    }

    public class Category<T extends Instrument> {

        protected final IConf<T>         conf;
        protected final Fields           fields;
        protected final String           name;
        protected final List<Plotted<T>> list = new LinkedList<>();

        public Category(String name, IConf<T> conf) {

            this.name   = name;
            this.conf   = conf;
            this.fields = new Fields(name);

            topRow.add(fields);
            categories.add(this);

        }

        public void addMeasurement(String name, String unit, Measurable<T> measurement) {

            Plotted<T> plotted = new Plotted<>(new Col(name, unit), measurement, fields.addCheckBox(name, false));
            list.add(plotted);

        }

    }

    public synchronized void start(String filePath) throws IOException {

        if (logger != null && logger.isRunning()) {
            return;
        }

        List<Col>                columns   = new LinkedList<>();
        List<Returnable<Double>> toMeasure = new LinkedList<>();

        columns.add(new Col("Time", "mins"));

        for (Category<Instrument> cat : categories) {

            Instrument instrument = cat.conf.get();

            int i = 1;
            for (Plotted<Instrument> plotted : cat.list) {

                columns.add(plotted.header);

                if (instrument == null || !(plotted.checkBox.get())) {
                    toMeasure.add(() -> 0.0);
                } else {
                    toMeasure.add(() -> plotted.measurable.get(instrument));
                }

            }

        }

        stream = new ResultStream(filePath, columns.toArray(new Col[0]));

        int j = 1;
        for (Category<?> cat : categories) {

            for (Plotted<?> plotted : cat.list) {

                if (plotted.checkBox.get()) {

//                    plotted.plot.clear();
//                    plotted.plot.createSeries()
//                                .watch(stream, 0, j)
//                                .setName("Data")
//                                .setColour(Series.defaultColours[(j - 1) % Series.defaultColours.length])
//                                .showMarkers(false);

                    System.out.println(Series.defaultColours[(j - 1) % Series.defaultColours.length].toString());

                    plotted.plot.showLegend(false);

                }

                j++;

            }

        }

        logger = new RTask(interval, (task) -> {

            double[] data = new double[columns.size()];

            data[0] = task.getSecFromStart() / 60.0;

            for (int i = 0; i < toMeasure.size(); i++) {
                data[i + 1] = toMeasure.get(i).get();
            }

            stream.addData(data);

        });

        logger.start();

        startButton.setDisabled(true);

    }

    public void stop() {

        startButton.setDisabled(false);

        if (logger != null) {
            logger.stop();
        }

        if (stream != null) {
            stream.finalise();
        }

    }

    private class Plotted<T extends Instrument> {

        protected final Field<Boolean> checkBox;
        protected final Measurable<T>  measurable;
        protected final Plot           plot;
        protected final Col            header;

        public Plotted(Col header, Measurable<T> measurable, Field<Boolean> checkBox) {

            this.header     = header;
            this.measurable = measurable;
            this.plot       = new Plot(header.getName(), "Time [mins]", header.getTitle());
            this.checkBox   = checkBox;

            checkBox.setOnChange(() -> {

                plots.remove(plot);

                if (checkBox.get()) {
                    plots.add(plot);
                }

            });

        }

    }

    public interface Measurable<T extends Instrument> {

        double get(T device) throws DeviceException, IOException;

    }

}
