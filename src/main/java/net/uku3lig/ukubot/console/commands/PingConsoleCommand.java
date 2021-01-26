package net.uku3lig.ukubot.console.commands;

import net.uku3lig.ukubot.console.ConsoleCommand;
import net.uku3lig.ukubot.core.Main;

public class PingConsoleCommand extends ConsoleCommand {
    @Override
    public String command() {
        return "ping";
    }

    @Override
    public void onCommandReceived(String[] args) {
        Main.getJda().getRestPing().queue(restPing ->
                logger.info("Gateway: %d ms | REST %d ms".formatted(Main.getJda().getGatewayPing(), restPing)));
    }
}
