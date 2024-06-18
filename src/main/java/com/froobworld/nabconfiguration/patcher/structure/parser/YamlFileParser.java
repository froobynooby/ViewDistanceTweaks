package com.froobworld.nabconfiguration.patcher.structure.parser;

import com.froobworld.nabconfiguration.patcher.structure.*;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: Make this less terrible
public class YamlFileParser {
    private static final Yaml YAML = new Yaml();
    private static final String COMMENT_REGEX = "(#[^\r\n|\r|\n]{0,200}(\r\n|\r|\n))";
    private static final String KEY_REGEX = "((?<=\\r\\n|\\r|\\n)[a-zA-Z0-9-_][a-zA-Z0-9-_ ]*:)";
    private static final Pattern YAML_ENTRY_SEPARATOR_PATTERN = Pattern.compile(
            "(?=" +                                     // Want to match empty space before the following pattern
            "(?<!" + COMMENT_REGEX + ")" +              // Don't want any comments before the empty space
            COMMENT_REGEX + "*" +                       // Want to capture all comments after the empty space
            KEY_REGEX                                   // Require a key at the end
            + ")"
    );
    private static final String KEY_LINE_REGEX = "[a-zA-Z0-9-_][a-zA-Z0-9-_ ]*:.*";
    private static final String COMMENT_LINE_REGEX = "#.*";

    public static YamlFile parse(File file) throws IOException {
        return new YamlFile(parse(
                new BufferedReader(new FileReader(file)).lines()
                        .collect(Collectors.joining("\n"))
        ));
    }

    private static List<YamlElement> parse(String input) throws IOException {
        Map<String, Object> yamlMap = YAML.load(input);
        if (yamlMap == null) {
            return new ArrayList<>();
        }
        Set<String> sectionKeys = yamlMap.entrySet().stream()
                .filter(entry -> entry.getValue() == null || entry.getValue() instanceof Map)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        List<YamlElement> elementList = new ArrayList<>();
        for (String entry : YAML_ENTRY_SEPARATOR_PATTERN.split(input)) {
            ElementGibberishPairBuilder builder = new ElementGibberishPairBuilder();
            for (String line : entry.split("\n", -1)) {
                if (line.trim().isEmpty()) {
                    builder.empty();
                } else if (line.matches(KEY_LINE_REGEX)) {
                    String[] keyValueArray = line.split(":", 2);
                    builder.key(keyValueArray[0], keyValueArray[1], sectionKeys.contains(keyValueArray[0]));
                } else if (line.matches(" *" + COMMENT_LINE_REGEX)) {
                    builder.comment(line);
                } else {
                    builder.other(line);
                }
            }
            ElementGibberishPair pair = builder.build();
            if (pair.element != null) {
                elementList.add(pair.element);
            }
            if (pair.gibberish != null) {
                elementList.add(pair.gibberish);
            }
        }

        return elementList;
    }

    private static class ElementGibberishPairBuilder {
        private boolean section;           // Is it a section?
        private Integer sectionIndentation; // How much is the section indented by?
        private List<String> comment;      // The comment appearing above the key
        private String key;                // The key
        private String value;              // The stuff appearing after the : of the key
        private List<String> body;         // The stuff appearing beneath the key, excluding comments
        private List<String> tail;         // Everything following the body, gibberish

        public ElementGibberishPairBuilder() {
            this.comment = new ArrayList<>();
            this.body = new ArrayList<>();
            this.tail = new ArrayList<>();
        }

        public ElementGibberishPairBuilder key(String key, String value, boolean section) {
            this.key = key;
            this.value = value;
            this.section = section;
            return this;
        }

        public ElementGibberishPairBuilder comment(String line) {
            if (key == null) {
                comment.add(line);
            } else {
                tail.add(line);
            }
            return this;
        }

        public ElementGibberishPairBuilder other(String line) {
            if (key == null) {
                throw new IllegalArgumentException("Can't accept non-comment value before key has been supplied.");
            } else {
                body.addAll(tail);
                body.add(line);
                tail.clear();
                if (section && sectionIndentation == null) {
                    sectionIndentation = 0;
                    for (char c : line.toCharArray()) {
                        if (c == ' ') {
                            sectionIndentation++;
                        } else {
                            break;
                        }
                    }
                }
            }
            return this;
        }

        public ElementGibberishPairBuilder empty() {
            if (key == null) {
                comment.add("");
            } else {
                tail.add("");
            }
            return this;
        }

        public ElementGibberishPair build() throws IOException {
            // Remove leading empty strings
            for (ListIterator<String> iterator = tail.listIterator(); iterator.hasNext();) {
                if (iterator.next().isEmpty()) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            for (ListIterator<String> iterator = comment.listIterator(); iterator.hasNext();) {
                if (iterator.next().isEmpty()) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            // Remove tailing empty strings
            for (ListIterator<String> iterator = tail.listIterator(tail.size()); iterator.hasPrevious();) {
                if (iterator.previous().isEmpty()) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            for (ListIterator<String> iterator = comment.listIterator(comment.size()); iterator.hasPrevious();) {
                if (iterator.previous().isEmpty()) {
                    iterator.remove();
                } else {
                    break;
                }
            }

            YamlElement element = null;
            YamlGibberish gibberish = key == null ?
                    (comment.isEmpty() ? null : new YamlGibberish(comment)) :
                    (tail.isEmpty() ? null : new YamlGibberish(tail));
            if (key != null) {
                if (section) {
                    String sectionInput = body.stream()
                            .map(line -> line.replaceFirst(" {0," + sectionIndentation + "}", ""))
                            .collect(Collectors.joining("\n"));

                    List<YamlElement> sectionElements = parse(sectionInput);
                    if (sectionElements.isEmpty()) {
                        body.addAll(tail);
                        gibberish = null;
                        element = new YamlField(key, comment, value, body);
                    } else {
                        element = new YamlSection(key, comment, value, parse(sectionInput));
                    }
                } else {
                    body.addAll(tail);
                    gibberish = null;
                    element = new YamlField(key, comment, value, body);
                }
            }

            return new ElementGibberishPair(element, gibberish);
        }

    }

    private static class ElementGibberishPair {
        public final YamlElement element;
        public final YamlGibberish gibberish;

        private ElementGibberishPair(YamlElement element, YamlGibberish gibberish) {
            this.element = element;
            this.gibberish = gibberish;
        }

    }

}
