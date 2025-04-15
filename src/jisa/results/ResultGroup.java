package jisa.results;

import jisa.Util;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResultGroup {

    private final Map<String, ResultTable> tables = new LinkedHashMap<>();
    private final Map<String, ResultGroup> groups = new LinkedHashMap<>();

    public static ResultGroup loadFile(String path) throws IOException {

        ResultGroup root = new ResultGroup();

        FileInputStream           fis = new FileInputStream(path);
        BufferedInputStream       bis = new BufferedInputStream(fis);
        GzipCompressorInputStream gis = new GzipCompressorInputStream(bis);
        TarArchiveInputStream     tar = new TarArchiveInputStream(gis);

        while (tar.getNextTarEntry() != null) {

            TarArchiveEntry entry = tar.getCurrentEntry();
            String[]        parts = entry.getName().split("/");
            String          name  = parts[parts.length - 1];

            ResultGroup last = root;

            for (int i = 0; i < parts.length - 1; i++) {

                ResultGroup group;

                if (last.containsGroup(parts[i])) {
                    group = last.getGroup(parts[i]);
                } else {
                    group = new ResultGroup();
                    last.addGroup(parts[i], group);
                }

                last = group;

            }

            if (name.endsWith(".csv")) {
                last.addTable(name.replace(".csv", ""), ResultList.loadFromCSVStream(tar));
            } else if (name.endsWith(".jdf")) {
                last.addTable(name.replace(".jdf", ""), ResultList.loadFromBinaryStream(tar));
            }

        }

        return root;

    }

    private static void addDirectory(ResultGroup group, TarArchiveInputStream tar, List<TarArchiveEntry> entries) throws IOException {

        while (tar.getNextTarEntry() != null && entries.contains(tar.getCurrentEntry())) {

            TarArchiveEntry entry = tar.getCurrentEntry();

            if (entry.isDirectory()) {

                ResultGroup subGroup = new ResultGroup();
                addDirectory(subGroup, tar, List.of(entry.getDirectoryEntries()));
                group.addGroup(entry.getName(), subGroup);

            } else if (entry.getName().endsWith(".csv")) {
                ResultTable table = ResultList.loadFromCSVStream(tar);
                group.addTable(entry.getName().replace(".csv", ""), table);
            } else if (entry.getName().endsWith(".jdf")) {
                ResultTable table = ResultList.loadFromBinaryStream(tar);
                group.addTable(entry.getName().replace(".jdf", ""), table);
            }

        }

    }

    /**
     * Returns a flat list of all tables stored in this group (i.e., including those in subgroups).
     *
     * @return Flat list of all tables
     */
    public Map<String, ResultTable> getAllTables() {
        Map<String, ResultTable> list = new LinkedHashMap<>();
        addFlatListings(list, "", this);
        return list;
    }

    private void addFlatListings(Map<String, ResultTable> list, String prefix, ResultGroup group) {

        group.getTables().forEach((k, v) -> list.put(prefix + k, v));
        group.getGroups().forEach((k, v) -> addFlatListings(list, prefix + k + "/", v));

    }

    public ResultGroup() {
    }

    public void addTable(String name, ResultTable table) {

        String[] parts = name.split("/");
        String   file  = parts[parts.length - 1];

        ResultGroup group = this;

        for (int i = 0; i < parts.length - 1; i++) {

            if (!group.containsGroup(parts[i])) {
                group.addGroup(parts[i], new ResultGroup());
            }

            group = group.getGroup(parts[i]);

        }

        group.tables.put(file, table);
    }

    public void addGroup(String name, ResultGroup group) {

        String[] parts = name.split("/");
        String   file  = parts[parts.length - 1];

        ResultGroup grp = this;

        for (int i = 0; i < parts.length - 1; i++) {

            if (!grp.containsGroup(parts[i])) {
                grp.addGroup(parts[i], new ResultGroup());
            }

            grp = grp.getGroup(parts[i]);

        }

        grp.groups.put(file, group);

    }

    public void removeTable(String name) {

        String[] parts = name.split("/");
        String   file  = parts[parts.length - 1];

        ResultGroup grp = this;

        for (int i = 0; i < parts.length - 1; i++) {

            if (!grp.containsGroup(parts[i])) {
                grp.addGroup(parts[i], new ResultGroup());
            }

            grp = grp.getGroup(parts[i]);

        }

        grp.tables.remove(file);
    }

    public void removeGroup(String name) {

        String[] parts = name.split("/");
        String   file  = parts[parts.length - 1];

        ResultGroup grp = this;

        for (int i = 0; i < parts.length - 1; i++) {

            if (!grp.containsGroup(parts[i])) {
                grp.addGroup(parts[i], new ResultGroup());
            }

            grp = grp.getGroup(parts[i]);

        }

        grp.groups.remove(file);

    }

    public void removeGroup(ResultGroup group) {
        groups.entrySet().removeIf(entry -> entry.getValue() == group);
    }

    public void removeTable(ResultTable table) {
        tables.entrySet().removeIf(entry -> entry.getValue() == table);
    }

    public boolean containsTable(String name) {

        String[] parts = name.split("/");
        String   file  = parts[parts.length - 1];

        ResultGroup grp = this;

        for (int i = 0; i < parts.length - 1; i++) {

            if (!grp.containsGroup(parts[i])) {
                grp.addGroup(parts[i], new ResultGroup());
            }

            grp = grp.getGroup(parts[i]);

        }

        return grp.tables.containsKey(file);
    }

    public boolean containsGroup(String name) {

        String[] parts = name.split("/");
        String   file  = parts[parts.length - 1];

        ResultGroup grp = this;

        for (int i = 0; i < parts.length - 1; i++) {

            if (!grp.containsGroup(parts[i])) {
                grp.addGroup(parts[i], new ResultGroup());
            }

            grp = grp.getGroup(parts[i]);

        }

        return grp.groups.containsKey(file);
    }

    public boolean containsTable(ResultTable table) {
        return tables.containsValue(table);
    }

    public boolean containsGroup(ResultGroup group) {
        return groups.containsValue(group);
    }

    public boolean contains(ResultTable table) {
        return containsTable(table);
    }

    public boolean contains(ResultGroup group) {
        return containsGroup(group);
    }

    public ResultTable getTable(String name) {

        String[] parts = name.split("/");
        String   file  = parts[parts.length - 1];

        ResultGroup group = this;

        for (int i = 0; i < parts.length - 1; i++) {
            group = group.getGroup(parts[i]);
        }

        if (group.containsTable(file)) {
            return group.tables.get(file);
        } else {
            throw new NotInGroupException("No such table in group: " + name);
        }

    }

    public ResultGroup getGroup(String name) {

        String[] parts = name.split("/");
        String   file  = parts[parts.length - 1];

        ResultGroup group = this;

        for (int i = 0; i < parts.length - 1; i++) {
            group = group.getGroup(parts[i]);
        }

        if (group.containsGroup(file)) {
            return group.groups.get(file);
        } else {
            throw new NotInGroupException("No such group in group: " + name);
        }

    }

    public ResultTable get(String name) {
        return getTable(name);
    }

    public Map<String, ResultTable> getTables() {
        return Map.copyOf(tables);
    }

    public List<String> getTableNames() {
        return List.copyOf(tables.keySet());
    }

    public Map<String, ResultGroup> getGroups() {
        return Map.copyOf(groups);
    }

    public List<String> getGroupNames() {
        return List.copyOf(groups.keySet());
    }

    public void outputCSV(String path) throws IOException {

        FileOutputStream           fos  = new FileOutputStream(path);
        BufferedOutputStream       bos  = new BufferedOutputStream(fos);
        GzipCompressorOutputStream gzip = new GzipCompressorOutputStream(bos);
        TarArchiveOutputStream     out  = new TarArchiveOutputStream(gzip);

        outputCSV("", out);

        out.close();

    }

    public void outputBinary(String path) throws IOException {

        FileOutputStream           fos  = new FileOutputStream(path);
        BufferedOutputStream       bos  = new BufferedOutputStream(fos);
        GzipCompressorOutputStream gzip = new GzipCompressorOutputStream(bos);
        TarArchiveOutputStream     out  = new TarArchiveOutputStream(gzip);

        outputBinary("", out);

        out.close();

    }

    protected void outputCSV(String prefix, TarArchiveOutputStream out) throws IOException {

        for (Map.Entry<String, ResultTable> entry : tables.entrySet()) {

            String                name    = entry.getKey();
            ResultTable           table   = entry.getValue();
            ByteArrayOutputStream buffer  = new ByteArrayOutputStream(table.size() * 1024);
            PrintStream           printer = new PrintStream(buffer);

            table.outputCSV(printer);

            TarArchiveEntry tar = new TarArchiveEntry(Util.joinPath(prefix, name + ".csv"), true);
            tar.setSize(buffer.size());

            out.putArchiveEntry(tar);
            out.write(buffer.toByteArray());
            out.closeArchiveEntry();

            buffer.close();

        }

        for (Map.Entry<String, ResultGroup> entry : groups.entrySet()) {

            String      name  = entry.getKey();
            ResultGroup group = entry.getValue();

            group.outputCSV(Util.joinPath(prefix, name), out);

        }

    }

    protected void outputBinary(String prefix, TarArchiveOutputStream out) throws IOException {

        for (Map.Entry<String, ResultTable> entry : tables.entrySet()) {

            String                name   = entry.getKey();
            ResultTable           table  = entry.getValue();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(table.size() * 1024);

            table.outputBinary(buffer);

            TarArchiveEntry tar = new TarArchiveEntry(Util.joinPath(prefix, name + ".jdf"), true);
            tar.setSize(buffer.size());
            out.putArchiveEntry(tar);
            out.write(buffer.toByteArray());
            out.closeArchiveEntry();

            buffer.close();

        }

        for (Map.Entry<String, ResultGroup> entry : groups.entrySet()) {

            String      name  = entry.getKey();
            ResultGroup group = entry.getValue();

            group.outputBinary(Util.joinPath(prefix, name), out);

        }

    }

}
