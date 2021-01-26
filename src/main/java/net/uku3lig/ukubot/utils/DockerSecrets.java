package net.uku3lig.ukubot.utils;

import net.uku3lig.ukubot.core.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DockerSecrets {
    private static final Path basePath = Path.of("/run/secrets/");
    private static final String[] secretsNames =
            {"token", "db_pwd", "client_secret", "reddit_password", "reddit_client_secret"};
    private static Map<String, String> secrets = null;
    private static final Logger logger = LoggerFactory.getLogger(DockerSecrets.class);

    private DockerSecrets() {

    }

    public static Optional<String> getSecret(String name) {
        if (secrets == null) secrets = init();
        return Optional.ofNullable(secrets.get(name));
    }

    public static String getSecretOrFile(String name, Path orElse) {
        return getSecret(name).orElseGet(() -> {
            try {
                return Files.readString(orElse);
            } catch (IOException e) {
                logger.error("Error: are you sure " + name + " is in " + orElse.toString());
                Runtime.getRuntime().exit(5);
                return "";
            }
        });
    }

    private static Map<String, String> init() {
        if (!Main.isDocker()) return Collections.emptyMap();
        final Map<String, String> secrets = new HashMap<>();
        for (String secret : secretsNames) {
            if (!basePath.resolve(secret).toFile().exists()) continue;
            try {
                secrets.put(secret, Files.readString(basePath.resolve(secret)));
            } catch (Exception ignored) {
            }
        }
        return Collections.unmodifiableMap(secrets);
    }
}
