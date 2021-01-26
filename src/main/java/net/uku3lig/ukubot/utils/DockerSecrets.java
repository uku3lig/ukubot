package net.uku3lig.ukubot.utils;

import net.uku3lig.ukubot.core.Main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DockerSecrets {
    private static final Path basePath = Path.of("/run/secrets/");
    private static final String[] secretsNames = {"token", "db_pwd", "client_secret"};
    private static Map<String, String> secrets = null;

    private DockerSecrets() {

    }

    public static Optional<String> getSecret(String name) {
        if (secrets == null) secrets = init();
        return Optional.ofNullable(secrets.get(name));
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
