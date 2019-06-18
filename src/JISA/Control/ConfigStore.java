package JISA.Control;

import JISA.Addresses.StrAddress;
import JISA.GUI.Fields;
import JISA.GUI.InstrumentConfig;
import JISA.Util;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class ConfigStore {

    private JSONObject json        = null;
    private JSONObject data        = null;
    private JSONObject instruments = null;
    private JSONObject instConfigs = null;
    private JSONObject fields      = null;
    private String     path;

    public ConfigStore(String name) throws IOException {

        path = System.getProperty("user.home") + File.separator + ".config" + File.separator + name + ".json";

        if (!Files.exists(Paths.get(System.getProperty("user.home") + File.separator + ".config"))) {
            Files.createDirectory(Paths.get(System.getProperty("user.home") + File.separator + ".config"));
        }

        if (Files.exists(Paths.get(path))) {
            String raw = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            try {
                json        = new JSONObject(raw);
                data        = json.getJSONObject("data");
                instruments = json.getJSONObject("instruments");
                instConfigs = json.getJSONObject("instConfigs");
                fields      = json.getJSONObject("fields");
            } catch (Exception e) {
                if (json == null) {
                    json = new JSONObject();
                }
                if (data == null) {
                    data = new JSONObject();
                    json.put("data", data);
                }
                if (instruments == null) {
                    instruments = new JSONObject();
                    json.put("instruments", instruments);
                }
                if (instConfigs == null) {
                    instConfigs = new JSONObject();
                    json.put("instConfigs", instruments);
                }

                fields = new JSONObject();
                json.put("fields", fields);

                save();

            }
        } else {
            json        = new JSONObject();
            data        = new JSONObject();
            instruments = new JSONObject();
            instConfigs = new JSONObject();
            fields      = new JSONObject();
            json.put("name", name);
            json.put("lastSave", System.currentTimeMillis());
            json.put("data", data);
            json.put("instruments", instruments);
            json.put("instConfigs", instConfigs);
            json.put("fields", fields);

            save();
        }

    }

    public int getLastSave() {
        return json.getInt("lastSave");
    }

    public int getInt(String key) {
        return data.getInt(key);
    }

    public int getIntOrDefault(String key, int def) {
        return has(key) ? getInt(key) : def;
    }

    public double getDouble(String key) {
        return data.getDouble(key);
    }

    public double getDoubleOrDefault(String key, double def) {
        return has(key) ? getDouble(key) : def;
    }

    public boolean getBoolean(String key) {
        return data.getBoolean(key);
    }

    public boolean getBooleanOrDefault(String key, boolean def) {
        return has(key) ? getBoolean(key) : def;
    }

    public String getString(String key) {
        return data.getString(key);
    }

    public String getStringOrDefault(String key, String def) {
        return has(key) ? getString(key) : def;
    }

    public void set(String key, Object value) throws IOException {
        data.put(key, value);
        save();
    }

    public void save() throws IOException {
        json.put("lastSave", System.currentTimeMillis());
        FileWriter writer = new FileWriter(path);
        json.write(writer, 4, 0);
        writer.close();
    }

    public boolean has(String key) {
        return data.has(key);
    }

    public void saveInstrument(String key, InstrumentConfig config) {
        JSONObject output = new JSONObject();
        output.put("address", config.getAddress() == null ? "null" : config.getAddress().toString());
        output.put("driver", config.getDriver() == null ? "null" : config.getDriver().getName());
        instruments.put(key, output);
        try {
            save();
        } catch (IOException e) {
            Util.errLog.println(e.getMessage());
        }
    }

    public void loadInstrument(String key, InstrumentConfig config) {

        try {
            JSONObject input = instruments.getJSONObject(key);
            config.setAddress(new StrAddress(input.getString("address")));
            config.setDriver(Class.forName(input.getString("driver")));
        } catch (Exception e) {
            Util.errLog.println(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }

    }

    public void saveInstConfig(String key, JSONObject data) throws IOException {
        instConfigs.put(key, data);
        save();
    }

    public JSONObject getInstConfig(String key) {

        if (instConfigs.has(key)) {
            return instConfigs.getJSONObject(key);
        } else {
            return null;
        }

    }

    public void saveFields(String name, Fields toSave) throws IOException {

        JSONArray block;

        if (fields.has(name)) {
            block = fields.getJSONArray(name);
        } else {
            block = new JSONArray();
            fields.put(name, block);
        }

        for (Field f : toSave) {
            block.put(f.get());
        }

        save();

    }

    public void loadFields(String name, Fields toLoad) {

        if (!fields.has(name)) {
            return;
        }

        JSONArray array = fields.getJSONArray(name);

        Iterator<Object> objects = array.iterator();
        Iterator<Field>  fields  = toLoad.iterator();

        while (objects.hasNext() && fields.hasNext()) {

            Object o = objects.next();
            Field  f = fields.next();

            try {
                f.set(o);
            } catch (Throwable ignored) {}

        }

    }

    public boolean hasInstConfig(String key) {
        return instConfigs.has(key);
    }

}
