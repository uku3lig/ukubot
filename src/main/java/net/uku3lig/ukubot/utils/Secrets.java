package net.uku3lig.ukubot.utils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Secrets {
    @Getter
    private static final Path basePath = Path.of("secrets/").toAbsolutePath();
    private static final Set<File> files = findFiles();
    private static final Logger logger = LoggerFactory.getLogger(Secrets.class);

    @Nullable @CheckReturnValue
    public static String getSecretUnsafe(@NotNull String fileName) throws IOException{
        File secret = files.stream().filter(f -> f.getName().equalsIgnoreCase(fileName)).findFirst().orElse(null);
        if (secret == null) return null;
        String content = Files.readString(secret.toPath()).strip();
        if (content.isEmpty() || content.isBlank()) return null;
        return content;
    }

    public static Optional<String> getSecret(@NotNull String fileName) {
        try {
            return Optional.ofNullable(getSecretUnsafe(fileName));
        } catch (IOException e) {
            logger.error("Could not read secret " + fileName);
            Runtime.getRuntime().exit(18);
            return Optional.empty();
        }
    }

    public static String findSecret(@NotNull String fileName) {
        try {
            return Objects.requireNonNull(getSecretUnsafe(fileName));
        } catch (IOException e) {
            logger.error("Could not read secret " + fileName);
            Runtime.getRuntime().exit(18);
            return "";
        } catch (NullPointerException e) {
            logger.error("IMPORTANT cannot find secret " + fileName);
            Runtime.getRuntime().exit(19);
            return "";
        }
    }

    private static Set<File> findFiles() {
        File base = basePath.toFile();
        if (!base.isDirectory()) return Collections.emptySet();
        return Arrays.stream(base.listFiles()).collect(Collectors.toSet());
    }
}
