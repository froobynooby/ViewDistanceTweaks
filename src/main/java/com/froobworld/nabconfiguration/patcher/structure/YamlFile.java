package com.froobworld.nabconfiguration.patcher.structure;

import com.froobworld.nabconfiguration.utils.YamlUtils;

import java.util.ArrayList;
import java.util.List;

public class YamlFile {
    private List<String> header;
    private List<YamlElement> elements;

    public YamlFile(List<YamlElement> elements) {
        this.elements = elements;
        if (!elements.isEmpty()) {
            YamlElement firstElement = elements.get(0);
            if (firstElement instanceof YamlGibberish) {
                header = firstElement.toLines();
                elements.remove(0);
            }
        }
        if (header == null) {
            header = new ArrayList<>();
        }
    }


    public void setHeader(List<String> header) {
        this.header = new ArrayList<>(header);
    }

    public List<String> toLines() {
        List<String> lines = new ArrayList<>();
        if (!header.isEmpty()) {
            lines.addAll(header);
            lines.add("");
        }
        lines.addAll(YamlUtils.elementsToBody(elements, 0));
        return lines;
    }

    public List<YamlElement> getElements() {
        return elements;
    }

}
