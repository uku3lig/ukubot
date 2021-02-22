package net.uku3lig.ukubot.utils.translation;

import net.dv8tion.jda.api.entities.Guild;
import net.uku3lig.ukubot.config.Config;
import net.uku3lig.ukubot.config.Settings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.util.Arrays;
import java.util.Objects;

public enum Language {
    English("en_us"),
    French("fr_fr");

    public final String locale;

    Language(String locale) {
        this.locale = locale;
    }

    public static Language current(Guild g) {
        return current(Config.getEffectiveConfig(g));
    }

    public static Language current(Config cfg) {
        try {
            return Objects.requireNonNull(of(cfg.getLanguage()));
        } catch (Exception e) {
            Settings.Language.get().editValue(cfg, "en_us");
            return Language.English;
        }
    }

    @Nullable @CheckReturnValue
    public static Language of(@NotNull String name) {
        return Arrays.stream(values())
                .filter(l -> l.locale.equalsIgnoreCase(name) || l.name().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
}
