package com.froobworld.nabconfiguration.patcher.jobs;

import com.froobworld.nabconfiguration.patcher.structure.YamlElement;
import com.froobworld.nabconfiguration.patcher.structure.YamlField;
import com.froobworld.nabconfiguration.patcher.structure.YamlFile;
import com.froobworld.nabconfiguration.patcher.structure.YamlSection;
import com.froobworld.nabconfiguration.utils.YamlUtils;

import java.util.*;
import java.util.regex.Pattern;

public class CommentOutJob implements PatchJob {
    private String key;

    public CommentOutJob(Properties properties) {
        this.key = properties.getProperty("key");
    }


    @Override
    public void modify(YamlFile yamlFile) {
        String[] splitKey = key.split(Pattern.quote("."));
        List<YamlElement> elementList = yamlFile.getElements();
        for (int i = 0; i < splitKey.length; i++) {
            String currentKey = splitKey[i];
            Optional<YamlElement> nextElement = elementList.stream()
                    .filter(element -> {
                        if (element instanceof YamlField) {
                            return ((YamlField) element).getKey().equals(currentKey);
                        } else if (element instanceof YamlSection) {
                            return ((YamlSection) element).getKey().equals(currentKey);
                        }
                        return false;
                    })
                    .findFirst();
            if (i == splitKey.length - 1) {
                if (nextElement.isPresent()) {
                    for (ListIterator<YamlElement> iterator = elementList.listIterator(); iterator.hasNext();) {
                        YamlElement next = iterator.next();
                        if (next instanceof YamlField) {
                            if (((YamlField) next).getKey().equals(currentKey)) {
                                iterator.set(YamlUtils.commentOut(next));
                            }
                        } else if (next instanceof YamlSection) {
                            if (((YamlSection) next).getKey().equals(currentKey)) {
                                iterator.set(YamlUtils.commentOut(next));
                            }
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Tried to add comment to element with key " + key + ", but element does not exist.");
                }
            } else {
                if (nextElement.isPresent() && nextElement.get() instanceof YamlSection) {
                    elementList = ((YamlSection) nextElement.get()).elements();
                } else {
                    throw new IllegalArgumentException("Tried to add comment to element with key " + key + ", but a section named " + currentKey + " does not exist.");
                }
            }
        }
    }
}
