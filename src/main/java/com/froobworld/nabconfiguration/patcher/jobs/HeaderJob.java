package com.froobworld.nabconfiguration.patcher.jobs;

import com.froobworld.nabconfiguration.patcher.structure.YamlFile;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class HeaderJob implements PatchJob {
    private List<String> header;

    public HeaderJob(Properties properties) {
        this.header = Arrays.asList(properties.getProperty("header").split("\n"));
    }


    @Override
    public void modify(YamlFile yamlFile) {
        yamlFile.setHeader(header);
    }
}
