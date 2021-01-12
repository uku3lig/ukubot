package net.uku3lig.ukubot.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.Arrays;

public class CommandReceivedEvent extends GuildMessageReceivedEvent {
    public final String[] args;
    public final String command;

    public CommandReceivedEvent(GuildMessageReceivedEvent event) {
        super(event.getJDA(), event.getResponseNumber(), event.getMessage());
        String[] splitMessage = event.getMessage().getContentRaw().split(" ");
        args = Arrays.stream(splitMessage).skip(1).toArray(String[]::new);
        command = splitMessage[0].substring(CommandAdapter.prefixes.get(event.getGuild()).length());
    }
}
