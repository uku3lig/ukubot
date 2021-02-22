package net.uku3lig.ukubot.config;

import net.dv8tion.jda.api.entities.Guild;
import net.uku3lig.ukubot.hibernate.Database;
import net.uku3lig.ukubot.utils.translation.Language;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public enum Settings {
    Prefix(new Setting<>("prefix",
            "settings prefix <newPrefix>",
            "Any char sequence, up to 5 chars",
            Config::getPrefix, Config::editPrefix,
            s -> s.length() <= 5)),
    XpFactor(new Setting<>("xpFactor",
            "settings xpFactor <factor>",
            "Any number between 0 and 1.5",
            Config::getXpFactor, (c, s) -> c.setXpFactor(Double.parseDouble(s)),
            NumberUtils::isCreatable)),
    LevelUpMessage(new Setting<>("lvlMsg",
            "settings lvlMsg <message>",
            "A message, containing \"`@mention`\" and \"`(level)`\"",
            Config::getLevelUpMessage, Config::setLevelUpMessage,
            s -> s.contains("@mention") && s.contains("(level)"))),
    Language(new Setting<>("language",
            "setting language <language>",
            "Any value listed in with `languages` command",
            Config::getLanguage, Config::setLanguage,
            s -> Objects.nonNull(net.uku3lig.ukubot.utils.translation.Language.of(s))));

    private final Setting<?> setting;

    public Setting<?> get() {
        return setting;
    }

    Settings(Setting<?> setting) {
        this.setting = setting;
    }

    public static class Setting<T> {
        public final String name, commandToEdit, allowedValues;
        private final Function<Config, T> currentValue;
        private final BiConsumer<Config, String> editValue;
        private final Predicate<String> isValid;

        public T currentValue(Guild g) {
            return currentValue(Config.getEffectiveConfig(g));
        }

        public T currentValue(Config cfg) {
            return currentValue.apply(cfg);
        }

        public boolean editValue(Guild g, String args) {
            return editValue(Config.getEffectiveConfig(g), args);
        }

        public boolean editValue(Config cfg, String args) {
            if (!isValid.test(args)) return false;
            editValue.accept(cfg, args);
            Database.saveOrUpdate(cfg);
            return true;
        }

        public Setting(String name, String commandToEdit, String allowedValues,
                       Function<Config, T> currentValue, BiConsumer<Config, String> editValue,
                       Predicate<String> isValid) {
            this.name = name;
            this.commandToEdit = commandToEdit;
            this.allowedValues = allowedValues;
            this.currentValue = currentValue;
            this.editValue = editValue;
            this.isValid = isValid;
        }
    }
}
