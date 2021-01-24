package net.uku3lig.ukubot.console.commands;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.uku3lig.ukubot.console.ConsoleCommand;
import org.slf4j.LoggerFactory;

public class LogLevelConsoleCommand extends ConsoleCommand {
    private final Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @Override
    public String command() {
        return "loglevel";
    }

    @Override
    public void onCommandReceived(String[] args) {
        if (args.length == 0) logger.info("Current log level: %s (%d)"
                .formatted(rootLogger.getLevel().levelStr, rootLogger.getLevel().levelInt));
        else {
            Level level;
            try {
                level = Level.toLevel(Integer.parseInt(args[0]), Level.OFF);
            } catch (NumberFormatException e) {
                level = Level.toLevel(args[0], Level.OFF);
            }
            if (level.levelInt == Level.OFF.levelInt) {
                logger.error("Cannot find this log level");
                return;
            }
            rootLogger.setLevel(level);
            logger.info("Successfully set log level to " + level.levelStr);
        }
    }
}
