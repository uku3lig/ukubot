package net.uku3lig.ukubot.command;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.uku3lig.ukubot.Main;
import net.uku3lig.ukubot.core.ICommand;

import java.util.Objects;

public class ConfigCommand implements ICommand {
    @Override
    public CommandData getCommandData() {
        SubcommandData formChannel = new SubcommandData("formchannel", "sets the channel in which the form results are sent")
                .addOption(OptionType.CHANNEL, "channel", "the channel", true);
        SubcommandData requestsOpen = new SubcommandData("requestsopen", "sets if requests are open")
                .addOption(OptionType.BOOLEAN, "open", "the state of requests", true);

        return Commands.slash("config", "configures the bot to your liking")
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
                .addSubcommands(formChannel, requestsOpen);
    }

    @Override
    public void onCommand(GenericCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        String subcommand = event.getSubcommandName();
        if (subcommand == null) return;

        switch (subcommand) {
            case "formchannel" -> {
                OptionMapping option = Objects.requireNonNull(event.getOption("channel"));
                GuildChannelUnion channel = option.getAsChannel();

                if (channel instanceof TextChannel textChannel) {
                    Main.editGuildConfig(event.getGuild(), cfg -> cfg.set("form_channel", textChannel.getIdLong()));
                    event.replyFormat("Set form channel to %s", textChannel.getAsMention()).setEphemeral(true).queue();
                } else {
                    event.reply("Not a text channel.").setEphemeral(true).queue();
                }
            }
            case "requestsopen" -> {
                OptionMapping option = Objects.requireNonNull(event.getOption("open"));
                boolean enabled = option.getAsBoolean();
                Main.editGuildConfig(event.getGuild(), cfg -> cfg.set("requests_open", enabled));
                event.replyFormat("Requests are now %s.", enabled ? "open" : "closed").setEphemeral(true).queue();
            }
            default -> event.reply("Unknown setting.").setEphemeral(true).queue();
        }
    }
}
