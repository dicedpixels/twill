package xyz.dicedpixels;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Repository {
    private static final Logger logger = LoggerFactory.getLogger(Repository.class);
    private static final String FABRIC_API_REPO = "https://github.com/FabricMC/fabric-api.git";

    private final Path directory;

    public Repository(Path directory) {
        this.directory = directory;
    }

    public void ensure(String tag) throws IOException, GitAPIException {
        var tagFile = directory.resolve(".tag");

        if (Files.exists(tagFile)) {
            var cachedTag = Files.readString(tagFile).strip();

            if (tag.equals(cachedTag)) {
                logger.info("Reusing cached repository at tag '{}'", tag);
                return;
            }

            logger.info("Cached repository is tag '{}', requested '{}' — re-cloning", cachedTag, tag);
            deleteDirectory(directory);
        }

        clone(tag);
        Files.writeString(tagFile, tag);
    }

    private void clone(String tag) throws GitAPIException {
        logger.info("Cloning '{}' at tag '{}'", FABRIC_API_REPO, tag);

        try (var _ = Git.cloneRepository()
                .setURI(FABRIC_API_REPO)
                .setDirectory(directory.toFile())
                .setBranch("refs/tags/" + tag)
                .setDepth(1)
                .call()) {
            logger.info("Clone complete");
        }
    }

    private void deleteDirectory(Path target) throws IOException {
        Files.walkFileTree(target, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                file.toFile().setWritable(true);
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException e) throws IOException {
                if (e != null) {
                    throw e;
                }
                Files.delete(directory);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
