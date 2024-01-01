package com.froobworld.nabconfiguration.patcher.structure;

import com.froobworld.nabconfiguration.utils.YamlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class YamlSection implements YamlElement {
    protected List<String> comment;
    protected String key;
    protected String value;
    protected List<YamlElement> elements;

    public YamlSection(String key, List<String> comment, String value, List<YamlElement> elements) {
        this.key = key;
        this.comment = comment;
        this.value = value;
        this.elements = elements;
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

    public List<YamlElement> elements() {
        return elements;
    }

    @Override
    public List<String> toLines() {
        List<String> lines = new ArrayList<>(comment);
        lines.add(key + ":" + value);
        lines.addAll(YamlUtils.elementsToBody(elements, 2));
        return lines;
    }

}
