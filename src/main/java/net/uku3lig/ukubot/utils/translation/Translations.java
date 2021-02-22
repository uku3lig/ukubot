package net.uku3lig.ukubot.utils.translation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Translations {
    private static final Map<Language, Translations> instances = Collections.synchronizedMap(new HashMap<>());
    private static final Logger logger = LoggerFactory.getLogger(Translations.class);
    private final JsonObject root;
    @Getter
    private final Language language;

    @NotNull
    public static Translations getInstance(@NotNull Language l) {
        if (!instances.containsKey(Language.English)) instances.put(Language.English, new Translations(Language.English));
        if (!instances.containsKey(l)) instances.put(l, new Translations(l));
        return instances.get(l);
    }

    private Translations(final Language language) {
        this.language = language;

        InputStream input = getClass().getClassLoader().getResourceAsStream("lang/" + language.locale + ".json");
        if (input == null) {
            logger.warn("Language " + language.locale + " does not have a file! Falling back to english.");
            Objects.requireNonNull(input = getClass().getClassLoader().getResourceAsStream("lang/en_us.json"));
        }

        JsonObject root = new Gson().fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), JsonObject.class);
        if (root == null || root.keySet().isEmpty()) {
            logger.warn("Language file " + language.locale + " is empty! Falling back to english");
            root = instances.get(Language.English).root;
        }
        this.root = root;
    }

    @Nullable @CheckReturnValue
    public String getTranslation(@NotNull String key) {
        try {
            AtomicReference<JsonObject> obj = new AtomicReference<>(root);
            String[] keys = key.split("\\.");
            Arrays.stream(keys).limit(keys.length - 1).forEach(s -> obj.getAndUpdate(j -> j.getAsJsonObject(s)));
            return Objects.requireNonNull(obj.get().get(keys[keys.length - 1]).getAsString());
        } catch (Exception e) {
            logger.warn(language.locale + " does not have a value for " + key);
            return null;
        }
    }

    @NotNull
    public Optional<String> getEffectiveTranslation(@NotNull String key) {
        String tr = getTranslation(key);
        if (tr == null || tr.isEmpty() || tr.isBlank()) tr = instances.get(Language.English).getTranslation(key);
        return Optional.ofNullable(tr);
    }
}
