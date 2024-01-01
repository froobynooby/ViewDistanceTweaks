package com.froobworld.nabconfiguration;

import com.froobworld.nabconfiguration.annotations.Entry;
import com.froobworld.nabconfiguration.annotations.EntryMap;
import com.froobworld.nabconfiguration.annotations.Section;
import com.froobworld.nabconfiguration.annotations.SectionMap;
import com.froobworld.nabconfiguration.utils.InstantFallbackConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.lang.reflect.Field;

public class ConfigSectionPopulator {
    private InstantFallbackConfigurationSection configurationSection;
    private ConfigSection workConfigSection;

    public ConfigSectionPopulator(File configFile, ConfigSection configSection) {
        this(new InstantFallbackConfigurationSection(YamlConfiguration.loadConfiguration(configFile)), configSection);
    }

    private ConfigSectionPopulator(InstantFallbackConfigurationSection configurationSection, ConfigSection workingConfigSection) {
        this.configurationSection = configurationSection;
        this.workConfigSection = workingConfigSection;
    }

    public void populate() throws Exception {

        for (Field field : workConfigSection.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            {
                Section sectionAnnotation = field.getAnnotation(Section.class);
                if (sectionAnnotation != null) {
                    ConfigSection subConfigSection = (ConfigSection) field.get(workConfigSection);
                    new ConfigSectionPopulator(configurationSection.getSection(sectionAnnotation.key(), null), subConfigSection).populate();
                }
            }

            {
                SectionMap sectionMapAnnotation = field.getAnnotation(SectionMap.class);
                if (sectionMapAnnotation != null) {
                    ConfigSectionMap configSectionMap = (ConfigSectionMap) field.get(workConfigSection);
                    configSectionMap.clear();
                    InstantFallbackConfigurationSection mapSection = configurationSection.getSection(sectionMapAnnotation.key(), null);

                    ConfigSection defaultEntry = (ConfigSection) configSectionMap.entryType().getConstructor().newInstance();
                    new ConfigSectionPopulator(mapSection.getSection(sectionMapAnnotation.defaultKey(), null), defaultEntry).populate();
                    configSectionMap.setDefaultSection(defaultEntry);

                    for (String key : mapSection.getKeys(false)) {
                        ConfigSection newEntry = (ConfigSection) configSectionMap.entryType().getConstructor().newInstance();
                        new ConfigSectionPopulator(mapSection.getSection(key, sectionMapAnnotation.defaultKey()), newEntry).populate();
                        configSectionMap.put(key, newEntry);
                    }
                }
            }
            {
                Entry entryAnnotation = field.getAnnotation(Entry.class);
                if (entryAnnotation != null) {
                    ConfigEntry configEntry = (ConfigEntry) field.get(workConfigSection);
                    configEntry.setValue(configurationSection.get(entryAnnotation.key()));
                }
            }
            {
                EntryMap entryMapAnnotation = field.getAnnotation(EntryMap.class);
                if (entryMapAnnotation != null) {
                    ConfigEntryMap configEntryMap = (ConfigEntryMap) field.get(workConfigSection);
                    configEntryMap.clear();
                    InstantFallbackConfigurationSection mapSection = configurationSection.getSection(entryMapAnnotation.key(), null);

                    configEntryMap.setDefault(mapSection.get(entryMapAnnotation.defaultKey()));
                    for (String key : mapSection.getKeys(false)) {
                        configEntryMap.put(key, mapSection.get(key));
                    }
                }
            }
        }
    }

}
