package jisa.gui.controls;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import jisa.Util;
import jisa.gui.GUI;
import jisa.gui.form.Field;
import jisa.gui.form.Form;
import jisa.maths.Range;

import java.util.Arrays;
import java.util.stream.Collectors;

public class RangeInput extends VBox {

    public final  TableView<Double>           tableView;
    private final TableColumn<Double, Double> column;

    private final MenuButton addButton      = new MenuButton("✚");
    private final Button     remButton      = new Button("✕");
    private final Button     mUpButton      = new Button("▲");
    private final Button     mDnButton      = new Button("▼");
    private final Button     clrButton      = new Button("Clear");
    private final MenuItem   addManual      = new MenuItem("Single Value...");
    private final MenuItem   addManualList  = new MenuItem("Comma-Separated List...");
    private final MenuItem   addLinear      = new MenuItem("Linear Range...");
    private final MenuItem   addStep        = new MenuItem("Equal Space Range...");
    private final MenuItem   addPoly        = new MenuItem("Polynomial Range...");
    private final MenuItem   addGeometric   = new MenuItem("Geometric Range...");
    private final MenuItem   addExponential = new MenuItem("Exponential Range...");

    public final  Form          manualResponse;
    private final Field<Double> manualValue;

    public final  Form           linearResponse;
    private final Field<Double>  linearStart;
    private final Field<Double>  linearStop;
    private final Field<Integer> linearSteps;
    private final Field<Boolean> linearSym;

    public final  Form           stepResponse;
    private final Field<Double>  stepStart;
    private final Field<Double>  stepStop;
    private final Field<Double>  stepStep;
    private final Field<Boolean> stepSym;

    public final  Form           polyResponse;
    private final Field<Double>  polyStart;
    private final Field<Double>  polyStop;
    private final Field<Integer> polySteps;
    private final Field<Integer> polyOrder;
    private final Field<Boolean> polySym;

    public final  Form           geomResponse;
    private final Field<Double>  geomStart;
    private final Field<Double>  geomStop;
    private final Field<Double>  geomStep;
    private final Field<Boolean> geomSym;

    public final  Form           expResponse;
    private final Field<Double>  expStart;
    private final Field<Double>  expStop;
    private final Field<Integer> expSteps;
    private final Field<Boolean> expSym;

    public RangeInput(String name, double min, double max, int count, double dStep, int dOrder) {

        tableView = new TableView<>();
        column    = new TableColumn(name);

        manualResponse = new Form("Add Single Value");
        manualValue    = manualResponse.addDoubleField("Value");

        linearResponse = new Form("Add Linear Range");
        linearStart    = linearResponse.addDoubleField("Start", min);
        linearStop     = linearResponse.addDoubleField("Stop", max);
        linearSteps    = linearResponse.addIntegerField("No. Steps", count);
        linearSym      = linearResponse.addCheckBox("Symmetrical", false);

        stepResponse = new Form("Add Equal Space Range");
        stepStart    = stepResponse.addDoubleField("Start", min);
        stepStop     = stepResponse.addDoubleField("Stop", max);
        stepStep     = stepResponse.addDoubleField("Step Size", dStep);
        stepSym      = stepResponse.addCheckBox("Symmetrical", false);

        polyResponse = new Form("Add Polynomial Range");
        polyStart    = polyResponse.addDoubleField("Start", min);
        polyStop     = polyResponse.addDoubleField("Stop", max);
        polySteps    = polyResponse.addIntegerField("No. Steps", count);
        polyOrder    = polyResponse.addIntegerField("Order", dOrder);
        polySym      = polyResponse.addCheckBox("Symmetrical", false);

        geomResponse = new Form("Add Geometric Range");
        geomStart    = geomResponse.addDoubleField("Start", min);
        geomStop     = geomResponse.addDoubleField("Stop", max);
        geomStep     = geomResponse.addDoubleField("Factor", dStep);
        geomSym      = geomResponse.addCheckBox("Symmetrical", false);

        expResponse = new Form("Add Geometric Range");
        expStart    = expResponse.addDoubleField("Start", min);
        expStop     = expResponse.addDoubleField("Stop", max);
        expSteps    = expResponse.addIntegerField("No. Steps", count);
        expSym      = expResponse.addCheckBox("Symmetrical", false);

        column.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue()));

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setMinHeight(150.0);
        tableView.setPrefWidth(75.0);
        tableView.getColumns().add(column);

        remButton.setFont(Font.font(remButton.getFont().getFamily(), FontWeight.BOLD, remButton.getFont().getSize()));

        addManual.setOnAction(e -> Util.runAsync(() -> {

            if (manualResponse.showAsConfirmation()) {
                tableView.getItems().add(manualValue.get());
            }

        }));

        addManualList.setOnAction(e -> Util.runAsync(() -> {

            String[] raw = GUI.inputWindow("Add", "Add Values", "Please enter your values as a comma-separated list:", "Values");

            if (raw != null) {
                tableView.getItems().addAll(
                    Arrays.stream(raw[0].split(","))
                          .map(String::trim)
                          .map(Double::parseDouble)
                          .collect(Collectors.toList())
                );
            }

        }));

        addLinear.setOnAction(e -> Util.runAsync(() -> {

            if (linearResponse.showAsConfirmation()) {

                Range<Double> range = Range.linear(linearStart.get(), linearStop.get(), linearSteps.get());

                if (linearSym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(range.array());

            }

        }));

        addStep.setOnAction(e -> Util.runAsync(() -> {

            if (stepResponse.showAsConfirmation()) {

                Range<Double> range = Range.step(stepStart.get(), stepStop.get(), stepStep.get());

                if (stepSym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(range.array());

            }

        }));

        addPoly.setOnAction(e -> Util.runAsync(() -> {

            if (polyResponse.showAsConfirmation()) {

                Range<Double> range = Range.polynomial(polyStart.get(), polyStop.get(), polySteps.get(), polyOrder.get());

                if (polySym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(range.array());

            }

        }));

        addGeometric.setOnAction(e -> Util.runAsync(() -> {

            if (geomResponse.showAsConfirmation()) {

                Range<Double> range = Range.geometric(geomStart.get(), geomStop.get(), geomStep.get());

                if (geomSym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(range.array());
            }

        }));

        addExponential.setOnAction(e -> Util.runAsync(() -> {

            if (expResponse.showAsConfirmation()) {

                Range<Double> range = Range.exponential(expStart.get(), expStop.get(), expSteps.get());

                if (expSym.get()) {
                    range = range.mirror();
                }

                tableView.getItems().addAll(range.array());

            }

        }));

        remButton.setOnAction(e -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index > -1) {
                tableView.getItems().remove(index);
            }

        });

        mUpButton.setOnAction(e -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index > 0) {
                Double toMove = tableView.getItems().get(index);
                tableView.getItems().set(index, tableView.getItems().get(index - 1));
                tableView.getItems().set(index - 1, toMove);
                tableView.getSelectionModel().select(index - 1);
            }

        });

        mDnButton.setOnAction(e -> {

            int index = tableView.getSelectionModel().getSelectedIndex();

            if (index < tableView.getItems().size() - 1 && index > -1) {
                Double toMove = tableView.getItems().get(index);
                tableView.getItems().set(index, tableView.getItems().get(index + 1));
                tableView.getItems().set(index + 1, toMove);
                tableView.getSelectionModel().select(index + 1);
            }

        });

        clrButton.setOnAction(e -> tableView.getItems().clear());

        addButton.getItems()
                 .addAll(addManual, addManualList, addLinear, addStep, addPoly, addGeometric, addExponential);

        HBox hBox = new HBox(addButton, remButton, mUpButton, mDnButton, clrButton);

        this.getChildren().setAll(hBox, tableView);

        hBox.setSpacing(5.0);
        this.setSpacing(5.0);


    }

    public void disabled(boolean disabled) {
        tableView.setDisable(disabled);
        addButton.setDisable(disabled);
        remButton.setDisable(disabled);
        mUpButton.setDisable(disabled);
        mDnButton.setDisable(disabled);
    }

    public boolean disabled() {
        return tableView.isDisabled();
    }

    public Range<Double> getRange() {
        return new Range<>(tableView.getItems().toArray(Double[]::new));
    }

    public void setRange(Range<Double> range) {

        tableView.getItems().clear();
        tableView.getItems().addAll(range.array());

    }

}
