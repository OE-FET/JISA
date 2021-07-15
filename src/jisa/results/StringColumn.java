package jisa.results;

public class StringColumn extends Column<String> {

    public StringColumn(String name, String units) {
        super(name, units, String.class);
    }

    public StringColumn(String name) {
        super(name, String.class);
    }

    public StringColumn(String name, RowEvaluable<String> evaluable) {
        super(name, String.class, evaluable);
    }

    public StringColumn(String name, String units, RowEvaluable<String> evaluable) {
        super(name, units, String.class, evaluable);
    }

    @Override
    public String stringify(Object value) {
        return "\"" + (value.toString().replace("\"", "\\\"")) + "\"";
    }

    @Override
    public String parse(String string) {
        return string.replace("\\\"", "&quot;").replace("\"", "").replace("&quot;", "\"");
    }

}
