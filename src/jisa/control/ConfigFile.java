package jisa.control;

import jisa.Util;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigFile implements ConfigBlock {

    private final File       file;
    private       JSONObject root;

    public ConfigFile(String name) throws IOException {

        String filePath  = Util.joinPath(System.getProperty("user.home"), ".config", name + ".json");
        String directory = Util.joinPath(System.getProperty("user.home"), ".config");

        file = new File(filePath);

        if (!Files.exists(Paths.get(directory))) Files.createDirectory(Paths.get(directory));

        if (file.exists()) {

            String raw = Files.readString(file.toPath());

            try {
                root = new JSONObject(raw);
            } catch (Exception e) {
                root = new JSONObject();
            }

        } else {
            root = new JSONObject();
        }

        Util.addShutdownHook(this::save);

    }

    public synchronized void save() {

        try {
            root.put("lastSave", System.currentTimeMillis());
            FileWriter writer = new FileWriter(file);
            root.write(writer, 4, 0);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Value<String> stringValue(String name) {

        if (!root.has(name)) {
            root.put(name, "");
        }

        return new Value<>() {

            @Override
            public void set(Object value) {
                root.put(name, value);
            }

            @Override
            public String get() {
                return root.getString(name);
            }

            @Override
            public String getOrDefault(String defaultValue) {
                return root.has(name) ? get() : defaultValue;
            }

        };

    }

    public Value<Double> doubleValue(String name) {

        if (!root.has(name)) {
            root.put(name, "");
        }

        return new Value<>() {

            @Override
            public void set(Object value) {
                root.put(name, value);
            }

            @Override
            public Double get() {
                return root.getDouble(name);
            }

            @Override
            public Double getOrDefault(Double defaultValue) {
                return root.has(name) ? get() : defaultValue;
            }

        };

    }

    public Value<Integer> intValue(String name) {

        return new Value<>() {

            @Override
            public void set(Object value) {
                root.put(name, value);
            }

            @Override
            public Integer get() {
                return root.getInt(name);
            }

            @Override
            public Integer getOrDefault(Integer defaultValue) {
                return root.has(name) ? get() : defaultValue;
            }

        };

    }

    public Value<Boolean> booleanValue(String name) {

        if (!root.has(name)) {
            root.put(name, "");
        }

        return new Value<>() {

            @Override
            public void set(Object value) {
                root.put(name, value);
            }

            @Override
            public Boolean get() {
                return root.getBoolean(name);
            }

            @Override
            public Boolean getOrDefault(Boolean defaultValue) {
                return root.has(name) ? get() : defaultValue;
            }

        };

    }

    @Override
    public ConfigBlock subBlock(String name) {

        JSONObject object;
        if (root.has(name)) {
            object = root.getJSONObject(name);
        } else {
            object = new JSONObject();
            root.put(name, object);
            save();
        }

        return new Sub(object);

    }

    protected class Sub implements ConfigBlock {

        private final JSONObject sub;

        Sub(JSONObject sub) {
            this.sub = sub;
        }

        @Override
        public Value<String> stringValue(String name) {

            return new Value<>() {

                @Override
                public void set(Object value) {
                    sub.put(name, value);
                }

                @Override
                public String get() {
                    return sub.getString(name);
                }

                @Override
                public String getOrDefault(String defaultValue) {
                    return sub.has(name) ? get() : defaultValue;
                }

            };

        }

        @Override
        public Value<Integer> intValue(String name) {

            return new Value<>() {

                @Override
                public void set(Object value) {
                    sub.put(name, value);
                }

                @Override
                public Integer get() {
                    return sub.getInt(name);
                }

                @Override
                public Integer getOrDefault(Integer defaultValue) {
                    return sub.has(name) ? get() : defaultValue;
                }

            };

        }

        @Override
        public Value<Double> doubleValue(String name) {

            return new Value<>() {

                @Override
                public void set(Object value) {
                    sub.put(name, value);
                }

                @Override
                public Double get() {
                    return sub.getDouble(name);
                }

                @Override
                public Double getOrDefault(Double defaultValue) {
                    return sub.has(name) ? get() : defaultValue;
                }

            };

        }

        @Override
        public Value<Boolean> booleanValue(String name) {

            return new Value<>() {

                @Override
                public void set(Object value) {
                    sub.put(name, value);
                }

                @Override
                public Boolean get() {
                    return sub.getBoolean(name);
                }

                @Override
                public Boolean getOrDefault(Boolean defaultValue) {
                    return sub.has(name) ? get() : defaultValue;
                }

            };

        }

        @Override
        public ConfigBlock subBlock(String name) {

            JSONObject object;
            if (sub.has(name)) {
                object = sub.getJSONObject(name);
            } else {
                object = new JSONObject();
                sub.put(name, object);
                save();
            }

            return new Sub(object);

        }

        @Override
        public void save() {
            ConfigFile.this.save();
        }
    }

}
