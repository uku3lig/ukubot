package net.uku3lig.ukubot.core;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public interface ICommand {
    CommandData getCommandData();

    void onCommand(SlashCommandInteractionEvent event);
}
