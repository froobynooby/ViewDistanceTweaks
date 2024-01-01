package com.froobworld.nabconfiguration;

import com.froobworld.nabconfiguration.patcher.ConfigPatchLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NabConfiguration extends ConfigSection {
    private final Yaml yaml = new Yaml();
    private final File configFile;
    private final Supplier<InputStream> copyStreamSupplier;
    private final Function<Integer, InputStream> patchStreamSupplier;
    private final int currentVersion;

    public NabConfiguration(File configFile, Supplier<InputStream> copyStreamSupplier, Function<Integer, InputStream> patchStreamSupplier, int currentVersion) {
        this.configFile = configFile;
        this.copyStreamSupplier = copyStreamSupplier;
        this.patchStreamSupplier = patchStreamSupplier;
        this.currentVersion = currentVersion;
    }


    public void load() throws Exception {
        init();
        new ConfigSectionPopulator(configFile, this).populate();
    }

    private void init() throws IOException {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            Files.copy(copyStreamSupplier.get(), configFile.toPath());
        }
        for (;;) {
            int version = -1;
            try (FileReader reader = new FileReader(configFile)) {
                version = (int) ((Map) yaml.load(reader)).get("version");
            } catch (Exception ex) {
                throw new IllegalStateException("Could not find version while initialising " + configFile.getName() + ".", ex);
            }
            if (version < currentVersion) {
                try (InputStream inputStream = patchStreamSupplier.apply(version)) {
                    ConfigPatchLoader.parse(inputStream).patchFile(configFile);
                    updateVersion(version + 1);
                }
            } else {
                break;
            }
        }
    }

    private void updateVersion(int newVersion) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            List<String> lines = reader.lines()
                    .map(line -> {
                        return line.startsWith("version: ") ? "version: " + newVersion : line;
                    })
                    .collect(Collectors.toList());
            try (PrintWriter writer = new PrintWriter(new FileWriter(configFile))) {
                for (String line : lines) {
                    writer.println(line);
                }
            }
        }
    }

}
