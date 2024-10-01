package jisa.results;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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

    @Override
    public void writeToStream(OutputStream stream, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        stream.write(Ints.toByteArray(bytes.length));
        stream.write(bytes);
    }

    @Override
    public String readFromStream(InputStream stream) throws IOException {
        return new String(stream.readNBytes(Ints.fromByteArray(stream.readNBytes(Integer.BYTES))), StandardCharsets.UTF_8);
    }

}
