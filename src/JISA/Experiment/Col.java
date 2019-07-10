package JISA.Experiment;

public class Col {

    private String                name;
    private String                unit;
    private ResultTable.Evaluable function;

    public Col(String name, String unit, ResultTable.Evaluable function) {
        this.name = name;
        this.unit = unit;
        this.function = function;
    }

    public Col(String name, String unit) {
        this(name, unit, null);
    }

    public Col(String name, ResultTable.Evaluable function) {
        this(name, null, function);
    }

    public Col(String name) {
        this(name, null, null);
    }

    public String getName() {
        return name;
    }

    public String getTitle() {

        if (unit == null) {
            return name;
        } else {
            return String.format("%s [%s]", name, unit);
        }

    }

    public String getUnit() {
        return unit;
    }

    public ResultTable.Evaluable getFunction() {
        return function;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean hasUnit() {
        return unit != null;
    }

    public boolean isFunction() {
        return function != null;
    }

}
