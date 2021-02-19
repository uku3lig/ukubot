package net.uku3lig.ukubot.commands.core;

import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.commands.CommandAdapter;
import net.uku3lig.ukubot.commands.IsSenderAllowed;

import java.util.Optional;

public class EnableDisableCommand extends Command {
    @Override
    public String command() {
        return "command";
    }

    @Override
    public String description() {
        return "Manages the commands";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        if (event.args.length < 2) {
            sendHelp(event.getMessage());
            return;
        }

        Optional<Command> opt = CommandAdapter.getCommands().stream()
                .filter(c -> c.command().equalsIgnoreCase(event.args[1]) ||
                        c.getClass().getSimpleName().equalsIgnoreCase(event.args[1]))
                .findFirst();
        if (opt.isEmpty()) {
            event.getChannel().sendMessage("Error: cannot find this command").queue();
            return;
        }
        Command command = opt.get();

        switch (event.args[0]) {
            case "enable" -> {
                command.enabled = true;
                event.getChannel().sendMessage("Successfully enabled `%s`".formatted(event.args[1])).queue();
            }
            case "disable" -> {
                command.enabled = false;
                event.getChannel().sendMessage("Successfully disabled `%s`".formatted(event.args[1])).queue();
            }
        }
    }

    @Override
    public String help() {
        return "command enable|disable <name>";
    }

    @Override
    public IsSenderAllowed allowed() {
        return IsSenderAllowed.Uku;
    }
}
