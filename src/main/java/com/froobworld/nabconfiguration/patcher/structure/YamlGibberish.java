package com.froobworld.nabconfiguration.patcher.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YamlGibberish implements YamlElement {
    private List<String> lines;

    public YamlGibberish(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public List<String> toLines() {
        return new ArrayList<>(lines);
    }

}
