package xyz.dicedpixels;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileParser {

    private static final Logger logger = LoggerFactory.getLogger(FileParser.class);

    public record Entry(String category, String className, String name, String type, String javadoc) {}

    public record ParseSummary(int filesProcessed, int filesFailed, int entriesFound, List<String> failures) {}

    public record ParseResult(List<Entry> entries, ParseSummary summary) {}

    private final List<Parser> parsers = new ArrayList<>();

    public FileParser() {}

    public void addParser(Parser parser) {
        parsers.add(parser);
    }

    public ParseResult parse(Map<String, List<Path>> files) {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

        var results = new ArrayList<Entry>();
        var failures = new ArrayList<String>();
        var filesProcessed = 0;
        var filesFailed = 0;

        for (var fileList : files.values()) {
            for (var file : fileList) {
                logger.info("Processing: {}", file);

                try {
                    var compilationUnit = StaticJavaParser.parse(file);

                    compilationUnit.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
                        var className = clazz.getFullyQualifiedName().orElse(clazz.getNameAsString());

                        for (var parser : parsers) {
                            if (parser.condition(clazz)) {
                                results.addAll(parser.parse(clazz, className));
                            }
                        }
                    });

                    filesProcessed++;

                } catch (ParseProblemException e) {
                    filesFailed++;
                    failures.add("Parse error in " + file + ": " + e.getMessage());
                    logger.warn("Skipping {} — parse error: {}", file, e.getMessage());
                } catch (IOException e) {
                    filesFailed++;
                    failures.add("IO error reading " + file + ": " + e.getMessage());
                    logger.warn("Skipping {} — IO error: {}", file, e.getMessage());
                }
            }
        }

        return new ParseResult(results, new ParseSummary(filesProcessed, filesFailed, results.size(), failures));
    }

    public static class EventsParser implements Parser {
        private boolean isEventField(FieldDeclaration field) {
            if (!field.getElementType().isClassOrInterfaceType()) {
                return false;
            }

            return field.getElementType().asClassOrInterfaceType().getNameAsString().equals("Event");
        }

        @Override
        public boolean condition(ClassOrInterfaceDeclaration clazz) {
            if (clazz.isInterface() && clazz.getFields().stream().anyMatch(field ->
                    isEventField(field) && field.getVariable(0).getNameAsString().equals("EVENT"))) {
                return false;
            }

            return clazz.getFields().stream().anyMatch(this::isEventField);
        }

        @Override
        public List<Entry> parse(ClassOrInterfaceDeclaration clazz, String className) {
            var results = new ArrayList<Entry>();

            clazz.getFields().forEach(field -> {
                if (!isEventField(field)) {
                    return;
                }

                var name = field.getVariable(0).getNameAsString();
                var type = field.getElementType().asString();
                var javadoc = field.getJavadoc().map(Javadoc::toText).orElse("");

                results.add(new Entry("events", className, name, type, javadoc));
            });

            return results;
        }
    }

    public static class RegistriesParser implements Parser {
        private String buildSignature(MethodDeclaration method) {
            var params = method.getParameters().stream()
                    .map(parameter -> parameter.getType().asString() + " " + parameter.getNameAsString())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

            return method.getType().asString() + " " + method.getNameAsString() + "(" + params + ")";
        }

        @Override
        public boolean condition(ClassOrInterfaceDeclaration clazz) {
            return clazz.getMethods().stream().anyMatch(MethodDeclaration::isPublic);
        }

        @Override
        public List<Entry> parse(ClassOrInterfaceDeclaration clazz, String className) {
            var results = new ArrayList<Entry>();

            clazz.getMethods().forEach(method -> {
                if (!method.isPublic()) {
                    return;
                }

                var name = buildSignature(method);
                var javadoc = method.getJavadoc().map(Javadoc::toText).orElse("");

                results.add(new Entry("registries", className, name, "", javadoc));
            });

            return results;
        }
    }

    public static class CallbacksParser implements Parser {
        private boolean isEventField(FieldDeclaration field) {
            if (!field.getElementType().isClassOrInterfaceType()) {
                return false;
            }

            return field.getElementType().asClassOrInterfaceType().getNameAsString().equals("Event");
        }

        @Override
        public boolean condition(ClassOrInterfaceDeclaration clazz) {
            if (!clazz.isInterface()) {
                return false;
            }

            return clazz.getFields().stream().anyMatch(field ->
                    isEventField(field) && field.getVariable(0).getNameAsString().equals("EVENT")
            );
        }

        @Override
        public List<Entry> parse(ClassOrInterfaceDeclaration clazz, String className) {
            var javadoc = clazz.getJavadoc().map(Javadoc::toText).orElse("");

            return List.of(new Entry("callbacks", className, "", "", javadoc));
        }
    }
}
