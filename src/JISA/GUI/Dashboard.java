package JISA.GUI;

import JISA.Control.Field;
import JISA.Control.IConf;
import JISA.Control.RTask;
import JISA.Control.Returnable;
import JISA.Devices.DeviceException;
import JISA.Devices.Instrument;
import JISA.Experiment.Col;
import JISA.Experiment.ResultStream;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Dashboard extends Grid {

    private final Grid           topRow     = new Grid(4);
    private final Grid           plots      = new Grid(3);
    private final List<Category> categories = new LinkedList<>();
    private       ResultStream   stream     = null;
    private       RTask          logger     = null;

    public Dashboard(String title) {
        super(title, 1);
        setGrowth(true, false);
        addAll(topRow, plots);
    }

    public <T extends Instrument> Category<T> addInstrument(String name, IConf<T> conf) {

        return new Category<>(name, conf);

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

        }

        public void addMeasurement(String name, String unit, Measurable<T> measurement) {

            Plotted<T> plotted = new Plotted<>(new Col(name, unit), measurement, fields.addCheckBox(name, false));

        }

    }

    public void start(String filePath) throws IOException {

        if (logger != null && logger.isRunning()) {
            return;
        }

        List<Col>                columns   = new LinkedList<>();
        List<Returnable<Double>> toMeasure = new LinkedList<>();

        columns.add(new Col("Time", "mins"));

        for (Category cat : categories) {

            Instrument instrument = cat.conf.get();

            int i = 1;
            for (Object p : cat.list) {

                Plotted plotted = (Plotted) p;
                columns.add(plotted.header);

                if (instrument == null || !((Boolean) plotted.checkBox.get())) {
                    toMeasure.add(() -> 0.0);
                } else {
                    toMeasure.add(() -> plotted.measurable.get(instrument));
                }

            }

        }

        stream = new ResultStream(filePath, columns.toArray(new Col[0]));

        logger = new RTask(2000, (task) -> {

            double[] data = new double[columns.size()];

            data[0] = task.getSecFromStart() / 60.0;

            for (int i = 0; i < toMeasure.size(); i++) {
                data[i + 1] = toMeasure.get(i).get();
            }

            stream.addData(data);

        });

        logger.start();

    }

    public void stop() {

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
