package net.uku3lig.ukubot.utils.translation;

import net.dv8tion.jda.api.entities.Guild;
import net.uku3lig.ukubot.config.Config;
import net.uku3lig.ukubot.config.Settings;

import java.util.Arrays;

public enum Language {
    English("en_us"),
    French("fr_fr");

    public final String locale;

    Language(String locale) {
        this.locale = locale;
    }

    public static Language current(Guild g) {
        Config cfg = Config.getEffectiveConfig(g);
        try {
            return Arrays.stream(Language.values())
                    .filter(l -> l.locale.equalsIgnoreCase(cfg.getLanguage()))
                    .findFirst().orElseGet(() -> Language.valueOf(cfg.getLanguage()));
        } catch (IllegalArgumentException e) {
            Settings.Language.get().editValue.accept(g, new String[]{"en_us"});
            return Language.English;
        }
    }
}
