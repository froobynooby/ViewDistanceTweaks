package com.froobworld.nabconfiguration.patcher.jobs;

import com.froobworld.nabconfiguration.patcher.structure.YamlFile;

public interface PatchJob {

    void modify(YamlFile yamlFile);

}
