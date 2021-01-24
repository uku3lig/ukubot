package net.uku3lig.ukubot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.uku3lig.ukubot.core.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public abstract class Command {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public boolean enabled = true;

    public abstract String command();

    public abstract String description();

    public abstract void onCommandReceived(CommandReceivedEvent event);

    public Collection<String> aliases() {
        return Collections.emptyList();
    }

    public IsSenderAllowed allowed() {
        return IsSenderAllowed.Default;
    }

    public Duration cooldown() {
        return switch (allowed()) {
            case Friend -> Duration.ofSeconds(30);
            case Moderator -> Duration.ofMinutes(1);
            case Administrator -> Duration.ofSeconds(150); //2m30
            default -> Duration.ofSeconds(5);
        };
    }

    /**
     * Help syntax:
     * <ul>
     *     <li><code>[param]</code>: optional parameter</li>
     *     <li><code>&lt;param&gt;</code>: mandatory parameter</li>
     *     <li><code>("defined arg" + list of params)</code>: way of a doing a command (ex: <code>rr (add &lt;name>)</code>)</li>
     *     <li><code>(part) <b>|</b> (part)</code>: separator between the ways</li>
     * </ul>
     * Example:
     * <code>reddit (search &lt;query> [subreddit]) | (random [subreddit]) | (info &lt;subreddit>)</code>
     * @return The command's help, <b>WITHOUT THE PREFIX</b>.
     */
    public abstract String help();

    public void sendHelp(TextChannel c) {
        String desc = "%s\n\nUsage: `%s%s`"
                .formatted(description(), CommandAdapter.prefixes.get(c.getGuild()), help());
        if (!aliases().isEmpty()) desc += "\nAliases: " + String.join(", ", aliases());
        EmbedBuilder builder = Main.getDefaultEmbed()
                .setTitle("Help: %s".formatted(command()))
                .setDescription(desc)
                .setTimestamp(LocalDateTime.now());
        c.sendMessage(builder.build()).queue();
    }
}
