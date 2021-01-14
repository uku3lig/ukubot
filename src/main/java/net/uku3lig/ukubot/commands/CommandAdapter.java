package net.uku3lig.ukubot.commands;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.utils.ClassScanner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandAdapter extends ListenerAdapter {
    private static CommandAdapter instance = null;
    private final Logger logger = LoggerFactory.getLogger(CommandAdapter.class);
    @Getter
    private static final Set<Command> commands = new HashSet<>();
    public static final Map<Guild, String> prefixes = new HashMap<>();
    public static final String defaultPrefix = "?";

    @Getter
    private static final Map<Thread, Command> threads = new HashMap<>();

    public static CommandAdapter getInstance() {
        if (instance == null) instance = new CommandAdapter();
        return instance;
    }

    private CommandAdapter() {
        Main.runWhenReady(jda -> jda.getGuilds().stream().filter(g -> !prefixes.containsKey(g))
                .forEach(g -> prefixes.put(g, defaultPrefix)));
        commands.addAll(ClassScanner.findCommands());
        logger.info("Found %s commands".formatted(commands.size()));
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event instanceof CommandReceivedEvent) return;
        //If message doesn't start with guild prefix, not a command
        if (!event.getMessage().getContentRaw().startsWith(prefixes.get(event.getGuild()))) return;
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
                    try {
                        Thread t = new Thread(() -> command.onCommandReceived(event));
                        t.setName(command.getClass().getSimpleName() + " - " + LocalDateTime.now().toString());
                        t.start();
                        threads.put(t, command);
                    } catch (Exception e) {
                        event.getChannel().sendMessage("An unexpected error occurred").queue();
                        e.printStackTrace();
                    }
                });
    }
}
