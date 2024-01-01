package com.froobworld.nabconfiguration.patcher;

import com.froobworld.nabconfiguration.patcher.jobs.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ConfigPatchLoader {
    private static final Pattern PATCH_HEADER_PATTERN = Pattern.compile("\\[[a-z-]+\\]");

    private static final Map<String, Function<Properties, PatchJob>> PATCH_JOBS = new HashMap<>();
    static {
        PATCH_JOBS.put("add-field", AddFieldJob::new);
        PATCH_JOBS.put("add-section", AddSectionJob::new);
        PATCH_JOBS.put("comment", CommentJob::new);
        PATCH_JOBS.put("comment-out", CommentOutJob::new);
        PATCH_JOBS.put("header", HeaderJob::new);
        PATCH_JOBS.put("move", MoveJob::new);
    }

    public static ConfigPatch parse(InputStream inputStream) throws IOException {
        List<PatchJob> patchJobs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            Function<Properties, PatchJob> currentJobFunction = null;
            StringBuilder currentProperties = new StringBuilder();

            for (Iterator<String> it = reader.lines().iterator(); it.hasNext(); ) {
                String line = it.next();
                if (PATCH_HEADER_PATTERN.matcher(line).matches()) {
                    if (currentJobFunction != null) {
                        Properties properties = new Properties();
                        properties.load(new StringReader(currentProperties.toString()));
                        patchJobs.add(currentJobFunction.apply(properties));
                    }

                    currentJobFunction = PATCH_JOBS.get(getPatchJobType(line));
                    currentProperties = new StringBuilder();
                } else {
                    currentProperties.append(line).append("\n");
                }
            }
            if (currentJobFunction != null) {
                Properties properties = new Properties();
                properties.load(new StringReader(currentProperties.toString()));
                patchJobs.add(currentJobFunction.apply(properties));
            }
        }
        return new ConfigPatch(patchJobs);
    }

    private static String getPatchJobType(String line) {
        return line.replace("[", "").replace("]", "");
    }

}
