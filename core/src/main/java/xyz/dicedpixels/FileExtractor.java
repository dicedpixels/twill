package xyz.dicedpixels;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileExtractor {

    private static final Logger logger = LoggerFactory.getLogger(FileExtractor.class);

    private final List<Pattern> exclusions = new ArrayList<>();
    private final Map<String, Pattern> patterns = new LinkedHashMap<>();

    public FileExtractor() {}

    public void addExclusion(String pattern) {
        exclusions.add(Pattern.compile(pattern));
    }

    public void addPattern(String key, String pattern) {
        patterns.put(key, Pattern.compile(pattern));
    }

    public Map<String, List<Path>> extract(Path root) throws IOException {
        var results = new HashMap<String, List<Path>>();

        for (var key : patterns.keySet()) {
            results.put(key, new ArrayList<>());
        }

        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                var filePath = file.toString();

                for (var exclusion : exclusions) {
                    if (exclusion.matcher(filePath).matches()) {
                        return FileVisitResult.CONTINUE;
                    }
                }

                for (var entry : patterns.entrySet()) {
                    if (entry.getValue().matcher(filePath).matches()) {
                        results.get(entry.getKey()).add(file);
                    }
                }

                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                logger.warn("Skipping {} — could not read file: {}", file, e.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });

        return results;
    }
}
