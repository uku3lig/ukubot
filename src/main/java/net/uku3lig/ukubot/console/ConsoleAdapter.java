package net.uku3lig.ukubot.console;

import lombok.Getter;
import lombok.SneakyThrows;
import net.uku3lig.ukubot.utils.ClassScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConsoleAdapter implements Runnable {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final Logger logger = LoggerFactory.getLogger(ConsoleAdapter.class);
    @Getter
    private static final Set<ConsoleCommand> commands = new HashSet<>();
    private BufferedReader consoleScanner = null;

    private static final ConsoleAdapter instance = new ConsoleAdapter();

    public static ConsoleAdapter getInstance() {
        return instance;
    }

    public void start() {
        consoleScanner = new BufferedReader(new InputStreamReader(System.in));
        commands.addAll(ClassScanner.findConsoleCommands());
        logger.info("Found %s console commands".formatted(commands.size()));
        if (!findNullCommands().isEmpty()) {
            String nullCommands = findNullCommands().stream()
                    .map(c -> c.getClass().getSimpleName()).collect(Collectors.joining(", "));
            logger.error(nullCommands + " do NOT have a command set!");
        }
        executor.scheduleWithFixedDelay(this, 0, 2, TimeUnit.MILLISECONDS);
    }

    @SneakyThrows(IOException.class)
    @Override
    public void run() {
        String input = consoleScanner.readLine();
        if (input == null || input.isEmpty() || input.isBlank()) return;
        String[] args = input.split("\\s+");

        commands.stream().filter(command -> command.command().equalsIgnoreCase(args[0]))
                .forEach(command -> {
                    try {
                        command.onCommandReceived(Arrays.stream(args).skip(1).toArray(String[]::new));
                    } catch (Exception e) {
                        logger.error("An unexpected error occurred");
                        e.printStackTrace();
                    }
                });
    }

    private Set<ConsoleCommand> findNullCommands() {
        return ConsoleAdapter.commands.stream().filter(command -> command.command() == null).collect(Collectors.toSet());
    }
}
