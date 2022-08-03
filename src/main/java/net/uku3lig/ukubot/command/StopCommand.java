package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.core.ICommand;

public class StopCommand implements ICommand {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("stop", "stops the bot")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        event.reply("shutting down").queue(i -> Main.getJda().shutdown());
    }
}
