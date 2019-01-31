package JISA.Control;

import JISA.Addresses.StrAddress;
import JISA.GUI.InstrumentConfig;
import JISA.VISA.VISADevice;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigStore {

    private JSONObject json        = null;
    private JSONObject data        = null;
    private JSONObject instruments = null;
    private JSONObject instConfigs = null;
    private String     path;

    public ConfigStore(String name) throws IOException {

        path = System.getProperty("user.home") + File.separator + ".config" + File.separator + name + ".json";

        if (!Files.exists(Paths.get(System.getProperty("user.home") + File.separator + ".config"))) {
            Files.createDirectory(Paths.get(System.getProperty("user.home") + File.separator + ".config"));
        }

        if (Files.exists(Paths.get(path))) {
            String raw = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            try {
                json = new JSONObject(raw);
                data = json.getJSONObject("data");
                instruments = json.getJSONObject("instruments");
                instConfigs = json.getJSONObject("instConfigs");
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
            }
        } else {
            json = new JSONObject();
            data = new JSONObject();
            instruments = new JSONObject();
            instConfigs = new JSONObject();
            json.put("name", name);
            json.put("lastSave", System.currentTimeMillis());
            json.put("data", data);
            json.put("instruments", instruments);
            json.put("instConfigs", instConfigs);
        }

    }

    public int getLastSave() {
        return json.getInt("lastSave");
    }

    public int getInt(String key) {
        return data.getInt(key);
    }

    public double getDouble(String key) {
        return data.getDouble(key);
    }

    public boolean getBoolean(String key) {
        return data.getBoolean(key);
    }

    public String getString(String key) {
        return data.getString(key);
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
        output.put("address", config.getAddress() == null ? "null" : config.getAddress().getVISAAddress());
        output.put("driver", config.getDriver() == null ? "null" : config.getDriver().getName());
        instruments.put(key, output);
        try {
            save();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void loadInstrument(String key, InstrumentConfig config) {

        try {
            JSONObject input = instruments.getJSONObject(key);
            config.setAddress(new StrAddress(input.getString("address")));
            config.setDriver(Class.forName(input.getString("driver")));
        } catch (Exception e) {
            System.err.println(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
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

    public boolean hasInstConfig(String key) {
        return instConfigs.has(key);
    }

}
