package jisa.control;

import jisa.Util;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigStore {

    private JSONObject json = null;
    private JSONObject gui  = null;
    private JSONObject data = null;
    private String     path;

    public ConfigStore(String name) throws IOException {

        path = System.getProperty("user.home") + File.separator + ".config" + File.separator + name + ".json";

        if (!Files.exists(Paths.get(System.getProperty("user.home") + File.separator + ".config"))) {
            Files.createDirectory(Paths.get(System.getProperty("user.home") + File.separator + ".config"));
        }

        if (Files.exists(Paths.get(path))) {
            String raw = Files.readString(Paths.get(path));
            try {
                json = new JSONObject(raw);
                gui  = json.getJSONObject("gui");
                data = json.getJSONObject("data");
            } catch (Exception e) {

                if (json == null) {
                    json = new JSONObject();
                }

                if (gui == null) {
                    gui = new JSONObject();
                }

                data = new JSONObject();
                json.put("gui", gui);
                json.put("data", data);

                save();

            }
        } else {
            json = new JSONObject();
            gui  = new JSONObject();
            data = new JSONObject();
            json.put("name", name);
            json.put("lastSave", System.currentTimeMillis());
            json.put("gui", gui);
            json.put("data", data);
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

    public JSONObject getGUIConfigs(Class<?> guiClass) {

        try {
            if (gui.has(guiClass.getName())) {
                return gui.getJSONObject(guiClass.getName());
            } else {
                JSONObject object = new JSONObject();
                gui.put(guiClass.getName(), object);
                save();
                return object;
            }
        } catch (IOException e) {
            Util.errLog.println(e.getMessage());
            return new JSONObject();
        }

    }

//    public void saveFields(String name, Fields toSave) throws IOException {
//
//        JSONArray block = new JSONArray();
//        fields.put(name, block);
//
//        for (Field f : toSave) {
//            block.put(f.get());
//        }
//
//        save();
//
//    }
//
//    public void loadFields(String name, Fields toLoad) {
//
//        if (!fields.has(name)) {
//            return;
//        }
//
//        JSONArray array = fields.getJSONArray(name);
//
//        Iterator<Object> objects = array.iterator();
//        Iterator<Field>  fields  = toLoad.iterator();
//
//        while (objects.hasNext() && fields.hasNext()) {
//
//            Object o = objects.next();
//            Field  f = fields.next();
//
//            try {
//
//                if (f.get() instanceof Double && o instanceof Integer) {
//                    f.set(((Integer) o).doubleValue());
//                } else {
//                    f.set(o);
//                }
//
//            } catch (Throwable ignored) {
//            }
//
//        }
//
//    }

    public class SubStore {

        private final JSONObject root;

        public SubStore(JSONObject root) {
            this.root = root;
        }



    }

}
