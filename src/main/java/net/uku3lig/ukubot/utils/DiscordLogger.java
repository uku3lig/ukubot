package net.uku3lig.ukubot.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.uku3lig.ukubot.core.Main;
import org.gradle.internal.impldep.com.google.common.base.Splitter;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class DiscordLogger extends AppenderBase<ILoggingEvent> {
    private final WebhookClient webhook = Main.createWebhook(DockerSecrets.getSecretOrFile("", Path.of("WH_LOGS")));

    @Override
    protected void append(ILoggingEvent event) {
        if (!event.getLevel().isGreaterOrEqual(Level.WARN)) return;
        Splitter.fixedLength(MessageEmbed.TEXT_MAX_LENGTH).split(event.getMessage()).forEach(s -> {
            WebhookEmbedBuilder builder = Main.getDefaultWebhookEmbed()
                    .setColor(getColor(event.getLevel()))
                    .setTitle(new WebhookEmbed.EmbedTitle("%s by `%s`".formatted(event.getLevel().levelStr,
                            getLoggerName(event)), null))
                    .setDescription(s)
                    .setFooter(new WebhookEmbed.EmbedFooter("Thread `%s`".formatted(event.getThreadName()),
                            Main.getJda().getSelfUser().getEffectiveAvatarUrl()));
            webhook.send(builder.build());
        });
    }

    private int getColor(Level level) {
        return switch (level.levelInt) {
            case Level.WARN_INT -> 0xd67309;
            case Level.ERROR_INT -> 0xdb3f23;
            default -> Main.embedColor;
        };
    }

    private String getLoggerName(ILoggingEvent event) {
        String[] parts = event.getLoggerName().split("\\.");
        return parts[parts.length - 1];
    }
}
