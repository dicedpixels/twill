package xyz.dicedpixels;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Twill {
    private static final Logger logger = LoggerFactory.getLogger(Twill.class);

    static void main(String[] args) {
        if (args.length < 1) {
            logger.error("Usage: Twill <version-tag> [cache-dir] [metadata-dir]");
            System.exit(1);
        }

        var tag = args[0];
        var cacheDir = Path.of(args.length > 1 ? args[1] : ".temp");
        var metadataDir = Path.of(args.length > 2 ? args[2] : "metadata");

        try {
            new Repository(cacheDir).ensure(tag);

            var extractor = new FileExtractor();

            extractor.addExclusion(".*[/\\\\]impl[/\\\\].*");
            extractor.addPattern("events", ".*Events\\.java$");
            extractor.addPattern("registries", ".*Registry\\.java$");
            extractor.addPattern("renderLayers", ".*BlockRenderLayerMap\\.java$");
            extractor.addPattern("callbacks", ".*Callback\\.java$");
            logger.info("Scanning: {}", cacheDir);

            var files = extractor.extract(cacheDir);

            for (var entry : files.entrySet()) {
                logger.info("Found {} file(s) in category '{}'", entry.getValue().size(), entry.getKey());
            }

            var fileParser = new FileParser();

            fileParser.addParser(new FileParser.EventsParser());
            fileParser.addParser(new FileParser.RegistriesParser());
            fileParser.addParser(new FileParser.CallbacksParser());

            var result = fileParser.parse(files);
            var summary = result.summary();

            logger.info("---");
            logger.info("Summary:");
            logger.info("  Files processed : {}", summary.filesProcessed());
            logger.info("  Files failed    : {}", summary.filesFailed());
            logger.info("  Entries found   : {}", summary.entriesFound());

            if (!summary.failures().isEmpty()) {
                logger.warn("Failures:");
                for (var failure : summary.failures()) {
                    logger.warn("  {}", failure);
                }
            }

            new Serializer(metadataDir).serialize(result, tag);

        } catch (IOException | GitAPIException e) {
            logger.error("Fatal: {}", e.getMessage());
            System.exit(1);
        }
    }
}
