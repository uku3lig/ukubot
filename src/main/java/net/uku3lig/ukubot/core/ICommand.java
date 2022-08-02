package net.uku3lig.ukubot.core;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Modal;

public interface ICommand {
    CommandData getData();

    void onCommand(SlashCommandInteractionEvent event);
}
