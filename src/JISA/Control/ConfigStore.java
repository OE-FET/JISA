package JISA.Control;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigStore {

    private JSONObject json;
    private JSONObject data;
    private String     path;

    public ConfigStore(String name) throws IOException {

        path = System.getProperty("user.home") + File.separator + ".config" + File.separator + name + ".json";

        if (!Files.exists(Paths.get(System.getProperty("user.home") + File.separator + ".config/"))) {
            Files.createDirectory(Paths.get(System.getProperty("user.home") + File.separator + ".config/"));
        }

        if (Files.exists(Paths.get(path))) {
            String raw = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            json = new JSONObject(raw);
            data = json.getJSONObject("data");
        } else {
            json = new JSONObject();
            data = new JSONObject();
            json.put("name", name);
            json.put("lastSave", System.currentTimeMillis());
            json.put("data", data);
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
        json.write(writer);
        writer.close();
    }

}
