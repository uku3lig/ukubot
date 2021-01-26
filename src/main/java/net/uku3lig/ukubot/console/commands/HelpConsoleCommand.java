package net.uku3lig.ukubot.console.commands;

import net.uku3lig.ukubot.console.ConsoleCommand;
import net.uku3lig.ukubot.console.ConsoleAdapter;

import java.util.stream.Collectors;

public class HelpConsoleCommand extends ConsoleCommand {
    @Override
    public String command() {
        return "help";
    }

    @Override
    public void onCommandReceived(String[] args) {
        logger.info("Available commands:\n" + ConsoleAdapter.getCommands().stream().map(ConsoleCommand::command)
                .collect(Collectors.joining("\n")));
    }
}
