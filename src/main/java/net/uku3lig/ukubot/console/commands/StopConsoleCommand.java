package net.uku3lig.ukubot.console.commands;

import net.uku3lig.ukubot.commands.core.StopCommand;
import net.uku3lig.ukubot.console.ConsoleCommand;

public class StopConsoleCommand extends ConsoleCommand {
    @Override
    public String command() {
        return "stop";
    }

    @Override
    public void onCommandReceived(String[] args) {
        boolean now = args.length > 0 && args[0].equalsIgnoreCase("now");
        logger.info("Shutting down...");
        StopCommand.stop(null, now);
    }
}
