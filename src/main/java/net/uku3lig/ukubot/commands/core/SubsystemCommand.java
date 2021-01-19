package net.uku3lig.ukubot.commands.core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.commands.IsSenderAllowed;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.subsystems.Subsystem;
import net.uku3lig.ukubot.subsystems.SubsystemAdapter;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class SubsystemCommand extends Command {
    @Override
    public String command() {
        return "subsystem";
    }

    @Override
    public String description() {
        return "Manages the active subsystems";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        if (event.args.length >= 1 && event.args[0].equals("list")) {
            String subs = SubsystemAdapter.getSubsystems().stream()
                    .map(s -> s.getClass().getSimpleName() + " (" + (s.enabled ? "enabled" : "disabled") + ")")
                    .collect(Collectors.joining("\n"));
            EmbedBuilder builder = new EmbedBuilder()
                    .setAuthor(Main.botName)
                    .setColor(Main.embedColor)
                    .setTitle("Subsystems")
                    .setDescription(subs)
                    .setTimestamp(Instant.now());
            event.getChannel().sendMessage(builder.build()).queue();
            return;
        } else if (event.args.length < 2) {
            sendHelp(event.getChannel());
            return;
        }

        Optional<Subsystem> opt = SubsystemAdapter.getSubsystems().stream()
                .filter(s -> s.getClass().getSimpleName().equalsIgnoreCase(event.args[1]))
                .findFirst();
        if (opt.isEmpty()) {
            event.getChannel().sendMessage("Error: cannot find this subsystem").queue();
            return;
        }
        Subsystem subsystem = opt.get();

        switch (event.args[0]) {
            case "enable" -> {
                subsystem.enabled = true;
                event.getChannel().sendMessage("Successfully enabled `%s`".formatted(event.args[1])).queue();
            }
            case "disable" -> {
                subsystem.enabled = false;
                event.getChannel().sendMessage("Successfully disabled `%s`".formatted(event.args[1])).queue();
            }
        }
    }

    @Override
    public String help() {
        return "subsystem (enable|disable <name>) | list";
    }

    @Override
    public IsSenderAllowed allowed() {
        return IsSenderAllowed.Uku;
    }

    @Override
    public Collection<String> aliases() {
        return Collections.singleton("sub");
    }
}
