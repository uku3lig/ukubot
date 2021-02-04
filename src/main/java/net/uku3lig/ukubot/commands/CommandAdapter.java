package net.uku3lig.ukubot.commands;

import lombok.Getter;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.uku3lig.ukubot.config.Config;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.utils.ClassScanner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class CommandAdapter extends ListenerAdapter {
    private static CommandAdapter instance = null;
    private static final Logger logger = LoggerFactory.getLogger(CommandAdapter.class);
    @Getter
    private static final Set<Command> commands = new HashSet<>();

    @Getter
    private static final Set<ThreadGroup> threadGroups = new HashSet<>();
    private static final AtomicLong executedCommands = new AtomicLong(0);

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();



    public static CommandAdapter getInstance() {
        if (instance == null) instance = new CommandAdapter();
        return instance;
    }

    private CommandAdapter() {
        commands.addAll(ClassScanner.findCommands());
        logger.info("Found %s commands".formatted(commands.size()));
        if (!findNullCommands().isEmpty()) {
            String nullCommands = findNullCommands().stream()
                    .map(c -> c.getClass().getSimpleName()).collect(Collectors.joining(", "));
            logger.error(nullCommands + " do NOT have a command set!");
        }
        executor.scheduleWithFixedDelay(new CheckForGroups(), 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event instanceof CommandReceivedEvent) return;
        //If message doesn't start with guild prefix, not a command
        if (!event.getMessage().getContentRaw().startsWith(Config.getEffectiveConfig(event.getGuild()).getPrefix())) return;
        //We don't want self loops
        if (event.getAuthor().getId().equals(Main.getJda().getSelfUser().getId())) return;
        //Bot loops neither
        if (event.getAuthor().isBot()) return;

        //Something looking like a command is received POG
        onCommandReceived(new CommandReceivedEvent(event));
    }

    private void onCommandReceived(CommandReceivedEvent event) {
        commands.stream()
                .filter(command -> event.command.equalsIgnoreCase(command.command()) ||
                        command.aliases().stream().anyMatch(event.command::equalsIgnoreCase))
                .filter(command -> command.allowed().isAllowed(event.getMember()) ||
                        IsSenderAllowed.Uku.isAllowed(event.getMember()))
                .filter(command -> command.enabled)
                .forEach(command -> {
                    logger.debug("%s issued '%s' in #%s"
                            .formatted(event.getAuthor().getName(),
                                    command.command(),
                                    event.getChannel().getName()));

                    //if does not contain thread group with command,
                    if (threadGroups.stream().noneMatch(group -> group.getName().equals(command.command())))
                        //add a new one
                        threadGroups.add(new ThreadGroup(command.command()));

                    //find all thread groups with command,
                    threadGroups.stream().filter(group -> group.getName().equals(command.command()))
                            //create new thread with parent, and runnable
                            .forEach(group -> {
                                Thread t = new Thread(group,
                                        () -> command.onCommandReceived(event),
                                        "CommandInstance" + executedCommands.getAndIncrement() + "-" + System.currentTimeMillis());
                                t.setUncaughtExceptionHandler((thread, e) -> exceptionThrown(thread, e, event.getChannel()));
                                t.start();
                            });
                });
    }

    private Set<Command> findNullCommands() {
        return CommandAdapter.commands.stream().filter(command -> command.command() == null).collect(Collectors.toSet());
    }

    private void exceptionThrown(Thread thread, Throwable exception, TextChannel channel) {
        if (exception instanceof ThreadDeath) return;
        channel.sendMessage("An unexpected error occurred").queue();
        logger.error("Thread %s (%s) threw an exception".formatted(thread.getName(), thread.getThreadGroup().getName()));
        exception.printStackTrace();
    }

    private static class CheckForGroups implements Runnable {
        @Override
        public void run() {
            threadGroups.stream()
                    .filter(group -> group.activeCount() == 0)
                    .forEach(group -> {
                        group.destroy();
                        threadGroups.remove(group);
                    });
        }
    }
}
