package com.froobworld.nabconfiguration.patcher.structure;

import java.util.ArrayList;
import java.util.List;

public class YamlField implements YamlElement {
    protected List<String> comment;
    protected String key;
    protected String value;
    protected List<String> body;

    public YamlField(String key, List<String> comment, String value, List<String> body) {
        this.comment = comment;
        this.key = key;
        this.value = value;
        this.body = body;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setComment(List<String> comment) {
        this.comment = comment;
    }

    @Override
    public List<String> toLines() {
        List<String> lines = new ArrayList<>(comment);
        lines.add(key + ":" + value);
        lines.addAll(body);
        return lines;
    }

}
