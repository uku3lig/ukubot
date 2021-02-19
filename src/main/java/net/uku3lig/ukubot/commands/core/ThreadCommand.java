package net.uku3lig.ukubot.commands.core;

import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.uku3lig.ukubot.commands.Command;
import net.uku3lig.ukubot.commands.CommandAdapter;
import net.uku3lig.ukubot.commands.CommandReceivedEvent;
import net.uku3lig.ukubot.commands.IsSenderAllowed;
import net.uku3lig.ukubot.core.Main;
import net.uku3lig.ukubot.subsystems.core.ReactionListener;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ThreadCommand extends Command {
    @Override
    public String command() {
        return "thread";
    }

    @Override
    public String description() {
        return "Lists and manages active command threads";
    }

    @Override
    public void onCommandReceived(CommandReceivedEvent event) {
        if (event.args.length < 1) {
            sendHelp(event.getMessage());
            return;
        }

        switch (event.args[0]) {
            case "list":
                EmbedBuilder builder = Main.getDefaultEmbed(event.getAuthor())
                        .setTitle("Threads")
                        .setTimestamp(LocalDateTime.now());
                getThreadsByGroup(CommandAdapter.getThreadGroups()).forEach((group, thread) -> {
                    String threadInfo = thread.stream().map(t -> "%s (`%s`), state: `%s`"
                            .formatted(t.getName(), t.getId(), t.getState().name()))
                            .collect(Collectors.joining("\n"));
                    if (!threadInfo.isBlank() && !threadInfo.isEmpty())
                        builder.addField(group.getName(), threadInfo, false);
                });
                event.getChannel().sendMessage(builder.build()).queue();
                break;
            case "stop":
                if (event.args.length < 2) {
                    sendHelp(event.getMessage());
                    break;
                }

                long id = Long.parseLong(event.args[1].replaceAll("\\D", ""));
                getByIdAndStop(id, false, event.getChannel());
                break;
            case "fs":
                if (event.args.length < 2) {
                    sendHelp(event.getMessage());
                    break;
                }

                id = Long.parseLong(event.args[1].replaceAll("\\D", ""));

                event.getChannel().sendMessage("Are you sure you want to force stop ?")
                        .queue(confirm -> ReactionListener.yesNo(
                                () -> getByIdAndStop(id, true, event.getChannel()),
                                event.getChannel())
                                .source(confirm)
                                .allowed(allowed().get())
                                .build());
                break;
            case "info":
                if (event.args.length < 2) {
                    sendHelp(event.getMessage());
                    break;
                }

                id = Long.parseLong(event.args[1].replaceAll("\\D", ""));
                findThreadById(id, CommandAdapter.getThreadGroups()).ifPresentOrElse(
                        t -> event.getChannel().sendMessage(buildThreadInfo(t)).queue(),
                        () -> event.getChannel().sendMessage("Error: cannot find thread with this id").queue());
                break;
            default:
                sendHelp(event.getMessage());
        }
    }

    private void getByIdAndStop(long id, boolean force, TextChannel channel) {
        findThreadById(id, CommandAdapter.getThreadGroups()).ifPresentOrElse(
                t -> channel.sendMessage(
                        stopThread(t, force) ?
                                "Thread stopped successfully" :
                                "Could not stop thread, see console").queue(),
                () -> channel.sendMessage("Error: cannot find thread with this id").queue());
    }

    private boolean stopThread(Thread t, boolean force) {
        try {
            if (force) t.stop();
            else t.interrupt();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<ThreadGroup, Set<Thread>> getThreadsByGroup(Collection<ThreadGroup> groups) {
        Map<ThreadGroup, Set<Thread>> threadsByGroup = new HashMap<>();
        groups.forEach(group -> {
            Thread[] tmp = new Thread[group.activeCount()];
            group.enumerate(tmp);
            threadsByGroup.put(group, Set.of(tmp).stream()
                    .filter(t -> t.getName().contains("CommandInstance")).collect(Collectors.toSet()));
        });
        return threadsByGroup;
    }

    private Set<Thread> getAllThreads(Collection<ThreadGroup> groups) {
        Set<Thread> allThreads = new HashSet<>();
        groups.forEach(group -> {
            Thread[] tmp = new Thread[group.activeCount()];
            group.enumerate(tmp);
            allThreads.addAll(Set.of(tmp).stream()
                    .filter(t -> t.getName().contains("CommandInstance")).collect(Collectors.toSet()));
        });
        return allThreads;
    }

    private Optional<Thread> findThreadById(long id, Collection<ThreadGroup> groups) {
        return getAllThreads(groups).stream()
                .filter(t -> t.getId() == id)
                .findFirst();
    }

    private MessageEmbed buildThreadInfo(Thread t) {
        Instant since = Instant.ofEpochMilli(Long.parseLong(t.getName().split("-")[1]
                .replaceAll("\\D", "")));
        return Main.getDefaultEmbed()
                .setTitle("Thread info: %s".formatted(t.getName()))
                .addField("Id", String.valueOf(t.getId()), true)
                .addField("Command", t.getThreadGroup().getName(), false)
                .addField("Running since", since.toString(), true)
                .addField("Running for", Duration.between(since, Instant.now()).toString(), false)
                .addField("Priority", String.valueOf(t.getPriority()), true)
                .addField("State", t.getState().name(), false)
                .addField("Alive", t.isAlive() ? "Yes" : "No", true)
                .addField("Daemon", t.isDaemon() ? "Yes" : "No", true)
                .addField("Interrupted", t.isInterrupted() ? "Yes" : "No", true)
                .build();
    }

    @Override
    public String help() {
        return "thread (stop <id>) | (list) | (info <id>)";
    }

    @Override
    public IsSenderAllowed allowed() {
        return IsSenderAllowed.Uku;
    }
}
