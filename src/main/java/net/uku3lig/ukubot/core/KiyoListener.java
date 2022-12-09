package net.uku3lig.ukubot.core;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class KiyoListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().toLowerCase(Locale.ROOT).contains("kiyohime")) {
            event.getMessage().reply("<:kiyobean:739895868215263232>").mentionRepliedUser(false).queue();
        }

        if (event.getMessage().getContentRaw().toLowerCase(Locale.ROOT).contains("uku")) {
            event.getMessage().reply("<:uku:1007036728294527066>").mentionRepliedUser(false).queue();
        }
    }
}
