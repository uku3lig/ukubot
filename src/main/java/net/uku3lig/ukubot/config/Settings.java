package net.uku3lig.ukubot.config;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum Settings {
    Prefix(new Setting<>("prefix",
            "settings prefix <newPrefix>",
            "Any char sequence, up to 5 chars",
            g -> cfg(g).getPrefix(),
            (g, s) -> cfg(g).editPrefix(s[0].substring(0, Math.min(s[0].length(), 5))))),
    XpFactor(new Setting<>("xpFactor",
            "settings xpFactor <factor>",
            "Any number between 0 and 1.5",
            g -> cfg(g).getXpFactor(),
            (g, s) -> cfg(g).setXpFactor(Double.parseDouble(s[0])))),
    LevelUpMessage(new Setting<>("lvlMsg",
            "settings lvlMsg <message>",
            "A message, containing \"`@mention`\" and \"`(level)`\"",
            g -> cfg(g).getLevelUpMessage(),
            (g, s) -> cfg(g).setLevelUpMessage(String.join(" ", s))));

    private final Setting<?> setting;

    public Setting<?> get() {
        return setting;
    }

    Settings(Setting<?> setting) {
        this.setting = setting;
    }

    private static Config cfg(Guild g) {
        return Config.getEffectiveConfig(g);
    }

    public static class Setting<T> {
        public final String name, commandToEdit, allowedValues;
        public final Function<Guild, T> currentValue;
        public final BiConsumer<Guild, String[]> editValue;

        public Setting(String name, String commandToEdit, String allowedValues,
                       Function<Guild, T> currentValue, BiConsumer<Guild, String[]> editValue) {
            this.name = name;
            this.commandToEdit = commandToEdit;
            this.allowedValues = allowedValues;
            this.currentValue = currentValue;
            this.editValue = editValue;
        }
    }
}
