package net.uku3lig.ukubot.console;

public abstract class ConsoleCommand {
    public abstract String command();

    public abstract void onCommandReceived(String[] args);
}
