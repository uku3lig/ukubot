package net.uku3lig.ukubot.utils.translation;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class T {
    public static Translations getI(@NotNull Language l) {
        return Translations.getInstance(l);
    }

    public static Translations getI(@NotNull Guild g) {
        return getI(Language.current(g));
    }

    public static Translations getI(@NotNull GenericGuildEvent ge) {
        return getI(ge.getGuild());
    }

    public static Optional<String> get(@NotNull String k, @NotNull Language l) {
        return getI(l).getEffectiveTranslation(k);
    }

    public static Optional<String> get(@NotNull String k, @NotNull Guild g) {
        return get(k, Language.current(g));
    }

    public static Optional<String> get(@NotNull String k, @NotNull GenericGuildEvent ge) {
        return get(k, ge.getGuild());
    }
}
