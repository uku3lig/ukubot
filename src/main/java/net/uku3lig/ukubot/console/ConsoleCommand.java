package net.uku3lig.ukubot.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConsoleCommand {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public abstract String command();

    public abstract void onCommandReceived(String[] args);
}
