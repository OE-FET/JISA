package jisa;

import jisa.addresses.Address;
import jisa.experiment.IVCurve;
import jisa.experiment.queue.*;
import jisa.gui.DeviceShell;
import jisa.gui.Doc;
import jisa.gui.GUI;
import jisa.gui.Grid;
import jisa.gui.queue.ActionQueueDisplay;
import jisa.gui.queue.ActionQueueMessageDisplay;
import jisa.maths.Range;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Main {

    private final static int CHOICE_SCAN = 0;
    private final static int CHOICE_ADDR = 1;
    private final static int CHOICE_HELP = 2;
    private final static int CHOICE_EXIT = 3;

    public static void main(String[] args) {

        try {

            ActionQueue queue = new ActionQueue();

            SweepAction<Double> sweep1 = new SweepAction<>("Voltage Sweep", (v, actions) -> {

                List<Action> acts = new LinkedList<>();
                acts.add(new SimpleAction(String.format("Change voltage to %.02g V", v), action -> Util.sleep(1000)));
                acts.addAll(actions);

                return acts;

            }, v -> Map.of("VOLTAGE", v), Range.linear(0, 4).list());

            SweepAction<Double> sweep2 = new SweepAction<>("Temperature Sweep", (v, actions) -> {

                List<Action> acts = new LinkedList<>();
                acts.add(new SimpleAction(String.format("Change temperature to %.02g K", v), action -> Util.sleep(1000)));
                acts.addAll(actions);

                return acts;

            }, v -> Map.of("TEMPERATURE", v), List.of(100.0, 200.0, 300.0));

            MeasurementAction<IVCurve> measurementAction = new MeasurementAction<>(new IVCurve("Measurement"), (m, d) -> d.forEach((k, v) -> m.getData().setAttribute(k, v)));
            sweep1.addSweepAction(measurementAction);

            sweep2.addSweepAction(sweep1);

            queue.addAction(sweep2);

            sweep1.addSweepAction(new SimpleAction("Wait", action -> Util.sleep(1000)));

            ActionQueueDisplay        display  = new ActionQueueDisplay("Queue", queue);
            ActionQueueMessageDisplay messages = new ActionQueueMessageDisplay("Messages", queue);

            Grid grid = new Grid("Queue", display, messages);

            grid.setWindowSize(1024, 800);
            grid.show();

            Util.sleep(2500);

            queue.run();

            System.in.read();

            Doc doc = new Doc("Help");

            doc.addImage(Main.class.getResource("gui/images/jisa.png"))
               .setAlignment(Doc.Align.CENTRE);

            doc.addHeading("Testing Utility")
               .setAlignment(Doc.Align.CENTRE);

            doc.addText("This is the built-in testing utility for JISA. Using this utility, you can:");

            doc.addList(false)
               .addItem("Scan for instruments, to see what instruments JISA can detect")
               .addItem("Enter address manually, to connect to an instrument with a known address")
               .addItem("Exit, to exit this utility");

            doc.addText("For more information regarding how to include and use this library in your project, take a look at the JISA wiki at:");

            doc.addLink("https://github.com/OE-FET/JISA/wiki", "https://github.com/OE-FET/JISA/wiki")
               .setAlignment(Doc.Align.CENTRE);

            while (true) {

                // Ask the user if they want to perform a test
                int result = GUI.choiceWindow(
                    "JISA",
                    String.format("JISA Library - William Wood - 2018-%d", LocalDateTime.now().getYear()),
                    "What would you like to do?",
                    "Scan for Instruments",
                    "Enter Address Manually",
                    "Help",
                    "Exit"
                );

                switch (result) {

                    case CHOICE_SCAN:

                        Address address = GUI.browseVISA();

                        if (address == null) {
                            break;
                        }

                        // Create the device shell, connect to the device and show
                        DeviceShell shell = new DeviceShell(address);
                        shell.connect();
                        shell.showAndWait();
                        break;


                    case CHOICE_ADDR:

                        String[] values = GUI.inputWindow("JISA", "Input Address", "Please type the VISA address to connect to...", "Address");

                        if (values == null) {
                            break;
                        }

                        DeviceShell conShell = new DeviceShell(Address.parse(values[0]));
                        conShell.connect();
                        conShell.showAndWait();
                        break;


                    case CHOICE_HELP:

                        doc.showAsAlert();
                        break;


                    case CHOICE_EXIT:

                        System.exit(0);
                        break;


                }

            }

        } catch (Exception | Error e) {

            e.printStackTrace();
            GUI.showException(e);
            GUI.stopGUI();
            System.exit(0);

        }

    }

}
