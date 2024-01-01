package com.froobworld.nabconfiguration.utils;

import com.froobworld.nabconfiguration.patcher.structure.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class YamlUtils {

    private YamlUtils() {}

    public static boolean isComment(String line) {
        return line.trim().startsWith("#");
    }

    public static List<String> elementsToBody(List<YamlElement> elements, int indentLevel) {
        List<String> lines = new ArrayList<>();
        for (Iterator<YamlElement> iterator = elements.iterator(); iterator.hasNext();) {
            YamlElement element = iterator.next();
            lines.addAll(element.toLines());
            if (iterator.hasNext()) {
                lines.add("");
            }
        }

        return lines.stream()
                .map(string -> (string.isEmpty() ? "" : String.join("", Collections.nCopies(indentLevel, " "))) + string)
                .collect(Collectors.toList());
    }

    public static YamlGibberish commentOut(YamlElement element) {
        List<String> lines = element.toLines();
        return new YamlGibberish(
                lines.stream()
                .map(line -> isComment(line) ? line : ("#" + line))
                .collect(Collectors.toList())
        );
    }

    public static List<WildcardString> getMatchingKeys(String keyPattern, YamlFile yamlFile) {
        List<WildcardString> keys = new ArrayList<>();
        for (String key : getAllKeys(yamlFile.getElements())) {
            String[] keySplit = key.split(Pattern.quote("."));
            String[] keyPatternSplit = keyPattern.split(Pattern.quote("."));
            if (keySplit.length == keyPatternSplit.length) {
                List<String> wildcardMatches = new ArrayList<>();
                for (int i = 0; i < keySplit.length; i++) {
                    if (keyPatternSplit[i].equalsIgnoreCase("*")) {
                        wildcardMatches.add(keySplit[i]);
                    } else {
                        if (!keySplit[i].equals(keyPatternSplit[i])) {
                            break;
                        }
                    }
                    if (i == keySplit.length - 1) {
                        keys.add(new WildcardString(key, wildcardMatches));
                    }
                }
            }
        }
        return keys;
    }

    private static List<String> getAllKeys(List<YamlElement> elements) {
        List<String> keys = new ArrayList<>();
        for (YamlElement element : elements) {
            if (element instanceof YamlSection) {
                keys.add(((YamlSection) element).getKey());
                keys.addAll(getAllKeys(((YamlSection) element).elements()).stream()
                        .map(key -> ((YamlSection) element).getKey() + "." + key)
                        .collect(Collectors.toSet()));
            }
            if (element instanceof YamlField) {
                keys.add(((YamlField) element).getKey());
            }
        }
        return keys;
    }

    public static class WildcardString {
        public final String string;
        public final List<String> wildcardMatches;

        public WildcardString(String string, List<String> wildcardMatches) {
            this.string = string;
            this.wildcardMatches = wildcardMatches;
        }
    }

}
