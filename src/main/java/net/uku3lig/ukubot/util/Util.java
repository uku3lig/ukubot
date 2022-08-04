package net.uku3lig.ukubot.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;

public class Util {
    public static EmbedBuilder getEmbed(ComponentInteraction event) {
        return new EmbedBuilder(event.getMessage().getEmbeds().stream().findFirst().orElse(null));
    }

    private Util() {}
}
