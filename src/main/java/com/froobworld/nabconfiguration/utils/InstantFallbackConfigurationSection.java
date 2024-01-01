package com.froobworld.nabconfiguration.utils;

import com.google.common.collect.Sets;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.Set;

public class InstantFallbackConfigurationSection {
    private final ConfigurationSection baseSection;
    private InstantFallbackConfigurationSection fallbackSection;

    public InstantFallbackConfigurationSection(ConfigurationSection baseSection) {
        this(baseSection, null);
    }

    private InstantFallbackConfigurationSection(ConfigurationSection baseSection, InstantFallbackConfigurationSection fallbackSection) {
        this.baseSection = baseSection;
        this.fallbackSection = fallbackSection;
    }


    public Object get(String key) {
        return baseSection.get(key, fallbackSection == null ? null : fallbackSection.get(key));
    }

    public Set<String> getKeys(boolean deep) {
        return Sets.union(baseSection.getKeys(deep), fallbackSection == null ? Collections.emptySet() : fallbackSection.getKeys(deep));
    }

    public InstantFallbackConfigurationSection getSection(String key, String defaultKey) {
        ConfigurationSection newBaseSection = baseSection.getConfigurationSection(key);
        ConfigurationSection newFirstFallbackSection = defaultKey == null ? null : baseSection.getConfigurationSection(defaultKey);
        InstantFallbackConfigurationSection fallbackSectionTail = fallbackSection == null ? null : fallbackSection.getSection(key, defaultKey);
        if (newBaseSection == null && newFirstFallbackSection == null) {
            return fallbackSection.getSection(key, defaultKey);
        }
        if (newBaseSection == null) {
            return new InstantFallbackConfigurationSection(newFirstFallbackSection, fallbackSectionTail);
        }
        if (newFirstFallbackSection == null) {
            return new InstantFallbackConfigurationSection(newBaseSection, fallbackSectionTail);
        }
        return new InstantFallbackConfigurationSection(newBaseSection, new InstantFallbackConfigurationSection(newFirstFallbackSection, fallbackSectionTail));
    }

    public ConfigurationSection getBaseSection() {
        return baseSection;
    }

}
