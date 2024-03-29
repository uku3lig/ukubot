package net.uku3lig.ukubot.core;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Locale;

public class KiyoListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;
        String content = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);

        if (content.contains("kiyohime")) {
            event.getMessage().reply("<:kiyobean:739895868215263232>").mentionRepliedUser(false).queue();
        }

        if (content.contains("uku3lig")) {
            event.getMessage().addReaction(Emoji.fromFormatted("<:uku:1007036728294527066>")).queue();
        }

        if (content.contains("gay") && LocalDateTime.now().getMonth().equals(Month.JUNE)) {
            event.getChannel().sendMessage("<:Gayge:1107308980994318356>").queue();
        }
    }
}
