package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.core.ICommand;

import java.util.ArrayList;
import java.util.List;

public class BlacklistCommand implements ICommand {
    @Override
    public CommandData getCommandData() {
        SubcommandData add = new SubcommandData("add", "adds someone to the blacklist")
                .addOption(OptionType.USER, "user", "the user to blacklist");
        SubcommandData remove = new SubcommandData("remove", "removes someone from the blacklist")
                .addOption(OptionType.USER, "user", "the user to unblacklist");

        return Commands.slash("blacklist", "manages the blacklist")
                .addSubcommands(add, remove)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS));
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        if (event.getGuild() == null) return;
        if (event.getSubcommandName() == null) return;

        User u = event.getOption("user", OptionMapping::getAsUser);
        if (u == null) {
            event.reply("unknown user").setEphemeral(true).queue();
            return;
        }

        Main.editGuildConfig(event.getGuild(), cfg -> {
            List<Long> ids = cfg.getOrElse("blacklisted", new ArrayList<>());

            switch (event.getSubcommandName()) {
                case "add" -> {
                    ids.add(u.getIdLong());
                    event.replyFormat("Added %s to the blacklist.", u.getName()).setEphemeral(true).queue();
                }
                case "remove" -> {
                    ids.remove(u.getIdLong());
                    event.replyFormat("Removed %s from the blacklist.", u.getName()).setEphemeral(true).queue();
                }
                default -> event.reply("unknown subcommand").setEphemeral(true).queue();
            }

            cfg.set("blacklisted", ids);
        });
    }
}
