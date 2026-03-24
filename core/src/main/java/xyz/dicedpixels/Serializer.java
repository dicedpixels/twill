package xyz.dicedpixels;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import okio.Okio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Serializer {

    private static final Logger logger = LoggerFactory.getLogger(Serializer.class);

    private final Path metadataDirectory;

    public Serializer(Path metadataDirectory) {
        this.metadataDirectory = metadataDirectory;
    }

    public void serialize(FileParser.ParseResult result, String tag) throws IOException {
        Files.createDirectories(metadataDirectory);

        var outputFile = metadataDirectory.resolve(tag + ".json");

        try (var sink = Okio.buffer(Okio.sink(outputFile));
             var writer = JsonWriter.of(sink)) {
            writer.setIndent("  ");
            writeResult(writer, result, tag);
        }

        logger.info("Wrote {}", outputFile);

        updateVersionsIndex(tag);
    }

    private void updateVersionsIndex(String tag) throws IOException {
        var versionsFile = metadataDirectory.resolve("versions.json");

        var versions = new ArrayList<String>();

        if (Files.exists(versionsFile)) {
            try (var source = Okio.buffer(Okio.source(versionsFile));
                 var reader = JsonReader.of(source)) {
                reader.beginArray();
                while (reader.hasNext()) {
                    versions.add(reader.nextString());
                }
                reader.endArray();
            }
        }

        if (!versions.contains(tag)) {
            versions.add(tag);
            Collections.sort(versions, Collections.reverseOrder());

            try (var sink = Okio.buffer(Okio.sink(versionsFile));
                 var writer = JsonWriter.of(sink)) {
                writer.setIndent("  ");
                writer.beginArray();
                for (var version : versions) {
                    writer.value(version);
                }
                writer.endArray();
            }

            logger.info("Updated versions.json — {} version(s)", versions.size());
        }
    }

    private void writeResult(JsonWriter writer, FileParser.ParseResult result, String tag) throws IOException {
        var events = result.entries().stream()
                .filter(entry -> entry.category().equals("events"))
                .toList();

        var registries = result.entries().stream()
                .filter(entry -> entry.category().equals("registries"))
                .toList();

        var callbacks = result.entries().stream()
                .filter(entry -> entry.category().equals("callbacks"))
                .toList();

        writer.beginObject();
        writer.name("version").value(tag);
        writer.name("events");
        writeEntries(writer, events);
        writer.name("registries");
        writeEntries(writer, registries);
        writer.name("callbacks");
        writeEntries(writer, callbacks);
        writer.endObject();
    }

    private void writeEntries(JsonWriter writer, List<FileParser.Entry> entries) throws IOException {
        writer.beginArray();

        for (var entry : entries) {
            writer.beginObject();
            writer.name("className").value(entry.className());
            writer.name("name").value(entry.name());
            writer.name("type").value(entry.type());
            writer.name("javadoc").value(entry.javadoc());
            writer.endObject();
        }

        writer.endArray();
    }
}
