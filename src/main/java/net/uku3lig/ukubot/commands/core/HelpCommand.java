package net.uku3lig.ukubot.commands.core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandAdapter;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.core.Main;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HelpCommand extends Command {
    @Override
    public String command() {
        return "help";
    }

    @Override
    public String description() {
        return "Sends help about all or a specific command";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        List<Command> allowed = CommandAdapter.getCommands().stream()
                .filter(c -> c.allowed().isAllowed(event.getMember()))
                .filter(c -> c.enabled)
                .collect(Collectors.toList());

        if (event.args.length == 0) {
            String important = "Use `%shelp <command>` to find more specific help on a command"
                    .formatted(CommandAdapter.prefixes.get(event.getGuild()));
            String commands = allowed.stream().map(Command::command).collect(Collectors.joining("\n"));

            EmbedBuilder builder = new EmbedBuilder()
                    .setAuthor(Main.botName)
                    .setColor(Main.embedColor)
                    .setTitle("Help")
                    .setDescription(important)
                    .addField("Commands", commands, true)
                    .setTimestamp(LocalDateTime.now());
            event.getChannel().sendMessage(builder.build()).queue();
            return;
        }
        Optional<Command> command = allowed.stream().filter(c -> c.command().equalsIgnoreCase(event.args[0])).findFirst();
        if (command.isEmpty()) {
            event.getChannel().sendMessage("No matching command found with query `%s`".formatted(event.args[0])).queue();
            return;
        }
        command.get().sendHelp(event.getChannel());
    }

    @Override
    public String help() {
        return "help [command]";
    }
}
