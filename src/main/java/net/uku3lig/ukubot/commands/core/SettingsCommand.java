package net.uku3lig.ukubot.commands.core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Config;
import net.uku3lig.ukubot.core.Main;

import java.util.Arrays;

public class SettingsCommand extends Command {
    @Override
    public String command() {
        return "settings";
    }

    @Override
    public String description() {
        return "Changes the settings of the current guild";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        switch (event.args.length) {
            case 0 -> sendHelp(event.getChannel());
            case 1 -> {
                if (Arrays.stream(Config.Settings.values()).noneMatch(s -> s.name.equalsIgnoreCase(event.args[0])))
                    sendHelp(event.getChannel());
                else Arrays.stream(Config.Settings.values()).filter(s -> s.name.equalsIgnoreCase(event.args[0]))
                        .forEach(s -> event.getChannel().sendMessage(getSettingDesc(s, event.getGuild())).queue());
            }
            default -> {
                if (Arrays.stream(Config.Settings.values()).noneMatch(s -> s.name.equalsIgnoreCase(event.args[0])))
                    sendHelp(event.getChannel());
                else {
                    Arrays.stream(Config.Settings.values()).filter(s -> s.name.equalsIgnoreCase(event.args[0]))
                            .forEach(s -> s.editValue.accept(event.getGuild(), skipOneArg(event.args)));
                    event.getChannel().sendMessage("Successfully set new value for the setting").queue();
                }
            }
        }
    }

    private String[] skipOneArg(String[] args) {
        return Arrays.stream(args).skip(1).toArray(String[]::new);
    }

    @Override
    public String help() {
        return "settings <setting> [new value]";
    }

    @Override
    public void sendHelp(TextChannel c) {
        EmbedBuilder builder = Main.getDefaultEmbed()
                .setTitle("Settings")
                .setDescription("Use `settings <option>` to see more specific help")
                .addField("Prefix", "`settings prefix`", true);
        c.sendMessage(builder.build()).queue();
    }

    private MessageEmbed getSettingDesc(Config.Settings setting, Guild g) {
        return Main.getDefaultEmbed()
                .setTitle(setting.name())
                .addField("Current value", "`" + setting.currentValue.apply(g) + "`", false)
                .addField("Command to edit", "`" + setting.commandToEdit + "`", false)
                .addField("Allowed values", setting.allowedValues, false)
                .build();
    }
}
