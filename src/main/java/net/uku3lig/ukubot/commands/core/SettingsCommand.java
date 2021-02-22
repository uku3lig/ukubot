package net.uku3lig.ukubot.commands.core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.commands.IsSenderAllowed;
import net.uku3lig.ukubot.config.Settings;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.utils.Util;

import java.util.Arrays;
import java.util.stream.Collectors;

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
            case 0 -> sendHelp(event.getMessage());
            case 1 -> {
                if (Arrays.stream(Settings.values()).noneMatch(s -> s.get().name.equalsIgnoreCase(event.args[0])))
                    sendHelp(event.getMessage());
                else Arrays.stream(Settings.values()).filter(s -> s.get().name.equalsIgnoreCase(event.args[0]))
                        .forEach(s -> event.getChannel().sendMessage(getSettingDesc(s, event.getGuild())).queue());
            }
            default -> {
                if (Arrays.stream(Settings.values()).noneMatch(s -> s.get().name.equalsIgnoreCase(event.args[0])))
                    sendHelp(event.getMessage());
                else {
                    Arrays.stream(Settings.values()).filter(s -> s.get().name.equalsIgnoreCase(event.args[0]))
                            .map(s -> {
                                if (s.get().editValue(event.getGuild(), Util.skipOneArgAndJoin(event.args)))
                                    return "Successfully set new value for " + s.get().name;
                                else return "Error: cannot set new value, it is incorrect. Please do `settings %s`"
                                        .formatted(s.get().name);
                            }).forEach(s -> event.getChannel().sendMessage(s).queue());
                }
            }
        }
    }

    @Override
    public String help() {
        return "settings <setting> [new value]";
    }

    @Override
    public void sendHelp(Message m) {
        EmbedBuilder builder = Main.getDefaultEmbed()
                .setTitle("Settings")
                .setDescription("Use `settings <option>` to see more specific help");
        Arrays.stream(Settings.values())
                .map(s -> new MessageEmbed.Field(s.name(), "`settings " + s.get().name + "`", true))
                .forEach(builder::addField);
        m.getChannel().sendMessage(builder.build()).queue();
    }

    private MessageEmbed getSettingDesc(Settings setting, Guild g) {
        return Main.getDefaultEmbed()
                .setTitle(setting.get().name)
                .addField("Current value", "`" + setting.get().currentValue(g) + "`", false)
                .addField("Command to edit", "`" + setting.get().commandToEdit + "`", false)
                .addField("Allowed values", setting.get().allowedValues, false)
                .build();
    }

    @Override
    public IsSenderAllowed allowed() {
        return IsSenderAllowed.Administrator;
    }
}
